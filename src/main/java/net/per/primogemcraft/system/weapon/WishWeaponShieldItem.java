package net.per.primogemcraft.system.weapon;

import net.minecraft.util.Formatting;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShieldItem;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.world.World;
import net.per.primogemcraft.system.wish.WishReports;
import net.per.primogemcraft.util.PGCTimer;

import java.util.List;

public abstract class WishWeaponShieldItem extends ShieldItem implements WishWeapon {
    private static final int HELD_EFFECT_TICKS = 60;
    private static final int MIN_BLOCK_COST = 1;
    private static final int MAX_BLOCK_COST = 5;
    private static final int DISABLE_COOLDOWN = 100;
    private static final double COOLDOWN_REDUCTION_PER_LEVEL = 0.01D;
    private static final String HANDY_TIMER = "weapon/handy_shield";

    protected WishWeaponShieldItem(Settings properties) {
        super(properties);
    }

    @Override
    public List<WeaponModifier> passives() {
        return List.of();
    }

    @Override
    public List<WeaponDescription> descriptions(ItemStack stack) {
        return description(stack);
    }

    protected abstract List<WeaponDescription> description(ItemStack stack);

    protected StatusEffect heldEffect(ItemStack stack) {
        return null;
    }

    protected int heldAmplifier(ItemStack stack) {
        return 0;
    }

    protected void refresh(ItemStack stack) {
    }

    protected void heldTick(ServerPlayerEntity player, ItemStack stack) {
    }

    protected static int refinementOf(ItemStack stack) {
        return WeaponState.of(stack).refinements();
    }

    protected static Text cooldownReduction() {
        return WishReports.percent(COOLDOWN_REDUCTION_PER_LEVEL, Formatting.AQUA);
    }

    @Override
    public void appendTooltip(ItemStack stack, @org.jetbrains.annotations.Nullable World level, List<Text> tooltip, TooltipContext flag) {
        tooltip.add(Text.translatable(WeaponType.of(stack).labelKey()));
        tooltip.addAll(WishWeaponTooltips.lines(stack.getTranslationKey(), descriptions(stack)));
    }

    @Override
    public void inventoryTick(ItemStack stack, World level, Entity entity, int slot, boolean selected) {
        if (level.isClient() || !(entity instanceof ServerPlayerEntity player)) return;
        refresh(stack);
        if (isHeld(player, slot, stack)) return;
        applyHeldEffect(player, stack);
        withstandBlockCooldown(player, stack);
        heldTick(player, stack);
    }

    private void applyHeldEffect(ServerPlayerEntity player, ItemStack stack) {
        var effect = heldEffect(stack);
        if (effect == null) return;
        var amplifier = heldAmplifier(stack);
        var instance = player.getStatusEffect(effect);
        if (instance != null && instance.getAmplifier() == amplifier) return;
        player.addStatusEffect(new StatusEffectInstance(effect, HELD_EFFECT_TICKS, amplifier, false, false));
    }

    private static void withstandBlockCooldown(ServerPlayerEntity player, ItemStack stack) {
        var cooldowns = player.getItemCooldownManager();
        if (!cooldowns.isCoolingDown(stack.getItem())) return;
        if (!PGCTimer.isDone(player, HANDY_TIMER)) return;
        cooldowns.remove(stack.getItem());
        var remaining = remainingCooldown(WeaponState.of(stack).level());
        if (remaining > 0) cooldowns.set(stack.getItem(), remaining);
        PGCTimer.set(player, HANDY_TIMER, remaining);
        stack.damage(MathHelper.nextInt(player.getRandom(), MIN_BLOCK_COST, MAX_BLOCK_COST), player, item -> {
        });
    }

    private static int remainingCooldown(int level) {
        return Math.max(0, (int) Math.round(DISABLE_COOLDOWN * (1.0D - COOLDOWN_REDUCTION_PER_LEVEL * level)));
    }
}
