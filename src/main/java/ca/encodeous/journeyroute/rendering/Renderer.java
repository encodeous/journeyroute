package ca.encodeous.journeyroute.rendering;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.awt.*;
import java.util.OptionalDouble;

import static net.minecraft.client.renderer.RenderStateShard.*;

public class Renderer {
    private VertexConsumer consumer;
    private PoseStack matrices;
    public static final RenderType.CompositeRenderType LINES = RenderType.CompositeRenderType.create("lines", DefaultVertexFormat.POSITION_COLOR_NORMAL, VertexFormat.Mode.LINES, 256, RenderType.CompositeState.builder()
       .setShaderState(RENDERTYPE_LINES_SHADER)
       .setLineState(new RenderStateShard.LineStateShard(OptionalDouble.empty()))
       .setLayeringState(VIEW_OFFSET_Z_LAYERING)
       .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
       .setOutputState(ITEM_ENTITY_TARGET)
       .setWriteMaskState(COLOR_DEPTH_WRITE)
       .setCullState(NO_CULL)
       .createCompositeState(false));

    public Renderer(RenderBuffers buffer, PoseStack matrices) {
        RenderSystem.lineWidth(30.0F);
        this.consumer = buffer.bufferSource().getBuffer(RenderType.LINES);
        this.matrices = matrices;
    }

    public void drawLine(Vec3 p1, Vec3 p2, Color color){
        drawLine(p1.x, p1.y, p1.z, p2.x, p2.y, p2.z, color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f, color.getAlpha() / 255.0f);
    }

    public void drawLine(double x1, double y1, double z1, double x2, double y2, double z2, float r, float g, float b, float a) {
        var entry = matrices.last();
        consumer.vertex(entry.pose(), (float) x1, (float) y1, (float) z1).color(r, g, b, a).normal(entry.normal(), r, g, b).endVertex();
        consumer.vertex(entry.pose(), (float) x2, (float) y2, (float) z2).color(r, g, b, a).normal(entry.normal(), r, g, b).endVertex();
    }

    public void drawShapeOutline(VoxelShape shape, Vec3 pos, Color color, float scale){
        drawShapeOutline(shape, pos.x, pos.y, pos.z, color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f, color.getAlpha() / 255.0f, scale);
    }

    public void drawShapeOutline(VoxelShape voxelShape, double x, double y, double z, float red, float green, float blue, float alpha, float scale) {
        matrices.pushPose();
        var entry = matrices.last();
        voxelShape.forAllEdges((k, l, m, n, o, p) -> {
            float q = (float)(n * scale - k * scale);
            float r = (float)(o * scale - l * scale);
            float s = (float)(p * scale - m * scale);
            float t = Mth.sqrt(q * q + r * r + s * s);
            q /= t;
            r /= t;
            s /= t;
            consumer.vertex(entry.pose(), (float)(k * scale + x), (float)(l * scale + y), (float)(m * scale + z)).color(red, green, blue, alpha).normal(entry.normal(), q, r, s).endVertex();
            consumer.vertex(entry.pose(), (float)(n * scale + x), (float)(o * scale + y), (float)(p * scale + z)).color(red, green, blue, alpha).normal(entry.normal(), q, r, s).endVertex();
        });
        matrices.popPose();
    }

    public void drawBox(AABB box, Color color){
        drawBox(box, color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f, color.getAlpha() / 255.0f);
    }

    public void drawBox(AABB box, float red, float green, float blue, float alpha){
        LevelRenderer.renderLineBox(matrices, consumer, box, red, green, blue, alpha);
    }
}
