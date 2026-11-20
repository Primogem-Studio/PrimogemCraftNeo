package net.per.primogemcraft.entity.mob;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.util.Identifier;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.sound.SoundCategory;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Hand;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.*;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.ai.control.FlightMoveControl;
import net.minecraft.entity.ai.pathing.BirdNavigation;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.item.*;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.block.Blocks;
import net.minecraft.block.BlockState;
import net.minecraft.util.TypeFilter;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.per.primogemcraft.collab.genshincraft.GenshinCraftIntegration;
import net.per.primogemcraft.entity.misc.LivingItemDrop;

import org.jetbrains.annotations.Nullable;
import java.util.*;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public class LivingItemEntity extends PathAwareEntity {
    public static final TrackedData<ItemStack> DATA_ITEM = DataTracker.registerData(LivingItemEntity.class, TrackedDataHandlerRegistry.ITEM_STACK);
    public static final TrackedData<Integer> DATA_SWING = DataTracker.registerData(LivingItemEntity.class, TrackedDataHandlerRegistry.INTEGER);
    public static final TrackedData<Integer> DATA_ROTATE = DataTracker.registerData(LivingItemEntity.class, TrackedDataHandlerRegistry.INTEGER);
    public static final TrackedData<Optional<UUID>> DATA_OWNER = DataTracker.registerData(LivingItemEntity.class, TrackedDataHandlerRegistry.OPTIONAL_UUID);

    private static final String NBT_ITEM = "LivingItemStack";
    private static final String NBT_OWNER = "LivingItemOwner";
    private static final String NBT_TICKS = "LivingItemTicks";
    private static final String NBT_ATTACK_CD = "LivingItemAttackCd";
    private static final String NBT_USE_CD = "LivingItemUseCd";
    private static final String NBT_RANGE = "LivingItemRange";
    private static final String NBT_INFINITE_MAX_HEALTH = "LivingItemInfiniteMaxHealth";

    private static final TagKey<Item> TAG_RIGHT_CLICK = TagKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, "living_item_right_click"));

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
    private static final int ATTACK_INVULNERABLE_TICKS = 5;
    private static final float INFINITE_HEALTH = 500.0F;
    private static final double TICKS_PER_HEALTH = 10.0;

    private int remainingTicks;
    private float infiniteMaxHealth = INFINITE_HEALTH;
    private boolean durationHealthInitialized;
    private int attackCooldown;
    private int attackInterval = 20;
    private int useCooldown;
    private int scanCooldown;
    private double attackRange = 3.0;
    private Vec3d hoverTarget;
    private int hoverTimer;
    private boolean reverted;
    private double orbitAngle = Double.NaN;
    private double lastChaseDist = Double.MAX_VALUE;
    private int chaseStuckTicks;
    private final Map<UUID, Long> ownerAttackMemory = new HashMap<>();
    private final Map<UUID, Long> attackBlacklist = new HashMap<>();

    public LivingItemEntity(EntityType<? extends LivingItemEntity> entityType, World world) {
        super(entityType, world);
        experiencePoints = 0;
        setNoGravity(true);
        setPersistent();
        moveControl = new FlightMoveControl(this, 10, true);
    }

    public void startLiving(PlayerEntity owner, ItemStack stack, int ticks) {
        dataTracker.set(DATA_OWNER, Optional.of(owner.getUuid()));
        dataTracker.set(DATA_ITEM, stack.copy());
        setRemainingTicks(ticks);
        applyItemStats();
        attackCooldown = random.nextInt(Math.max(1, attackInterval));
        useCooldown = 40 + random.nextInt(40);
        joinOwnerTeam(owner);
    }

    public boolean isInfinite() {
        return remainingTicks < 0;
    }

    public void setInfinite() {
        setRemainingTicks(-1);
    }

    public int getRemainingTicks() {
        return remainingTicks;
    }

    public void setRemainingTicks(int ticks) {
        remainingTicks = ticks < 0 ? -1 : Math.max(1, ticks);
        durationHealthInitialized = true;
        syncDurationHealth(true);
    }

    /** Adds {@code ticks} to a finite item's duration, preserving infinite duration and preventing overflow. */
    public void addRemainingTicks(int ticks) {
        if (isInfinite()) return;
        setRemainingTicks((int) net.minecraft.util.math.MathHelper.clamp((long) remainingTicks + ticks, 1L, (long) Integer.MAX_VALUE));
    }

    /** Increases an infinite item's maximum and current health by the health equivalent of {@code ticks}. */
    public void addInfiniteHealthFromDuration(int ticks) {
        if (getWorld().isClient || !isInfinite() || ticks <= 0 || !isAlive() || isRemoved()) return;
        var restoredHealth = (float) (ticks / TICKS_PER_HEALTH);
        var health = getHealth();
        infiniteMaxHealth += restoredHealth;
        syncDurationHealth(false);
        setHealth(health + restoredHealth);
    }

    private void syncDurationHealth(boolean replenishInfinite) {
        var health = isInfinite() ? infiniteMaxHealth : (float) (remainingTicks / TICKS_PER_HEALTH);
        var maximumHealth = getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
        if (maximumHealth != null) maximumHealth.setBaseValue(Math.max(0.1F, health));
        if (!isInfinite() || replenishInfinite) super.setHealth(health);
        else super.setHealth(Math.min(getHealth(), health));
    }

    @Override
    public double getAttributeValue(EntityAttribute attribute) {
        if (attribute == EntityAttributes.GENERIC_MAX_HEALTH) return getAttributeBaseValue(attribute);
        return super.getAttributeValue(attribute);
    }

    @Override
    public void setHealth(float health) {
        super.setHealth(health);
        if (durationHealthInitialized && !getWorld().isClient && !isInfinite()) {
            remainingTicks = getHealth() <= 0.0F ? 0
                    : (int) Math.max(1L, Math.min(Integer.MAX_VALUE, Math.round(getHealth() * TICKS_PER_HEALTH)));
        }
    }

    public void applyItemStats() {
        var stack = getCarriedStack();
        var mods = stack.getAttributeModifiers(EquipmentSlot.MAINHAND);
        attackInterval = getAttackInterval();

        var attack = getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE);
        if (attack != null) {
            clearItemModifiers(attack);
            attack.setBaseValue(BASE_ATTACK_DAMAGE);
            addItemModifiers(attack, mods);
        }
    }

    private int getAttackInterval() {
        var attackSpeed = new EntityAttributeInstance(EntityAttributes.GENERIC_ATTACK_SPEED, attribute -> {});
        attackSpeed.setBaseValue(4.0);
        var owner = getOwner();
        if (owner != null && owner.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_SPEED) != null) {
            attackSpeed.setBaseValue(owner.getAttributeBaseValue(EntityAttributes.GENERIC_ATTACK_SPEED));
            for (var modifier : owner.getMainHandStack().getAttributeModifiers(EquipmentSlot.MAINHAND).get(EntityAttributes.GENERIC_ATTACK_SPEED))
                attackSpeed.removeModifier(modifier.getId());
        }
        for (var modifier : getCarriedStack().getAttributeModifiers(EquipmentSlot.MAINHAND).get(EntityAttributes.GENERIC_ATTACK_SPEED)) {
            attackSpeed.removeModifier(modifier.getId());
            attackSpeed.addTemporaryModifier(modifier);
        }
        return Math.max(1, (int) Math.ceil(20.0 / attackSpeed.getValue()));
    }

    private void clearItemModifiers(EntityAttributeInstance instance) {
        for (var modifier : List.copyOf(instance.getModifiers())) {
            // 1.20.1 修饰器用 UUID 标识；本模组写入时把前缀编进名称
            if (modifier.getName().startsWith(MODIFIER_PREFIX)) instance.removeModifier(modifier.getId());
        }
    }

    private void addItemModifiers(EntityAttributeInstance instance,
                                  com.google.common.collect.Multimap<EntityAttribute, EntityAttributeModifier> mods) {
        if (mods == null) return;
        for (var source : mods.get(EntityAttributes.GENERIC_ATTACK_DAMAGE)) {
            var name = MODIFIER_PREFIX + source.getName();
            instance.addTemporaryModifier(new EntityAttributeModifier(
                    net.per.primogemcraft.compat.attribute.AttributesCompat.uuid(
                            Identifier.of(MOD_ID, name)),
                    name, source.getValue(), source.getOperation()));
        }
    }

    public void setAttackRange(double range) {
        attackRange = Math.max(1.0, range);
    }

    public double getAttackRange() {
        return attackRange;
    }

    public ItemStack getCarriedStack() {
        return dataTracker.get(DATA_ITEM);
    }

    public int getSwingTicks() {
        return dataTracker.get(DATA_SWING);
    }

    public int getRotateTicks() {
        return dataTracker.get(DATA_ROTATE);
    }

    public PlayerEntity getOwner() {
        var uuid = dataTracker.get(DATA_OWNER).orElse(null);
        if (uuid == null || getWorld().isClient) return null;
        if (getWorld() instanceof ServerWorld serverLevel) return serverLevel.getServer().getPlayerManager().getPlayer(uuid);
        return null;
    }

    public UUID getOwnerUuid() {
        return dataTracker.get(DATA_OWNER).orElse(null);
    }

    public void joinOwnerTeam(PlayerEntity owner) {
        if (getWorld().isClient) return;
        var team = owner.getScoreboardTeam();
        if (team instanceof net.minecraft.scoreboard.Team playerTeam) getWorld().getScoreboard().addPlayerToTeam(getUuidAsString(), playerTeam);
    }

    @Override
    protected EntityNavigation createNavigation(World world) {
        return new BirdNavigation(this, world);
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
    public void pushAwayFrom(Entity entity) {
    }

    @Override
    public boolean canImmediatelyDespawn(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (getWorld().isClient || isRemoved() || isDead()) return false;
        if (source.getAttacker() == null && !source.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY)) return false;
        if (source.getAttacker() != null) {
            var ownerUuid = getOwnerUuid();
            if (ownerUuid != null && ownerUuid.equals(source.getAttacker().getUuid())) return false;
        }
        if (!super.damage(source, amount)) return false;
        syncDurationHealth(false);
        if (source.getAttacker() instanceof LivingEntity attacker && attacker.isAlive() && isAlive()) {
            setAttacker(attacker);
            ownerAttackMemory.put(attacker.getUuid(), getWorld().getTime());
            setTarget(attacker);
            var dx = getX() - attacker.getX();
            var dz = getZ() - attacker.getZ();
            takeKnockback(0.4, dx, dz);
        }
        if (getHealth() <= 0.0F) {
            setHealth(0.0F);
            revertByDeath();
            return true;
        }
        return true;
    }

    @Override
    public boolean isImmuneToExplosion() {
        return true;
    }

    @Override
    public boolean handleFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public boolean canSpawn(WorldView world) {
        return world.isSpaceEmpty(this);
    }

    @Override
    public boolean canWalkOnFluid(net.minecraft.fluid.FluidState state) {
        return false;
    }

    @Override
    public boolean isPushedByFluids() {
        return false;
    }

    @Override
    protected void fall(double y, boolean onGround, BlockState state, BlockPos pos) {
    }

    @Override
    public void setNoGravity(boolean ignored) {
        super.setNoGravity(true);
    }

    @Override
    public void travel(Vec3d travelVector) {
        if (!isAiDisabled() || isLogicalSideForUpdatingMovement()) {
            move(net.minecraft.entity.MovementType.SELF, getVelocity());
            setVelocity(getVelocity().multiply(AIR_FRICTION));
        }
        updateLimbs(false);
    }

    private void steerTo(double x, double y, double z, double speed) {
        var dx = x - getX();
        var dy = y - getY();
        var dz = z - getZ();
        var distanceSquared = dx * dx + dy * dy + dz * dz;
        if (distanceSquared < 1.0E-4) {
            setVelocity(0.0, 0.0, 0.0);
            return;
        }
        var distance = Math.sqrt(distanceSquared);
        var factor = Math.min(speed * fluidSpeedFactor(), distance) / distance;
        setVelocity(dx * factor, dy * factor, dz * factor);
    }

    private double fluidSpeedFactor() {
        if (isTouchingWater()) return WATER_SPEED_FACTOR;
        if (isInLava()) return LAVA_SPEED_FACTOR;
        return 1.0;
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(DATA_ITEM, ItemStack.EMPTY);
        this.dataTracker.startTracking(DATA_SWING, 0);
        this.dataTracker.startTracking(DATA_ROTATE, 0);
        this.dataTracker.startTracking(DATA_OWNER, Optional.empty());
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound compound) {
        super.writeCustomDataToNbt(compound);
        var stack = getCarriedStack();
        if (!stack.isEmpty()) compound.put(NBT_ITEM, stack.writeNbt(new net.minecraft.nbt.NbtCompound()));
        dataTracker.get(DATA_OWNER).ifPresent(uuid -> compound.putUuid(NBT_OWNER, uuid));
        compound.putInt(NBT_TICKS, remainingTicks);
        compound.putInt(NBT_ATTACK_CD, attackCooldown);
        compound.putInt(NBT_USE_CD, useCooldown);
        compound.putDouble(NBT_RANGE, attackRange);
        compound.putFloat(NBT_INFINITE_MAX_HEALTH, infiniteMaxHealth);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound compound) {
        durationHealthInitialized = false;
        super.readCustomDataFromNbt(compound);
        setNoGravity(true);
        if (compound.contains(NBT_ITEM))
            setCarriedStackSilent(ItemStack.fromNbt(compound.getCompound(NBT_ITEM)));
        if (compound.containsUuid(NBT_OWNER)) dataTracker.set(DATA_OWNER, Optional.of(compound.getUuid(NBT_OWNER)));
        remainingTicks = compound.getInt(NBT_TICKS);
        var savedMaxHealth = compound.getFloat(NBT_INFINITE_MAX_HEALTH);
        infiniteMaxHealth = Float.isFinite(savedMaxHealth) ? Math.max(INFINITE_HEALTH, savedMaxHealth) : INFINITE_HEALTH;
        attackCooldown = compound.getInt(NBT_ATTACK_CD);
        useCooldown = compound.getInt(NBT_USE_CD);
        attackRange = compound.getDouble(NBT_RANGE);
        applyItemStats();
        durationHealthInitialized = true;
        syncDurationHealth(false);
    }

    private void setCarriedStackSilent(ItemStack stack) {
        dataTracker.set(DATA_ITEM, stack.copy());
    }

    @Override
    public void tick() {
        super.tick();
        setNoGravity(true);
        var swing = dataTracker.get(DATA_SWING);
        if (swing > 0) dataTracker.set(DATA_SWING, swing - 1);
        var rotate = dataTracker.get(DATA_ROTATE);
        if (rotate > 0) dataTracker.set(DATA_ROTATE, rotate - 1);
        if (getWorld().isClient) return;
        if (isRemoved() || isDead()) return;
        var owner = getOwner();
        if (owner != null && owner.isAlive() && getWorld() != owner.getWorld()) {
            setTarget(null);
            followOwner(owner);
            return;
        }
        var motion = getVelocity();
        if (motion.y < -MAX_FALL_SPEED) setVelocity(motion.multiply(1.0, 0.5, 1.0));
        var grounded = isOnGround() && !isTouchingWater();
        if (grounded && motion.y < 0) setVelocity(getVelocity().multiply(1.0, 0.0, 1.0));
        if (grounded && getVelocity().horizontalLengthSquared() > 0.0001)
            setVelocity(getVelocity().add(0.0, GROUND_LIFT, 0.0));
        if (remainingTicks > 0) {
            remainingTicks--;
            if (remainingTicks <= 0) {
                if (!tryThrowSelf()) revertToItem();
                return;
            }
            syncDurationHealth(false);
        }
        if (owner == null) return;
        if (owner.isDead() || !owner.isAlive()) {
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

    private void syncInvisibility(PlayerEntity owner) {
        var ownerInvisibility = owner.getStatusEffect(StatusEffects.INVISIBILITY);
        if (ownerInvisibility != null) {
            var mine = getStatusEffect(StatusEffects.INVISIBILITY);
            if (mine == null || mine.getAmplifier() != ownerInvisibility.getAmplifier() || mine.getDuration() < 40)
                addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, Math.max(ownerInvisibility.getDuration(), 200), ownerInvisibility.getAmplifier(), false, false));
        } else if (hasStatusEffect(StatusEffects.INVISIBILITY)) {
            removeStatusEffect(StatusEffects.INVISIBILITY);
        }
    }

    private void followOwner(PlayerEntity owner) {
        if (getWorld().getRegistryKey() != owner.getWorld().getRegistryKey()) {
            if (owner.getWorld() instanceof ServerWorld ownerLevel) {
                teleport(ownerLevel, owner.getX(), owner.getY() + 2.5, owner.getZ(), Set.of(), owner.getYaw(), 0);
            } else {
                teleport(owner.getX(), owner.getY() + 2.5, owner.getZ());
            }
            hoverTarget = null;
            return;
        }
        var distanceSquared = squaredDistanceTo(owner);
        if (distanceSquared > 24.0 * 24.0) {
            teleport(owner.getX(), owner.getY() + 2.5, owner.getZ());
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
        if (squaredDistanceTo(hoverTarget.x, hoverTarget.y, hoverTarget.z) > 0.5 * 0.5) {
            steerTo(hoverTarget.x, hoverTarget.y, hoverTarget.z, MOVE_SPEED_IDLE);
        } else {
            hoverTarget = null;
        }
    }

    private Vec3d pickHoverPoint(PlayerEntity owner) {
        for (var attempt = 0; attempt < 5; attempt++) {
            var point = randomHoverPoint(owner);
            if (isHoverPointFree(point)) return point;
        }
        return randomHoverPoint(owner);
    }

    private Vec3d randomHoverPoint(PlayerEntity owner) {
        var angle = random.nextDouble() * Math.PI * 2.0;
        var radius = 1.5 + random.nextDouble() * 2.5;
        var x = owner.getX() + Math.cos(angle) * radius;
        var z = owner.getZ() + Math.sin(angle) * radius;
        var y = owner.getY() + 2.2 + random.nextDouble() * 1.2;
        if (!getWorld().getBlockState(BlockPos.ofFloored(x, y, z)).isReplaceable()) y += 1.0;
        return new Vec3d(x, y, z);
    }

    private boolean isHoverPointFree(Vec3d point) {
        if (getWorld().isClient) return true;
        var box = new Box(point.x - 1.2, point.y - 1.2, point.z - 1.2, point.x + 1.2, point.y + 1.2, point.z + 1.2);
        return !hasNearbyAlly(box);
    }

    private void updateCombat(PlayerEntity owner, LivingEntity target) {
        hoverTarget = null;
        lookAt(net.minecraft.command.argument.EntityAnchorArgumentType.EntityAnchor.EYES, target.getPos());
        var stack = getCarriedStack();
        if (isBowLike(stack)) {
            if (attackCooldown > 0) {
                attackCooldown--;
                dataTracker.set(DATA_SWING, attackCooldown);
            } else if (bowShot(owner, target)) {
                dataTracker.set(DATA_SWING, 0);
                attackCooldown = Math.max(10, attackInterval);
            } else {
                attackCooldown = 5;
            }
            return;
        }
        if (attackCooldown > 0 && --attackCooldown > 0) return;
        var distanceSquared = target.getBoundingBox().squaredMagnitude(getPos());
        if (distanceSquared > attackRange * attackRange) {
            chaseTarget(owner, target, distanceSquared);
            return;
        }
        if (target.hurtTime > 0) {
            if (random.nextFloat() < 0.25F) {
                var alternative = findTarget(owner);
                if (alternative != null && alternative != target && isValidTarget(alternative, owner))
                    setTarget(alternative);
            }
            return;
        }
        attackInterval = getAttackInterval();
        attackCooldown = attackInterval;
        if (attackWithStack(owner, target)) {
            dataTracker.set(DATA_SWING, 5);
            if (random.nextFloat() < RETARGET_CHANCE) {
                var alternative = findTarget(owner);
                if (alternative != null && alternative != target && isValidTarget(alternative, owner))
                    setTarget(alternative);
            }
        }
    }

    private void chaseTarget(PlayerEntity owner, LivingEntity target, double distanceSquared) {
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
    }

    private Vec3d combatHoverPoint(LivingEntity target) {
        var radius = Math.max(1.0, attackRange * 0.65);
        var x = target.getX() + Math.cos(orbitAngle) * radius;
        var z = target.getZ() + Math.sin(orbitAngle) * radius;
        var y = target.getY() + 1.0;
        if (!getWorld().getBlockState(BlockPos.ofFloored(x, y, z)).isReplaceable()) y += 1.0;
        return new Vec3d(x, y, z);
    }

    private boolean isCrowded() {
        if (getWorld().isClient) return false;
        return hasNearbyAlly(getBoundingBox().expand(COMBAT_SPREAD_RADIUS));
    }

    private boolean hasNearbyAlly(Box bounds) {
        var ownerUuid = getOwnerUuid();
        var matches = getWorld().getEntitiesByClass(LivingItemEntity.class, bounds,
                entity -> entity != this && Objects.equals(entity.getOwnerUuid(), ownerUuid));
        return !matches.isEmpty();
    }

    private static DamageSource elementSource(ItemStack stack, DamageSource source) {
        return GenshinCraftIntegration.damageOf(stack, source);
    }

    private boolean attackWithStack(PlayerEntity owner, LivingEntity target) {
        if (getWorld().isClient()) return false;
        if (!(getWorld() instanceof ServerWorld serverLevel)) return false;
        var stack = getCarriedStack().copy();
        if (stack.isEmpty()) return false;
        var source = elementSource(stack, getWorld().getDamageSources().playerAttack(owner));
        var baseDamage = (float) getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
        var finalDamage = baseDamage + EnchantmentHelper.getAttackDamage(stack, target.getGroup());
        if (!target.damage(source, finalDamage)) {
            attackBlacklist.put(target.getUuid(), getWorld().getTime());
            setTarget(null);
            return false;
        }
        target.timeUntilRegen = ATTACK_INVULNERABLE_TICKS;
        if (stack.getItem() instanceof SwordItem || EnchantmentHelper.getAttackDamage(stack, target.getGroup()) > 0.0F)
            sweepAttack(serverLevel, owner, target, stack, baseDamage);
        var knockback = getAttributeValue(EntityAttributes.GENERIC_ATTACK_KNOCKBACK) + EnchantmentHelper.getKnockback(owner);
        if (knockback > 0.0) {
            var dx = target.getX() - getX();
            var dz = target.getZ() - getZ();
            target.takeKnockback(knockback * 0.5, dx, dz);
        }
        stack.getItem().postHit(stack, target, owner);
        EnchantmentHelper.onTargetDamaged(owner, target);
        if (!stack.isEmpty() && !isInfinite()) stack.damage(1, owner, p -> p.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND));
        getDataTracker().set(DATA_ITEM, stack.copy());
        getWorld().playSound(null, getX(), getY(), getZ(), SoundEvents.ENTITY_PLAYER_ATTACK_STRONG, SoundCategory.HOSTILE, 1.0F, 0.8F + random.nextFloat() * 0.4F);
        if (stack.isEmpty()) revertToItem();
        return true;
    }

    private void sweepAttack(ServerWorld serverLevel, PlayerEntity owner, LivingEntity target, ItemStack stack, float baseDamage) {
        var sweepLevel = EnchantmentHelper.getLevel(net.minecraft.enchantment.Enchantments.SWEEPING, stack);
        var ratio = sweepLevel <= 0 ? 0.0F : 1.0F - 1.0F / (sweepLevel + 1.0F);
        var sweepDamage = 1.0F + ratio * baseDamage;
        var source = elementSource(stack, serverLevel.getDamageSources().playerAttack(owner));
        var targets = serverLevel.getEntitiesByClass(LivingEntity.class, target.getBoundingBox().expand(1.0, 0.25, 1.0), entity ->
                entity != this && entity != target && !isTeammate(entity)
                        && entity.getType() != EntityType.ARMOR_STAND
                        && !(entity instanceof LivingItemEntity living && Objects.equals(living.getOwnerUuid(), getOwnerUuid()))
                        && squaredDistanceTo(entity) < 9.0);
        for (var entity : targets) {
            if (entity.hurtTime > 0) continue;
            if (entity.damage(source, sweepDamage + EnchantmentHelper.getAttackDamage(stack, entity.getGroup())))
                entity.hurtTime = ATTACK_INVULNERABLE_TICKS;
        }
        serverLevel.playSound(null, getX(), getY(), getZ(), SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.PLAYERS, 1.0F, 1.0F);
        serverLevel.spawnParticles(ParticleTypes.SWEEP_ATTACK, getX(), getY() + 0.3, getZ(), 1, 0.0, 0.0, 0.0, 0.0);
    }

    private net.minecraft.entity.mob.MobEntity findTarget(PlayerEntity owner) {
        var range = Math.max(8.0, attackRange * 2.0);
        var now = getWorld().getTime();
        var mobs = getWorld().getEntitiesByClass(net.minecraft.entity.mob.MobEntity.class, getBoundingBox().expand(range), mob -> {
            if (mob == this || mob.isDead() || mob.isRemoved() || mob.getUuid().equals(owner.getUuid()))
                return false;
            if (mob instanceof LivingItemEntity) return false;
            if (mob instanceof TameableEntity tamable && owner.getUuid().equals(tamable.getOwnerUuid())) return false;
            if (mob.isTeammate(this)) return false;
            var blacklisted = attackBlacklist.get(mob.getUuid());
            return (blacklisted == null || now - blacklisted >= ATTACK_BLACKLIST_TIME) && isThreat(mob, owner, now);
        });
        attackBlacklist.entrySet().removeIf(entry -> now - entry.getValue() >= ATTACK_BLACKLIST_TIME);
        ownerAttackMemory.entrySet().removeIf(entry -> now - entry.getValue() >= OWNER_ATTACK_MEMORY);
        if (mobs.isEmpty()) return null;
        var targetLocks = new IdentityHashMap<LivingEntity, Integer>();
        var ownerUuid = getOwnerUuid();
        var allies = getWorld().getEntitiesByClass(LivingItemEntity.class, getBoundingBox().expand(MAX_CHASE_DISTANCE),
                entity -> entity != this && Objects.equals(entity.getOwnerUuid(), ownerUuid));
        for (var ally : allies) {
            var target = ally.getTarget();
            if (target != null) targetLocks.merge(target, 1, Integer::sum);
        }
        net.minecraft.entity.mob.MobEntity best = null;
        var bestLock = Integer.MAX_VALUE;
        var bestDistance = Double.MAX_VALUE;
        for (var mob : mobs) {
            var lock = targetLocks.getOrDefault(mob, 0);
            var distance = squaredDistanceTo(mob);
            if (lock < bestLock || (lock == bestLock && distance < bestDistance)) {
                bestLock = lock;
                bestDistance = distance;
                best = mob;
            }
        }
        return best;
    }

    private boolean hasAllyTargeting(LivingEntity candidate) {
        if (getWorld().isClient) return false;
        var ownerUuid = getOwnerUuid();
        var matches = getWorld().getEntitiesByClass(LivingItemEntity.class, getBoundingBox().expand(MAX_CHASE_DISTANCE),
                entity -> entity != this && Objects.equals(entity.getOwnerUuid(), ownerUuid)
                        && entity.getTarget() == candidate);
        return !matches.isEmpty();
    }

    private boolean isThreat(net.minecraft.entity.mob.MobEntity mob, PlayerEntity owner, long now) {
        if (mob.getTarget() == owner || mob.getTarget() == this) return true;
        var attacked = ownerAttackMemory.get(mob.getUuid());
        return attacked != null && now - attacked < OWNER_ATTACK_MEMORY;
    }

    private boolean isValidTarget(LivingEntity target, PlayerEntity owner) {
        var now = getWorld().getTime();
        if (squaredDistanceTo(target) > MAX_CHASE_DISTANCE * MAX_CHASE_DISTANCE) return false;
        if (target instanceof net.minecraft.entity.mob.MobEntity mob && (mob.getTarget() == owner || mob.getTarget() == this)) return true;
        var attacked = ownerAttackMemory.get(target.getUuid());
        return attacked != null && now - attacked < OWNER_ATTACK_MEMORY;
    }

    public void onOwnerAttack(Entity target) {
        if (getWorld().isClient || isRemoved()) return;
        if (target == this || target instanceof LivingItemEntity other && Objects.equals(other.getOwnerUuid(), getOwnerUuid()))
            return;
        if (target instanceof LivingEntity living && living.isAlive() && !living.isRemoved()) {
            ownerAttackMemory.put(living.getUuid(), getWorld().getTime());
            if (getTarget() == null && !hasAllyTargeting(living)) setTarget(living);
        }
    }

    private void attemptRightClick(PlayerEntity owner) {
        var stack = getCarriedStack();
        if (stack.isEmpty() || owner == null) return;
        var item = stack.getItem();
        if (owner.getItemCooldownManager().isCoolingDown(item)) return;
        var focus = getTarget() != null ? getTarget() : owner;
        if (isBowLike(stack)) {
            if (focus != owner) bowShot(owner, focus);
            return;
        }
        if (item == Items.WATER_BUCKET || item == Items.LAVA_BUCKET || item == Items.POWDER_SNOW_BUCKET) {
            placeFluidAt(focus, item);
            return;
        }
        if (item instanceof net.minecraft.item.SnowballItem || item instanceof net.minecraft.item.EggItem
                || item instanceof net.minecraft.item.PotionItem || item instanceof net.minecraft.item.ExperienceBottleItem) {
            launchProjectile(owner, focus, stack);
            return;
        }
        if (!canRightClick(stack)) return;
        dataTracker.set(DATA_ROTATE, 10);
        useAsPlayer(owner, stack);
    }

    private void launchProjectile(PlayerEntity owner, LivingEntity focus, ItemStack stack) {
        var position = getPos().add(0, getHeight() * 0.8, 0);
        var direction = focus.getEyePos().subtract(position).normalize();
        if (throwProjectile(owner, stack.copy(), position, direction)) return;
        if (!isInfinite()) stack.decrement(1);
        if (stack.isEmpty()) {
            dataTracker.set(DATA_ITEM, ItemStack.EMPTY);
            revertToItem();
        } else {
            dataTracker.set(DATA_ITEM, stack.copy());
        }
    }

    private boolean throwProjectile(PlayerEntity owner, ItemStack thrown, Vec3d position, Vec3d direction) {
        var item = thrown.getItem();
        if (item instanceof EnderPearlItem) {
            if (owner == null) return true;
            var pearl = new net.minecraft.entity.projectile.thrown.EnderPearlEntity(getWorld(), owner);
            pearl.setPos(position.getX(), position.getY(), position.getZ());
            pearl.setItem(thrown);
            pearl.setVelocity(direction.x, direction.y, direction.z, 1.5F, 1.0F);
            getWorld().spawnEntity(pearl);
            return false;
        }
        net.minecraft.entity.projectile.thrown.ThrownItemEntity projectile = null;
        if (item instanceof net.minecraft.item.SnowballItem)
            projectile = new net.minecraft.entity.projectile.thrown.SnowballEntity(getWorld(), owner);
        else if (item instanceof net.minecraft.item.EggItem)
            projectile = new net.minecraft.entity.projectile.thrown.EggEntity(getWorld(), owner);
        else if (item instanceof net.minecraft.item.ExperienceBottleItem)
            projectile = new net.minecraft.entity.projectile.thrown.ExperienceBottleEntity(getWorld(), owner);
        else if (item instanceof net.minecraft.item.PotionItem) {
            var potion = new net.minecraft.entity.projectile.thrown.PotionEntity(getWorld(), owner);
            potion.setItem(thrown);
            projectile = potion;
        }
        if (projectile != null) {
            if (!(projectile instanceof net.minecraft.entity.projectile.thrown.PotionEntity)) projectile.setItem(thrown);
            if (owner != null) projectile.setOwner(owner);
            projectile.setPos(position.getX(), position.getY(), position.getZ());
            projectile.setVelocity(direction.multiply(1.2));
            getWorld().spawnEntity(projectile);
            return false;
        }
        return true;
    }

    private void placeFluidAt(LivingEntity focus, Item item) {
        var world = getWorld();
        if (world.isClient()) return;
        var pos = focus.getBlockPos();
        if (!world.getBlockState(pos).isReplaceable()) pos = pos.up();
        BlockState state = null;
        if (item == Items.WATER_BUCKET) {
            state = Fluids.WATER.getDefaultState().getBlockState();
        } else if (item == Items.LAVA_BUCKET) {
            state = Fluids.LAVA.getDefaultState().getBlockState();
        } else if (item == Items.POWDER_SNOW_BUCKET) {
            state = Blocks.POWDER_SNOW.getDefaultState();
        }
        if (state != null && world.getBlockState(pos).isReplaceable()) {
            world.setBlockState(pos, state);
            world.playSound(null, pos, SoundEvents.ITEM_BUCKET_EMPTY, net.minecraft.sound.SoundCategory.BLOCKS, 1.0F, 1.0F);
            dataTracker.set(DATA_ITEM, new ItemStack(Items.BUCKET));
        }
    }

    private void useAsPlayer(PlayerEntity owner, ItemStack stack) {
        var slot = owner.getInventory().selectedSlot;
        var previous = owner.getInventory().getStack(slot);
        owner.getInventory().setStack(slot, stack);
        ItemStack result;
        try {
            result = stack.getItem().use(getWorld(), owner, Hand.MAIN_HAND).getValue();
        } finally {
            owner.getInventory().setStack(slot, previous);
        }
        if (result.isEmpty()) {
            if (isInfinite()) return;
            dataTracker.set(DATA_ITEM, ItemStack.EMPTY);
            revertToItem();
            return;
        }
        if (!ItemStack.areItemsEqual(stack, result) || stack.getCount() != result.getCount())
            dataTracker.set(DATA_ITEM, result.copy());
    }

    private static boolean canRightClick(ItemStack stack) {
        return stack.isIn(TAG_RIGHT_CLICK);
    }

    public static boolean isBowLike(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        var animation = stack.getUseAction();
        return animation == net.minecraft.util.UseAction.BOW || animation == net.minecraft.util.UseAction.CROSSBOW;
    }

    private static ItemStack findArrow(PlayerEntity owner) {
        var inventory = owner.getInventory();
        for (var stack : inventory.main) {
            if (isArrow(stack)) return stack;
        }
        for (var stack : inventory.offHand) {
            if (isArrow(stack)) return stack;
        }
        return ItemStack.EMPTY;
    }

    private static boolean isArrow(ItemStack stack) {
        var item = stack.getItem();
        return item == Items.ARROW || item == Items.SPECTRAL_ARROW || item == Items.TIPPED_ARROW;
    }

    private boolean bowShot(PlayerEntity owner, LivingEntity target) {
        var ammo = findArrow(owner);
        if (ammo == null || ammo.isEmpty()) return false;
        var arrowStack = ammo.copy();
        ammo.decrement(1);
        var arrow = ProjectileUtil.createArrowProjectile(owner, arrowStack, 2.5F);
        var y = getY() + getHeight() * 0.8;
        arrow.setPos(getX(), y, getZ());
        var to = new Vec3d(target.getX() - getX(), target.getEyeY() - y, target.getZ() - getZ());
        arrow.setVelocity(to.x, to.y, to.z, 2.2F, 0.8F);
        getWorld().spawnEntity(arrow);
        return true;
    }

    private boolean tryThrowSelf() {
        var stack = getCarriedStack();
        if (stack.isEmpty()) return false;
        var owner = getOwner();
        var aim = getTarget() != null ? getTarget() : owner;
        var position = getPos().add(0, getHeight() * 0.8, 0);
        var direction = aim != null ? aim.getEyePos().subtract(position).normalize() : getRotationVector();
        if (throwProjectile(owner, stack, position, direction)) return false;
        if (owner != null && stack.getCount() > 1) {
            var rest = stack.copy();
            rest.setCount(stack.getCount() - 1);
            owner.getInventory().insertStack(rest);
        }
        playDisappearEffects();
        reverted = true;
        discard();
        return true;
    }

    private void playDisappearEffects() {
        if (getWorld() instanceof ServerWorld serverLevel) {
            serverLevel.playSound(null, getX(), getY(), getZ(), SoundEvents.ENTITY_FIREWORK_ROCKET_LAUNCH, SoundCategory.AMBIENT, 1.0F, 1.0F);
            serverLevel.playSound(null, getX(), getY(), getZ(), SoundEvents.BLOCK_BEACON_DEACTIVATE, SoundCategory.AMBIENT, 1.0F, 1.0F);
            serverLevel.spawnParticles(ParticleTypes.FIREWORK, getX(), getY() + 0.6, getZ(), 24, 0.4, 0.4, 0.4, 0.2);
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
        var deathDrop = owner != null && owner.isDead() && !owner.getWorld().getGameRules().getBoolean(GameRules.KEEP_INVENTORY);
        var added = false;
        if (owner != null && !stack.isEmpty() && !deathDrop) added = owner.getInventory().insertStack(stack);
        if (!stack.isEmpty() && !added) {
            var x = owner != null && !deathDrop ? owner.getX() : getX();
            var y = owner != null && !deathDrop ? owner.getY() : getY();
            var z = owner != null && !deathDrop ? owner.getZ() : getZ();
            if (getWorld() instanceof ServerWorld serverLevel && y < serverLevel.getBottomY() + 5) {
                var spawn = serverLevel.getSpawnPos();
                x = spawn.getX() + 0.5;
                y = spawn.getY() + 1.0;
                z = spawn.getZ() + 0.5;
            }
            var pos = BlockPos.ofFloored(x, y, z);
            while (!getWorld().getBlockState(pos).isReplaceable() && pos.getY() < getWorld().getTopY() - 1)
                pos = pos.up();
            var drop = new LivingItemDrop(getWorld(), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack, owner != null ? owner.getUuid() : null);
            getWorld().spawnEntity(drop);
        }
        if (!getWorld().isClient()) getWorld().getScoreboard().clearPlayerTeam(getUuidAsString());
        discard();
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return net.minecraft.entity.mob.MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.8)
                .add(EntityAttributes.GENERIC_MAX_HEALTH, INFINITE_HEALTH)
                .add(EntityAttributes.GENERIC_ARMOR, 0)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 2.0)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 32)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 1)
                .add(EntityAttributes.GENERIC_ATTACK_KNOCKBACK, 0.5)
                .add(EntityAttributes.GENERIC_FLYING_SPEED, 0.8);
    }
}
