package net.per.primogemcraft.item.weapon;

import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.per.primogemcraft.component.WeaponCharge;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.system.weapon.*;
import net.per.primogemcraft.system.wish.WishReports;
import net.per.primogemcraft.util.TemporaryAttributes;

import java.util.List;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public class SkyriderSwordItem extends WishWeaponItem {
    private static final Tier TIER = new WeaponTier(1000, 4.0F, 2, WeaponTier.WOODEN_INCORRECT, PGCItems.UNFETTERED_METAL);

    private static final ResourceLocation FLIGHT_ID = ResourceLocation.fromNamespaceAndPath(MOD_ID, "weapon/flight");
    private static final double FLIGHT_LEVEL = 1.0D;
    private static final int MAX_HITS = 10;
    private static final int POWER_COOLDOWN = 400;
    private static final int FLIGHT_COOLDOWN = 600;
    private static final int FLIGHT_TICKS_BASE = 60;
    private static final int FLIGHT_TICKS_STEP = 20;
    private static final double POWER_BASE_RATIO = 0.12D;
    private static final double POWER_STEP_RATIO = 0.03D;
    private static final double MOVE_MULTIPLIER = 10.0D;
    private static final float ATTACK_DAMAGE = 6.0F;
    private static final float ATTACK_SPEED = -3.0F;
    private static final String RIGHT_CLICK = "right_click";
    private static final String SNEAK_USE = "sneak_use";
    private static final String POWER_TEXT = "power";
    private static final String COOLDOWN_TEXT = "cooldown";
    private static final String FLIGHT_TEXT = "flight";

    public SkyriderSwordItem(Properties properties) {
        super(TIER, properties.attributes(SwordItem.createAttributes(TIER, ATTACK_DAMAGE, ATTACK_SPEED)).fireResistant());
    }

    @Override
    protected List<WeaponDescription> description(ItemStack stack) {
        var refinement = WeaponState.of(stack).refinements();
        return List.of(
                WeaponDescription.of(RIGHT_CLICK, POWER_TEXT,
                        WishReports.percent(powerRatio(refinement), ChatFormatting.AQUA),
                        WishReports.percent(powerRatio(refinement) * MOVE_MULTIPLIER, ChatFormatting.AQUA)),
                WeaponDescription.note(COOLDOWN_TEXT,
                        WishReports.number(POWER_COOLDOWN / 20, ChatFormatting.AQUA),
                        WishReports.number(MAX_HITS, ChatFormatting.AQUA)),
                WeaponDescription.of(SNEAK_USE, FLIGHT_TEXT,
                        WishReports.number(flightTicks(refinement) / 20, ChatFormatting.AQUA)));
    }

    @Override
    public WeaponStacks stacks(ItemStack stack) {
        return WeaponStacks.temporary(WeaponCharge.of(stack), MAX_HITS);
    }

    @Override
    public List<WeaponModifier> conditionalPassives(Player player, ItemStack stack, int slot, int refinement) {
        if (isHeld(player, slot, stack) || WeaponCharge.of(stack) <= 0) return List.of();
        return List.of(
                WeaponModifier.conditional(Attributes.ATTACK_DAMAGE, AttributeModifier.Operation.ADD_MULTIPLIED_BASE,
                        (owner, worn, value) -> powerRatio(value)),
                WeaponModifier.conditional(Attributes.MOVEMENT_SPEED, AttributeModifier.Operation.ADD_MULTIPLIED_BASE,
                        (owner, worn, value) -> powerRatio(value) * MOVE_MULTIPLIER));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        if (level.isClientSide() || player.getCooldowns().isOnCooldown(stack.getItem())) return super.use(level, player, hand);
        var refinement = WeaponState.of(stack).refinements();
        if (player.isShiftKeyDown()) {
            TemporaryAttributes.apply(player, FLIGHT_ID, NeoForgeMod.CREATIVE_FLIGHT, FLIGHT_LEVEL,
                    AttributeModifier.Operation.ADD_VALUE, flightTicks(refinement));
            player.getCooldowns().addCooldown(stack.getItem(), FLIGHT_COOLDOWN);
            level.playSound(null, player.blockPosition(), SoundEvents.BAT_TAKEOFF, SoundSource.PLAYERS, 1.0F, 1.0F);
            return super.use(level, player, hand);
        }
        if (WeaponCharge.of(stack) > 0) return super.use(level, player, hand);
        WeaponCharge.set(stack, MAX_HITS);
        player.getCooldowns().addCooldown(stack.getItem(), POWER_COOLDOWN);
        level.playSound(null, player.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.5F, 1.2F);
        return super.use(level, player, hand);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        var result = super.hurtEnemy(stack, target, attacker);
        if (!(attacker.level() instanceof ServerLevel level)) return result;
        var remaining = WeaponCharge.of(stack);
        if (remaining <= 0) return result;
        WeaponCharge.set(stack, remaining - 1);
        if (remaining - 1 == 0)
            level.playSound(null, attacker.blockPosition(), SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 1.5F, 1.0F);
        return result;
    }

    private static double powerRatio(int refinement) {
        return POWER_BASE_RATIO + POWER_STEP_RATIO * (refinement - 1);
    }

    private static int flightTicks(int refinement) {
        return FLIGHT_TICKS_BASE + FLIGHT_TICKS_STEP * (refinement - 1);
    }
}
