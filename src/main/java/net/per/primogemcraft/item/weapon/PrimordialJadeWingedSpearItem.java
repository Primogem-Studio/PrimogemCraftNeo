package net.per.primogemcraft.item.weapon;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MaceItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
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
    private static final Tier TIER = new WeaponTier(2048, 4.0F, 20, WeaponTier.WOODEN_INCORRECT, Ingredient.of(Items.BREEZE_ROD));

    private static final ResourceLocation REACH_ID = ResourceLocation.fromNamespaceAndPath(MOD_ID, "weapon/reach");
    private static final ResourceLocation LIGHT_STEP_ID = ResourceLocation.fromNamespaceAndPath(MOD_ID, "weapon/light_step");
    private static final ResourceLocation LIGHT_STEP_GRAVITY_ID = ResourceLocation.fromNamespaceAndPath(MOD_ID, "weapon/light_step_gravity");

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
    private static final double SMASH_KNOCKBACK_RADIUS_SQUARED = MaceItem.SMASH_ATTACK_KNOCKBACK_RADIUS * MaceItem.SMASH_ATTACK_KNOCKBACK_RADIUS;
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

    public PrimordialJadeWingedSpearItem(Properties properties) {
        super(TIER, properties.attributes(attributes()).stacksTo(1).fireResistant());
    }

    private static ItemAttributeModifiers attributes() {
        return ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(MaceItem.BASE_ATTACK_DAMAGE_ID, ATTACK_DAMAGE, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(MaceItem.BASE_ATTACK_SPEED_ID, ATTACK_SPEED, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ENTITY_INTERACTION_RANGE, new AttributeModifier(REACH_ID, REACH_BONUS, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .build();
    }

    @Override
    public List<WeaponModifier> conditionalPassives(Player player, ItemStack stack, int slot, int refinement) {
        if (isHeld(player, slot, stack)) return List.of();
        return List.of(
                WeaponModifier.conditional(Attributes.ATTACK_DAMAGE, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL,
                        (owner, worn, value) -> POWER_BASE + POWER_STEP * (value - 1)),
                WeaponModifier.conditional(Attributes.ENTITY_INTERACTION_RANGE, AttributeModifier.Operation.ADD_MULTIPLIED_BASE,
                        (owner, worn, value) -> (POWER_BASE + POWER_STEP * (value - 1)) * REACH_RATIO));
    }

    @Override
    protected List<WeaponDescription> description(ItemStack stack) {
        var refinement = WeaponState.of(stack).refinements();
        return List.of(
                WeaponDescription.of(BASIC_SKILL, MOVESET_TEXT),
                WeaponDescription.of(RIGHT_CLICK, THRUST_TEXT,
                        WishReports.percent(THRUST_BASE + THRUST_STEP * (refinement - 1), ChatFormatting.AQUA),
                        WishReports.number(chargeSeconds(refinement), ChatFormatting.AQUA),
                        WishReports.number(maxCharges(refinement), ChatFormatting.AQUA)),
                WeaponDescription.note(STORAGE_TEXT),
                WeaponDescription.note(CHARGE_TEXT, WishReports.number(WeaponCharge.of(stack), ChatFormatting.AQUA)),
                WeaponDescription.of(SNEAK_USE, FLIGHT_TEXT,
                        WishReports.number(flightLevel(refinement), ChatFormatting.AQUA),
                        WishReports.number(flightTicks(refinement) / 20, ChatFormatting.AQUA)),
                WeaponDescription.of(PASSIVE_ACTION, POWER_TEXT,
                        WishReports.percent(POWER_BASE + POWER_STEP * (refinement - 1), ChatFormatting.AQUA)),
                WeaponDescription.note(REACH_TEXT,
                        WishReports.percent((POWER_BASE + POWER_STEP * (refinement - 1)) * REACH_RATIO, ChatFormatting.AQUA)));
    }

    @Override
    public float getAttackDamageBonus(Entity target, float damage, DamageSource source) {
        if (!(source.getDirectEntity() instanceof LivingEntity attacker) || !MaceItem.canSmashAttack(attacker)) return 0.0F;
        var bonus = smashBonus(attacker.fallDistance);
        return attacker.level() instanceof ServerLevel level
                ? bonus + EnchantmentHelper.modifyFallBasedDamage(level, attacker.getWeaponItem(), target, source, 0.0F) * attacker.fallDistance
                : bonus;
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        var result = super.hurtEnemy(stack, target, attacker);
        if (attacker instanceof ServerPlayer player && MaceItem.canSmashAttack(player)) smash(player, target);
        return result;
    }

    @Override
    public void postHurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        super.postHurtEnemy(stack, target, attacker);
        if (MaceItem.canSmashAttack(attacker)) attacker.resetFallDistance();
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        if (level.isClientSide() || !(entity instanceof Player player)) return;
        if (!selected) return;
        var refinement = WeaponState.of(stack).refinements();
        var charges = WeaponCharge.of(stack);
        if (charges >= maxCharges(refinement)) return;
        if (!PGCTimer.isDone(player, CHARGE_TIMER)) return;
        WeaponCharge.set(stack, charges + 1);
        if (charges < maxCharges(refinement) - CHARGE_RESTORE_THRESHOLD) PGCTimer.set(player, CHARGE_TIMER, chargeTicks(refinement));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        if (level.isClientSide()) return super.use(level, player, hand);
        var refinement = WeaponState.of(stack).refinements();
        if (player.isShiftKeyDown()) {
            if (player.hasEffect(MobEffects.JUMP) || !PGCTimer.isDone(player, FLIGHT_TIMER)) return super.use(level, player, hand);
            PGCTimer.set(player, FLIGHT_TIMER, FLIGHT_COOLDOWN);
            player.addEffect(new MobEffectInstance(MobEffects.JUMP, flightTicks(refinement), flightLevel(refinement) - 1, false, false));
            level.playSound(null, player.blockPosition(), SoundEvents.WARDEN_EMERGE, SoundSource.PLAYERS, 1.0F, 3.0F);
            return super.use(level, player, hand);
        }
        var charges = WeaponCharge.of(stack);
        if (charges <= 0) return super.use(level, player, hand);
        if (level instanceof ServerLevel server && player instanceof ServerPlayer serverPlayer) thrust(server, serverPlayer, refinement);
        WeaponCharge.set(stack, charges - 1);
        if (PGCTimer.isDone(player, CHARGE_TIMER)) PGCTimer.set(player, CHARGE_TIMER, chargeTicks(refinement));
        return super.use(level, player, hand);
    }

    private static void thrust(ServerLevel level, ServerPlayer player, int refinement) {
        var grounded = player.onGround();
        var speed = (DASH_BASE + DASH_STEP * (refinement - 1)) * (grounded ? 2.0D : 1.0D);
        var direction = Vec3.directionFromRotation(0.0F, player.getYRot());
        player.setDeltaMovement(direction.x * speed, grounded ? 0.0D : AIR_SPEED, direction.z * speed);
        player.hurtMarked = true;
        ThrustDash.start(player, (float) (player.getAttributeValue(Attributes.ATTACK_DAMAGE) * (THRUST_BASE + THRUST_STEP * (refinement - 1))));
        TemporaryAttributes.apply(player, LIGHT_STEP_ID, Attributes.STEP_HEIGHT, LIGHT_STEP_BONUS,
                AttributeModifier.Operation.ADD_VALUE, lightStepTicks(grounded));
        TemporaryAttributes.apply(player, LIGHT_STEP_GRAVITY_ID, Attributes.GRAVITY, LIGHT_STEP_GRAVITY,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, lightStepTicks(grounded));
        var flourish = player.getItemBySlot(EquipmentSlot.HEAD).is(PGCItems.COLORFUL_SUNGLASSES.get());
        level.playSound(null, BlockPos.containing(player.position()),
                flourish ? PGCSounds.THRUST_FLOURISH.get() : SoundEvents.TRIDENT_THROW.value(), SoundSource.PLAYERS, flourish ? 5.0F : 1.0F, 1.0F);
    }

    private static void smash(ServerPlayer player, LivingEntity target) {
        var position = player.position();
        if (player.isIgnoringFallDamageFromCurrentImpulse() && player.currentImpulseImpactPos != null) {
            if (player.currentImpulseImpactPos.y > position.y) player.currentImpulseImpactPos = position;
        } else {
            player.currentImpulseImpactPos = position;
        }
        player.setIgnoreFallDamageFromCurrentImpulse(true);
        player.setDeltaMovement(player.getDeltaMovement().with(Direction.Axis.Y, 0.01D));
        player.connection.send(new ClientboundSetEntityMotionPacket(player));
        var level = player.serverLevel();
        if (target.onGround()) {
            player.setSpawnExtraParticlesOnFall(true);
            var sound = player.fallDistance > SMASH_HEAVY_THRESHOLD ? SoundEvents.MACE_SMASH_GROUND_HEAVY : SoundEvents.MACE_SMASH_GROUND;
            level.playSound(null, position.x, position.y, position.z, sound, player.getSoundSource(), 1.0F, 1.0F);
        } else {
            level.playSound(null, position.x, position.y, position.z, SoundEvents.MACE_SMASH_AIR, player.getSoundSource(), 1.0F, 1.0F);
        }
        knockback(level, player, target);
    }

    private static float smashBonus(float fallDistance) {
        if (fallDistance <= SMASH_LIGHT_LIMIT) return (float) (SMASH_FALL_FACTOR * fallDistance);
        if (fallDistance <= SMASH_HEAVY_LIMIT) return (float) (SMASH_LIGHT_LIMIT * SMASH_FALL_FACTOR + 2.0D * (fallDistance - SMASH_LIGHT_LIMIT));
        return SMASH_HEAVY_BASE + fallDistance - SMASH_HEAVY_LIMIT;
    }

    private static void knockback(ServerLevel level, Player player, LivingEntity target) {
        level.levelEvent(SMASH_EFFECT, target.getOnPos(), SMASH_EFFECT_DATA);
        level.getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(MaceItem.SMASH_ATTACK_KNOCKBACK_RADIUS), knockbackTarget(player, target))
                .forEach(victim -> {
                    var offset = victim.position().subtract(target.position());
                    var power = knockbackPower(player, victim, offset);
                    if (power <= 0.0D) return;
                    var push = offset.normalize().scale(power);
                    victim.push(push.x, SMASH_KNOCKBACK_LIFT, push.z);
                    if (victim instanceof ServerPlayer pushed) pushed.connection.send(new ClientboundSetEntityMotionPacket(pushed));
                });
    }

    private static Predicate<LivingEntity> knockbackTarget(Player player, LivingEntity target) {
        return victim -> !victim.isSpectator()
                && victim != player
                && victim != target
                && !player.isAlliedTo(victim)
                && !ownedByPlayer(player, victim)
                && !isMarker(victim)
                && target.distanceToSqr(victim) <= SMASH_KNOCKBACK_RADIUS_SQUARED;
    }

    private static boolean ownedByPlayer(Player player, LivingEntity entity) {
        return entity instanceof TamableAnimal animal && animal.isTame() && player.getUUID().equals(animal.getOwnerUUID());
    }

    private static boolean isMarker(LivingEntity entity) {
        return entity instanceof ArmorStand stand && stand.isMarker();
    }

    private static double knockbackPower(Player player, LivingEntity victim, Vec3 offset) {
        return (MaceItem.SMASH_ATTACK_KNOCKBACK_RADIUS - offset.length())
                * SMASH_KNOCKBACK_POWER
                * (player.fallDistance > SMASH_HEAVY_THRESHOLD ? 2.0D : 1.0D)
                * (1.0D - victim.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
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
