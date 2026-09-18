package net.per.primogemcraft.system.weapon;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
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

    protected WishWeaponShieldItem(Properties properties) {
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

    protected Holder<MobEffect> heldEffect(ItemStack stack) {
        return null;
    }

    protected int heldAmplifier(ItemStack stack) {
        return 0;
    }

    protected void refresh(ItemStack stack) {
    }

    protected void heldTick(ServerPlayer player, ItemStack stack) {
    }

    protected static int refinementOf(ItemStack stack) {
        return WeaponState.of(stack).refinements();
    }

    protected static Component cooldownReduction() {
        return WishReports.percent(COOLDOWN_REDUCTION_PER_LEVEL, ChatFormatting.AQUA);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(WeaponType.of(stack).labelKey()));
        tooltip.addAll(WishWeaponTooltips.lines(stack.getDescriptionId(), descriptions(stack)));
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (level.isClientSide() || !(entity instanceof ServerPlayer player)) return;
        refresh(stack);
        if (isHeld(player, slot, stack)) return;
        applyHeldEffect(player, stack);
        withstandBlockCooldown(player, stack);
        heldTick(player, stack);
    }

    private void applyHeldEffect(ServerPlayer player, ItemStack stack) {
        var effect = heldEffect(stack);
        if (effect == null) return;
        var amplifier = heldAmplifier(stack);
        var instance = player.getEffect(effect);
        if (instance != null && instance.getAmplifier() == amplifier) return;
        player.addEffect(new MobEffectInstance(effect, HELD_EFFECT_TICKS, amplifier, false, false));
    }

    private static void withstandBlockCooldown(ServerPlayer player, ItemStack stack) {
        var cooldowns = player.getCooldowns();
        if (!cooldowns.isOnCooldown(stack.getItem())) return;
        if (!PGCTimer.isDone(player, HANDY_TIMER)) return;
        cooldowns.removeCooldown(stack.getItem());
        var remaining = remainingCooldown(WeaponState.of(stack).level());
        if (remaining > 0) cooldowns.addCooldown(stack.getItem(), remaining);
        PGCTimer.set(player, HANDY_TIMER, remaining);
        stack.hurtAndBreak(Mth.nextInt(player.getRandom(), MIN_BLOCK_COST, MAX_BLOCK_COST), player.serverLevel(), player, item -> {
        });
    }

    private static int remainingCooldown(int level) {
        return Math.max(0, (int) Math.round(DISABLE_COOLDOWN * (1.0D - COOLDOWN_REDUCTION_PER_LEVEL * level)));
    }
}
