package net.per.primogemcraft.item.weapon;

import net.minecraft.util.Formatting;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.world.World;
import net.minecraft.util.math.Vec3d;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.system.element.Element;
import net.per.primogemcraft.system.element.ElementDamageOptions;
import net.per.primogemcraft.system.weapon.*;
import net.per.primogemcraft.system.wish.WishReports;

import java.util.List;

public class EngulfingLightningItem extends WishWeaponItem {
    private static final ToolMaterial TIER = new WeaponTier(2000, 4.0F, 20, WeaponTier.WOODEN_INCORRECT, PGCItems.VAJRADA_AMETHYST_SLIVER);

    private static final int NO_KILL_COOLDOWN = 40;
    private static final int KILL_COOLDOWN = 800;
    private static final int MIN_FOOD = 9;
    private static final int FOOD_COST = 9;
    private static final int SATURATION_CAP = 20;
    private static final double RADIUS = 8.0D;
    private static final double LIGHTNING_PER_REFINEMENT = 0.3D;
    private static final double CORE_BASE = 0.14D;
    private static final double CORE_STEP = 0.035D;
    private static final double BONUS_BASE = 0.2D;
    private static final double BONUS_STEP = 0.05D;
    private static final double MISTSPLITTER_BASE = 0.04D;
    private static final double MISTSPLITTER_STEP = 0.01D;
    private static final double LOW_FOOD_PENALTY_BASE = 0.8D;
    private static final double LOW_FOOD_PENALTY_STEP = 0.1D;
    private static final double POWER_BASE = 0.08D;
    private static final double POWER_STEP = 0.02D;
    private static final int LOW_FOOD_LEVEL = 20;
    private static final float ATTACK_DAMAGE = 9.0F;
    private static final float ATTACK_SPEED = -2.0F;
    private static final String RIGHT_CLICK = "right_click_effect";
    private static final String PASSIVE_ACTION = "passive";
    private static final String CORE_ACTION = "core_passive";
    private static final String PENALTY_ACTION = "negative_passive";
    private static final String LIGHTNING_TEXT = "lightning";
    private static final String SATURATION_TEXT = "saturation";
    private static final String BONUS_TEXT = "bonus";
    private static final String ENHANCED_TEXT = "enhanced";
    private static final String MISTSPLITTER_TEXT = "mistsplitter";
    private static final String POWER_TEXT = "power";
    private static final String PENALTY_TEXT = "penalty";

    public EngulfingLightningItem(Settings properties) {
        super(TIER, ATTACK_DAMAGE, ATTACK_SPEED, properties.fireproof(), WeaponModifier.of(EntityAttributes.GENERIC_ATTACK_DAMAGE, POWER_BASE, POWER_STEP, EntityAttributeModifier.Operation.MULTIPLY_TOTAL));
    }

    @Override
    public List<WeaponModifier> conditionalPassives(PlayerEntity player, ItemStack stack, int slot, int refinement) {
        if (isHeld(player, slot, stack)) return List.of();
        var saturation = player.getHungerManager().getSaturationLevel();
        var core = core(saturation, refinement, offhandScale(player, refinement));
        return List.of(
                WeaponModifier.conditional(EntityAttributes.GENERIC_ATTACK_DAMAGE, EntityAttributeModifier.Operation.ADDITION, (owner, worn, value) -> core),
                WeaponModifier.conditional(EntityAttributes.GENERIC_ATTACK_DAMAGE, EntityAttributeModifier.Operation.MULTIPLY_TOTAL,
                        (owner, worn, value) -> owner.getHungerManager().getFoodLevel() < LOW_FOOD_LEVEL
                                ? -(LOW_FOOD_PENALTY_BASE - LOW_FOOD_PENALTY_STEP * (value - 1)) : 0.0D));
    }

