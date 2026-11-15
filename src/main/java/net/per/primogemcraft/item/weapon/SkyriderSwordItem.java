package net.per.primogemcraft.item.weapon;

import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.world.World;
import net.per.primogemcraft.compat.attribute.FlightCompat;
import net.per.primogemcraft.component.WeaponCharge;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.system.weapon.*;
import net.per.primogemcraft.system.wish.WishReports;
import net.per.primogemcraft.util.TemporaryAttributes;

import java.util.List;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public class SkyriderSwordItem extends WishWeaponItem {
    private static final ToolMaterial TIER = new WeaponTier(1000, 4.0F, 2, WeaponTier.WOODEN_INCORRECT, PGCItems.UNFETTERED_METAL);

    private static final Identifier FLIGHT_ID = Identifier.of(MOD_ID, "weapon/flight");
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

    public SkyriderSwordItem(Settings properties) {
        super(TIER, ATTACK_DAMAGE, ATTACK_SPEED, properties.fireproof());
    }

    @Override
    protected List<WeaponDescription> description(ItemStack stack) {
        var refinement = WeaponState.of(stack).refinements();
        return List.of(
                WeaponDescription.of(RIGHT_CLICK, POWER_TEXT,
                        WishReports.percent(powerRatio(refinement), Formatting.AQUA),
                        WishReports.percent(powerRatio(refinement) * MOVE_MULTIPLIER, Formatting.AQUA)),
                WeaponDescription.note(COOLDOWN_TEXT,
                        WishReports.number(POWER_COOLDOWN / 20, Formatting.AQUA),
                        WishReports.number(MAX_HITS, Formatting.AQUA)),
                WeaponDescription.of(SNEAK_USE, FLIGHT_TEXT,
                        WishReports.number(flightTicks(refinement) / 20, Formatting.AQUA)));
    }

    @Override
    public WeaponStacks stacks(ItemStack stack) {
        return WeaponStacks.temporary(WeaponCharge.of(stack), MAX_HITS);
    }

    @Override
    public List<WeaponModifier> conditionalPassives(PlayerEntity player, ItemStack stack, int slot, int refinement) {
        if (isHeld(player, slot, stack) || WeaponCharge.of(stack) <= 0) return List.of();
        return List.of(
                WeaponModifier.conditional(EntityAttributes.GENERIC_ATTACK_DAMAGE, EntityAttributeModifier.Operation.MULTIPLY_BASE,
                        (owner, worn, value) -> powerRatio(value)),
                WeaponModifier.conditional(EntityAttributes.GENERIC_MOVEMENT_SPEED, EntityAttributeModifier.Operation.MULTIPLY_BASE,
                        (owner, worn, value) -> powerRatio(value) * MOVE_MULTIPLIER));
    }

    @Override
    public TypedActionResult<ItemStack> use(World level, PlayerEntity player, Hand hand) {
        var stack = player.getStackInHand(hand);
        if (level.isClient() || player.getItemCooldownManager().isCoolingDown(stack.getItem())) return super.use(level, player, hand);
        var refinement = WeaponState.of(stack).refinements();
        if (player.isSneaking()) {
            FlightCompat.grant(player, FLIGHT_ID, flightTicks(refinement));
            player.getItemCooldownManager().set(stack.getItem(), FLIGHT_COOLDOWN);
            level.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_BAT_TAKEOFF, SoundCategory.PLAYERS, 1.0F, 1.0F);
            return super.use(level, player, hand);
        }
        if (WeaponCharge.of(stack) > 0) return super.use(level, player, hand);
        WeaponCharge.set(stack, MAX_HITS);
        player.getItemCooldownManager().set(stack.getItem(), POWER_COOLDOWN);
        level.playSound(null, player.getBlockPos(), SoundEvents.BLOCK_BEACON_ACTIVATE, SoundCategory.PLAYERS, 1.5F, 1.2F);
        return super.use(level, player, hand);
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        var result = super.postHit(stack, target, attacker);
        if (!(attacker.getWorld() instanceof ServerWorld level)) return result;
        var remaining = WeaponCharge.of(stack);
        if (remaining <= 0) return result;
        WeaponCharge.set(stack, remaining - 1);
        if (remaining - 1 == 0)
            level.playSound(null, attacker.getBlockPos(), SoundEvents.BLOCK_BEACON_DEACTIVATE, SoundCategory.PLAYERS, 1.5F, 1.0F);
        return result;
    }

    private static double powerRatio(int refinement) {
        return POWER_BASE_RATIO + POWER_STEP_RATIO * (refinement - 1);
    }

    private static int flightTicks(int refinement) {
        return FLIGHT_TICKS_BASE + FLIGHT_TICKS_STEP * (refinement - 1);
    }
}
