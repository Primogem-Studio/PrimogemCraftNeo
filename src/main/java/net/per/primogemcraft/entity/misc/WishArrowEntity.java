package net.per.primogemcraft.entity.misc;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.per.primogemcraft.registry.PGCEntities;
import net.per.primogemcraft.entity.mob.LivingItemEntity;
import net.per.primogemcraft.system.weapon.BowRefinement;
import net.per.primogemcraft.system.weapon.WeaponEnhancement;
import net.per.primogemcraft.system.weapon.WeaponDamage;
import net.per.primogemcraft.item.weapon.SkywardHarpItem;
import net.per.primogemcraft.item.weapon.TheViridescentHuntItem;
import net.per.primogemcraft.util.PGCTimer;
import net.per.primogemcraft.system.element.Element;
import net.per.primogemcraft.system.element.ElementDamage;
import net.per.primogemcraft.system.element.ElementDamageOptions;
import net.per.primogemcraft.system.element.ElementStyle;

import java.util.HashSet;
import java.util.Set;

public final class WishArrowEntity extends Arrow {
    private static final EntityDataAccessor<String> TEXTURE = SynchedEntityData.defineId(WishArrowEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Boolean> EMPOWERED = SynchedEntityData.defineId(WishArrowEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> ANEMO = SynchedEntityData.defineId(WishArrowEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> FIRE_ARROW = SynchedEntityData.defineId(WishArrowEntity.class, EntityDataSerializers.BOOLEAN);
    private static final ResourceLocation DEFAULT_TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/projectiles/arrow.png");
    private double targetRange = BowRefinement.targetRange(1);
    private LivingEntity target;
    private ItemStack weapon = ItemStack.EMPTY;
    private float speed;
    private boolean spectral;
    private byte empowermentPiercing;
    private float empowermentDamageBonus;
    private int splitArrows;
    private int vortexDuration;
    private boolean splitArrow;
    private final Set<Integer> piercedTargets = new HashSet<>();

    public WishArrowEntity(EntityType<? extends WishArrowEntity> type, Level level) {
        super(type, level);
        pickup = Pickup.DISALLOWED;
        setNoGravity(true);
    }

    public WishArrowEntity(ServerLevel level, Player owner, LivingEntity shooter, ItemStack ammunition, ItemStack weapon,
                           float damage, float speed, ResourceLocation texture) {
        this(PGCEntities.WISH_ARROW.get(), level);
        setOwner(owner);
        setPos(shooter.getX(), shooter.getEyeY() - 0.1D, shooter.getZ());
        setPickupItemStack(ammunition.copyWithCount(1));
        this.weapon = weapon.copy();
        entityData.set(ANEMO, weapon.getItem() instanceof SkywardHarpItem);
        setBaseDamage(damage);
        this.speed = speed;
        targetRange = BowRefinement.targetRange(WeaponEnhancement.refinementOf(owner, weapon));
        spectral = ammunition.is(Items.SPECTRAL_ARROW);
        entityData.set(TEXTURE, texture.toString());
        EnchantmentHelper.onProjectileSpawned(level, weapon, this, item -> this.weapon = ItemStack.EMPTY);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(TEXTURE, DEFAULT_TEXTURE.toString());
        builder.define(EMPOWERED, false);
        builder.define(ANEMO, false);
        builder.define(FIRE_ARROW, false);
    }

    public ResourceLocation texture() {
        return ResourceLocation.parse(entityData.get(TEXTURE));
    }

    public boolean isEmpowered() {
        return entityData.get(EMPOWERED);
    }

    public boolean isAnemo() {
        return entityData.get(ANEMO);
    }

    public boolean isFireArrow() {
        return entityData.get(FIRE_ARROW);
    }

    public void makeFireArrow() {
        entityData.set(FIRE_ARROW, true);
        igniteForSeconds(5.0F);
    }

    public void configureSkywardHarp(int splits, int duration) {
        splitArrows = splits;
        vortexDuration = duration;
    }

    public void empower(int piercing, float damageBonus) {
        entityData.set(EMPOWERED, true);
        empowermentPiercing = (byte) Math.clamp(piercing, 1, 127);
        empowermentDamageBonus = damageBonus;
        igniteForSeconds(5.0F);
    }

    @Override
    public byte getPierceLevel() {
        return (byte) Math.max(super.getPierceLevel(), empowermentPiercing);
    }

    @Override
    public ItemStack getWeaponItem() {
        return weapon;
    }

    @Override
    public void tick() {
        if (!level().isClientSide()) {
            if (!(getOwner() instanceof Player owner) || !owner.isAlive() || owner.isSpectator()
                    || tickCount >= 100 || inGround) {
                discard();
                return;
            }
            if (target != null && (piercedTargets.contains(target.getId()) || !canTarget(owner, target)
                    || target.getBoundingBox().getCenter().distanceToSqr(position()) > targetRange * targetRange)) target = null;
            if (target == null && tickCount % 5 == 0) target = findTarget(owner);
            var direction = getDeltaMovement().normalize();
            if (target != null && hasLineOfSight(target)) {
                var desired = target.getBoundingBox().getCenter().subtract(position()).normalize();
                direction = direction.lerp(desired, 0.3D).normalize();
            }
            setDeltaMovement(direction.scale(speed));
            hasImpulse = true;
            if (splitArrows > 0) {
                for (var index = 0; index < splitArrows; index++) {
                    var arrow = new WishArrowEntity((ServerLevel) level(), owner, owner, getPickupItem(), weapon,
                            (float) getBaseDamage(), speed, texture());
                    arrow.splitArrow = true;
                    arrow.setPos(position());
                    var spread = (index - (splitArrows - 1) / 2.0D) * 12.0D;
                    if (splitArrows == 1) spread = owner.getRandom().nextBoolean() ? 12.0D : -12.0D;
                    var movement = getDeltaMovement().yRot((float) Math.toRadians(spread));
                    arrow.shoot(movement.x, movement.y, movement.z, speed, 0.0F);
                    level().addFreshEntity(arrow);
                }
                splitArrows = 0;
            }
        }
        super.tick();
        if (inGround) discard();
    }

    private boolean hasLineOfSight(LivingEntity candidate) {
        return level().clip(new ClipContext(position(), candidate.getBoundingBox().getCenter(),
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this)).getType() == HitResult.Type.MISS;
    }

    private LivingEntity findTarget(Player owner) {
        LivingEntity chosen = null;
        var bestScore = Double.NEGATIVE_INFINITY;
        for (var candidate : level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(targetRange))) {
            if (piercedTargets.contains(candidate.getId()) || !canTarget(owner, candidate) || !hasLineOfSight(candidate)) continue;
            var offset = candidate.getBoundingBox().getCenter().subtract(position());
            var direction = getDeltaMovement().lengthSqr() > 0.0D ? getDeltaMovement().normalize() : owner.getLookAngle();
            var alignment = direction.dot(offset.normalize());
            if (alignment < 0.8D || offset.lengthSqr() > targetRange * targetRange) continue;
            var score = alignment - offset.length() / (targetRange * 10.0D);
            if (score > bestScore) {
                bestScore = score;
                chosen = candidate;
            }
        }
        return chosen;
    }

    public static boolean canTarget(Player owner, LivingEntity candidate) {
        return candidate != owner && candidate.isAlive() && !candidate.isSpectator() && candidate.isPickable()
                && !candidate.isInvulnerable() && !owner.isAlliedTo(candidate)
                && !(candidate instanceof TamableAnimal tame && tame.isOwnedBy(owner))
                && !(candidate instanceof LivingItemEntity item && owner.getUUID().equals(item.getOwnerUuid()))
                && (!(candidate instanceof Player player) || (!player.isCreative() && owner.canHarmPlayer(player)));
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        return !piercedTargets.contains(entity.getId()) && super.canHitEntity(entity) && (!(entity instanceof LivingEntity living)
                || getOwner() instanceof Player owner && canTarget(owner, living));
    }

    @Override
    protected void onHitEntity(EntityHitResult hit) {
        if (!level().isClientSide()) {
            var victim = hit.getEntity();
            if (!piercedTargets.add(victim.getId())) return;
            spawnVortex(hit.getLocation());
            var owner = getOwner();
            var source = damageSources().arrow(this, owner == null ? this : owner);
            if (owner instanceof LivingEntity living) living.setLastHurtMob(victim);
            var fireTicks = victim.getRemainingFireTicks();
            if (isEmpowered() || isFireArrow())
                source = ElementDamage.of(Element.PYRO, source, ElementStyle.NORMAL, ElementDamageOptions.REACTING);
            else if (isAnemo())
                source = ElementDamage.of(Element.ANEMO, source, ElementStyle.NORMAL, ElementDamageOptions.REACTING);
            if (isOnFire() && !isEmpowered() && !isFireArrow() && victim.getType() != EntityType.ENDERMAN)
                victim.igniteForSeconds(5.0F);
            var finalDamage = EnchantmentHelper.modifyDamage((ServerLevel) level(), weapon, victim, source, (float) getBaseDamage());
            if (splitArrow) finalDamage *= 0.5F;
            if (isEmpowered()) finalDamage *= 1.0F + empowermentDamageBonus;
            var hurt = victim instanceof LivingEntity living
                    ? WeaponDamage.extraHit(living, source, finalDamage) : victim.hurt(source, finalDamage);
            if (hurt) {
                if (isEmpowered()) victim.igniteForSeconds(5.0F);
                if (victim instanceof LivingEntity living) {
                    if (weapon.getItem() instanceof TheViridescentHuntItem && owner instanceof Player player)
                        TheViridescentHuntItem.trySpawnCyclone(player, weapon, living.getBoundingBox().getCenter());
                    var knockback = EnchantmentHelper.modifyKnockback((ServerLevel) level(), weapon, living, source, 0.0F);
                    var resistance = Math.max(0.0D, 1.0D - living.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
                    var push = getDeltaMovement().multiply(1.0D, 0.0D, 1.0D).normalize().scale(knockback * 0.6D * resistance);
                    if (push.lengthSqr() > 0.0D) living.push(push.x, 0.1D, push.z);
                    EnchantmentHelper.doPostAttackEffectsWithItemSource((ServerLevel) level(), living, source, weapon);
                    doPostHurtEffects(living);
                    if (spectral) living.addEffect(new MobEffectInstance(MobEffects.GLOWING, 200), owner);
                    if (!splitArrow && weapon.getItem() instanceof SkywardHarpItem && owner instanceof Player player) {
                        var refinement = WeaponEnhancement.refinementOf(player, weapon);
                        if (SkywardHarpItem.triggers(refinement, player)) {
                            PGCTimer.set(player, SkywardHarpItem.COOLDOWN, Math.max(40, 80 - 10 * (refinement - 1)));
                            var areaDamage = (float) (player.getAttributeValue(Attributes.ATTACK_DAMAGE) * 1.25D);
                            for (var nearby : level().getEntitiesOfClass(LivingEntity.class, victim.getBoundingBox().inflate(2.0D)))
                                if (nearby != victim && canTarget(player, nearby))
                                    WeaponDamage.extraHit(nearby, source, areaDamage);
                        }
                    }
                }
            } else {
                victim.setRemainingFireTicks(fireTicks);
            }
        }
        target = null;
        if (piercedTargets.size() >= Math.max(1, getPierceLevel())) discard();
    }

    @Override
    protected void onHitBlock(BlockHitResult hit) {
        spawnVortex(hit.getLocation());
        if (level() instanceof ServerLevel serverLevel) hitBlockEnchantmentEffects(serverLevel, hit, weapon);
        discard();
    }

    private void spawnVortex(Vec3 location) {
        if (vortexDuration > 0 && level() instanceof ServerLevel serverLevel && getOwner() instanceof Player owner) {
            var refinement = WeaponEnhancement.refinementOf(owner, weapon);
            SkywardHarpVortexEntity.spawn(serverLevel, owner, location, net.per.primogemcraft.system.weapon.SkywardHarpStats.vortexRadius(refinement), vortexDuration);
            vortexDuration = 0;
        }
    }
}
