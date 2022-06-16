package ca.encodeous.journeyroute.rendering;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.Vec3;

import java.awt.*;
import java.util.OptionalDouble;

import static net.minecraft.client.renderer.RenderStateShard.*;

/**
 * Custom minecraft render hook
 */
public class Renderer {
    private VertexConsumer consumer;
    private PoseStack matrices;
    /**
     * A custom line renderer for journeyroute lines
     */
    public static final RenderType.CompositeRenderType LINES = RenderType.CompositeRenderType.create("lines", DefaultVertexFormat.POSITION_COLOR_NORMAL, VertexFormat.Mode.LINES, 256, RenderType.CompositeState.builder()
       .setShaderState(RENDERTYPE_LINES_SHADER)
       .setLineState(new RenderStateShard.LineStateShard(OptionalDouble.of(3)))
       .setLayeringState(VIEW_OFFSET_Z_LAYERING)
       .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
       .setOutputState(ITEM_ENTITY_TARGET)
       .setWriteMaskState(COLOR_DEPTH_WRITE)
       .setCullState(NO_CULL)
       .createCompositeState(false));

    public Renderer(RenderBuffers buffer, PoseStack matrices) {
        this.consumer = buffer.bufferSource().getBuffer(LINES);
        this.matrices = matrices;
    }

    /**
     * Draws a line
     * @param p1 endpoint 1
     * @param p2 endpoint 2
     * @param color the color of the line
     */
    public void drawLine(Vec3 p1, Vec3 p2, Color color){
        drawLine(p1.x, p1.y, p1.z, p2.x, p2.y, p2.z, color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f, color.getAlpha() / 255.0f);
    }

    public void drawLine(double x1, double y1, double z1, double x2, double y2, double z2, float r, float g, float b, float a) {
        var entry = matrices.last();
        consumer.vertex(entry.pose(), (float) x1, (float) y1, (float) z1).color(r, g, b, a).normal(entry.normal(), r, g, b).endVertex();
        consumer.vertex(entry.pose(), (float) x2, (float) y2, (float) z2).color(r, g, b, a).normal(entry.normal(), r, g, b).endVertex();
    }
}
