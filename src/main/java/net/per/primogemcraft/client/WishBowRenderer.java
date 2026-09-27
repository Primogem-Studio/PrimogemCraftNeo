package net.per.primogemcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.packs.resources.ResourceManager;
import net.per.primogemcraft.PrimogemCraft;
import net.per.primogemcraft.registry.PGCDataComponents;
import net.per.primogemcraft.system.weapon.WishWeaponBowItem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class WishBowRenderer extends BlockEntityWithoutLevelRenderer {
    private final Map<WishWeaponBowItem, List<List<BowSpriteMesh.Quad>>> meshes = new HashMap<>();

    public WishBowRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack poses, MultiBufferSource buffers, int light, int overlay) {
        if (!(stack.getItem() instanceof WishWeaponBowItem bow)) return;
        var level = Minecraft.getInstance().level;
        var shot = stack.get(PGCDataComponents.BOW_SHOT_TIME.get());
        var frame = level == null || shot == null ? 0 : bow.cycle().frame(level.getGameTime() - shot);
        var frames = meshes.computeIfAbsent(bow, this::load);
        if (frames.isEmpty()) return;
        var vertices = ItemRenderer.getFoilBufferDirect(buffers, RenderType.entityCutout(bow.texture()), true, stack.hasFoil());
        var pose = poses.last();
        for (var quad : frames.get(frame)) {
            for (var vertex : quad.vertices()) {
                vertices.addVertex(pose, vertex.x(), vertex.y(), vertex.z()).setColor(-1).setUv(vertex.u(), vertex.v())
                        .setOverlay(overlay).setLight(light).setNormal(pose, quad.normalX(), quad.normalY(), quad.normalZ());
            }
        }
    }

    private List<List<BowSpriteMesh.Quad>> load(WishWeaponBowItem bow) {
        try (var stream = Minecraft.getInstance().getResourceManager().open(bow.texture()); var image = NativeImage.read(stream)) {
            var size = image.getWidth();
            var count = bow.cycle().frameCount();
            if (image.getHeight() != size * count) throw new IOException("Bow texture must contain vertically stacked square frames");
            var frames = new ArrayList<List<BowSpriteMesh.Quad>>(count);
            for (var frame = 0; frame < count; frame++) {
                var offset = frame * size;
                frames.add(BowSpriteMesh.build(size, frame, count, pixel -> (image.getPixelRGBA(pixel % size, offset + pixel / size) >>> 24) != 0));
            }
            return List.copyOf(frames);
        } catch (IOException exception) {
            PrimogemCraft.LOGGER.error("Unable to load bow texture {}", bow.texture(), exception);
            return List.of();
        }
    }

    @Override
    public void onResourceManagerReload(ResourceManager resources) {
        meshes.clear();
    }
}
