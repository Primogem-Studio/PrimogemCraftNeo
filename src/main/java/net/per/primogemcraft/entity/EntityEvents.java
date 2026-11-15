package net.per.primogemcraft.entity;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.per.primogemcraft.entity.mob.AbundanceBlightZombieEntity;
import net.per.primogemcraft.entity.mob.LivingItemEntity;
import net.per.primogemcraft.registry.PGCEntities;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

@Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class EntityEvents {
    @SubscribeEvent
    public static void registerEntityAttributes(EntityAttributeCreationEvent event) {
        event.put(PGCEntities.ABUNDANCE_BLIGHT_ZOMBIE.get(), AbundanceBlightZombieEntity.createAttributes().build());
        event.put(PGCEntities.LIVING_ITEM.get(), LivingItemEntity.createAttributes().build());
    }
}
