package net.per.primogemcraft.entity;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.per.primogemcraft.entity.mob.AbundanceBlightZombieEntity;
import net.per.primogemcraft.entity.mob.LivingItemEntity;
import net.per.primogemcraft.registry.PGCEntities;

public final class EntityEvents {
    public static void register() {
        FabricDefaultAttributeRegistry.register(PGCEntities.ABUNDANCE_BLIGHT_ZOMBIE.get(), AbundanceBlightZombieEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(PGCEntities.LIVING_ITEM.get(), LivingItemEntity.createAttributes());
    }
}