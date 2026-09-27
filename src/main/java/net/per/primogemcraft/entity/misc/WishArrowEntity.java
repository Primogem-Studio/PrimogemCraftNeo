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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.per.primogemcraft.registry.PGCEntities;

public final class WishArrowEntity extends Arrow {
    private static final EntityDataAccessor<String> TEXTURE = SynchedEntityData.defineId(WishArrowEntity.class, EntityDataSerializers.STRING);
    private static final ResourceLocation DEFAULT_TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/projectiles/arrow.png");
    private static final double TARGET_RANGE = 8.0D;
    private LivingEntity target;
    private ItemStack weapon = ItemStack.EMPTY;
    private float damage;
    private float speed;
    private boolean spectral;

    public WishArrowEntity(EntityType<? extends WishArrowEntity> type, Level level) {
        super(type, level);
        pickup = Pickup.DISALLOWED;
        setNoGravity(true);
    }

    public WishArrowEntity(ServerLevel level, Player owner, ItemStack ammunition, ItemStack weapon,
                           float damage, float speed, ResourceLocation texture) {
        this(PGCEntities.WISH_ARROW.get(), level);
        setOwner(owner);
        setPos(owner.getX(), owner.getEyeY() - 0.1D, owner.getZ());
        setPickupItemStack(ammunition.copyWithCount(1));
        this.weapon = weapon.copy();
        this.damage = damage;
        this.speed = speed;
        spectral = ammunition.is(Items.SPECTRAL_ARROW);
        entityData.set(TEXTURE, texture.toString());
        target = findTarget(owner);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(TEXTURE, DEFAULT_TEXTURE.toString());
    }

    public ResourceLocation texture() {
        return ResourceLocation.parse(entityData.get(TEXTURE));
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
            if (target != null && (!canTarget(owner, target)
                    || target.getBoundingBox().getCenter().distanceToSqr(position()) > TARGET_RANGE * TARGET_RANGE)) target = null;
            if (target == null && tickCount % 5 == 0) target = findTarget(owner);
            var direction = getDeltaMovement().normalize();
            if (target != null && hasLineOfSight(target)) {
                var desired = target.getBoundingBox().getCenter().subtract(position()).normalize();
                direction = direction.lerp(desired, 0.3D).normalize();
            }
            setDeltaMovement(direction.scale(speed));
            hasImpulse = true;
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
        for (var candidate : level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(TARGET_RANGE))) {
            if (!canTarget(owner, candidate) || !hasLineOfSight(candidate)) continue;
            var offset = candidate.getBoundingBox().getCenter().subtract(position());
            var alignment = owner.getLookAngle().dot(offset.normalize());
            if (alignment < 0.8D || offset.lengthSqr() > TARGET_RANGE * TARGET_RANGE) continue;
            var score = alignment - offset.length() / (TARGET_RANGE * 10.0D);
            if (score > bestScore) {
                bestScore = score;
                chosen = candidate;
            }
        }
        return chosen;
    }

    private static boolean canTarget(Player owner, LivingEntity candidate) {
        return candidate != owner && candidate.isAlive() && !candidate.isSpectator() && candidate.isPickable()
                && !candidate.isInvulnerable() && !owner.isAlliedTo(candidate)
                && !(candidate instanceof TamableAnimal tame && tame.isOwnedBy(owner))
                && (!(candidate instanceof Player player) || (!player.isCreative() && owner.canHarmPlayer(player)));
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        return super.canHitEntity(entity) && (!(entity instanceof LivingEntity living)
                || getOwner() instanceof Player owner && canTarget(owner, living));
    }

    @Override
    protected void onHitEntity(EntityHitResult hit) {
        if (!level().isClientSide()) {
            var victim = hit.getEntity();
            var owner = getOwner();
            var source = damageSources().arrow(this, owner == null ? this : owner);
            if (owner instanceof LivingEntity living) living.setLastHurtMob(victim);
            if (victim.hurt(source, damage) && victim instanceof LivingEntity living) {
                doPostHurtEffects(living);
                if (spectral) living.addEffect(new MobEffectInstance(MobEffects.GLOWING, 200), owner);
            }
        }
        discard();
    }

    @Override
    protected void onHitBlock(BlockHitResult hit) {
        discard();
    }
}
