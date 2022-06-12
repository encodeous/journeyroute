package ca.encodeous.journeyroute;

import ca.encodeous.journeyroute.client.plugin.JourneyMapPlugin;
import ca.encodeous.journeyroute.gui.RouterGui;
import ca.encodeous.journeyroute.gui.RouterScreen;
import ca.encodeous.journeyroute.utils.WorldUtils;
import ca.encodeous.journeyroute.world.Route;
import ca.encodeous.journeyroute.world.JourneyWorld;
import com.mojang.blaze3d.platform.InputConstants;
import io.netty.buffer.*;
import journeymap.client.api.display.Displayable;
import journeymap.client.api.display.PolygonOverlay;
import journeymap.client.api.model.MapPolygon;
import journeymap.client.api.model.ShapeProperties;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
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

public class JourneyRoute implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final String MODID = "journeyroute";
	public static final Logger LOGGER = LoggerFactory.getLogger(MODID);
	public static JourneyRoute INSTANCE;
	private static PolygonOverlay overlay = null;
	public JourneyWorld World;
	private File openWorldFile;
	public static Vec3i RouteDest;
	public static Route Route;
	private static KeyMapping guiBinding;
	private static ClientLevel prevLevel = null;
	private static int tickCount = 0;
	private static final int tickSaveInterval = 20 * 60 * 5; // 5 min

	@Override
	public void onInitialize() {
		INSTANCE = this;
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		guiBinding = KeyBindingHelper.registerKeyBinding(new KeyMapping(
				"Open Router", // The translation key of the keybinding's name
				InputConstants.Type.KEYSYM, // The type of the keybinding, KEYSYM for keyboard, MOUSE for mouse.
				GLFW.GLFW_KEY_R, // The keycode of the key
				"JourneyRoute" // The translation key of the keybinding's category.
		));
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			tickCount++;
			if(tickCount % tickSaveInterval == 0){
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

	public static void tryRouteTo(Vec3i dest, Consumer<Boolean> onCompletion){
		var thread = new Thread(()->{
			try{
				var route = INSTANCE.World.getRouteTo(Minecraft.getInstance().player.blockPosition().below(), dest);
				if(route == null || route.Path.isEmpty() || route.Path.size() == 1){
					onCompletion.accept(false);
				}else{
					Route = route;
					route.bakeRenderPath();
					// update JourneyMap polygon
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
					JourneyMapPlugin.CLIENT.show(overlay);
					onCompletion.accept(true);
				}
			}
			catch(Exception e){
				System.out.println(e.getMessage());
			}
		});
		thread.start();
	}

	private static String getMapFileName(ClientLevel level){
		var sid = "";
		if(Minecraft.getInstance().getCurrentServer() != null){
			sid = Minecraft.getInstance().getCurrentServer().ip;
		}else if(Minecraft.getInstance().hasSingleplayerServer()){
			sid = Minecraft.getInstance().getSingleplayerServer().getWorldData().getLevelName();
		}
		var cs = sid.replace('.', '_');
		return "jr-" + cs + "-" + level.dimension().location().getPath() + ".jrw";
	}

	private void saveMapping() {
		if(prevLevel == null) return;
		LOGGER.info("Saving journeyroute world to " + openWorldFile.getAbsolutePath());
		var buf = new UnpooledHeapByteBuf(ByteBufAllocator.DEFAULT, 0, Integer.MAX_VALUE);
		World.write(buf);
		try {
			var fo = new FileOutputStream(openWorldFile);
			fo.write(buf.array(), buf.arrayOffset(), buf.readableBytes());
			fo.close();;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

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
			World = new JourneyWorld();
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
		World = new JourneyWorld();
		World.read(buf);
	}

	public static void startMappingFor(ClientLevel level) {
		Route = null;
		if(overlay != null){
			JourneyMapPlugin.CLIENT.remove(overlay);
		}
		var dir = new File(Minecraft.getInstance().gameDirectory, "journeyroute");
		if(dir.exists() && !dir.isDirectory()){
			throw new RuntimeException("Unable to create journeyroute folder in the minecraft directory. Please delete any files named \"journeyroute\" at " + dir.getAbsolutePath());
		}
		if (!dir.exists()) {
			dir.mkdirs();
		}
		// save current
		if (prevLevel != null) {
			INSTANCE.saveMapping();
		}
		prevLevel = level;
		if (level != null) {
			INSTANCE.loadMapping(dir);
		}
	}
}
