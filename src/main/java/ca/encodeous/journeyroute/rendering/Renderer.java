package ca.encodeous.journeyroute.rendering;

import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3d;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;

import java.awt.*;

public class Renderer {
    private VertexConsumer consumer;
    private MatrixStack matrices;

    public Renderer(BufferBuilderStorage buffer, MatrixStack matrices) {
        this.consumer = buffer.getEntityVertexConsumers().getBuffer(RenderLayer.getLines());
        this.matrices = matrices;
    }

    public void drawLine(Vec3d p1, Vec3d p2, Color color){
        drawLine(p1.x, p1.y, p1.z, p2.x, p2.y, p2.z, color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f, color.getAlpha() / 255.0f);
    }

    public void drawLine(double x1, double y1, double z1, double x2, double y2, double z2, float r, float g, float b, float a) {
        var entry = matrices.peek();
        consumer.vertex(entry.getPositionMatrix(), (float) x1, (float) y1, (float) z1).color(r, g, b, a).normal(entry.getNormalMatrix(), r, g, b).next();
        consumer.vertex(entry.getPositionMatrix(), (float) x2, (float) y2, (float) z2).color(r, g, b, a).normal(entry.getNormalMatrix(), r, g, b).next();
    }

    public void drawShapeOutline(VoxelShape shape, Vec3d pos, Color color, float scale){
        drawShapeOutline(shape, pos.x, pos.y, pos.z, color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f, color.getAlpha() / 255.0f, scale);
    }

    public void drawShapeOutline(VoxelShape voxelShape, double x, double y, double z, float red, float green, float blue, float alpha, float scale) {
        matrices.push();
        MatrixStack.Entry entry = matrices.peek();
        voxelShape.forEachEdge((k, l, m, n, o, p) -> {
            float q = (float)(n * scale - k * scale);
            float r = (float)(o * scale - l * scale);
            float s = (float)(p * scale - m * scale);
            float t = MathHelper.sqrt(q * q + r * r + s * s);
            q /= t;
            r /= t;
            s /= t;
            consumer.vertex(entry.getPositionMatrix(), (float)(k * scale + x), (float)(l * scale + y), (float)(m * scale + z)).color(red, green, blue, alpha).normal(entry.getNormalMatrix(), q, r, s).next();
            consumer.vertex(entry.getPositionMatrix(), (float)(n * scale + x), (float)(o * scale + y), (float)(p * scale + z)).color(red, green, blue, alpha).normal(entry.getNormalMatrix(), q, r, s).next();
        });
        matrices.pop();
    }

    public void drawBox(Box box, Color color){
        drawBox(box, color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f, color.getAlpha() / 255.0f);
    }

    public void drawBox(Box box, float red, float green, float blue, float alpha){
        WorldRenderer.drawBox(matrices, consumer, box, red, green, blue, alpha);
    }
}
