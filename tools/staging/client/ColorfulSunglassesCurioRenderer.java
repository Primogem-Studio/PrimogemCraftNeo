package net.per.primogemcraft.client;

import com.mojang.blaze3d.vertex.MatrixStack;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.util.Identifier;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.per.primogemcraft.registry.PGCItems;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;
import top.theillusivec4.curios.api.client.ICurioRenderer;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public final class ColorfulSunglassesCurioRenderer implements ICurioRenderer {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(MOD_ID, "textures/models/armor/colorful_sunglasses_layer_1.png");

    private final HumanoidModel<LivingEntity> model = new HumanoidModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR));

    public static void register() {
        CuriosRendererRegistry.register(PGCItems.COLORFUL_SUNGLASSES.get(), ColorfulSunglassesCurioRenderer::new);
    }

    @Override
    public <T extends LivingEntity, M extends EntityModel<T>> void render(ItemStack stack, SlotContext slotContext,
            MatrixStack poseStack, RenderLayerParent<T, M> parent, MultiBufferSource buffers, int light,
            float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        var entity = slotContext.entity();
        if (entity.isInvisible() || !(parent.getModel() instanceof HumanoidModel<?>)) return;
        if (entity.getItemBySlot(EquipmentSlot.HEAD).is(PGCItems.COLORFUL_SUNGLASSES.get())) return;
        ICurioRenderer.followBodyRotations(entity, model);
        model.setAllVisible(false);
        model.head.visible = true;
        model.hat.visible = true;
        var buffer = ItemRenderer.getArmorFoilBuffer(buffers, RenderType.armorCutoutNoCull(TEXTURE), false, stack.hasFoil());
        model.renderToBuffer(poseStack, buffer, light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
    }
}
