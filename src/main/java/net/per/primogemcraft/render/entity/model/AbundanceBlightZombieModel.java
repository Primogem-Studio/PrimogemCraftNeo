package net.per.primogemcraft.render.entity.model;

import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.per.primogemcraft.entity.mob.AbundanceBlightZombieEntity;

public class AbundanceBlightZombieModel extends HumanoidModel<AbundanceBlightZombieEntity> {
    public AbundanceBlightZombieModel(ModelPart root) {
        super(root);
    }

    @Override
    public void setupAnim(AbundanceBlightZombieEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        AnimationUtils.animateZombieArms(leftArm, rightArm, entity.isAggressive(), attackTime, ageInTicks);
    }
}
