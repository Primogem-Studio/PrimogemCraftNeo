package net.per.primogemcraft.item.weapon;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.system.element.Element;
import net.per.primogemcraft.system.element.ElementDamageOptions;
import net.per.primogemcraft.system.weapon.*;
import net.per.primogemcraft.system.wish.WishReports;

import java.util.List;

public class EngulfingLightningItem extends WishWeaponItem {
    private static final Tier TIER = new WeaponTier(2000, 4.0F, 20, WeaponTier.WOODEN_INCORRECT, PGCItems.VAJRADA_AMETHYST_SLIVER);

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

    public EngulfingLightningItem(Properties properties) {
        super(TIER, properties.attributes(SwordItem.createAttributes(TIER, ATTACK_DAMAGE, ATTACK_SPEED)).fireResistant(),
                WeaponModifier.of(Attributes.ATTACK_DAMAGE, POWER_BASE, POWER_STEP, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    }

    @Override
    public List<WeaponModifier> conditionalPassives(Player player, ItemStack stack, int slot, int refinement) {
        if (isHeld(player, slot, stack)) return List.of();
        var saturation = player.getFoodData().getSaturationLevel();
        var core = core(saturation, refinement, offhandScale(player, refinement));
        return List.of(
                WeaponModifier.conditional(Attributes.ATTACK_DAMAGE, AttributeModifier.Operation.ADD_VALUE, (owner, worn, value) -> core),
                WeaponModifier.conditional(Attributes.ATTACK_DAMAGE, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL,
                        (owner, worn, value) -> owner.getFoodData().getFoodLevel() < LOW_FOOD_LEVEL
                                ? -(LOW_FOOD_PENALTY_BASE - LOW_FOOD_PENALTY_STEP * (value - 1)) : 0.0D));
    }

    @Override
    protected List<WeaponDescription> description(ItemStack stack) {
        var state = WeaponState.of(stack);
        var refinement = state.refinements();
        return List.of(
                WeaponDescription.of(RIGHT_CLICK, LIGHTNING_TEXT,
                        WishReports.percent(LIGHTNING_PER_REFINEMENT * refinement, ChatFormatting.AQUA),
                        WishReports.number(NO_KILL_COOLDOWN / 20, ChatFormatting.AQUA)),
                WeaponDescription.note(SATURATION_TEXT,
                        WishReports.number(saturationSeconds(refinement), ChatFormatting.AQUA)),
                WeaponDescription.of(CORE_ACTION, BONUS_TEXT,
                        WishReports.percent(CORE_BASE + CORE_STEP * (refinement - 1), ChatFormatting.AQUA)),
                WeaponDescription.of(PASSIVE_ACTION, ENHANCED_TEXT,
                        WishReports.percent(BONUS_BASE + BONUS_STEP * (refinement - 1), ChatFormatting.AQUA)),
                WeaponDescription.note(MISTSPLITTER_TEXT,
                        WishReports.percent(MISTSPLITTER_BASE + MISTSPLITTER_STEP * (refinement - 1), ChatFormatting.AQUA)),
                WeaponDescription.note(POWER_TEXT,
                        WishReports.percent(POWER_BASE + POWER_STEP * (refinement - 1), ChatFormatting.AQUA)),
                WeaponDescription.of(PENALTY_ACTION, PENALTY_TEXT,
                        WishReports.percent(LOW_FOOD_PENALTY_BASE - LOW_FOOD_PENALTY_STEP * (refinement - 1), ChatFormatting.AQUA)));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        if (level.isClientSide() || player.getCooldowns().isOnCooldown(stack.getItem())) return super.use(level, player, hand);
        if (player.isShiftKeyDown()) return super.use(level, player, hand);
        if (player.getFoodData().getFoodLevel() < MIN_FOOD) return super.use(level, player, hand);
        var refinement = WeaponState.of(stack).refinements();
        var damage = (float) (player.getAttributeValue(Attributes.ATTACK_DAMAGE) * LIGHTNING_PER_REFINEMENT * refinement);
        var defeated = 0;
        if (level instanceof ServerLevel server) {
            for (var candidate : server.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(RADIUS))) {
                if (candidate == player || !candidate.isAlive()) continue;
                if (!(candidate instanceof Mob mob) || mob.getTarget() != player) continue;
                WeaponDamage.extraHit(candidate, WeaponDamage.lightning(server, Element.ELECTRO, candidate, player, ElementDamageOptions.DETACHED), damage);
                if (!candidate.isAlive()) defeated++;
                strike(server, candidate);
                player.getFoodData().setFoodLevel(Math.max(0, player.getFoodData().getFoodLevel() - FOOD_COST));
            }
            server.sendParticles(ParticleTypes.ELECTRIC_SPARK, player.getX(), player.getY() + 1.0D, player.getZ(), 12, 0.4D, 0.4D, 0.4D, 0.05D);
        }
        player.getCooldowns().addCooldown(stack.getItem(), defeated > 0 ? KILL_COOLDOWN : NO_KILL_COOLDOWN);
        if (defeated > 0) {
            player.getFoodData().setSaturation(player.getFoodData().getSaturationLevel() + Math.min(SATURATION_CAP, defeated));
            player.getFoodData().setFoodLevel(SATURATION_CAP);
            level.playSound(null, player.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.0F);
        }
        return super.use(level, player, hand);
    }

    private static void strike(ServerLevel level, LivingEntity target) {
        var bolt = EntityType.LIGHTNING_BOLT.create(level);
        if (bolt == null) return;
        bolt.moveTo(Vec3.atBottomCenterOf(target.blockPosition()));
        bolt.setVisualOnly(true);
        level.addFreshEntity(bolt);
    }

    private static double offhandScale(Player player, int refinement) {
        var offhand = player.getOffhandItem();
        if (!offhand.is(PGCItems.MISTSPLITTER_REFORGED.get())) return 1.0D;
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
