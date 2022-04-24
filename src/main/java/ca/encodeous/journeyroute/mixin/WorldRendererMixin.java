package ca.encodeous.journeyroute.mixin;

import ca.encodeous.journeyroute.events.EventManager;
import ca.encodeous.journeyroute.events.RenderEvent;
import ca.encodeous.journeyroute.rendering.Renderer;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {
    @Shadow
    private @Final MinecraftClient client;
    @Shadow
    private @Final BufferBuilderStorage bufferBuilders;

    @Inject(method = "render", at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V", args = { "ldc=clear" }))
    public void render(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f positionMatrix, CallbackInfo ci) {
        client.getProfiler().push("journeyroute-render");

        matrices.push();
        matrices.translate(-camera.getPos().x, -camera.getPos().y, -camera.getPos().z);
        var renderer = new Renderer(bufferBuilders, matrices);

        EventManager.BUS.post(new RenderEvent(renderer));

        matrices.pop();
        RenderSystem.applyModelViewMatrix();
        client.getProfiler().pop();
    }
}
