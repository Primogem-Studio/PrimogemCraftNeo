package net.per.primogemcraft.system.loot;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Evoker;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraft.world.entity.monster.Vindicator;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.GameRules;
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
        if (!(entity.level() instanceof ServerLevel level) || !level.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)) return;
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
