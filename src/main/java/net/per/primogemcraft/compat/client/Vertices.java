package net.per.primogemcraft.compat.client;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public final class Vertices {

    private Vertices() {
    }

    public static VertexConsumer add(VertexConsumer consumer, net.minecraft.client.util.math.MatrixStack.Entry entry, float x, float y, float z) {
        var matrix = entry.getPositionMatrix();
        var vector = new org.joml.Vector4f(x, y, z, 1.0F);
        vector.mul(matrix);
        return consumer.vertex(vector.x(), vector.y(), vector.z());
    }

    public static VertexConsumer color(VertexConsumer consumer, int argb) {
        int a = (argb >>> 24) & 0xFF;
        int r = (argb >>> 16) & 0xFF;
        int g = (argb >>> 8) & 0xFF;
        int b = argb & 0xFF;
        return consumer.color(r, g, b, a);
    }

    public static VertexConsumer normal(VertexConsumer consumer, net.minecraft.client.util.math.MatrixStack.Entry entry, float nx, float ny, float nz) {
        var normalMatrix = entry.getNormalMatrix();
        var vector = new org.joml.Vector3f(nx, ny, nz);
        vector.mul(normalMatrix);
        return consumer.normal(vector.x(), vector.y(), vector.z());
    }

    public static void end(VertexConsumer consumer) {
        consumer.next();
    }

    public static Matrix4f modelView(net.minecraft.client.util.math.MatrixStack stack) {
        return stack.peek().getPositionMatrix();
    }

    public static Matrix3f normalMatrix(net.minecraft.client.util.math.MatrixStack stack) {
        return stack.peek().getNormalMatrix();
    }
}