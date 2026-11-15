package net.per.primogemcraft.item.weapon;

import net.minecraft.util.Formatting;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.system.weapon.*;
import net.per.primogemcraft.system.wish.WishReports;
import net.per.primogemcraft.util.PGCTimer;

import java.util.List;

public class WasterGreatswordItem extends WishWeaponItem {
    private static final ToolMaterial TIER = new WeaponTier(256, 4.0F, 2, WeaponTier.WOODEN_INCORRECT, PGCItems.ENHANCEMENT_ORE);

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

    public WasterGreatswordItem(Settings properties) {
        super(TIER, ATTACK_DAMAGE, ATTACK_SPEED, properties.fireproof());
    }

    @Override
    protected List<WeaponDescription> description(ItemStack stack) {
        return List.of(
                WeaponDescription.of(NORMAL_ATTACK, RESTORE_TEXT,
                        WishReports.percent(TRIGGER_CHANCE, Formatting.AQUA),
                        WishReports.number(DURABILITY_RESTORE, Formatting.AQUA)),
                WeaponDescription.of(PASSIVE_ACTION, SPEED_TEXT,
                        WishReports.number(TIMER_TICKS / 20, Formatting.AQUA)));
    }

    @Override
    public List<WeaponModifier> conditionalPassives(PlayerEntity player, ItemStack stack, int slot, int refinement) {
        if (isHeld(player, slot, stack) || PGCTimer.isDone(player, TIMER)) return List.of();
        return List.of(WeaponModifier.conditional(EntityAttributes.GENERIC_ATTACK_SPEED, EntityAttributeModifier.Operation.ADDITION,
                (owner, worn, value) -> SPEED_BONUS));
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        var result = super.postHit(stack, target, attacker);
        if (!(attacker.getWorld() instanceof ServerWorld level)) return result;
        if (level.getRandom().nextDouble() >= TRIGGER_CHANCE) return result;
        if (!WeaponAttributes.restoreDurability(stack, DURABILITY_RESTORE)) return result;
        if (attacker instanceof PlayerEntity player) PGCTimer.set(player, TIMER, TIMER_TICKS);
        return result;
    }
}
