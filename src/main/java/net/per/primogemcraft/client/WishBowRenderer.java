package net.per.primogemcraft.client;

import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.item.BuiltinModelItemRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.resource.ResourceManager;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.item.ItemStack;
import net.per.primogemcraft.PrimogemCraft;
import net.per.primogemcraft.compat.component.StackComponents;
import net.per.primogemcraft.registry.PGCDataComponents;
import net.per.primogemcraft.system.weapon.WishWeaponBowItem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class WishBowRenderer extends BuiltinModelItemRenderer {
    private final Map<WishWeaponBowItem, List<List<BowSpriteMesh.Quad>>> meshes = new HashMap<>();

    public WishBowRenderer() {
        super(MinecraftClient.getInstance().getBlockEntityRenderDispatcher(), MinecraftClient.getInstance().getEntityModelLoader());
    }

    @Override
    public void render(ItemStack stack, ModelTransformationMode context, MatrixStack poses, VertexConsumerProvider buffers, int light, int overlay) {
        if (!(stack.getItem() instanceof WishWeaponBowItem bow)) return;
        var level = MinecraftClient.getInstance().world;
        var shot = StackComponents.get(stack, PGCDataComponents.BOW_SHOT_TIME);
        var duration = StackComponents.getOrDefault(stack, PGCDataComponents.BOW_DRAW_DURATION, 0);
        var firstPerson = context == ModelTransformationMode.FIRST_PERSON_LEFT_HAND || context == ModelTransformationMode.FIRST_PERSON_RIGHT_HAND;
        var frame = level == null || shot == null || !firstPerson && context != ModelTransformationMode.THIRD_PERSON_LEFT_HAND
                && context != ModelTransformationMode.THIRD_PERSON_RIGHT_HAND ? 0
                : bow.cycle().frame(duration - (shot - level.getTime()), duration);
        var frames = meshes.computeIfAbsent(bow, this::load);
        if (frames.isEmpty()) return;
        var vertices = ItemRenderer.getDirectItemGlintConsumer(buffers, RenderLayer.getEntityCutout(bow.texture()), true, stack.hasGlint());
        var pose = poses.peek();
        for (var quad : frames.get(frame)) {
            for (var vertex : quad.vertices()) {
                vertices.vertex(pose.getPositionMatrix(), vertex.x(), vertex.y(), vertex.z()).color(-1).texture(vertex.u(), vertex.v())
                        .overlay(overlay).light(light).normal(pose.getNormalMatrix(), quad.normalX(), quad.normalY(), quad.normalZ())
                        .next();
            }
        }
    }

    private List<List<BowSpriteMesh.Quad>> load(WishWeaponBowItem bow) {
        try (var stream = MinecraftClient.getInstance().getResourceManager().open(bow.texture()); var image = NativeImage.read(stream)) {
            var size = image.getWidth();
            var count = bow.cycle().frameCount();
            if (image.getHeight() != size * count) throw new IOException("Bow texture must contain vertically stacked square frames");
            var frames = new ArrayList<List<BowSpriteMesh.Quad>>(count);
            for (var frame = 0; frame < count; frame++) {
                var offset = frame * size;
                frames.add(BowSpriteMesh.build(size, frame, count, pixel -> (image.getColor(pixel % size, offset + pixel / size) >>> 24) != 0));
            }
            return List.copyOf(frames);
        } catch (IOException exception) {
            PrimogemCraft.LOGGER.error("Unable to load bow texture {}", bow.texture(), exception);
            return List.of();
        }
    }

    @Override
    public void reload(ResourceManager resources) {
        meshes.clear();
    }
}
