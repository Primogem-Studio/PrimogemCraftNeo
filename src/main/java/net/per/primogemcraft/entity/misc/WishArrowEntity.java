package net.per.primogemcraft.entity.misc;

import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.util.Identifier;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.per.primogemcraft.compat.item.Enchantments120;
import net.per.primogemcraft.entity.mob.LivingItemEntity;
import net.per.primogemcraft.item.weapon.SkywardHarpItem;
import net.per.primogemcraft.item.weapon.TheViridescentHuntItem;
import net.per.primogemcraft.registry.PGCEntities;
import net.per.primogemcraft.system.element.Element;
import net.per.primogemcraft.system.element.ElementDamage;
import net.per.primogemcraft.system.element.ElementDamageOptions;
import net.per.primogemcraft.system.element.ElementStyle;
import net.per.primogemcraft.system.weapon.BowRefinement;
import net.per.primogemcraft.system.weapon.SkywardHarpStats;
import net.per.primogemcraft.system.weapon.WeaponDamage;
import net.per.primogemcraft.system.weapon.WeaponEnhancement;
import net.per.primogemcraft.util.PGCTimer;

import java.util.HashSet;
import java.util.Set;

public final class WishArrowEntity extends ArrowEntity {
    private static final TrackedData<String> TEXTURE = DataTracker.registerData(WishArrowEntity.class, TrackedDataHandlerRegistry.STRING);
    private static final TrackedData<Boolean> EMPOWERED = DataTracker.registerData(WishArrowEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> ANEMO = DataTracker.registerData(WishArrowEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> FIRE_ARROW = DataTracker.registerData(WishArrowEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final Identifier DEFAULT_TEXTURE = new Identifier("minecraft", "textures/entity/projectiles/arrow.png");

    private double targetRange = BowRefinement.targetRange(1);
    private LivingEntity target;
    private ItemStack weapon = ItemStack.EMPTY;
    private ItemStack ammunition = ItemStack.EMPTY;
    private float speed;
    private boolean spectral;
    private byte empowermentPiercing;
    private float empowermentDamageBonus;
    private int splitArrows;
    private int vortexDuration;
    private boolean splitArrow;
    private final Set<Integer> piercedTargets = new HashSet<>();

    public WishArrowEntity(EntityType<? extends WishArrowEntity> type, World level) {
        super(type, level);
        pickupType = PersistentProjectileEntity.PickupPermission.DISALLOWED;
        setNoGravity(true);
    }

    public WishArrowEntity(ServerWorld level, PlayerEntity owner, LivingEntity shooter, ItemStack ammunition, ItemStack weapon,
                           float damage, float speed, Identifier texture) {
        this(PGCEntities.WISH_ARROW.get(), level);
        setOwner(owner);
        setPos(shooter.getX(), shooter.getEyeY() - 0.1D, shooter.getZ());
        this.ammunition = ammunition.copyWithCount(1);
        this.weapon = weapon.copy();
        dataTracker.set(ANEMO, weapon.getItem() instanceof SkywardHarpItem);
        setDamage(damage);
        this.speed = speed;
        targetRange = BowRefinement.targetRange(WeaponEnhancement.refinementOf(owner, weapon));
        spectral = ammunition.isOf(Items.SPECTRAL_ARROW);
        dataTracker.set(TEXTURE, texture.toString());
        initFromStack(ammunition);
        var piercing = Enchantments120.level(weapon, Enchantments.PIERCING);
        if (piercing > 0) setPierceLevel((byte) piercing);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        dataTracker.startTracking(TEXTURE, DEFAULT_TEXTURE.toString());
        dataTracker.startTracking(EMPOWERED, false);
        dataTracker.startTracking(ANEMO, false);
        dataTracker.startTracking(FIRE_ARROW, false);
    }

    public Identifier texture() {
        return new Identifier(dataTracker.get(TEXTURE));
    }

    public boolean isEmpowered() {
        return dataTracker.get(EMPOWERED);
    }

    public boolean isAnemo() {
        return dataTracker.get(ANEMO);
    }

    public boolean isFireArrow() {
        return dataTracker.get(FIRE_ARROW);
    }

    public void makeFireArrow() {
        dataTracker.set(FIRE_ARROW, true);
        setOnFireFor(5);
    }

    public void configureSkywardHarp(int splits, int duration) {
        splitArrows = splits;
        vortexDuration = duration;
    }

    public void empower(int piercing, float damageBonus) {
        dataTracker.set(EMPOWERED, true);
        empowermentPiercing = (byte) Math.max(1, Math.min(127, piercing));
        empowermentDamageBonus = damageBonus;
    }

    @Override
    public byte getPierceLevel() {
        return (byte) Math.max(super.getPierceLevel(), empowermentPiercing);
    }

    public ItemStack getWeaponItem() {
        return weapon;
    }

    @Override
    protected ItemStack asItemStack() {
        return ammunition.copy();
    }

    @Override
    public void tick() {
        if (!getWorld().isClient()) {
            if (!(getOwner() instanceof PlayerEntity owner) || !owner.isAlive() || owner.isSpectator()
                    || age >= 100 || inGround) {
                discard();
                return;
            }
            if (target != null && (piercedTargets.contains(target.getId()) || !canTarget(owner, target)
                    || target.getBoundingBox().getCenter().squaredDistanceTo(getPos()) > targetRange * targetRange)) target = null;
            if (target == null && age % 5 == 0) target = findTarget(owner);
            var direction = getVelocity().normalize();
            if (target != null && hasLineOfSight(target)) {
                var desired = target.getBoundingBox().getCenter().subtract(getPos()).normalize();
                direction = direction.lerp(desired, 0.3D).normalize();
            }
            setVelocity(direction.multiply(speed));
            velocityDirty = true;
            if (splitArrows > 0) {
                for (var index = 0; index < splitArrows; index++) {
                    var arrow = new WishArrowEntity((ServerWorld) getWorld(), owner, owner, ammunition, weapon,
                            (float) getDamage(), speed, texture());
                    arrow.splitArrow = true;
                    arrow.setPos(getX(), getY(), getZ());
                    var spread = (index - (splitArrows - 1) / 2.0D) * 12.0D;
                    if (splitArrows == 1) spread = owner.getRandom().nextBoolean() ? 12.0D : -12.0D;
                    var movement = getVelocity().rotateY((float) Math.toRadians(spread));
                    arrow.setVelocity(movement.x, movement.y, movement.z, speed, 0.0F);
                    getWorld().spawnEntity(arrow);
                }
                splitArrows = 0;
            }
        }
        super.tick();
        if (inGround) discard();
    }

    private boolean hasLineOfSight(LivingEntity candidate) {
        return getWorld().raycast(new RaycastContext(getPos(), candidate.getBoundingBox().getCenter(),
                RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, this)).getType() == HitResult.Type.MISS;
    }

    private LivingEntity findTarget(PlayerEntity owner) {
        LivingEntity chosen = null;
        var bestScore = Double.NEGATIVE_INFINITY;
        for (var candidate : getWorld().getEntitiesByClass(LivingEntity.class, getBoundingBox().expand(targetRange), e -> true)) {
            if (piercedTargets.contains(candidate.getId()) || !canTarget(owner, candidate) || !hasLineOfSight(candidate)) continue;
            var offset = candidate.getBoundingBox().getCenter().subtract(getPos());
            var direction = getVelocity().lengthSquared() > 0.0D ? getVelocity().normalize() : owner.getRotationVector();
            var alignment = direction.dotProduct(offset.normalize());
            if (alignment < 0.8D || offset.lengthSquared() > targetRange * targetRange) continue;
            var score = alignment - offset.length() / (targetRange * 10.0D);
            if (score > bestScore) {
                bestScore = score;
                chosen = candidate;
            }
        }
        return chosen;
    }

    public static boolean canTarget(PlayerEntity owner, LivingEntity candidate) {
        return candidate != owner && candidate.isAlive() && !candidate.isSpectator() && candidate.canHit()
                && !candidate.isInvulnerable() && !owner.isTeammate(candidate)
                && !(candidate instanceof TameableEntity tame && tame.isOwner(owner))
                && !(candidate instanceof LivingItemEntity item && owner.getUuid().equals(item.getOwnerUuid()))
                && (!(candidate instanceof PlayerEntity player) || (!player.isCreative() && owner.canTarget(player)));
    }

    @Override
    protected boolean canHit(Entity entity) {
        return !piercedTargets.contains(entity.getId()) && super.canHit(entity) && (!(entity instanceof LivingEntity living)
                || getOwner() instanceof PlayerEntity owner && canTarget(owner, living));
    }

    @Override
    protected void onEntityHit(EntityHitResult hit) {
        if (!getWorld().isClient()) {
            var victim = hit.getEntity();
            if (!piercedTargets.add(victim.getId())) return;
            spawnVortex(hit.getPos());
            var owner = getOwner();
            var source = getDamageSources().arrow(this, owner == null ? this : owner);
            if (owner instanceof LivingEntity living && victim instanceof LivingEntity target) living.setAttacker(target);
            var fireTicks = victim.getFireTicks();
            if (isEmpowered() || isFireArrow())
                source = ElementDamage.of(Element.PYRO, source, ElementStyle.NORMAL, ElementDamageOptions.REACTING);
            else if (isAnemo())
                source = ElementDamage.of(Element.ANEMO, source, ElementStyle.NORMAL, ElementDamageOptions.REACTING);
            if (isOnFire() && !isEmpowered() && !isFireArrow() && victim.getType() != EntityType.ENDERMAN)
                victim.setOnFireFor(5);
            var finalDamage = (float) getDamage();
            if (victim instanceof LivingEntity living)
                finalDamage += EnchantmentHelper.getAttackDamage(weapon, living.getGroup());
            if (splitArrow) finalDamage *= 0.5F;
            if (isEmpowered()) finalDamage *= 1.0F + empowermentDamageBonus;
            boolean hurt;
            if (victim instanceof LivingEntity living) {
                WeaponDamage.extraHit(living, source, finalDamage);
                hurt = true;
            } else {
                hurt = victim.damage(source, finalDamage);
            }
            if (hurt) {
                if (isEmpowered()) victim.setOnFireFor(5);
                if (victim instanceof LivingEntity living) {
                    if (weapon.getItem() instanceof TheViridescentHuntItem && owner instanceof PlayerEntity player)
                        TheViridescentHuntItem.trySpawnCyclone(player, weapon, living.getBoundingBox().getCenter());
                    var knockback = Enchantments120.level(weapon, Enchantments.PUNCH);
                    var resistance = Math.max(0.0D, 1.0D - living.getAttributeValue(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE));
                    var push = getVelocity().multiply(1.0D, 0.0D, 1.0D).normalize().multiply(knockback * 0.6D * resistance);
                    if (push.lengthSquared() > 0.0D) living.addVelocity(push.x, 0.1D, push.z);
                    if (owner instanceof LivingEntity attacker) {
                        EnchantmentHelper.onTargetDamaged(attacker, living);
                        EnchantmentHelper.onTargetDamaged(living, attacker);
                    }
                    applyDamageEffects(living, this);
                    if (spectral) living.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 200), owner);
                    if (!splitArrow && weapon.getItem() instanceof SkywardHarpItem && owner instanceof PlayerEntity player) {
                        var refinement = WeaponEnhancement.refinementOf(player, weapon);
                        if (SkywardHarpItem.triggers(refinement, player)) {
                            PGCTimer.set(player, SkywardHarpItem.COOLDOWN, Math.max(40, 80 - 10 * (refinement - 1)));
                            var areaDamage = (float) (player.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE) * 1.25D);
                            for (var nearby : getWorld().getEntitiesByClass(LivingEntity.class, victim.getBoundingBox().expand(2.0D), e -> true))
                                if (nearby != victim && canTarget(player, nearby))
                                    WeaponDamage.extraHit(nearby, source, areaDamage);
                        }
                    }
                }
            } else {
                victim.setFireTicks(fireTicks);
            }
        }
        target = null;
        if (piercedTargets.size() >= Math.max(1, getPierceLevel())) discard();
    }

    @Override
    protected void onBlockHit(BlockHitResult hit) {
        spawnVortex(hit.getPos());
        super.onBlockHit(hit);
        discard();
    }

    private void spawnVortex(Vec3d location) {
        if (vortexDuration > 0 && getWorld() instanceof ServerWorld serverLevel && getOwner() instanceof PlayerEntity owner) {
            var refinement = WeaponEnhancement.refinementOf(owner, weapon);
            SkywardHarpVortexEntity.spawn(serverLevel, owner, location, SkywardHarpStats.vortexRadius(refinement), vortexDuration);
            vortexDuration = 0;
        }
    }
}
