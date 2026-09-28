package net.per.primogemcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.per.primogemcraft.registry.PGCEntities;
import net.per.primogemcraft.registry.PGCDataComponents;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.render.entity.WishArrowRenderer;
import net.per.primogemcraft.system.weapon.WishWeaponBowItem;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

@EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT)
public final class WishBowClient {
    private static WishBowRenderer renderer;

    @SubscribeEvent
    public static void registerExtensions(RegisterClientExtensionsEvent event) {
        var extension = new IClientItemExtensions() {
            @Override
            public boolean applyForgeHandTransform(PoseStack poses, LocalPlayer player, HumanoidArm arm, ItemStack stack,
                                                   float partialTick, float equipProgress, float swingProgress) {
                if (!player.isUsingItem() || player.getMainArm() != arm || player.getUseItem().getItem() != stack.getItem())
                    return false;
                var activeStack = player.getUseItem();
                var duration = activeStack.getOrDefault(PGCDataComponents.BOW_DRAW_DURATION.get(), 0);
                var shot = activeStack.getOrDefault(PGCDataComponents.BOW_SHOT_TIME.get(), player.level().getGameTime());
                var elapsed = duration - (shot - player.level().getGameTime()) + partialTick;
                var progress = duration <= 0 ? 0.0F : Mth.clamp(elapsed / duration, 0.0F, 1.0F);
                var direction = arm == HumanoidArm.RIGHT ? 1 : -1;
                poses.translate(direction * 0.56F, -0.52F - equipProgress * 0.6F, -0.72F);
                poses.translate(direction * -0.2785682F, 0.18344387F, 0.15731531F);
                poses.mulPose(Axis.XP.rotationDegrees(-13.935F));
                poses.mulPose(Axis.YP.rotationDegrees(direction * 35.3F));
                poses.mulPose(Axis.ZP.rotationDegrees(direction * -9.785F));
                poses.translate(0.0F, 0.0F, progress * 0.04F);
                poses.scale(1.0F, 1.0F, 1.0F + progress * 0.2F);
                poses.mulPose(Axis.YN.rotationDegrees(direction * 45.0F));
                return true;
            }

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) renderer = new WishBowRenderer();
                return renderer;
            }
        };
        for (var entry : PGCItems.REGISTRY.getEntries())
            if (entry.get() instanceof WishWeaponBowItem) event.registerItem(extension, entry.get());
    }

    @SubscribeEvent
    public static void registerReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener((ResourceManagerReloadListener) resources -> {
            if (renderer != null) renderer.onResourceManagerReload(resources);
        });
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(PGCEntities.WISH_ARROW.get(), WishArrowRenderer::new);
    }
}
