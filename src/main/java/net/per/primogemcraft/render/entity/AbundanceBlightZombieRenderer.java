package net.per.primogemcraft.render.entity;

import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;
import net.per.primogemcraft.render.entity.model.AbundanceBlightZombieModel;
import net.per.primogemcraft.entity.mob.AbundanceBlightZombieEntity;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public class AbundanceBlightZombieRenderer extends HumanoidMobRenderer<AbundanceBlightZombieEntity, AbundanceBlightZombieModel> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/entity/abundance_blight_zombie.png");
    private static final float SHADOW_RADIUS = 0.5F;

    public AbundanceBlightZombieRenderer(EntityRendererProvider.Context context) {
        super(context, new AbundanceBlightZombieModel(context.bakeLayer(ModelLayers.PLAYER)), SHADOW_RADIUS);
        addLayer(new HumanoidArmorLayer<>(this, new AbundanceBlightZombieModel(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)), new AbundanceBlightZombieModel(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)), context.getModelManager()));
    }

    @Override
    public ResourceLocation getTextureLocation(AbundanceBlightZombieEntity entity) {
        return TEXTURE;
    }
}
