package ca.encodeous.journeyroute.mixin;

import ca.encodeous.journeyroute.JourneyRoute;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Inject(method = "updateLevelInEngines(Lnet/minecraft/client/multiplayer/ClientLevel;)V", at = @At("HEAD"))
    public void updateLevelInEngines(ClientLevel clientLevel, CallbackInfo ci){
        JourneyRoute.startMappingFor(clientLevel);
    }
}
