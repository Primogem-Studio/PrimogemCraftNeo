package net.per.primogemcraft.render.entity;

import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.util.Identifier;
import net.per.primogemcraft.render.entity.model.AbundanceBlightZombieModel;
import net.per.primogemcraft.entity.mob.AbundanceBlightZombieEntity;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public class AbundanceBlightZombieRenderer extends BipedEntityRenderer<AbundanceBlightZombieEntity, AbundanceBlightZombieModel> {
    private static final Identifier TEXTURE = Identifier.of(MOD_ID, "textures/entity/abundance_blight_zombie.png");
    private static final float SHADOW_RADIUS = 0.5F;

    public AbundanceBlightZombieRenderer(EntityRendererFactory.Context context) {
        super(context, new AbundanceBlightZombieModel(context.getPart(EntityModelLayers.PLAYER)), SHADOW_RADIUS);
        addFeature(new ArmorFeatureRenderer<>(this, new AbundanceBlightZombieModel(context.getPart(EntityModelLayers.PLAYER_INNER_ARMOR)), new AbundanceBlightZombieModel(context.getPart(EntityModelLayers.PLAYER_OUTER_ARMOR)), context.getModelManager()));
    }

    @Override
    public Identifier getTexture(AbundanceBlightZombieEntity entity) {
        return TEXTURE;
    }
}
