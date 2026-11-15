package net.per.primogemcraft.compat.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/**
 * Vertex submission compatibility for 1.20.1.
 *
 * <p>1.21 replaced the {@code vertex(...)/color(...)/uv(...)/endVertex()} chain with a
 * fluent {@code addVertex(PoseStack.Pose, float, float, float)} builder. 1.20.1 still
 * uses the explicit chain, so this helper reproduces the 1.21 call shape:
 * it writes the position, then lets the caller continue with color/uv/normal and
 * finally {@link #end(VertexConsumer)}.
 */
public final class Vertices {

    private Vertices() {
    }

    /** Equivalent of 1.21 {@code consumer.addVertex(pose, x, y, z)}. */
    public static VertexConsumer add(VertexConsumer consumer, PoseStack.Pose pose, float x, float y, float z) {
        var matrix = pose.pose();
        var vector = new org.joml.Vector4f(x, y, z, 1.0F);
        vector.mul(matrix);
        return consumer.vertex(vector.x(), vector.y(), vector.z());
    }

    /** Equivalent of 1.21 {@code .setColor(argb)}. */
    public static VertexConsumer color(VertexConsumer consumer, int argb) {
        int a = (argb >>> 24) & 0xFF;
        int r = (argb >>> 16) & 0xFF;
        int g = (argb >>> 8) & 0xFF;
        int b = argb & 0xFF;
        return consumer.color(r, g, b, a);
    }

    /** Equivalent of 1.21 {@code .setNormal(pose, nx, ny, nz)}. */
    public static VertexConsumer normal(VertexConsumer consumer, PoseStack.Pose pose, float nx, float ny, float nz) {
        var normalMatrix = pose.normal();
        var vector = new org.joml.Vector3f(nx, ny, nz);
        vector.mul(normalMatrix);
        return consumer.normal(vector.x(), vector.y(), vector.z());
    }

    /** 1.20.1 requires an explicit endVertex() after each vertex. */
    public static void end(VertexConsumer consumer) {
        consumer.endVertex();
    }

    /** 1.21 exposes the model-view matrix on render events; 1.20.1 uses the pose stack. */
    public static Matrix4f modelView(PoseStack stack) {
        return stack.last().pose();
    }

    public static Matrix3f normalMatrix(PoseStack stack) {
        return stack.last().normal();
    }
}
