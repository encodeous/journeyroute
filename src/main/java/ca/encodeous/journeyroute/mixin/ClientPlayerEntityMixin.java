package ca.encodeous.journeyroute.mixin;

import ca.encodeous.journeyroute.events.EventManager;
import ca.encodeous.journeyroute.events.TickEvent;
import ca.encodeous.journeyroute.tracker.MovementTracker;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin {
    @Inject(method = "tick()V", at = @At("HEAD"))
    public void tick(CallbackInfo ci){
        var p = MinecraftClient.getInstance().player;
        if (MinecraftClient.getInstance().world.isPosLoaded(p.getBlockX(), p.getBlockZ())) {
            EventManager.BUS.post(new TickEvent());
        }
    }
}
