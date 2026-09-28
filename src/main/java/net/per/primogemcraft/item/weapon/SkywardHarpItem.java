package net.per.primogemcraft.item.weapon;

import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.per.primogemcraft.entity.misc.WishArrowEntity;
import net.per.primogemcraft.system.weapon.SkywardHarpStats;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.per.primogemcraft.system.weapon.BowAttackCycle;
import net.per.primogemcraft.system.weapon.WeaponDescription;
import net.per.primogemcraft.system.weapon.WeaponEnhancement;
import net.per.primogemcraft.system.weapon.WeaponModifier;
import net.per.primogemcraft.system.weapon.WishWeaponBowItem;
import net.per.primogemcraft.system.wish.WishReports;
import net.per.primogemcraft.system.wish.WishTooltips;
import net.per.primogemcraft.util.PGCTimer;

import java.util.List;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public final class SkywardHarpItem extends WishWeaponBowItem {
    public static final String COOLDOWN = "skyward_harp_aoe";
    private static final String VORTEX_READY = "skyward_harp_vortex_ready";
    private static final String VORTEX_COOLDOWN = "skyward_harp_vortex_cooldown";

    public SkywardHarpItem(Properties properties) {
        super(properties.fireResistant(), new BowAttackCycle(7, 20, 5), 3.0F,
                ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/item/skyward_harp.png"),
                ResourceLocation.withDefaultNamespace("textures/entity/projectiles/arrow.png"),
                net.minecraft.sounds.SoundEvents.ARROW_SHOOT,
                WeaponModifier.of(Attributes.ATTACK_DAMAGE, 0.20D, 0.05D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    }

    @Override
    public List<WeaponDescription> descriptions(ItemStack stack) {
        var refinement = WeaponEnhancement.refinementOf(WishTooltips.viewer(), stack);
        return List.of(WeaponDescription.of("passive", "echo",
                WishReports.percent(0.20D + 0.05D * (refinement - 1), ChatFormatting.AQUA),
                WishReports.percent(0.60D + 0.10D * (refinement - 1), ChatFormatting.AQUA),
                WishReports.number(Math.max(2.0D, 4.0D - 0.5D * (refinement - 1)), ChatFormatting.AQUA)),
                WeaponDescription.of("passive", "split",
                        WishReports.percent(SkywardHarpStats.splitChance(refinement), ChatFormatting.AQUA),
                        WishReports.number(SkywardHarpStats.splitCount(refinement), ChatFormatting.AQUA)),
                WeaponDescription.of("sneak_use", "vortex",
                        WishReports.number(SkywardHarpStats.vortexDuration(refinement) / 20.0D, ChatFormatting.AQUA),
                        WishReports.number(SkywardHarpStats.vortexCooldown(refinement) / 20.0D, ChatFormatting.AQUA),
                        WishReports.number(SkywardHarpStats.vortexRadius(refinement), ChatFormatting.AQUA)));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!player.isShiftKeyDown()) return super.use(level, player, hand);
        var stack = player.getItemInHand(hand);
        if (hand != InteractionHand.MAIN_HAND || !player.isAlive() || player.isSpectator())
            return InteractionResultHolder.fail(stack);
        if (level.isClientSide()) return InteractionResultHolder.consume(stack);
        if (player.containerMenu != player.inventoryMenu || !PGCTimer.isDone(player, VORTEX_COOLDOWN)
                || !PGCTimer.isDone(player, VORTEX_READY)) return InteractionResultHolder.fail(stack);
        player.stopUsingItem();
        PGCTimer.set(player, VORTEX_READY, Integer.MAX_VALUE);
        PGCTimer.set(player, VORTEX_COOLDOWN, SkywardHarpStats.vortexCooldown(WeaponEnhancement.refinementOf(player, stack)));
        level.playSound(null, player.blockPosition(), net.minecraft.sounds.SoundEvents.BREEZE_WIND_CHARGE_BURST.value(),
                net.minecraft.sounds.SoundSource.PLAYERS, 0.6F, 1.4F);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    protected Projectile createProjectile(Level level, LivingEntity shooter, ItemStack weapon, ItemStack ammunition, boolean critical) {
        var projectile = super.createProjectile(level, shooter, weapon, ammunition, critical);
        if (projectile instanceof WishArrowEntity arrow && arrow.getOwner() instanceof Player owner) {
            var refinement = WeaponEnhancement.refinementOf(owner, weapon);
            var splits = owner.getRandom().nextDouble() < SkywardHarpStats.splitChance(refinement)
                    ? SkywardHarpStats.splitCount(refinement) : 0;
            var duration = PGCTimer.isDone(shooter, VORTEX_READY) ? 0 : SkywardHarpStats.vortexDuration(refinement);
            arrow.configureSkywardHarp(splits, duration);
            if (duration > 0) PGCTimer.clear(shooter, VORTEX_READY);
        }
        return projectile;
    }

    public static boolean triggers(int refinement, Player player) {
        return PGCTimer.isDone(player, COOLDOWN)
                && player.getRandom().nextDouble() < 0.60D + 0.10D * (refinement - 1);
    }
}
