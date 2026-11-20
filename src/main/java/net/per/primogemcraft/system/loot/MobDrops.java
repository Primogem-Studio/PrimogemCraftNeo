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
import net.per.primogemcraft.compat.event.EventDispatch;
import net.per.primogemcraft.registry.PGCItems;

/**
 * 村民/灾厄村民/凋灵被玩家击杀时按概率掉落特殊物品。
 *
 * <p>Forge 用 {@code LivingDropsEvent}；Fabric 侧由 {@code LivingEntityDropsMixin}
 * 收集掉落后经 {@link EventDispatch} 分发，这里注册监听即可。
 */
public final class MobDrops {
    private MobDrops() {
    }

    public static void register() {
        EventDispatch.onLivingDrops(MobDrops::onLivingDrops);
    }

    private static void onLivingDrops(LivingEntity entity, net.minecraft.entity.damage.DamageSource source,
                                      java.util.List<ItemEntity> drops) {
        if (source == null) return;
        if (!(entity.getWorld() instanceof ServerWorld level) || !level.getGameRules().getBoolean(GameRules.DO_MOB_LOOT)) return;
        var attacker = source.getAttacker();
        var villager = entity instanceof VillagerEntity && attacker instanceof PlayerEntity;
        var illager = entity instanceof PillagerEntity || entity instanceof VindicatorEntity
                || entity instanceof EvokerEntity || entity instanceof RavagerEntity;
        if (!villager && !(illager && attacker instanceof PlayerEntity)
                && !(entity instanceof WitherEntity && attacker != null)) return;
        var looting = attacker instanceof LivingEntity living ? EnchantmentHelper.getLooting(living) : 0;
        var chance = villager || illager ? 0.10D + looting * 0.01D : 0.15D + looting * 0.05D;
        if (entity.getRandom().nextDouble() >= chance) return;
        var stack = new ItemStack(villager || illager ? PGCItems.PRAISE_OF_HIGH_MORALS.get() : PGCItems.ENIGMATA_FACTION_BOND.get());
        var drop = new ItemEntity(level, entity.getX(), entity.getY(), entity.getZ(), stack);
        drop.resetPickupDelay();
        drops.add(drop);
    }
}