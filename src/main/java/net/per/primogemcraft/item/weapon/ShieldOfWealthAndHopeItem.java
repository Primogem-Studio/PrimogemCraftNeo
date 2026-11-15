package net.per.primogemcraft.item.weapon;

import net.minecraft.util.Formatting;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.system.weapon.WeaponDescription;
import net.per.primogemcraft.system.weapon.WishWeaponShieldItem;
import net.per.primogemcraft.system.wish.WishReports;
import net.per.primogemcraft.util.PGCTimer;

import java.util.List;

public class ShieldOfWealthAndHopeItem extends WishWeaponShieldItem {
    private static final int DURABILITY = 2048;
    private static final int MAX_RESISTANCE = 2;
    private static final int ABSORPTION_TICKS = 800;
    private static final int TICKS_PER_SECOND = 20;
    private static final String ABSORPTION_TIMER = "weapon/mora_shield_absorption";
    private static final String FLAVOR0 = "flavor0";
    private static final String SEPARATOR = "separator";
    private static final String RESISTANCE = "resistance";
    private static final String RESISTANCE_EFFECT = "resistance_effect";
    private static final String ABSORPTION = "absorption";
    private static final String ABSORPTION_EFFECT = "absorption_effect";
    private static final String HANDY = "handy";
    private static final String HANDY_BLOCK = "handy_block";
    private static final String HANDY_COOLDOWN = "handy_cooldown";

    public ShieldOfWealthAndHopeItem(Settings properties) {
        super(properties.maxDamage(DURABILITY).fireproof());
    }

    @Override
    public boolean canRepair(ItemStack stack, ItemStack repairCandidate) {
        return repairCandidate.isOf(PGCItems.REFINED_MORA.get()) || repairCandidate.isOf(PGCItems.FINE_ENHANCEMENT_ORE.get());
    }

    @Override
    protected StatusEffect heldEffect(ItemStack stack) {
        return StatusEffects.RESISTANCE;
    }

    @Override
    protected int heldAmplifier(ItemStack stack) {
        return Math.min(refinementOf(stack), MAX_RESISTANCE) - 1;
    }

    @Override
    protected void heldTick(ServerPlayerEntity player, ItemStack stack) {
        var overflow = refinementOf(stack) - MAX_RESISTANCE;
        if (overflow <= 0) return;
        if (!PGCTimer.isDone(player, ABSORPTION_TIMER)) return;
        PGCTimer.set(player, ABSORPTION_TIMER, ABSORPTION_TICKS);
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, ABSORPTION_TICKS, overflow - 1, false, false));
    }

    @Override
    protected List<WeaponDescription> description(ItemStack stack) {
        return List.of(
                WeaponDescription.note(FLAVOR0),
                WeaponDescription.note(SEPARATOR),
                WeaponDescription.note(RESISTANCE),
                WeaponDescription.note(RESISTANCE_EFFECT),
                WeaponDescription.note(ABSORPTION),
                WeaponDescription.note(ABSORPTION_EFFECT,
                        WishReports.number(ABSORPTION_TICKS / TICKS_PER_SECOND, Formatting.AQUA)),
                WeaponDescription.note(HANDY),
                WeaponDescription.note(HANDY_BLOCK),
                WeaponDescription.note(HANDY_COOLDOWN, cooldownReduction()));
    }
}
