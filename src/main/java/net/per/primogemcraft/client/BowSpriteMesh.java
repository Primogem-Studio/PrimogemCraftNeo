package net.per.primogemcraft.client;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntPredicate;

public final class BowSpriteMesh {
    private static final float BACK = 7.5F / 16.0F;
    private static final float FRONT = 8.5F / 16.0F;

    private BowSpriteMesh() {
    }

    public static List<Quad> build(int size, int frame, int frameCount, IntPredicate opaque) {
        if (size < 1 || frame < 0 || frame >= frameCount) throw new IllegalArgumentException("Invalid bow sprite dimensions");
        var quads = new ArrayList<Quad>();
        var top = frame / (float) frameCount;
        var bottom = (frame + 1.0F) / frameCount;
        quads.add(new Quad(List.of(new Vertex(0, 0, FRONT, 0, bottom), new Vertex(1, 0, FRONT, 1, bottom),
                new Vertex(1, 1, FRONT, 1, top), new Vertex(0, 1, FRONT, 0, top)), 0, 0, 1));
        quads.add(new Quad(List.of(new Vertex(1, 0, BACK, 1, bottom), new Vertex(0, 0, BACK, 0, bottom),
                new Vertex(0, 1, BACK, 0, top), new Vertex(1, 1, BACK, 1, top)), 0, 0, -1));
        for (var y = 0; y < size; y++) {
            for (var x = 0; x < size; x++) {
                if (!opaque.test(y * size + x)) continue;
                var left = x / (float) size;
                var right = (x + 1.0F) / size;
                var low = 1.0F - (y + 1.0F) / size;
                var high = 1.0F - y / (float) size;
                var u = (x + 0.5F) / size;
                var v = (frame + (y + 0.5F) / size) / frameCount;
                if (x == 0 || !opaque.test(y * size + x - 1))
                    quads.add(side(-1, 0, u, v, left, low, BACK, left, low, FRONT, left, high, FRONT, left, high, BACK));
                if (x == size - 1 || !opaque.test(y * size + x + 1))
                    quads.add(side(1, 0, u, v, right, low, FRONT, right, low, BACK, right, high, BACK, right, high, FRONT));
                if (y == 0 || !opaque.test((y - 1) * size + x))
                    quads.add(side(0, 1, u, v, left, high, FRONT, right, high, FRONT, right, high, BACK, left, high, BACK));
                if (y == size - 1 || !opaque.test((y + 1) * size + x))
                    quads.add(side(0, -1, u, v, left, low, BACK, right, low, BACK, right, low, FRONT, left, low, FRONT));
            }
        }
        var scale = size / 16.0F;
        return quads.stream().map(quad -> new Quad(quad.vertices().stream()
                .map(vertex -> new Vertex(0.5F + (vertex.x() - 0.5F) * scale,
                        0.5F + (vertex.y() - 0.5F) * scale, 0.5F + (vertex.z() - 0.5F) * scale,
                        vertex.u(), vertex.v())).toList(), quad.normalX(), quad.normalY(), quad.normalZ())).toList();
    }

    private static Quad side(int normalX, int normalY, float u, float v, float... coordinates) {
        var vertices = new ArrayList<Vertex>(4);
        for (var index = 0; index < coordinates.length; index += 3)
            vertices.add(new Vertex(coordinates[index], coordinates[index + 1], coordinates[index + 2], u, v));
        return new Quad(List.copyOf(vertices), normalX, normalY, 0);
    }

    public record Vertex(float x, float y, float z, float u, float v) {
    }

    public record Quad(List<Vertex> vertices, int normalX, int normalY, int normalZ) {
    }
}
