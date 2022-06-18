package ca.encodeous.journeyroute;

import ca.encodeous.journeyroute.algorithm.SearchingAlgorithms;
import ca.encodeous.journeyroute.client.plugin.JourneyMapPlugin;
import ca.encodeous.journeyroute.gui.RouterGui;
import ca.encodeous.journeyroute.gui.RouterScreen;
import ca.encodeous.journeyroute.world.Route;
import ca.encodeous.journeyroute.world.JourneyWorld;
import com.mojang.blaze3d.platform.InputConstants;
import io.netty.buffer.*;
import journeymap.client.api.display.PolygonOverlay;
import journeymap.client.api.model.MapPolygon;
import journeymap.client.api.model.ShapeProperties;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.function.Consumer;

/**
 * The main plugin class
 */
public class JourneyRoute implements ModInitializer {
	// constants
	private static final int TICK_SAVE_INTERVAL = 20 * 60 * 5; // 5 min
	public static final String MODID = "journeyroute";
	public static final Logger LOGGER = LoggerFactory.getLogger(MODID);
	// static fields
	public static JourneyRoute INSTANCE;
	private static PolygonOverlay overlay = null;
	public static File journeyRouteFolder;
	private static KeyMapping guiBinding;
	public static Route route;
	private static ClientLevel prevLevel = null;
	private static int tickCount = 0;
	private static File openWorldFile;
	// instance fields
	public JourneyWorld world;