    @Override
    protected List<WeaponDescription> description(ItemStack stack) {
        var state = WeaponState.of(stack);
        var refinement = state.refinements();
        return List.of(
                WeaponDescription.of(RIGHT_CLICK, LIGHTNING_TEXT,
                        WishReports.percent(LIGHTNING_PER_REFINEMENT * refinement, Formatting.AQUA),
                        WishReports.number(NO_KILL_COOLDOWN / 20, Formatting.AQUA)),
                WeaponDescription.note(SATURATION_TEXT,
                        WishReports.number(saturationSeconds(refinement), Formatting.AQUA)),
                WeaponDescription.of(CORE_ACTION, BONUS_TEXT,
                        WishReports.percent(CORE_BASE + CORE_STEP * (refinement - 1), Formatting.AQUA)),
                WeaponDescription.of(PASSIVE_ACTION, ENHANCED_TEXT,
                        WishReports.percent(BONUS_BASE + BONUS_STEP * (refinement - 1), Formatting.AQUA)),
                WeaponDescription.note(MISTSPLITTER_TEXT,
                        WishReports.percent(MISTSPLITTER_BASE + MISTSPLITTER_STEP * (refinement - 1), Formatting.AQUA)),
                WeaponDescription.note(POWER_TEXT,
                        WishReports.percent(POWER_BASE + POWER_STEP * (refinement - 1), Formatting.AQUA)),
                WeaponDescription.of(PENALTY_ACTION, PENALTY_TEXT,
                        WishReports.percent(LOW_FOOD_PENALTY_BASE - LOW_FOOD_PENALTY_STEP * (refinement - 1), Formatting.AQUA)));
    }

    @Override
    public TypedActionResult<ItemStack> use(World level, PlayerEntity player, Hand hand) {
        var stack = player.getStackInHand(hand);
        if (level.isClient() || player.getItemCooldownManager().isCoolingDown(stack.getItem())) return super.use(level, player, hand);
        if (player.isSneaking()) return super.use(level, player, hand);
        if (player.getHungerManager().getFoodLevel() < MIN_FOOD) return super.use(level, player, hand);
        var refinement = WeaponState.of(stack).refinements();
        var damage = (float) (player.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE) * LIGHTNING_PER_REFINEMENT * refinement);
        var defeated = 0;
        if (level instanceof ServerWorld server) {
            for (var candidate : server.getEntitiesByClass(LivingEntity.class, player.getBoundingBox().expand(RADIUS), e -> true)) {
                if (candidate == player || !candidate.isAlive()) continue;
                if (!(candidate instanceof MobEntity mob) || mob.getTarget() != player) continue;
                WeaponDamage.extraHit(candidate, WeaponDamage.lightning(server, Element.ELECTRO, candidate, player, ElementDamageOptions.DETACHED), damage);
                if (!candidate.isAlive()) defeated++;
                strike(server, candidate);
                player.getHungerManager().setFoodLevel(Math.max(0, player.getHungerManager().getFoodLevel() - FOOD_COST));
            }
            server.spawnParticles(ParticleTypes.ELECTRIC_SPARK, player.getX(), player.getY() + 1.0D, player.getZ(), 12, 0.4D, 0.4D, 0.4D, 0.05D);
        }
        player.getItemCooldownManager().set(stack.getItem(), defeated > 0 ? KILL_COOLDOWN : NO_KILL_COOLDOWN);
        if (defeated > 0) {
            player.getHungerManager().setSaturationLevel(player.getHungerManager().getSaturationLevel() + Math.min(SATURATION_CAP, defeated));
            player.getHungerManager().setFoodLevel(SATURATION_CAP);
            level.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 1.0F, 1.0F);
        }
        return super.use(level, player, hand);
    }

    private static void strike(ServerWorld level, LivingEntity target) {
        var bolt = EntityType.LIGHTNING_BOLT.create(level);
        if (bolt == null) return;
        bolt.refreshPositionAfterTeleport(Vec3d.ofBottomCenter(target.getBlockPos()));
        bolt.setCosmetic(true);
        level.spawnEntity(bolt);
    }

    private static double offhandScale(PlayerEntity player, int refinement) {
        var offhand = player.getOffHandStack();
        if (!offhand.isOf(PGCItems.MISTSPLITTER_REFORGED.get())) return 1.0D;
        return 1.0D + WeaponState.of(offhand).refinements() * (MISTSPLITTER_BASE + MISTSPLITTER_STEP * (refinement - 1));
    }

    private static double core(double saturation, int refinement, double scale) {
        var base = CORE_BASE + CORE_STEP * (refinement - 1);
        return (saturation * base + saturation * base * (BONUS_BASE + BONUS_STEP * (refinement - 1))) * scale;
    }

    private static int saturationTicks(int refinement) {
        return 160 + 40 * (refinement - 1);
    }

    private static int saturationSeconds(int refinement) {
        return saturationTicks(refinement) / 20;
    }
}
