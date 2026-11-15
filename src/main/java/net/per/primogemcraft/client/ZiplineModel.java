package net.per.primogemcraft.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.util.Identifier;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SynchronousResourceReloader;
import net.minecraft.util.profiler.Profiler;

import java.io.IOException;
import java.util.ArrayList;
import org.joml.Vector3f;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public final class ZiplineModel implements SynchronousResourceReloader {
    public static final ZiplineModel INSTANCE = new ZiplineModel();
    private float[] vertices = new float[0];

    @Override
    public void reload(ResourceManager manager) {
        try (var reader = manager.openAsReader(new Identifier(MOD_ID, "entity_models/zipline.json"))) {
            vertices = bake(JsonParser.parseReader(reader).getAsJsonObject().getAsJsonArray("elements"));
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load zipline model", exception);
        }
    }

    public void render(MatrixStack matrices, VertexConsumer buffer, int light) {
        var pose = matrices.peek();
        for (var index = 0; index < vertices.length; index += 8) {
            net.per.primogemcraft.compat.client.Vertices.add(buffer, pose, vertices[index], vertices[index + 1], vertices[index + 2])
                    .color(-1)
                    .texture(vertices[index + 3], vertices[index + 4]).overlay(OverlayTexture.DEFAULT_UV).light(light);
            net.per.primogemcraft.compat.client.Vertices.normal(buffer, pose, vertices[index + 5], vertices[index + 6], vertices[index + 7]);
            buffer.next();
        }
    }

    private static float[] bake(JsonArray elements) {
        var matrices = new MatrixStack();
        var baked = new ArrayList<Float>();
        for (var entry : elements) {
            var element = entry.getAsJsonObject();
            if (element.has("name") && element.get("name").getAsString().equals("value")) continue;
            var from = element.getAsJsonArray("from");
            var to = element.getAsJsonArray("to");
            var x0 = from.get(0).getAsFloat();
            var y0 = from.get(1).getAsFloat();
            var z0 = from.get(2).getAsFloat();
            var x1 = to.get(0).getAsFloat();
            var y1 = to.get(1).getAsFloat();
            var z1 = to.get(2).getAsFloat();
            matrices.push();
            if (element.has("rotation")) {
                var rotation = element.getAsJsonObject("rotation");
                var origin = rotation.getAsJsonArray("origin");
                var x = origin.get(0).getAsFloat();
                var y = origin.get(1).getAsFloat();
                var z = origin.get(2).getAsFloat();
                var axis = switch (rotation.get("axis").getAsString()) {
                    case "x" -> RotationAxis.POSITIVE_X;
                    case "z" -> RotationAxis.POSITIVE_Z;
                    default -> RotationAxis.POSITIVE_Y;
                };
                matrices.translate(x, y, z);
                matrices.multiply(axis.rotationDegrees(rotation.get("angle").getAsFloat()));
                matrices.translate(-x, -y, -z);
            }
            for (var face : element.getAsJsonObject("faces").entrySet()) {
                var verts = switch (face.getKey()) {
                    case "north" -> new float[][]{{x1,y1,z0},{x1,y0,z0},{x0,y0,z0},{x0,y1,z0}};
                    case "south" -> new float[][]{{x0,y1,z1},{x0,y0,z1},{x1,y0,z1},{x1,y1,z1}};
                    case "west" -> new float[][]{{x0,y1,z0},{x0,y0,z0},{x0,y0,z1},{x0,y1,z1}};
                    case "east" -> new float[][]{{x1,y1,z1},{x1,y0,z1},{x1,y0,z0},{x1,y1,z0}};
                    case "up" -> new float[][]{{x0,y1,z0},{x0,y1,z1},{x1,y1,z1},{x1,y1,z0}};
                    default -> new float[][]{{x0,y0,z1},{x0,y0,z0},{x1,y0,z0},{x1,y0,z1}};
                };
                var normal = switch (face.getKey()) {
                    case "north" -> new float[]{0,0,-1};
                    case "south" -> new float[]{0,0,1};
                    case "west" -> new float[]{-1,0,0};
                    case "east" -> new float[]{1,0,0};
                    case "up" -> new float[]{0,1,0};
                    default -> new float[]{0,-1,0};
                };
                var definition = face.getValue().getAsJsonObject();
                var uv = definition.getAsJsonArray("uv");
                var u0 = uv.get(0).getAsFloat() / 16;
                var v0 = uv.get(1).getAsFloat() / 16;
                var u1 = uv.get(2).getAsFloat() / 16;
                var v1 = uv.get(3).getAsFloat() / 16;
                var texture = new float[][]{{u0,v0},{u0,v1},{u1,v1},{u1,v0}};
                var rot = definition.has("rotation") ? definition.get("rotation").getAsInt() / 90 : 0;
                for (var index = 0; index < 4; index++) {
                    var vertex = verts[index];
                    var coordinates = texture[(index + rot) % 4];
                    var position = new Vector3f(vertex[0], vertex[1], vertex[2]).mulPosition(matrices.peek().getPositionMatrix());
                    var direction = new Vector3f(normal[0], normal[1], normal[2]).mul(matrices.peek().getNormalMatrix());
                    baked.add(position.x);
                    baked.add(position.y);
                    baked.add(position.z);
                    baked.add(coordinates[0]);
                    baked.add(coordinates[1]);
                    baked.add(direction.x);
                    baked.add(direction.y);
                    baked.add(direction.z);
                }
            }
            matrices.pop();
        }
        var result = new float[baked.size()];
        for (var index = 0; index < result.length; index++) result[index] = baked.get(index);
        return result;
    }
}
