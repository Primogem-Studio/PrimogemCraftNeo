package net.per.primogemcraft.item.weapon;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
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

public class MistsplitterReforgedItem extends WishWeaponItem {
    private static final Tier TIER = new WeaponTier(3000, 4.0F, 20, WeaponTier.WOODEN_INCORRECT, PGCItems.ETERNAL_METAL);

    private static final double RESTORE_PER_REFINEMENT = 0.5D;
    private static final double THUNDER_PER_REFINEMENT = 0.3D;
    private static final int FOOD_COST = 9;
    private static final int MIN_FOOD = 9;
    private static final double RADIUS = 8.0D;
    private static final int THUNDER_COOLDOWN = 40;
    private static final int SATURATION_TICKS = 5;
    private static final float SACRIFICE_RATIO = 0.2F;
    private static final float ATTACK_DAMAGE = 8.0F;
    private static final float ATTACK_SPEED = -2.4F;
    private static final String NORMAL_ATTACK = "normal_attack";
    private static final String RIGHT_CLICK = "right_click";
    private static final String SNEAK_USE = "sneak_use";
    private static final String DURABILITY_TEXT = "durability";
    private static final String THUNDER_TEXT = "thunder";
    private static final String SATURATION_TEXT = "saturation";

    public MistsplitterReforgedItem(Properties properties) {
        super(TIER, properties.attributes(SwordItem.createAttributes(TIER, ATTACK_DAMAGE, ATTACK_SPEED)).fireResistant());
    }

    @Override
    protected List<WeaponDescription> description(ItemStack stack) {
        var state = WeaponState.of(stack);
        var refinement = state.refinements();
        return List.of(
                WeaponDescription.of(NORMAL_ATTACK, DURABILITY_TEXT,
                        WishReports.number(RESTORE_PER_REFINEMENT * refinement, ChatFormatting.AQUA)),
                WeaponDescription.of(RIGHT_CLICK, THUNDER_TEXT,
                        WishReports.percent(thunderRatio(refinement), ChatFormatting.AQUA),
                        WishReports.number(THUNDER_COOLDOWN / 20, ChatFormatting.AQUA)),
                WeaponDescription.of(SNEAK_USE, SATURATION_TEXT,
                        WishReports.number(refinement, ChatFormatting.AQUA)));
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker.level() instanceof ServerLevel)
            WeaponAttributes.restoreDurability(stack, restorePoints(WeaponState.of(stack).refinements()));
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        if (level.isClientSide() || player.getCooldowns().isOnCooldown(stack.getItem())) return super.use(level, player, hand);
        var refinement = WeaponState.of(stack).refinements();
        if (player.isShiftKeyDown()) {
            player.hurt(player.damageSources().genericKill(), player.getMaxHealth() * SACRIFICE_RATIO);
            player.addEffect(new MobEffectInstance(MobEffects.SATURATION, SATURATION_TICKS, refinement - 1, false, false));
            player.getCooldowns().addCooldown(stack.getItem(), THUNDER_COOLDOWN);
            return super.use(level, player, hand);
        }
        if (player.getFoodData().getFoodLevel() < MIN_FOOD) return super.use(level, player, hand);
        player.getCooldowns().addCooldown(stack.getItem(), THUNDER_COOLDOWN);
        var damage = (float) (player.getAttributeValue(Attributes.ATTACK_DAMAGE) * thunderRatio(refinement));
        if (!(level instanceof ServerLevel server)) return super.use(level, player, hand);
        for (var candidate : server.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(RADIUS)))
            if (candidate != player && candidate.isAlive() && candidate instanceof Mob mob && mob.getTarget() == player) {
                WeaponDamage.extraHit(candidate,
                        WeaponDamage.lightning(server, Element.ELECTRO, candidate, player, ElementDamageOptions.DETACHED), damage);
                strike(server, candidate.blockPosition());
                player.getFoodData().setFoodLevel(Math.max(0, player.getFoodData().getFoodLevel() - FOOD_COST));
            }
        server.playSound(null, player.blockPosition(), SoundEvents.TRIDENT_THUNDER.value(), SoundSource.PLAYERS, 2.0F, 0.3F);
        return super.use(level, player, hand);
    }

    private static void strike(ServerLevel level, BlockPos pos) {
        var bolt = EntityType.LIGHTNING_BOLT.create(level);
        if (bolt == null) return;
        bolt.moveTo(Vec3.atBottomCenterOf(pos));
        bolt.setVisualOnly(true);
        level.addFreshEntity(bolt);
    }

    private static int restorePoints(int refinement) {
        return (int) Math.ceil(RESTORE_PER_REFINEMENT * refinement);
    }

    private static double thunderRatio(int refinement) {
        return THUNDER_PER_REFINEMENT * refinement;
    }
}
