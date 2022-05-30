package ca.encodeous.journeyroute;

import ca.encodeous.journeyroute.gui.RouterGui;
import ca.encodeous.journeyroute.gui.RouterScreen;
import ca.encodeous.journeyroute.tracker.MovementTracker;
import ca.encodeous.journeyroute.utils.WorldUtils;
import ca.encodeous.journeyroute.world.Route;
import ca.encodeous.journeyroute.world.JourneyWorld;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.TextComponent;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

public class JourneyRoute implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final String MODID = "journeyroute";
	public static final Logger LOGGER = LoggerFactory.getLogger(MODID);
	public static JourneyRoute INSTANCE;
	public JourneyWorld World = new JourneyWorld();
	public static Vec3i RouteDest;
	public static Route Route;
	private static KeyMapping guiBinding;

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
}
