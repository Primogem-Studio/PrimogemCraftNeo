package net.per.primogemcraft.system.loot;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.mob.EvokerEntity;
import net.minecraft.entity.mob.PillagerEntity;
import net.minecraft.entity.mob.RavagerEntity;
import net.minecraft.entity.mob.VindicatorEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.world.GameRules;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.per.primogemcraft.registry.PGCItems;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

@Mod.EventBusSubscriber(modid = MOD_ID)
public final class MobDrops {
    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        var entity = event.getEntity();
        if (!(entity.level() instanceof ServerWorld level) || !level.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)) return;
        var attacker = event.getSource().getEntity();
        var villager = entity instanceof Villager && attacker instanceof Player;
        var illager = entity instanceof Pillager || entity instanceof Vindicator || entity instanceof Evoker || entity instanceof Ravager;
        if (!villager && !(illager && attacker instanceof Player) && !(entity instanceof WitherBoss && attacker != null)) return;
        var looting = attacker instanceof LivingEntity living ? EnchantmentHelper.getMobLooting(living) : 0;
        var chance = villager || illager ? 0.10D + looting * 0.01D : 0.15D + looting * 0.05D;
        if (entity.getRandom().nextDouble() >= chance) return;
        var stack = new ItemStack(villager || illager ? PGCItems.PRAISE_OF_HIGH_MORALS.get() : PGCItems.ENIGMATA_FACTION_BOND.get());
        var drop = new ItemEntity(level, entity.getX(), entity.getY(), entity.getZ(), stack);
        drop.setDefaultPickUpDelay();
        event.getDrops().add(drop);
    }
}
