package ca.encodeous.journeyroute;

import ca.encodeous.journeyroute.events.EventManager;
import ca.encodeous.journeyroute.gui.RouterGui;
import ca.encodeous.journeyroute.gui.RouterScreen;
import ca.encodeous.journeyroute.tracker.MovementTracker;
import ca.encodeous.journeyroute.utils.WorldUtils;
import ca.encodeous.journeyroute.world.Route;
import ca.encodeous.journeyroute.world.JourneyWorld;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.math.Vec3i;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

public class JourneyRoute implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger("modid");
	public static JourneyRoute INSTANCE;
	public JourneyWorld World = new JourneyWorld();
	public static Vec3i RouteDest;
	public static Route Route;
	private static KeyBinding guiBinding;

	@Override
	public void onInitialize() {
		INSTANCE = this;
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		guiBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.journeyroute.route", // The translation key of the keybinding's name
				InputUtil.Type.KEYSYM, // The type of the keybinding, KEYSYM for keyboard, MOUSE for mouse.
				GLFW.GLFW_KEY_R, // The keycode of the key
				"category.journeyroute.route" // The translation key of the keybinding's category.
		));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (guiBinding.wasPressed()) {
				client.setScreen(new RouterScreen(new RouterGui()));
			}
		});

		LOGGER.info("Hello Fabric world!");
		EventManager.BUS.registerLambdaFactory("ca.encodeous.journeyroute", (lookupInMethod, klass) -> (MethodHandles.Lookup) lookupInMethod.invoke(null, klass, MethodHandles.lookup()));
		EventManager.BUS.subscribe(MovementTracker.class);
		ClientCommandManager.DISPATCHER.register(ClientCommandManager.literal("waypoint")
				.executes(source -> {
					RouteDest = WorldUtils.getSurfaceLevelBlock(source.getSource().getWorld(), source.getSource().getEntity().getBlockPos());
					if(RouteDest == null){
						RouteDest = source.getSource().getEntity().getBlockPos();
					}
					return 1;
				}));
		ClientCommandManager.DISPATCHER.register(ClientCommandManager.literal("route")
				.executes(source -> {
					if(RouteDest == null) return 0;
					Route = World.getRouteTo(source.getSource().getEntity().getBlockPos(), RouteDest);
					return 1;
				}));
	}
}
