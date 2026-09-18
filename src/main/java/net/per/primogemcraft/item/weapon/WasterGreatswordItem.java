package net.per.primogemcraft.item.weapon;

import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.system.weapon.*;
import net.per.primogemcraft.system.wish.WishReports;
import net.per.primogemcraft.util.PGCTimer;

import java.util.List;

public class WasterGreatswordItem extends WishWeaponItem {
    private static final Tier TIER = new WeaponTier(256, 4.0F, 2, WeaponTier.WOODEN_INCORRECT, PGCItems.ENHANCEMENT_ORE);

    private static final String TIMER = "waster_greatsword";
    private static final double TRIGGER_CHANCE = 0.5D;
    private static final int DURABILITY_RESTORE = 1;
    private static final int TIMER_TICKS = 100;
    private static final double SPEED_BONUS = 1.0D;
    private static final float ATTACK_DAMAGE = 7.0F;
    private static final float ATTACK_SPEED = -3.5F;
    private static final String NORMAL_ATTACK = "normal_attack";
    private static final String PASSIVE_ACTION = "passive";
    private static final String RESTORE_TEXT = "restore";
    private static final String SPEED_TEXT = "speed";

    public WasterGreatswordItem(Properties properties) {
        super(TIER, properties.attributes(SwordItem.createAttributes(TIER, ATTACK_DAMAGE, ATTACK_SPEED)).fireResistant());
    }

    @Override
    protected List<WeaponDescription> description(ItemStack stack) {
        return List.of(
                WeaponDescription.of(NORMAL_ATTACK, RESTORE_TEXT,
                        WishReports.percent(TRIGGER_CHANCE, ChatFormatting.AQUA),
                        WishReports.number(DURABILITY_RESTORE, ChatFormatting.AQUA)),
                WeaponDescription.of(PASSIVE_ACTION, SPEED_TEXT,
                        WishReports.number(TIMER_TICKS / 20, ChatFormatting.AQUA)));
    }

    @Override
    public List<WeaponModifier> conditionalPassives(Player player, ItemStack stack, int slot, int refinement) {
        if (isHeld(player, slot, stack) || PGCTimer.isDone(player, TIMER)) return List.of();
        return List.of(WeaponModifier.conditional(Attributes.ATTACK_SPEED, AttributeModifier.Operation.ADD_VALUE,
                (owner, worn, value) -> SPEED_BONUS));
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        var result = super.hurtEnemy(stack, target, attacker);
        if (!(attacker.level() instanceof ServerLevel level)) return result;
        if (level.getRandom().nextDouble() >= TRIGGER_CHANCE) return result;
        if (!WeaponAttributes.restoreDurability(stack, DURABILITY_RESTORE)) return result;
        if (attacker instanceof Player player) PGCTimer.set(player, TIMER, TIMER_TICKS);
        return result;
    }
}
