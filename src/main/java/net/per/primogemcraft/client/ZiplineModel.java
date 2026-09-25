package net.per.primogemcraft.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.io.IOException;
import java.util.ArrayList;
import org.joml.Vector3f;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public final class ZiplineModel extends SimplePreparableReloadListener<float[]> {
    public static final ZiplineModel INSTANCE = new ZiplineModel();
    private float[] vertices = new float[0];

    @Override
    protected float[] prepare(ResourceManager manager, ProfilerFiller profiler) {
        try (var reader = manager.openAsReader(ResourceLocation.fromNamespaceAndPath(MOD_ID, "entity_models/zipline.json"))) {
            return bake(JsonParser.parseReader(reader).getAsJsonObject().getAsJsonArray("elements"));
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load zipline model", exception);
        }
    }

    @Override
    protected void apply(float[] prepared, ResourceManager manager, ProfilerFiller profiler) {
        vertices = prepared;
    }

    public void render(PoseStack poses, VertexConsumer buffer, int light) {
        var pose = poses.last();
        for (var index = 0; index < vertices.length; index += 8) {
            buffer.addVertex(pose, vertices[index], vertices[index + 1], vertices[index + 2]).setColor(-1)
                    .setUv(vertices[index + 3], vertices[index + 4]).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light)
                    .setNormal(pose, vertices[index + 5], vertices[index + 6], vertices[index + 7]);
        }
    }

    private static float[] bake(JsonArray elements) {
        var poses = new PoseStack();
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
            poses.pushPose();
            if (element.has("rotation")) {
                var rotation = element.getAsJsonObject("rotation");
                var origin = rotation.getAsJsonArray("origin");
                var x = origin.get(0).getAsFloat();
                var y = origin.get(1).getAsFloat();
                var z = origin.get(2).getAsFloat();
                var axis = switch (rotation.get("axis").getAsString()) {
                    case "x" -> Axis.XP;
                    case "z" -> Axis.ZP;
                    default -> Axis.YP;
                };
                poses.translate(x, y, z);
                poses.mulPose(axis.rotationDegrees(rotation.get("angle").getAsFloat()));
                poses.translate(-x, -y, -z);
            }
            for (var face : element.getAsJsonObject("faces").entrySet()) {
                var vertices = switch (face.getKey()) {
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
                var rotation = definition.has("rotation") ? definition.get("rotation").getAsInt() / 90 : 0;
                for (var index = 0; index < 4; index++) {
                    var vertex = vertices[index];
                    var coordinates = texture[(index + rotation) % 4];
                    var position = new Vector3f(vertex[0], vertex[1], vertex[2]).mulPosition(poses.last().pose());
                    var direction = new Vector3f(normal[0], normal[1], normal[2]).mul(poses.last().normal());
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
            poses.popPose();
        }
        var result = new float[baked.size()];
        for (var index = 0; index < result.length; index++) result[index] = baked.get(index);
        return result;
    }
}
