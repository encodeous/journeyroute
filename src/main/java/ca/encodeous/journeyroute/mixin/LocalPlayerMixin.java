package ca.encodeous.journeyroute.mixin;

import ca.encodeous.journeyroute.events.TickEvent;
import ca.encodeous.journeyroute.tracker.MovementTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.SectionPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public class LocalPlayerMixin {
    @Inject(method = "tick()V", at = @At("HEAD"))
    public void tick(CallbackInfo ci){
        var p = Minecraft.getInstance().player;
        var blockPos = p.blockPosition();
        if (Minecraft.getInstance().level.getChunkSource().hasChunk(SectionPos.blockToSectionCoord(blockPos.getX()), SectionPos.blockToSectionCoord(blockPos.getZ()))) {
            MovementTracker.tick(new TickEvent());
        }
    }
}