	/**
	 * Called when the mod is initialized
	 */
	@Override
	public void onInitialize() {
		INSTANCE = this;
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		// register the keybinding in the minecraft key shortcuts
		guiBinding = KeyBindingHelper.registerKeyBinding(new KeyMapping(
				"Open Router", // name of the action
				InputConstants.Type.KEYSYM, // The type of the keybinding, KEYSYM for keyboard, MOUSE for mouse.
				GLFW.GLFW_KEY_R, // The keycode of the key
				"JourneyRoute" // the keybinding's category
		));
		journeyRouteFolder = new File(Minecraft.getInstance().gameDirectory, "journeyroute");
		if(journeyRouteFolder.exists() && !journeyRouteFolder.isDirectory()){
			throw new RuntimeException("Unable to create journeyroute folder in the minecraft directory. Please delete any files named \"journeyroute\" at " + journeyRouteFolder.getAbsolutePath());
		}
		if (!journeyRouteFolder.exists()) {
			journeyRouteFolder.mkdirs();
		}
		// save the journeyroute world every TICK_SAVE_INTERVAL
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			tickCount++;
			if(tickCount % TICK_SAVE_INTERVAL == 0){
				LOGGER.info("Autosaving JourneyRoute storage");
				saveMapping();
			}
			if (guiBinding.isDown()) {
				try{
					var screen = new RouterScreen(new RouterGui());
					client.setScreen(screen);
				}catch (Exception e){
					Minecraft.getInstance().player.displayClientMessage(new TextComponent("Unable to open GUI, " + e.getMessage()), false);
					e.printStackTrace();
				}
			}
		});

		LOGGER.info("Hello from JourneyRoute o/");

	}

	/**
	 * Generates and displays a route to a destination.
	 * @param dest the destination
	 * @param onCompletion code that is executed on the completion of the pathfinding
	 */
	public static void tryRouteTo(Vec3i dest, Consumer<Boolean> onCompletion){
		var thread = new Thread(()->{
			try{
				// generates the route using A*
				var route = SearchingAlgorithms.getRouteTo(INSTANCE.world, Minecraft.getInstance().player.blockPosition().below(), dest);
				// check if the route is successfully generated
				if(route == null || route.Path.isEmpty() || route.Path.size() == 1){
					onCompletion.accept(false);
				}else{
					JourneyRoute.route = route;
					route.bakeRenderPath();
					// update JourneyMap polygon
					INSTANCE.refreshJmOverlay();
					onCompletion.accept(true);
				}
			}
			catch(Exception e){
				System.out.println(e.getMessage());
			}
		});
		thread.start();
	}

	public void refreshJmOverlay(){
		if(route == null || Minecraft.getInstance().level == null) return;
		if(overlay != null){
			JourneyMapPlugin.CLIENT.remove(overlay);
		}
		ResourceKey<Level> level = Minecraft.getInstance().level.dimension();
		var properties = new ShapeProperties()
				.setFillColor(Color.WHITE.getRGB())
				.setStrokeWidth(0);
		var pt = new ArrayList<BlockPos>();
		for(var v : route.BakedJourneyMapPolygon){
			pt.add(new BlockPos(v));
		}
		overlay = new PolygonOverlay(MODID, "jr-wp-" + route.hashCode(), level, properties, new MapPolygon(pt));
		try {
			JourneyMapPlugin.CLIENT.show(overlay);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Gets the formatted journeyroute world filename for a level
	 * @param level the level
	 * @return a formatted file name for a level
	 */
	public static String getMapFileName(ClientLevel level){
		var sid = "";
		if(Minecraft.getInstance().getCurrentServer() != null){
			sid = Minecraft.getInstance().getCurrentServer().ip;
		}else if(Minecraft.getInstance().hasSingleplayerServer()){
			sid = Minecraft.getInstance().getSingleplayerServer().getWorldData().getLevelName();
		}
		var cs = sid.replace('.', '_');
		return "jr-" + cs + "-" + level.dimension().location().getPath() + ".jrw";
	}

	/**
	 * Saves the current journeyroute world onto disk
	 */
	private void saveMapping() {
		if(prevLevel == null) return;
		LOGGER.info("Saving journeyroute world to " + openWorldFile.getAbsolutePath());
		var buf = new UnpooledHeapByteBuf(ByteBufAllocator.DEFAULT, 0, Integer.MAX_VALUE);
		world.write(buf);
		try {
			var fo = new FileOutputStream(openWorldFile);
			fo.write(buf.array(), buf.arrayOffset(), buf.readableBytes());
			fo.close();;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Loads a saved version of the current world from disk
	 * @param mappingFolder the folder where worlds are stored
	 */
	private void loadMapping(File mappingFolder) {
		if(prevLevel == null) return;
		var name = getMapFileName(prevLevel);
		var mapFile = new File(mappingFolder, name);
		openWorldFile = mapFile;
		if(!mapFile.exists()){
			LOGGER.info("Creating new JourneyRoute storage " + name);
			try {
				mapFile.createNewFile();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			world = new JourneyWorld();
			return;
		}
		LOGGER.info("Loading " + name);
		ByteBuf buf = null;
		try {
			var str = new FileInputStream(mapFile);
			buf = Unpooled.wrappedBuffer(str.readAllBytes());
			str.close();;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		world = new JourneyWorld();
		world.read(buf);
	}
	public void saveRoute(){
		if(route == null) return;
		var file = new File(journeyRouteFolder, "saved-path.jrr");
		LOGGER.info("Saving route to " + file.getAbsolutePath());
		var buf = new UnpooledHeapByteBuf(ByteBufAllocator.DEFAULT, 0, Integer.MAX_VALUE);
		route.write(buf);
		try {
			var fo = new FileOutputStream(file);
			fo.write(buf.array(), buf.arrayOffset(), buf.readableBytes());
			fo.close();;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public boolean loadRoute(){
		var file = new File(journeyRouteFolder, "saved-path.jrr");
		if(!file.exists()){
			return false;
		}
		LOGGER.info("Loading saved route from disk.");
		ByteBuf buf = null;
		try {
			var str = new FileInputStream(file);
			buf = Unpooled.wrappedBuffer(str.readAllBytes());
			str.close();;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		route = new Route();
		route.read(buf);
		refreshJmOverlay();
		return true;
	}

	/**
	 * Switches the internal datastructures and prepares journeymap for a new world
	 * @param level the new world
	 */
	public static void startMappingFor(ClientLevel level) {
		route = null;
		if(route != null && !route.wasLoadedFromFile){
			route = null;
		}
		if(overlay != null){
			JourneyMapPlugin.CLIENT.remove(overlay);
		}
		// save current
		if (prevLevel != null) {
			INSTANCE.saveMapping();
		}
		prevLevel = level;
		if (level != null) {
			INSTANCE.loadMapping(journeyRouteFolder);
		}
		INSTANCE.refreshJmOverlay();
	}
}
