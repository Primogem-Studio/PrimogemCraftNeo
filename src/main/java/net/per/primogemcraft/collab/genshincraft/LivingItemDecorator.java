package net.per.primogemcraft.collab.genshincraft;

import com.mojang.serialization.MapCodec;
import net.hackermdch.genshincraft.entity.misc.SpellStub;
import net.hackermdch.genshincraft.spell.DecoratorInstance;
import net.hackermdch.genshincraft.spell.DecoratorType;
import net.hackermdch.genshincraft.spell.EffectSpec;
import net.hackermdch.genshincraft.spell.SpellBehavior;
import net.hackermdch.genshincraft.spell.SpellRuntimeContext;
import net.hackermdch.genshincraft.spell.Stats;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.per.primogemcraft.entity.mob.LivingItemEntity;
import net.per.primogemcraft.registry.PGCSpellDecoratorTypes;
import net.per.primogemcraft.system.living.LivingItemFormation;

import java.util.Locale;

public final class LivingItemDecorator extends DecoratorInstance<LivingItemDecorator> {
    public static final MapCodec<LivingItemDecorator> CODEC = StringRepresentable.fromEnum(Action::values)
            .fieldOf("action").xmap(LivingItemDecorator::new, decorator -> decorator.action);

    private final Action action;

    private LivingItemDecorator(Action action) {
        this.action = action;
    }

    @Override
    public void distribute(SpellRuntimeContext context, SpellStub stub, EffectSpec spec, int index, int count) {
        if (spec.behaviors().stream().noneMatch(Slot.class::isInstance)) spec.addBehavior(new Slot(index, count));
    }

    @Override
    public void afterInit(SpellRuntimeContext context, SpellStub stub, EffectSpec spec) {
        if (!(context.owner instanceof ServerPlayer owner) || stub.isRemoved()) return;
        var targets = spec.behaviors().stream().filter(LivingItemPrimitive.Targets.class::isInstance)
                .map(LivingItemPrimitive.Targets.class::cast).findFirst().orElse(null);
        if (targets == null || targets.items().isEmpty()) return;
        var slot = spec.behaviors().stream().filter(Slot.class::isInstance).map(Slot.class::cast)
                .findFirst().orElse(new Slot(0, 1));
        var size = spec.getStat(Stats.SIZE);
        if (!Double.isFinite(size) || size <= 0) return;
        var hit = action == Action.MOVE || action == Action.ATTACK ? aim(owner, stub, Math.min(16 * size, 64)) : null;
        if (action == Action.ATTACK && (!(hit instanceof EntityHitResult entityHit)
                || !(entityHit.getEntity() instanceof LivingEntity target) || !LivingItemEntity.canCommandAttack(owner, target))) return;
        var spacing = Math.clamp(1.5 * size, 0.5, 3);
        var items = targets.items();
        for (var index = 0; index < items.size(); index++) {
            var item = items.get(index);
            if (!item.isAlive() || item.isRemoved() || item.level() != owner.level()
                    || !owner.getUUID().equals(item.getOwnerUuid())) continue;
            var formationIndex = targets.created() ? slot.index() : index;
            var formationCount = targets.created() ? slot.count() : items.size();
            switch (action) {
                case RING, LINE, WEDGE -> item.commandFormation(LivingItemFormation.valueOf(action.name()), formationIndex, formationCount, spacing);
                case MOVE -> item.commandMove(hit.getLocation().add(0, 1.5, 0)
                        .add(LivingItemFormation.RING.offset(formationIndex, formationCount, spacing, stub.getInitialDirection())));
                case ATTACK -> item.commandAttack((LivingEntity) ((EntityHitResult) hit).getEntity());
                case FOLLOW -> item.clearCommand();
                case RECALL -> {
                    item.clearCommand();
                    item.teleportTo(owner.getX(), owner.getY() + 2, owner.getZ());
                }
                case RETURN -> item.revertByOwner();
            }
        }
        owner.serverLevel().sendParticles(ParticleTypes.ENCHANT,
                stub.getX(), stub.getY(), stub.getZ(), 16, 0.5, 0.5, 0.5, 0.1);
    }

    private static HitResult aim(ServerPlayer owner, SpellStub stub, double range) {
        var trigger = stub.getTriggerHit();
        if (trigger != null && trigger.getType() != HitResult.Type.MISS) return trigger;
        var start = stub.position();
        var direction = stub.getInitialDirection();
        if (direction.lengthSqr() < 1.0E-6) direction = owner.getLookAngle();
        var end = start.add(direction.normalize().scale(range));
        HitResult hit = owner.level().clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, owner));
        var nearest = start.distanceToSqr(hit.getLocation());
        for (var target : owner.level().getEntitiesOfClass(LivingEntity.class, new AABB(start, end).inflate(1),
                target -> LivingItemEntity.canCommandAttack(owner, target))) {
            var point = target.getBoundingBox().inflate(0.3).clip(start, end);
            if (point.isPresent() && start.distanceToSqr(point.get()) < nearest) {
                nearest = start.distanceToSqr(point.get());
                hit = new EntityHitResult(target, point.get());
            }
        }
        return hit;
    }

    @Override
    public DecoratorType<LivingItemDecorator> type() {
        return PGCSpellDecoratorTypes.LIVING_ITEM.get();
    }

    private record Slot(int index, int count) implements SpellBehavior {
    }

    private enum Action implements StringRepresentable {
        RING, LINE, WEDGE, MOVE, ATTACK, FOLLOW, RECALL, RETURN;

        @Override
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }
}
