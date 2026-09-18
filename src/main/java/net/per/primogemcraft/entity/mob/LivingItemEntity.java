package net.per.primogemcraft.entity.mob;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.SweepAttackEvent;
import net.neoforged.neoforge.fluids.FluidType;
import net.per.primogemcraft.collab.genshincraft.GenshinCraftIntegration;
import net.per.primogemcraft.entity.misc.LivingItemDrop;

import javax.annotation.Nullable;
import java.util.*;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public class LivingItemEntity extends PathfinderMob {
    public static final EntityDataAccessor<ItemStack> DATA_ITEM = SynchedEntityData.defineId(LivingItemEntity.class, EntityDataSerializers.ITEM_STACK);
    public static final EntityDataAccessor<Integer> DATA_SWING = SynchedEntityData.defineId(LivingItemEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Integer> DATA_ROTATE = SynchedEntityData.defineId(LivingItemEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Optional<UUID>> DATA_OWNER = SynchedEntityData.defineId(LivingItemEntity.class, EntityDataSerializers.OPTIONAL_UUID);

    private static final String NBT_ITEM = "LivingItemStack";
    private static final String NBT_OWNER = "LivingItemOwner";
    private static final String NBT_TICKS = "LivingItemTicks";
    private static final String NBT_ATTACK_CD = "LivingItemAttackCd";
    private static final String NBT_USE_CD = "LivingItemUseCd";
    private static final String NBT_RANGE = "LivingItemRange";

    private static final TagKey<Item> TAG_RIGHT_CLICK = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MOD_ID, "living_item_right_click"));

    private static final double BASE_ATTACK_DAMAGE = 0.3;
    public static final int BOW_DRAW_TICKS = 16;
    private static final double MAX_CHASE_DISTANCE = 24.0;
    private static final long OWNER_ATTACK_MEMORY = 100;
    private static final long ATTACK_BLACKLIST_TIME = 100;
    private static final String MODIFIER_PREFIX = "living_item_";
    private static final double MOVE_SPEED_IDLE = 0.1;
    private static final double MOVE_SPEED_ATTACK = 0.6;
    private static final double AIR_FRICTION = 0.91;
    private static final double WATER_SPEED_FACTOR = 0.8;
    private static final double LAVA_SPEED_FACTOR = 0.5;
    private static final double MAX_FALL_SPEED = 0.6;
    private static final double GROUND_LIFT = 0.4;
    private static final double COMBAT_SPREAD_RADIUS = 1.8;
    private static final int CHASE_STUCK_TICKS = 40;
    private static final float RETARGET_CHANCE = 0.35F;

    private int remainingTicks;
    private int attackCooldown;
    private int attackInterval = 20;
    private int useCooldown;
    private int scanCooldown;
    private double attackRange = 3.0;
    private Vec3 hoverTarget;
    private int hoverTimer;
    private boolean reverted;
    private double orbitAngle = Double.NaN;
    private double lastChaseDist = Double.MAX_VALUE;
    private int chaseStuckTicks;
    private final Map<UUID, Long> ownerAttackMemory = new HashMap<>();
    private final Map<UUID, Long> attackBlacklist = new HashMap<>();

    public LivingItemEntity(EntityType<? extends LivingItemEntity> entityType, Level level) {
        super(entityType, level);
        xpReward = 0;
        setNoGravity(true);
        setPersistenceRequired();
        moveControl = new FlyingMoveControl(this, 10, true);
    }

    public void startLiving(Player owner, ItemStack stack, int ticks) {
        entityData.set(DATA_OWNER, Optional.of(owner.getUUID()));
        entityData.set(DATA_ITEM, stack.copy());
        remainingTicks = ticks < 0 ? -1 : Math.max(1, ticks);
        applyItemStats();
        attackCooldown = random.nextInt(Math.max(1, attackInterval));
        joinOwnerTeam(owner);
    }

    public boolean isInfinite() {
        return remainingTicks < 0;
    }

    public void setInfinite() {
        remainingTicks = -1;
    }

    public int getRemainingTicks() {
        return remainingTicks;
    }

    public void setRemainingTicks(int ticks) {
        remainingTicks = ticks < 0 ? -1 : Math.max(1, ticks);
    }

    public void applyItemStats() {
        var stack = getCarriedStack();
        var mods = stack.get(DataComponents.ATTRIBUTE_MODIFIERS);
        var attackSpeed = 4.0;
        var hasAttackSpeedModifier = false;
        if (mods != null) {
            for (var entry : mods.modifiers()) {
                var modifier = entry.modifier();
                if (modifier.operation() != AttributeModifier.Operation.ADD_VALUE) continue;
                if (entry.attribute().is(Attributes.ATTACK_SPEED)) {
                    attackSpeed += modifier.amount();
                    hasAttackSpeedModifier = true;
                }
            }
        }
        if (!hasAttackSpeedModifier) attackSpeed = 1.0;
        attackInterval = Math.max(10, (int) Math.round(20.0 / Math.max(0.1, attackSpeed)) + 4);

        var attack = getAttribute(Attributes.ATTACK_DAMAGE);
        if (attack != null) {
            clearItemModifiers(attack);
            attack.setBaseValue(BASE_ATTACK_DAMAGE);
            addItemModifiers(attack, mods);
        }
    }

    private void clearItemModifiers(AttributeInstance instance) {
        for (var modifier : instance.getModifiers()) {
            if (modifier.id().getNamespace().equals(MOD_ID) && modifier.id().getPath().startsWith(MODIFIER_PREFIX))
                instance.removeModifier(modifier.id());
        }
    }

    private void addItemModifiers(AttributeInstance instance, ItemAttributeModifiers mods) {
        if (mods == null) return;
        for (var entry : mods.modifiers()) {
            if (!entry.attribute().is(Attributes.ATTACK_DAMAGE)) continue;
            var source = entry.modifier();
            var id = ResourceLocation.fromNamespaceAndPath(MOD_ID, MODIFIER_PREFIX + source.id().getNamespace() + "_" + source.id().getPath());
            instance.addTransientModifier(new AttributeModifier(id, source.amount(), source.operation()));
        }
    }

    public void setAttackRange(double range) {
        attackRange = Math.max(1.0, range);
    }

    public double getAttackRange() {
        return attackRange;
    }

    public ItemStack getCarriedStack() {
        return entityData.get(DATA_ITEM);
    }

    public int getSwingTicks() {
        return entityData.get(DATA_SWING);
    }

    public int getRotateTicks() {
        return entityData.get(DATA_ROTATE);
    }

    public Player getOwner() {
        var uuid = entityData.get(DATA_OWNER).orElse(null);
        if (uuid == null || level().isClientSide) return null;
        if (level() instanceof ServerLevel serverLevel) return serverLevel.getServer().getPlayerList().getPlayer(uuid);
        return null;
    }

    public UUID getOwnerUuid() {
        return entityData.get(DATA_OWNER).orElse(null);
    }

    public void joinOwnerTeam(Player owner) {
        if (level().isClientSide) return;
        var team = owner.getTeam();
        if (team != null) level().getScoreboard().addPlayerToTeam(getStringUUID(), team);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new FlyingPathNavigation(this, level);
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        super.setTarget(target);
        if (target == null) {
            lastChaseDist = Double.MAX_VALUE;
            chaseStuckTicks = 0;
        } else if (Double.isNaN(orbitAngle)) {
            orbitAngle = random.nextDouble() * Math.PI * 2.0;
        }
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void doPush(Entity entity) {
    }

    @Override
    public void push(Entity entity) {
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (level().isClientSide || isRemoved() || isDeadOrDying()) return false;
        if (source.getEntity() == null && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return false;
        if (source.getEntity() != null) {
            var ownerUuid = getOwnerUuid();
            if (ownerUuid != null && ownerUuid.equals(source.getEntity().getUUID())) return false;
        }
        if (!super.hurt(source, amount)) return false;
        if (source.getEntity() instanceof LivingEntity attacker && attacker.isAlive() && isAlive()) {
            setLastHurtByMob(attacker);
            ownerAttackMemory.put(attacker.getUUID(), level().getGameTime());
            setTarget(attacker);
            var dx = getX() - attacker.getX();
            var dz = getZ() - attacker.getZ();
            knockback(0.4, dx, dz);
        }
        if (getHealth() <= 0.0F) {
            setHealth(0.0F);
            revertByDeath();
            return true;
        }
        return true;
    }

    @Override
    public boolean ignoreExplosion(Explosion explosion) {
        return true;
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public boolean checkSpawnObstruction(LevelReader level) {
        return level.isUnobstructed(this);
    }

    @Override
    public boolean canDrownInFluidType(FluidType type) {
        return false;
    }

    @Override
    public boolean isPushedByFluid(FluidType type) {
        return false;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
    }

    @Override
    public void setNoGravity(boolean ignored) {
        super.setNoGravity(true);
    }

    @Override
    public void travel(Vec3 travelVector) {
        if (isEffectiveAi() || isControlledByLocalInstance()) {
            move(MoverType.SELF, getDeltaMovement());
            setDeltaMovement(getDeltaMovement().scale(AIR_FRICTION));
        }
        calculateEntityAnimation(false);
    }

    private void steerTo(double x, double y, double z, double speed) {
        var dx = x - getX();
        var dy = y - getY();
        var dz = z - getZ();
        var distanceSquared = dx * dx + dy * dy + dz * dz;
        if (distanceSquared < 1.0E-4) {
            setDeltaMovement(0.0, 0.0, 0.0);
            return;
        }
        var distance = Math.sqrt(distanceSquared);
        var factor = Math.min(speed * fluidSpeedFactor(), distance) / distance;
        setDeltaMovement(dx * factor, dy * factor, dz * factor);
    }

    private double fluidSpeedFactor() {
        if (isInWater()) return WATER_SPEED_FACTOR;
        if (isInLava()) return LAVA_SPEED_FACTOR;
        return 1.0;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ITEM, ItemStack.EMPTY);
        builder.define(DATA_SWING, 0);
        builder.define(DATA_ROTATE, 0);
        builder.define(DATA_OWNER, Optional.empty());
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        var stack = getCarriedStack();
        if (!stack.isEmpty()) compound.put(NBT_ITEM, stack.save(level().registryAccess()));
        entityData.get(DATA_OWNER).ifPresent(uuid -> compound.putUUID(NBT_OWNER, uuid));
        compound.putInt(NBT_TICKS, remainingTicks);
        compound.putInt(NBT_ATTACK_CD, attackCooldown);
        compound.putInt(NBT_USE_CD, useCooldown);
        compound.putDouble(NBT_RANGE, attackRange);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        setNoGravity(true);
        if (compound.contains(NBT_ITEM))
            ItemStack.parse(level().registryAccess(), compound.get(NBT_ITEM)).ifPresent(this::setCarriedStackSilent);
        if (compound.hasUUID(NBT_OWNER)) entityData.set(DATA_OWNER, Optional.of(compound.getUUID(NBT_OWNER)));
        remainingTicks = compound.getInt(NBT_TICKS);
        attackCooldown = compound.getInt(NBT_ATTACK_CD);
        useCooldown = compound.getInt(NBT_USE_CD);
        attackRange = compound.getDouble(NBT_RANGE);
        applyItemStats();
    }

    private void setCarriedStackSilent(ItemStack stack) {
        entityData.set(DATA_ITEM, stack.copy());
    }

    @Override
    public void tick() {
        super.tick();
        setNoGravity(true);
        int swing = entityData.get(DATA_SWING);
        if (swing > 0) entityData.set(DATA_SWING, swing - 1);
        int rotate = entityData.get(DATA_ROTATE);
        if (rotate > 0) entityData.set(DATA_ROTATE, rotate - 1);
        if (level().isClientSide) return;
        var motion = getDeltaMovement();
        if (motion.y < -MAX_FALL_SPEED) setDeltaMovement(motion.multiply(1.0, 0.5, 1.0));
        var grounded = onGround() && !isInWater();
        if (grounded && motion.y < 0) setDeltaMovement(getDeltaMovement().multiply(1.0, 0.0, 1.0));
        if (grounded && getDeltaMovement().horizontalDistanceSqr() > 0.0001)
            setDeltaMovement(getDeltaMovement().add(0.0, GROUND_LIFT, 0.0));
        if (remainingTicks > 0) {
            remainingTicks--;
            if (remainingTicks <= 0) {
                if (!tryThrowSelf()) revertToItem();
                return;
            }
        }
        var owner = getOwner();
        if (owner == null) return;
        if (owner.isDeadOrDying() || !owner.isAlive()) {
            if (isInfinite()) return;
            revertToItem();
            return;
        }
        syncInvisibility(owner);
        if (useCooldown > 0) {
            useCooldown--;
        } else {
            useCooldown = 120 + random.nextInt(120);
            attemptRightClick(owner);
        }
        var target = getTarget();
        if (target != null && target.isAlive() && !target.isRemoved() && isValidTarget(target, owner)) {
            updateCombat(owner, target);
        } else {
            setTarget(null);
            if (scanCooldown > 0) {
                scanCooldown--;
            } else {
                scanCooldown = 10;
                var found = findTarget(owner);
                if (found != null) {
                    setTarget(found);
                    updateCombat(owner, found);
                }
            }
            if (getTarget() == null) followOwner(owner);
        }
    }

    private void syncInvisibility(Player owner) {
        var ownerInvisibility = owner.getEffect(MobEffects.INVISIBILITY);
        if (ownerInvisibility != null) {
            var mine = getEffect(MobEffects.INVISIBILITY);
            if (mine == null || mine.getAmplifier() != ownerInvisibility.getAmplifier() || mine.getDuration() < 40)
                addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, Math.max(ownerInvisibility.getDuration(), 200), ownerInvisibility.getAmplifier(), false, false));
        } else if (hasEffect(MobEffects.INVISIBILITY)) {
            removeEffect(MobEffects.INVISIBILITY);
        }
    }

    private void followOwner(Player owner) {
        if (level().dimension() != owner.level().dimension()) {
            if (owner.level() instanceof ServerLevel ownerLevel) {
                teleportTo(ownerLevel, owner.getX(), owner.getY() + 2.5, owner.getZ(), Set.of(), owner.getYRot(), 0);
            } else {
                teleportTo(owner.getX(), owner.getY() + 2.5, owner.getZ());
            }
            hoverTarget = null;
            return;
        }
        var distanceSquared = distanceToSqr(owner);
        if (distanceSquared > 24.0 * 24.0) {
            teleportTo(owner.getX(), owner.getY() + 2.5, owner.getZ());
            hoverTarget = null;
            return;
        }
        if (hoverTarget == null || hoverTimer-- <= 0) {
            hoverTarget = pickHoverPoint(owner);
            hoverTimer = 80 + random.nextInt(80);
        }
        var hoverX = hoverTarget.x - owner.getX();
        var hoverZ = hoverTarget.z - owner.getZ();
        if (hoverX * hoverX + hoverZ * hoverZ > 6.0 * 6.0) {
            hoverTarget = pickHoverPoint(owner);
            hoverTimer = 80 + random.nextInt(80);
        }
        if (distanceToSqr(hoverTarget.x, hoverTarget.y, hoverTarget.z) > 0.5 * 0.5) {
            steerTo(hoverTarget.x, hoverTarget.y, hoverTarget.z, MOVE_SPEED_IDLE);
            lookAt(owner, 30.0F, 30.0F);
        } else {
            hoverTarget = null;
        }
    }

    private Vec3 pickHoverPoint(Player owner) {
        for (var attempt = 0; attempt < 5; attempt++) {
            var point = randomHoverPoint(owner);
            if (isHoverPointFree(point)) return point;
        }
        return randomHoverPoint(owner);
    }

    private Vec3 randomHoverPoint(Player owner) {
        var angle = random.nextDouble() * Math.PI * 2.0;
        var radius = 1.5 + random.nextDouble() * 2.5;
        var x = owner.getX() + Math.cos(angle) * radius;
        var z = owner.getZ() + Math.sin(angle) * radius;
        var y = owner.getY() + 2.2 + random.nextDouble() * 1.2;
        if (!level().getBlockState(BlockPos.containing(x, y, z)).canBeReplaced()) y += 1.0;
        return new Vec3(x, y, z);
    }

    private boolean isHoverPointFree(Vec3 point) {
        if (level().isClientSide) return true;
        var box = new AABB(point.x - 1.2, point.y - 1.2, point.z - 1.2, point.x + 1.2, point.y + 1.2, point.z + 1.2);
        return level().getEntities(EntityTypeTest.forClass(LivingItemEntity.class), box,
                entity -> entity != this && Objects.equals(entity.getOwnerUuid(), getOwnerUuid())).isEmpty();
    }

    private void updateCombat(Player owner, LivingEntity target) {
        hoverTarget = null;
        lookAt(target, 30.0F, 30.0F);
        var stack = getCarriedStack();
        if (isBowLike(stack)) {
            if (attackCooldown > 0) {
                attackCooldown--;
                entityData.set(DATA_SWING, attackCooldown);
            } else if (bowShot(owner, target)) {
                entityData.set(DATA_SWING, 0);
                attackCooldown = Math.max(10, attackInterval);
            } else {
                attackCooldown = 5;
            }
            return;
        }
        if (attackCooldown > 0) {
            attackCooldown--;
            return;
        }
        var distanceSquared = target.getBoundingBox().distanceToSqr(position());
        if (distanceSquared > attackRange * attackRange) {
            chaseTarget(owner, target, distanceSquared);
            return;
        }
        if (target.invulnerableTime > 0) {
            if (random.nextFloat() < 0.25F) {
                var alternative = findTarget(owner);
                if (alternative != null && alternative != target && isValidTarget(alternative, owner))
                    setTarget(alternative);
            }
        }
        if (attackWithStack(owner, target)) {
            entityData.set(DATA_SWING, 5);
            attackCooldown = attackInterval;
            if (random.nextFloat() < RETARGET_CHANCE) {
                var alternative = findTarget(owner);
                if (alternative != null && alternative != target && isValidTarget(alternative, owner))
                    setTarget(alternative);
            }
        }
    }

    private void chaseTarget(Player owner, LivingEntity target, double distanceSquared) {
        var previous = lastChaseDist;
        lastChaseDist = distanceSquared;
        if (distanceSquared < previous - 0.25) {
            chaseStuckTicks = 0;
        } else {
            chaseStuckTicks++;
        }
        if (chaseStuckTicks >= CHASE_STUCK_TICKS) {
            chaseStuckTicks = 0;
            orbitAngle += 1.0 + random.nextDouble() * 2.0;
        }
        if (isCrowded()) orbitAngle += 0.35;
        var point = combatHoverPoint(target);
        steerTo(point.x, point.y, point.z, MOVE_SPEED_ATTACK);
        attackCooldown = 2;
    }

    private Vec3 combatHoverPoint(LivingEntity target) {
        var radius = Math.max(1.0, attackRange * 0.65);
        var x = target.getX() + Math.cos(orbitAngle) * radius;
        var z = target.getZ() + Math.sin(orbitAngle) * radius;
        var y = target.getY() + 1.0;
        if (!level().getBlockState(BlockPos.containing(x, y, z)).canBeReplaced()) y += 1.0;
        return new Vec3(x, y, z);
    }

    private boolean isCrowded() {
        if (level().isClientSide) return false;
        return !level().getEntities(EntityTypeTest.forClass(LivingItemEntity.class),
                getBoundingBox().inflate(COMBAT_SPREAD_RADIUS),
                entity -> entity != this && Objects.equals(entity.getOwnerUuid(), getOwnerUuid())).isEmpty();
    }

    private static DamageSource elementSource(ItemStack stack, DamageSource source) {
        return GenshinCraftIntegration.damageOf(stack, source);
    }

    private boolean attackWithStack(Player owner, LivingEntity target) {
        if (level().isClientSide) return false;
        if (!(level() instanceof ServerLevel serverLevel)) return false;
        var stack = getCarriedStack().copy();
        if (stack.isEmpty()) return false;
        if (NeoForge.EVENT_BUS.post(new AttackEntityEvent(owner, target)).isCanceled()) return false;
        var source = elementSource(stack, level().damageSources().playerAttack(owner));
        var baseDamage = (float) getAttributeValue(Attributes.ATTACK_DAMAGE);
        var finalDamage = EnchantmentHelper.modifyDamage(serverLevel, stack, target, source, baseDamage);
        if (!target.hurt(source, finalDamage)) {
            attackBlacklist.put(target.getUUID(), level().getGameTime());
            setTarget(null);
            return false;
        }
        if (target.invulnerableTime > 0) target.invulnerableTime = Math.max(1, target.invulnerableTime / 4);
        if (stack.canPerformAction(ItemAbilities.SWORD_SWEEP) || stack.getItem().getAttackDamageBonus(target, baseDamage, source) > 0.0F)
            sweepAttack(serverLevel, owner, target, stack, baseDamage);
        var knockback = getAttributeValue(Attributes.ATTACK_KNOCKBACK) + EnchantmentHelper.modifyKnockback(serverLevel, stack, target, source, 0.0F);
        if (knockback > 0.0) {
            var dx = target.getX() - getX();
            var dz = target.getZ() - getZ();
            target.knockback(knockback * 0.5, dx, dz);
        }
        var hurtEnemy = stack.getItem().hurtEnemy(stack, target, owner);
        EnchantmentHelper.doPostAttackEffectsWithItemSource(serverLevel, target, source, stack);
        if (hurtEnemy) stack.getItem().postHurtEnemy(stack, target, owner);
        if (!stack.isEmpty() && !isInfinite()) stack.hurtAndBreak(1, owner, EquipmentSlot.MAINHAND);
        entityData.set(DATA_ITEM, stack.copy());
        level().playSound(null, getX(), getY(), getZ(), SoundEvents.PLAYER_ATTACK_STRONG, SoundSource.HOSTILE, 1.0F, 0.8F + random.nextFloat() * 0.4F);
        if (stack.isEmpty()) revertToItem();
        return true;
    }

    private void sweepAttack(ServerLevel serverLevel, Player owner, LivingEntity target, ItemStack stack, float baseDamage) {
        var event = new SweepAttackEvent(owner, target, true);
        NeoForge.EVENT_BUS.post(event);
        if (!event.isSweeping()) return;
        var sweepLevel = stack.getEnchantmentLevel(serverLevel.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SWEEPING_EDGE));
        var ratio = sweepLevel <= 0 ? 0.0F : 1.0F - 1.0F / (sweepLevel + 1.0F);
        var sweepDamage = 1.0F + ratio * baseDamage;
        var source = elementSource(stack, serverLevel.damageSources().playerAttack(owner));
        var targets = serverLevel.getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(1.0, 0.25, 1.0), entity ->
                entity != this && entity != target && !isAlliedTo(entity)
                        && entity.getType() != EntityType.ARMOR_STAND
                        && !(entity instanceof LivingItemEntity living && Objects.equals(living.getOwnerUuid(), getOwnerUuid()))
                        && distanceToSqr(entity) < 9.0);
        for (var entity : targets) {
            if (entity.hurt(source, EnchantmentHelper.modifyDamage(serverLevel, stack, entity, source, sweepDamage)) && entity.invulnerableTime > 0)
                entity.invulnerableTime = Math.max(1, entity.invulnerableTime / 4);
        }
        serverLevel.playSound(null, getX(), getY(), getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0F, 1.0F);
        serverLevel.sendParticles(ParticleTypes.SWEEP_ATTACK, getX(), getY() + 0.3, getZ(), 1, 0.0, 0.0, 0.0, 0.0);
    }

    private Mob findTarget(Player owner) {
        var range = Math.max(8.0, attackRange * 2.0);
        var now = level().getGameTime();
        var mobs = level().getEntities(EntityTypeTest.forClass(Mob.class), getBoundingBox().inflate(range), mob -> {
            if (mob == this || mob.isDeadOrDying() || mob.isRemoved() || mob.getUUID().equals(owner.getUUID()))
                return false;
            if (mob instanceof LivingItemEntity) return false;
            if (mob instanceof TamableAnimal tamable && owner.getUUID().equals(tamable.getOwnerUUID())) return false;
            if (mob.isAlliedTo(this)) return false;
            var blacklisted = attackBlacklist.get(mob.getUUID());
            return blacklisted == null || now - blacklisted >= ATTACK_BLACKLIST_TIME;
        });
        attackBlacklist.entrySet().removeIf(entry -> now - entry.getValue() >= ATTACK_BLACKLIST_TIME);
        ownerAttackMemory.entrySet().removeIf(entry -> now - entry.getValue() >= OWNER_ATTACK_MEMORY);
        Mob best = null;
        var bestLock = Integer.MAX_VALUE;
        var bestDistance = Double.MAX_VALUE;
        for (var mob : mobs) {
            if (!isThreat(mob, owner, now)) continue;
            var lock = countLocksOn(mob);
            var distance = distanceToSqr(mob);
            if (lock < bestLock || (lock == bestLock && distance < bestDistance)) {
                bestLock = lock;
                bestDistance = distance;
                best = mob;
            }
        }
        return best;
    }

    private int countLocksOn(LivingEntity candidate) {
        if (level().isClientSide) return 0;
        var count = 0;
        var allies = level().getEntities(EntityTypeTest.forClass(LivingItemEntity.class),
                getBoundingBox().inflate(MAX_CHASE_DISTANCE),
                entity -> entity != this && Objects.equals(entity.getOwnerUuid(), getOwnerUuid()));
        for (var ally : allies) {
            if (ally.getTarget() == candidate) count++;
        }
        return count;
    }

    private boolean isThreat(Mob mob, Player owner, long now) {
        if (mob.getTarget() == owner || mob.getTarget() == this) return true;
        var attacked = ownerAttackMemory.get(mob.getUUID());
        return attacked != null && now - attacked < OWNER_ATTACK_MEMORY;
    }

    private boolean isValidTarget(LivingEntity target, Player owner) {
        var now = level().getGameTime();
        if (distanceToSqr(target) > MAX_CHASE_DISTANCE * MAX_CHASE_DISTANCE) return false;
        if (target instanceof Mob mob && (mob.getTarget() == owner || mob.getTarget() == this)) return true;
        var attacked = ownerAttackMemory.get(target.getUUID());
        return attacked != null && now - attacked < OWNER_ATTACK_MEMORY;
    }

    public void onOwnerAttack(Entity target) {
        if (level().isClientSide || isRemoved()) return;
        if (target == this || target instanceof LivingItemEntity other && Objects.equals(other.getOwnerUuid(), getOwnerUuid()))
            return;
        if (target instanceof LivingEntity living && living.isAlive() && !living.isRemoved()) {
            ownerAttackMemory.put(living.getUUID(), level().getGameTime());
            if (getTarget() == null && countLocksOn(living) < 1) setTarget(living);
        }
    }

    private void attemptRightClick(Player owner) {
        var stack = getCarriedStack();
        if (stack.isEmpty() || owner == null) return;
        var item = stack.getItem();
        if (owner.getCooldowns().isOnCooldown(item)) return;
        entityData.set(DATA_ROTATE, 10);
        var focus = getTarget() != null ? getTarget() : owner;
        if (isBowLike(stack)) {
            if (focus != owner) bowShot(owner, focus);
            return;
        }
        if (item == Items.WATER_BUCKET || item == Items.LAVA_BUCKET || item == Items.POWDER_SNOW_BUCKET) {
            placeFluidAt(focus, item);
            return;
        }
        if (item instanceof ProjectileItem) {
            launchProjectile(owner, focus, stack);
            return;
        }
        if (!canRightClick(stack)) return;
        useAsPlayer(owner, stack);
    }

    private void launchProjectile(Player owner, LivingEntity focus, ItemStack stack) {
        var position = position().add(0, getBbHeight() * 0.8, 0);
        var direction = focus.getEyePosition().subtract(position).normalize();
        if (throwProjectile(owner, stack.copy(), position, direction)) return;
        if (!isInfinite()) stack.shrink(1);
        if (stack.isEmpty()) {
            entityData.set(DATA_ITEM, ItemStack.EMPTY);
            revertToItem();
        } else {
            entityData.set(DATA_ITEM, stack.copy());
        }
    }

    private boolean throwProjectile(Player owner, ItemStack thrown, Vec3 position, Vec3 direction) {
        var item = thrown.getItem();
        if (item instanceof EnderpearlItem) {
            if (owner == null) return true;
            var pearl = new ThrownEnderpearl(level(), owner);
            pearl.setPos(position.x, position.y, position.z);
            pearl.setItem(thrown);
            pearl.shoot(direction.x, direction.y, direction.z, 1.5F, 1.0F);
            level().addFreshEntity(pearl);
            return false;
        }
        if (item instanceof ProjectileItem projectileItem) {
            Projectile projectile = projectileItem.asProjectile(level(), position, thrown, Direction.UP);
            if (owner != null) projectile.setOwner(owner);
            projectile.setPos(position.x, position.y, position.z);
            projectile.setDeltaMovement(direction.scale(1.2));
            level().addFreshEntity(projectile);
            return false;
        }
        return true;
    }

    private void placeFluidAt(LivingEntity focus, Item item) {
        var level = level();
        if (level.isClientSide) return;
        var pos = focus.blockPosition();
        if (!level.getBlockState(pos).canBeReplaced()) pos = pos.above();
        BlockState state = null;
        if (item == Items.WATER_BUCKET) {
            state = Fluids.WATER.defaultFluidState().createLegacyBlock();
        } else if (item == Items.LAVA_BUCKET) {
            state = Fluids.LAVA.defaultFluidState().createLegacyBlock();
        } else if (item == Items.POWDER_SNOW_BUCKET) {
            state = Blocks.POWDER_SNOW.defaultBlockState();
        }
        if (state != null && level.getBlockState(pos).canBeReplaced()) {
            level.setBlockAndUpdate(pos, state);
            level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
            entityData.set(DATA_ITEM, new ItemStack(Items.BUCKET));
        }
    }

    private void useAsPlayer(Player owner, ItemStack stack) {
        var slot = owner.getInventory().selected;
        var previous = owner.getInventory().getItem(slot);
        owner.getInventory().setItem(slot, stack);
        ItemStack result;
        try {
            result = stack.getItem().use(level(), owner, InteractionHand.MAIN_HAND).getObject();
        } finally {
            owner.getInventory().setItem(slot, previous);
        }
        if (result.isEmpty()) {
            if (isInfinite()) return;
            entityData.set(DATA_ITEM, ItemStack.EMPTY);
            revertToItem();
            return;
        }
        if (!ItemStack.isSameItemSameComponents(stack, result) || stack.getCount() != result.getCount())
            entityData.set(DATA_ITEM, result.copy());
    }

    private static boolean canRightClick(ItemStack stack) {
        return stack.is(TAG_RIGHT_CLICK);
    }

    public static boolean isBowLike(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        var animation = stack.getUseAnimation();
        return animation == UseAnim.BOW || animation == UseAnim.CROSSBOW;
    }

    private static ItemStack findArrow(Player owner) {
        var inventory = owner.getInventory();
        for (var stack : inventory.items) {
            if (isArrow(stack)) return stack;
        }
        for (var stack : inventory.offhand) {
            if (isArrow(stack)) return stack;
        }
        return ItemStack.EMPTY;
    }

    private static boolean isArrow(ItemStack stack) {
        var item = stack.getItem();
        return item == Items.ARROW || item == Items.SPECTRAL_ARROW || item == Items.TIPPED_ARROW;
    }

    private boolean bowShot(Player owner, LivingEntity target) {
        var ammo = findArrow(owner);
        if (ammo == null || ammo.isEmpty()) return false;
        var arrowStack = ammo.copy();
        ammo.shrink(1);
        var arrow = ProjectileUtil.getMobArrow(owner, arrowStack, 2.5F, getCarriedStack());
        var y = getY() + getBbHeight() * 0.8;
        arrow.setPos(getX(), y, getZ());
        var to = new Vec3(target.getX() - getX(), target.getEyeY() - y, target.getZ() - getZ());
        arrow.shoot(to.x, to.y, to.z, 2.2F, 0.8F);
        level().addFreshEntity(arrow);
        return true;
    }

    private boolean tryThrowSelf() {
        var stack = getCarriedStack();
        if (stack.isEmpty()) return false;
        var owner = getOwner();
        var aim = getTarget() != null ? getTarget() : owner;
        var position = position().add(0, getBbHeight() * 0.8, 0);
        var direction = aim != null ? aim.getEyePosition().subtract(position).normalize() : getLookAngle();
        if (throwProjectile(owner, stack, position, direction)) return false;
        if (owner != null && stack.getCount() > 1) {
            var rest = stack.copy();
            rest.setCount(stack.getCount() - 1);
            owner.getInventory().add(rest);
        }
        playDisappearEffects();
        reverted = true;
        discard();
        return true;
    }

    private void playDisappearEffects() {
        if (level() instanceof ServerLevel serverLevel) {
            serverLevel.playSound(null, getX(), getY(), getZ(), SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.AMBIENT, 1.0F, 1.0F);
            serverLevel.playSound(null, getX(), getY(), getZ(), SoundEvents.BEACON_DEACTIVATE, SoundSource.AMBIENT, 1.0F, 1.0F);
            serverLevel.sendParticles(ParticleTypes.FIREWORK, getX(), getY() + 0.6, getZ(), 24, 0.4, 0.4, 0.4, 0.2);
        }
    }

    public void revertToItem() {
        if (isInfinite()) return;
        revert();
    }

    public void revertByDeath() {
        revert();
    }

    public void revertByOwner() {
        revert();
    }

    private void revert() {
        if (reverted) return;
        reverted = true;
        playDisappearEffects();
        var stack = getCarriedStack();
        var owner = getOwner();
        var deathDrop = owner != null && owner.isDeadOrDying() && !owner.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY);
        var added = false;
        if (owner != null && !stack.isEmpty() && !deathDrop) added = owner.getInventory().add(stack);
        if (!stack.isEmpty() && !added) {
            var x = owner != null && !deathDrop ? owner.getX() : getX();
            var y = owner != null && !deathDrop ? owner.getY() : getY();
            var z = owner != null && !deathDrop ? owner.getZ() : getZ();
            if (level() instanceof ServerLevel serverLevel && y < serverLevel.getMinBuildHeight() + 5) {
                var spawn = serverLevel.getSharedSpawnPos();
                x = spawn.getX() + 0.5;
                y = spawn.getY() + 1.0;
                z = spawn.getZ() + 0.5;
            }
            var pos = BlockPos.containing(x, y, z);
            while (!level().getBlockState(pos).canBeReplaced() && pos.getY() < level().getMaxBuildHeight() - 1)
                pos = pos.above();
            var drop = new LivingItemDrop(level(), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack, owner != null ? owner.getUUID() : null);
            level().addFreshEntity(drop);
        }
        if (!level().isClientSide) level().getScoreboard().removePlayerFromTeam(getStringUUID());
        discard();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.8)
                .add(Attributes.MAX_HEALTH, 20)
                .add(Attributes.ARMOR, 0)
                .add(Attributes.ATTACK_DAMAGE, 2.0)
                .add(Attributes.FOLLOW_RANGE, 32)
                .add(Attributes.STEP_HEIGHT, 0.6)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1)
                .add(Attributes.ATTACK_KNOCKBACK, 0.5)
                .add(Attributes.FLYING_SPEED, 0.8)
                .add(NeoForgeMod.SWIM_SPEED, 1);
    }
}
