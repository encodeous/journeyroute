package ca.encodeous.journeyroute;

import ca.encodeous.journeyroute.gui.RouterGui;
import ca.encodeous.journeyroute.gui.RouterScreen;
import ca.encodeous.journeyroute.tracker.MovementTracker;
import ca.encodeous.journeyroute.utils.WorldUtils;
import ca.encodeous.journeyroute.world.Route;
import ca.encodeous.journeyroute.world.JourneyWorld;
import ca.weblite.objc.Client;
import com.mojang.authlib.minecraft.client.MinecraftClient;
import com.mojang.blaze3d.platform.InputConstants;
import io.netty.buffer.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.network.chat.TextComponent;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.invoke.MethodHandles;
import java.util.function.Consumer;

public class JourneyRoute implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final String MODID = "journeyroute";
	public static final Logger LOGGER = LoggerFactory.getLogger(MODID);
	public static JourneyRoute INSTANCE;
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
//		RendererAccess.INSTANCE.getRenderer().meshBuilder().getEmitter().
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



		LOGGER.info("Hello Fabric world!");
		ClientCommandManager.DISPATCHER.register(ClientCommandManager.literal("waypoint")
				.executes(source -> {
					RouteDest = WorldUtils.getSurfaceLevelBlock(source.getSource().getWorld(), source.getSource().getEntity().blockPosition());
					if(RouteDest == null){
						RouteDest = source.getSource().getEntity().blockPosition();
					}
					return 1;
				}));
		ClientCommandManager.DISPATCHER.register(ClientCommandManager.literal("route")
				.executes(source -> {
					if(RouteDest == null) return 0;
					Route = World.getRouteTo(source.getSource().getEntity().blockPosition(), RouteDest);
					return 1;
				}));

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
		LOGGER.info("Saving journeymap world to " + openWorldFile.getAbsolutePath());
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
