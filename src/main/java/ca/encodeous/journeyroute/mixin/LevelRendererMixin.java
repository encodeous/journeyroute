package ca.encodeous.journeyroute.mixin;

import ca.encodeous.journeyroute.events.RenderEvent;
import ca.encodeous.journeyroute.rendering.Renderer;
import ca.encodeous.journeyroute.tracker.MovementTracker;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderBuffers;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
    @Shadow
    private @Final Minecraft minecraft;
    @Shadow
    private @Final RenderBuffers renderBuffers;
    @Inject(method = "renderLevel", at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V", args = { "ldc=clear" }))
    public void render(PoseStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightmapTextureManager, Matrix4f positionMatrix, CallbackInfo ci) {
        minecraft.getProfiler().push("journeyroute-render");

        matrices.pushPose();
        matrices.translate(-camera.getPosition().x, -camera.getPosition().y, -camera.getPosition().z);
        var renderer = new Renderer(renderBuffers, matrices);

        MovementTracker.render(new RenderEvent(renderer));

        matrices.popPose();
        RenderSystem.applyModelViewMatrix();
        minecraft.getProfiler().pop();
    }
}
