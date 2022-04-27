package ca.encodeous.journeyroute;

import ca.encodeous.journeyroute.events.EventManager;
import ca.encodeous.journeyroute.tracker.MovementTracker;
import ca.encodeous.journeyroute.world.RouteWorld;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.EventHandler;
import java.lang.invoke.MethodHandles;

public class JourneyRoute implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger("modid");
	public static JourneyRoute INSTANCE;
	public RouteWorld World = new RouteWorld();

	@Override
	public void onInitialize() {
		INSTANCE = this;
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		LOGGER.info("Hello Fabric world!");
		EventManager.BUS.registerLambdaFactory("ca.encodeous.journeyroute", (lookupInMethod, klass) -> (MethodHandles.Lookup) lookupInMethod.invoke(null, klass, MethodHandles.lookup()));
		EventManager.BUS.subscribe(MovementTracker.class);
	}
}
