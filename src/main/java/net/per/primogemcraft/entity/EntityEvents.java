package net.per.primogemcraft.entity;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.per.primogemcraft.entity.mob.AbundanceBlightZombieEntity;
import net.per.primogemcraft.entity.mob.LivingItemEntity;
import net.per.primogemcraft.registry.PGCEntities;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

@EventBusSubscriber(modid = MOD_ID)
public final class EntityEvents {
    @SubscribeEvent
    private static void registerEntityAttributes(EntityAttributeCreationEvent event) {
        event.put(PGCEntities.ABUNDANCE_BLIGHT_ZOMBIE.get(), AbundanceBlightZombieEntity.createAttributes().build());
        event.put(PGCEntities.LIVING_ITEM.get(), LivingItemEntity.createAttributes().build());
    }
}
