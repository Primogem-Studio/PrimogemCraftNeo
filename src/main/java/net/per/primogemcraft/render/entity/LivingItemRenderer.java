package net.per.primogemcraft.render.entity;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.Transformation;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.item.BlockItem;
import net.minecraft.item.MiningToolItem;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.per.primogemcraft.entity.mob.LivingItemEntity;

public class LivingItemRenderer extends EntityRenderer<LivingItemEntity> {
    private static final Identifier TEXTURE = new Identifier("textures/block/stone.png");
    private static final float SHADOW_RADIUS = 0.4F;

    private static Transformation defaultFlatThirdPerson;
    private static boolean defaultFlatThirdPersonResolved;

    public LivingItemRenderer(EntityRendererFactory.Context context) {
        super(context);
        shadowRadius = SHADOW_RADIUS;
    }

    @Override
    public Identifier getTexture(LivingItemEntity entity) {
        return TEXTURE;
    }

    @Override
    public void render(LivingItemEntity entity, float entityYaw, float partialTick, net.minecraft.client.util.math.MatrixStack poseStack, VertexConsumerProvider bufferSource, int packedLight) {
        if (entity.isInvisible()) return;
        var stack = entity.getCarriedStack();
        if (stack.isEmpty()) return;
        poseStack.push();
        var model = MinecraftClient.getInstance().getItemRenderer().getModels().getModel(stack);
        var swing = entity.getSwingTicks();
        var bowDrawing = LivingItemEntity.isBowLike(stack) && swing > 0;
        var displayContext = resolveDisplayContext(model, bowDrawing);
        var bob = (float) Math.sin((entity.age + partialTick) * 0.12) * 0.1F;
        poseStack.translate(0.0, 0.35 + bob, 0.0);
        poseStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-entity.getYaw() + 180.0F));
        if (bowDrawing) {
            applyBowDrawPose(swing, poseStack);
        } else {
            applySwingPose(entity, stack, poseStack);
        }
        if (stack.getItem() instanceof BlockItem || model.getTransformation() == ModelTransformation.NONE) {
            displayContext = ModelTransformationMode.NONE;
            poseStack.translate(0.0, 0.15, 0.0);
        }
        var rotate = entity.getRotateTicks();
        if (rotate > 0) {
            var progress = 1.0F - rotate / 10.0F;
            poseStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(360.0F * progress));
        }
        MinecraftClient.getInstance().getItemRenderer().renderItem(stack, displayContext, packedLight, OverlayTexture.DEFAULT_UV, poseStack, bufferSource, entity.getWorld(), entity.getId());
        poseStack.pop();
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    private ModelTransformationMode resolveDisplayContext(BakedModel model, boolean bowDrawing) {
        if (bowDrawing) return ModelTransformationMode.THIRD_PERSON_RIGHT_HAND;
        var transforms = model.getTransformation();
        if (transforms == ModelTransformation.NONE) return ModelTransformationMode.THIRD_PERSON_RIGHT_HAND;
        if (!transforms.thirdPersonRightHand.equals(defaultFlatThirdPerson())) return ModelTransformationMode.THIRD_PERSON_RIGHT_HAND;
        return ModelTransformationMode.FIXED;
    }

    private static Transformation defaultFlatThirdPerson() {
        if (!defaultFlatThirdPersonResolved) {
            var paper = MinecraftClient.getInstance().getItemRenderer().getModels().getModel(new ItemStack(Items.PAPER));
            defaultFlatThirdPerson = paper.getTransformation().thirdPersonRightHand;
            defaultFlatThirdPersonResolved = true;
        }
        return defaultFlatThirdPerson;
    }

    private void applyBowDrawPose(int swing, net.minecraft.client.util.math.MatrixStack poseStack) {
        var progress = MathHelper.clamp(1.0F - swing / (float) LivingItemEntity.BOW_DRAW_TICKS, 0.0F, 1.0F);
        poseStack.translate(-0.2785682F, 0.18344387F, 0.15731531F);
        poseStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-13.935F));
        poseStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(35.3F));
        poseStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-9.785F));
        poseStack.translate(0.0F, 0.0F, progress * 0.04F);
        poseStack.scale(1.0F, 1.0F, 1.0F + progress * 0.2F);
        poseStack.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees(45.0F));
    }

    private void applySwingPose(LivingItemEntity entity, ItemStack stack, net.minecraft.client.util.math.MatrixStack poseStack) {
        var swing = entity.getSwingTicks();
        if (swing <= 0) return;
        var progress = 1.0F - swing / 5.0F;
        if (stack.getItem() instanceof MiningToolItem) {
            poseStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-90.0F * progress));
        } else {
            poseStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-45.0F * progress));
        }
    }
}
