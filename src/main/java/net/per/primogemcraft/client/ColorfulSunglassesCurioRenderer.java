package net.per.primogemcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.per.primogemcraft.registry.PGCArmorMaterials;
import net.per.primogemcraft.registry.PGCItems;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;
import top.theillusivec4.curios.api.client.ICurioRenderer;

public final class ColorfulSunglassesCurioRenderer implements ICurioRenderer {
    private final HumanoidModel<LivingEntity> model = new HumanoidModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR));

    public static void register() {
        CuriosRendererRegistry.register(PGCItems.COLORFUL_SUNGLASSES.get(), ColorfulSunglassesCurioRenderer::new);
    }

    @Override
    public <T extends LivingEntity, M extends EntityModel<T>> void render(ItemStack stack, SlotContext slotContext,
            PoseStack poseStack, RenderLayerParent<T, M> parent, MultiBufferSource buffers, int light,
            float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        var entity = slotContext.entity();
        if (entity.isInvisible() || !(parent.getModel() instanceof HumanoidModel<?>)) return;
        if (entity.getItemBySlot(EquipmentSlot.HEAD).is(PGCItems.COLORFUL_SUNGLASSES.get())) return;
        ICurioRenderer.followBodyRotations(entity, model);
        model.setAllVisible(false);
        model.head.visible = true;
        model.hat.visible = true;
        for (var layer : PGCArmorMaterials.COLORFUL_SUNGLASSES.value().layers()) {
            var buffer = ItemRenderer.getArmorFoilBuffer(buffers, RenderType.armorCutoutNoCull(layer.texture(false)), stack.hasFoil());
            model.renderToBuffer(poseStack, buffer, light, OverlayTexture.NO_OVERLAY);
        }
    }
}
