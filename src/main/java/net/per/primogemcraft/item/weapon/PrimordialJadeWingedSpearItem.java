package net.per.primogemcraft.item.weapon;

import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.util.Identifier;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.*;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.ToolMaterial;
import net.minecraft.recipe.Ingredient;
import net.minecraft.world.World;
import net.minecraft.util.math.Vec3d;
import net.per.primogemcraft.component.WeaponCharge;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.registry.PGCSounds;
import net.per.primogemcraft.system.weapon.*;
import net.per.primogemcraft.system.wish.WishReports;
import net.per.primogemcraft.util.PGCTimer;
import net.per.primogemcraft.util.TemporaryAttributes;

import java.util.List;
import java.util.function.Predicate;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public class PrimordialJadeWingedSpearItem extends WishWeaponItem {
    private static final ToolMaterial TIER = new WeaponTier(2048, 4.0F, 20, WeaponTier.WOODEN_INCORRECT, Ingredient.ofItems(Items.BLAZE_ROD));

    private static final Identifier REACH_ID = Identifier.of(MOD_ID, "weapon/reach");
    private static final Identifier LIGHT_STEP_ID = Identifier.of(MOD_ID, "weapon/light_step");
    private static final Identifier LIGHT_STEP_GRAVITY_ID = Identifier.of(MOD_ID, "weapon/light_step_gravity");

    private static final String CHARGE_TIMER = "jade_winged_spear_charge";
    private static final String FLIGHT_TIMER = "jade_winged_spear_flight";
    private static final int BASE_CHARGES = 2;
    private static final int MAX_CHARGES = 3;
    private static final int CHARGE_TICKS_BASE = 160;
    private static final int CHARGE_TICKS_STEP = 20;
    private static final int CHARGE_RESTORE_THRESHOLD = 1;
    private static final double DASH_BASE = 2.0D;
    private static final double DASH_STEP = 0.2D;
    private static final double AIR_SPEED = 0.2D;
    private static final int LIGHT_STEP_TICKS_GROUND = 10;
    private static final int LIGHT_STEP_TICKS_AIR = 6;
    private static final double LIGHT_STEP_BONUS = 3.0D;
    private static final double LIGHT_STEP_GRAVITY = -0.9D;
    private static final double THRUST_BASE = 0.5D;
    private static final double THRUST_STEP = 0.125D;
    private static final double POWER_BASE = 0.1D;
    private static final double POWER_STEP = 0.025D;
    private static final double REACH_RATIO = 1.5D;
    private static final double REACH_BONUS = 3.0D;
    private static final int FLIGHT_TICKS_BASE = 200;
    private static final int FLIGHT_TICKS_STEP = 50;
    private static final int FLIGHT_LEVEL_BASE = 5;
    private static final int FLIGHT_LEVEL_STEP = 1;
    private static final int FLIGHT_COOLDOWN = 600;
    private static final float ATTACK_DAMAGE = 9.0F;
    private static final float ATTACK_SPEED = -2.0F;
    private static final float SMASH_LIGHT_LIMIT = 3.0F;
    private static final float SMASH_HEAVY_LIMIT = 8.0F;
    private static final float SMASH_HEAVY_THRESHOLD = 5.0F;
    private static final double SMASH_FALL_FACTOR = 4.0D;
    private static final float SMASH_HEAVY_BASE = 22.0F;
    private static final double SMASH_KNOCKBACK_POWER = 0.7D;
    private static final double SMASH_KNOCKBACK_LIFT = 0.7D;
    private static final double SMASH_KNOCKBACK_RADIUS = 3.5D;
    private static final double SMASH_KNOCKBACK_RADIUS_SQUARED = SMASH_KNOCKBACK_RADIUS * SMASH_KNOCKBACK_RADIUS;
    private static final int SMASH_EFFECT = 2013;
    private static final int SMASH_EFFECT_DATA = 750;
    private static final String BASIC_SKILL = "basic_skill";
    private static final String RIGHT_CLICK = "right_click";
    private static final String SNEAK_USE = "sneak_use";
    private static final String PASSIVE_ACTION = "passive";
    private static final String MOVESET_TEXT = "moveset";
    private static final String THRUST_TEXT = "thrust";
    private static final String STORAGE_TEXT = "storage";
    private static final String CHARGE_TEXT = "charge";
    private static final String FLIGHT_TEXT = "flight";
    private static final String POWER_TEXT = "power";
    private static final String REACH_TEXT = "reach";

    public PrimordialJadeWingedSpearItem(Settings properties) {
        super(TIER, ATTACK_DAMAGE, ATTACK_SPEED, properties.maxCount(1).fireproof());
    }

    @Override
    public com.google.common.collect.Multimap<net.minecraft.entity.attribute.EntityAttribute, EntityAttributeModifier> getAttributeModifiers(EquipmentSlot slot) {
        var modifiers = com.google.common.collect.ImmutableMultimap.<net.minecraft.entity.attribute.EntityAttribute, EntityAttributeModifier>builder();
        if (slot == EquipmentSlot.MAINHAND) {
            modifiers.put(EntityAttributes.GENERIC_ATTACK_DAMAGE, new EntityAttributeModifier(net.minecraft.item.Item.ATTACK_DAMAGE_MODIFIER_ID, "Weapon modifier", ATTACK_DAMAGE, EntityAttributeModifier.Operation.ADDITION));
            modifiers.put(EntityAttributes.GENERIC_ATTACK_SPEED, new EntityAttributeModifier(net.minecraft.item.Item.ATTACK_SPEED_MODIFIER_ID, "Weapon modifier", ATTACK_SPEED, EntityAttributeModifier.Operation.ADDITION));
            modifiers.put(net.per.primogemcraft.compat.attribute.AttributesCompat.ENTITY_REACH.get(),
                    net.per.primogemcraft.compat.attribute.AttributesCompat.modifier(REACH_ID, REACH_BONUS, EntityAttributeModifier.Operation.ADDITION));
        }
        return modifiers.build();
    }

    @Override
    public List<WeaponModifier> conditionalPassives(PlayerEntity player, ItemStack stack, int slot, int refinement) {
        if (isHeld(player, slot, stack)) return List.of();
        return List.of(
                WeaponModifier.conditional(EntityAttributes.GENERIC_ATTACK_DAMAGE, EntityAttributeModifier.Operation.MULTIPLY_TOTAL,
                        (owner, worn, value) -> POWER_BASE + POWER_STEP * (value - 1)),
                WeaponModifier.conditional(net.per.primogemcraft.compat.attribute.AttributesCompat.ENTITY_REACH.get(), EntityAttributeModifier.Operation.MULTIPLY_BASE,
                        (owner, worn, value) -> (POWER_BASE + POWER_STEP * (value - 1)) * REACH_RATIO));
    }

    @Override
    protected List<WeaponDescription> description(ItemStack stack) {
        var refinement = WeaponState.of(stack).refinements();
        return List.of(
                WeaponDescription.of(BASIC_SKILL, MOVESET_TEXT),
                WeaponDescription.of(RIGHT_CLICK, THRUST_TEXT,
                        WishReports.percent(THRUST_BASE + THRUST_STEP * (refinement - 1), Formatting.AQUA),
                        WishReports.number(chargeSeconds(refinement), Formatting.AQUA),
                        WishReports.number(maxCharges(refinement), Formatting.AQUA)),
                WeaponDescription.note(STORAGE_TEXT),
                WeaponDescription.note(CHARGE_TEXT, WishReports.number(WeaponCharge.of(stack), Formatting.AQUA)),
                WeaponDescription.of(SNEAK_USE, FLIGHT_TEXT,
                        WishReports.number(flightLevel(refinement), Formatting.AQUA),
                        WishReports.number(flightTicks(refinement) / 20, Formatting.AQUA)),
                WeaponDescription.of(PASSIVE_ACTION, POWER_TEXT,
                        WishReports.percent(POWER_BASE + POWER_STEP * (refinement - 1), Formatting.AQUA)),
                WeaponDescription.note(REACH_TEXT,
                        WishReports.percent((POWER_BASE + POWER_STEP * (refinement - 1)) * REACH_RATIO, Formatting.AQUA)));
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        var result = super.postHit(stack, target, attacker);
        if (canSmash(attacker)) {
            var bonus = smashBonus(attacker.fallDistance);
            if (bonus > 0.0F) {
                var source = attacker instanceof PlayerEntity player
                        ? attacker.getDamageSources().playerAttack(player)
                        : attacker.getDamageSources().mobAttack(attacker);
                target.damage(source, bonus);
            }
            if (attacker instanceof ServerPlayerEntity player) smash(player, target);
            attacker.fallDistance = 0;
        }
        return result;
    }

    /** 1.21 的 MaceItem.canSmashAttack：下落中且未着地时可发动重击。 */
    private static boolean canSmash(LivingEntity attacker) {
        return attacker.fallDistance > 0.0F && !attacker.isOnGround();
    }

    @Override
    public void inventoryTick(ItemStack stack, World level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        if (level.isClient() || !(entity instanceof PlayerEntity player)) return;
        if (!selected) return;
        var refinement = WeaponState.of(stack).refinements();
        var charges = WeaponCharge.of(stack);
        if (charges >= maxCharges(refinement)) return;
        if (!PGCTimer.isDone(player, CHARGE_TIMER)) return;
        WeaponCharge.set(stack, charges + 1);
        if (charges < maxCharges(refinement) - CHARGE_RESTORE_THRESHOLD) PGCTimer.set(player, CHARGE_TIMER, chargeTicks(refinement));
    }

    @Override
    public TypedActionResult<ItemStack> use(World level, PlayerEntity player, Hand hand) {
        var stack = player.getStackInHand(hand);
        if (level.isClient()) return super.use(level, player, hand);
        var refinement = WeaponState.of(stack).refinements();
        if (player.isSneaking()) {
            if (player.hasStatusEffect(StatusEffects.JUMP_BOOST) || !PGCTimer.isDone(player, FLIGHT_TIMER)) return super.use(level, player, hand);
            PGCTimer.set(player, FLIGHT_TIMER, FLIGHT_COOLDOWN);
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST, flightTicks(refinement), flightLevel(refinement) - 1, false, false));
            level.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_WARDEN_EMERGE, SoundCategory.PLAYERS, 1.0F, 3.0F);
            return super.use(level, player, hand);
        }
        var charges = WeaponCharge.of(stack);
        if (charges <= 0) return super.use(level, player, hand);
        if (level instanceof ServerWorld server && player instanceof ServerPlayerEntity serverPlayer) thrust(server, serverPlayer, refinement);
        WeaponCharge.set(stack, charges - 1);
        if (PGCTimer.isDone(player, CHARGE_TIMER)) PGCTimer.set(player, CHARGE_TIMER, chargeTicks(refinement));
        return super.use(level, player, hand);
    }

    private static void thrust(ServerWorld level, ServerPlayerEntity player, int refinement) {
        var grounded = player.isOnGround();
        var speed = (DASH_BASE + DASH_STEP * (refinement - 1)) * (grounded ? 2.0D : 1.0D);
        var direction = Vec3d.fromPolar(0.0F, player.getYaw());
        player.setVelocity(direction.x * speed, grounded ? 0.0D : AIR_SPEED, direction.z * speed);
        player.hurtTime = 10;
        ThrustDash.start(player, (float) (player.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE) * (THRUST_BASE + THRUST_STEP * (refinement - 1))));
        TemporaryAttributes.apply(player, LIGHT_STEP_ID, net.per.primogemcraft.compat.attribute.AttributesCompat.STEP_HEIGHT_ADDITION.get(), LIGHT_STEP_BONUS,
                EntityAttributeModifier.Operation.ADDITION, lightStepTicks(grounded));
        TemporaryAttributes.apply(player, LIGHT_STEP_GRAVITY_ID, net.minecraft.entity.attribute.EntityAttributes.GENERIC_MOVEMENT_SPEED, LIGHT_STEP_GRAVITY,
                EntityAttributeModifier.Operation.MULTIPLY_TOTAL, lightStepTicks(grounded));
        var flourish = player.getEquippedStack(EquipmentSlot.HEAD).isOf(PGCItems.COLORFUL_SUNGLASSES.get());
        level.playSound(null, BlockPos.ofFloored(player.getPos()),
                flourish ? PGCSounds.THRUST_FLOURISH.get() : SoundEvents.ITEM_TRIDENT_THROW, SoundCategory.PLAYERS, flourish ? 5.0F : 1.0F, 1.0F);
    }

    private static void smash(ServerPlayerEntity player, LivingEntity target) {
        var position = player.getPos();
        // 1.21 用 currentImpulseImpactPos 免疫本次冲量的摔落伤害；1.20.1 直接清零摔落距离等价实现
        player.fallDistance = 0;
        player.setVelocity(player.getVelocity().withAxis(Direction.Axis.Y, 0.01D));
        player.networkHandler.sendPacket(new net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket(player));
        var level = player.getServerWorld();
        // 1.20.1 没有重锤专属音效，用铁砧落地/爆炸声替代重击反馈
        var sound = player.fallDistance > SMASH_HEAVY_THRESHOLD ? SoundEvents.BLOCK_ANVIL_LAND : SoundEvents.ENTITY_GENERIC_EXPLODE;
        level.playSound(null, position.getX(), position.getY(), position.getZ(), sound, net.minecraft.sound.SoundCategory.PLAYERS, 1.0F, 1.0F);
        knockback(level, player, target);
    }

    private static float smashBonus(float fallDistance) {
        if (fallDistance <= SMASH_LIGHT_LIMIT) return (float) (SMASH_FALL_FACTOR * fallDistance);
        if (fallDistance <= SMASH_HEAVY_LIMIT) return (float) (SMASH_LIGHT_LIMIT * SMASH_FALL_FACTOR + 2.0D * (fallDistance - SMASH_LIGHT_LIMIT));
        return SMASH_HEAVY_BASE + fallDistance - SMASH_HEAVY_LIMIT;
    }

    private static void knockback(net.minecraft.server.world.ServerWorld level, net.minecraft.entity.player.PlayerEntity player, LivingEntity target) {
        level.syncWorldEvent(SMASH_EFFECT, target.getSteppingPos(), SMASH_EFFECT_DATA);
        level.getEntitiesByClass(LivingEntity.class, target.getBoundingBox().expand(SMASH_KNOCKBACK_RADIUS), knockbackTarget(player, target))
                .forEach(victim -> {
                    var offset = victim.getPos().subtract(target.getPos());
                    var power = knockbackPower(player, victim, offset);
                    if (power <= 0.0D) return;
                    var push = offset.normalize().multiply(power);
                    victim.addVelocity(push.x, SMASH_KNOCKBACK_LIFT, push.z);
                    if (victim instanceof net.minecraft.server.network.ServerPlayerEntity pushed) pushed.networkHandler.sendPacket(new net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket(pushed));
                });
    }

    private static Predicate<LivingEntity> knockbackTarget(net.minecraft.entity.player.PlayerEntity player, LivingEntity target) {
        return victim -> !victim.isSpectator()
                && victim != player
                && victim != target
                && !player.isTeammate(victim)
                && !ownedByPlayer(player, victim)
                && !isMarker(victim)
                && target.squaredDistanceTo(victim) <= SMASH_KNOCKBACK_RADIUS_SQUARED;
    }

    private static boolean ownedByPlayer(net.minecraft.entity.player.PlayerEntity player, LivingEntity entity) {
        return entity instanceof net.minecraft.entity.passive.TameableEntity animal && animal.isTamed() && player.getUuid().equals(animal.getOwnerUuid());
    }

    private static boolean isMarker(LivingEntity entity) {
        return entity instanceof net.minecraft.entity.decoration.ArmorStandEntity stand && stand.isMarker();
    }

    private static double knockbackPower(net.minecraft.entity.player.PlayerEntity player, LivingEntity victim, net.minecraft.util.math.Vec3d offset) {
        return (SMASH_KNOCKBACK_RADIUS - offset.length())
                * SMASH_KNOCKBACK_POWER
                * (player.fallDistance > SMASH_HEAVY_THRESHOLD ? 2.0D : 1.0D)
                * (1.0D - victim.getAttributeValue(net.minecraft.entity.attribute.EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE));
    }

    private static int maxCharges(int refinement) {
        return Math.min(MAX_CHARGES, BASE_CHARGES + (refinement > 4 ? refinement - 4 : 0));
    }

    private static int chargeTicks(int refinement) {
        return CHARGE_TICKS_BASE - CHARGE_TICKS_STEP * (refinement - 1);
    }

    private static int chargeSeconds(int refinement) {
        return chargeTicks(refinement) / 20;
    }

    private static int flightLevel(int refinement) {
        return FLIGHT_LEVEL_BASE + FLIGHT_LEVEL_STEP * (refinement - 1);
    }

    private static int flightTicks(int refinement) {
        return FLIGHT_TICKS_BASE + FLIGHT_TICKS_STEP * (refinement - 1);
    }

    private static int lightStepTicks(boolean grounded) {
        return grounded ? LIGHT_STEP_TICKS_GROUND : LIGHT_STEP_TICKS_AIR;
    }
}
