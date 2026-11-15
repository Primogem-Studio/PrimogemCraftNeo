package net.per.primogemcraft.render.entity.model;

import net.minecraft.client.render.entity.model.CrossbowPosing;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.model.ModelPart;
import net.per.primogemcraft.entity.mob.AbundanceBlightZombieEntity;

public class AbundanceBlightZombieModel extends BipedEntityModel<AbundanceBlightZombieEntity> {
    public AbundanceBlightZombieModel(ModelPart root) {
        super(root);
    }

    @Override
    public void setAngles(AbundanceBlightZombieEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        super.setAngles(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        CrossbowPosing.charge(leftArm, rightArm, entity, entity.isAttacking());
    }
}
