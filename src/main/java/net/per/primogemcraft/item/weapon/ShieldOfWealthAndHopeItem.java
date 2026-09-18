package net.per.primogemcraft.item.weapon;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
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

    public ShieldOfWealthAndHopeItem(Properties properties) {
        super(properties.durability(DURABILITY).fireResistant());
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack repairCandidate) {
        return repairCandidate.is(PGCItems.REFINED_MORA.get()) || repairCandidate.is(PGCItems.FINE_ENHANCEMENT_ORE.get());
    }

    @Override
    protected Holder<MobEffect> heldEffect(ItemStack stack) {
        return MobEffects.DAMAGE_RESISTANCE;
    }

    @Override
    protected int heldAmplifier(ItemStack stack) {
        return Math.min(refinementOf(stack), MAX_RESISTANCE) - 1;
    }

    @Override
    protected void heldTick(ServerPlayer player, ItemStack stack) {
        var overflow = refinementOf(stack) - MAX_RESISTANCE;
        if (overflow <= 0) return;
        if (!PGCTimer.isDone(player, ABSORPTION_TIMER)) return;
        PGCTimer.set(player, ABSORPTION_TIMER, ABSORPTION_TICKS);
        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, ABSORPTION_TICKS, overflow - 1, false, false));
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
                        WishReports.number(ABSORPTION_TICKS / TICKS_PER_SECOND, ChatFormatting.AQUA)),
                WeaponDescription.note(HANDY),
                WeaponDescription.note(HANDY_BLOCK),
                WeaponDescription.note(HANDY_COOLDOWN, cooldownReduction()));
    }
}
