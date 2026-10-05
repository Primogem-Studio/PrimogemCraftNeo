package net.per.primogemcraft.collab.genshincraft;

import com.mojang.serialization.MapCodec;
import net.hackermdch.genshincraft.entity.misc.SpellStub;
import net.hackermdch.genshincraft.interfaces.BypassEntity;
import net.hackermdch.genshincraft.spell.EffectSpec;
import net.hackermdch.genshincraft.spell.PrimitiveInstance;
import net.hackermdch.genshincraft.spell.PrimitiveType;
import net.hackermdch.genshincraft.spell.SpellRuntimeContext;
import net.hackermdch.genshincraft.spell.Stats;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.per.primogemcraft.registry.PGCSpellPrimitiveTypes;

import java.util.Locale;

public final class UtilityPulsePrimitive extends PrimitiveInstance<UtilityPulsePrimitive> {
    public static final MapCodec<UtilityPulsePrimitive> CODEC = StringRepresentable.fromEnum(Action::values)
            .fieldOf("action").xmap(UtilityPulsePrimitive::new, primitive -> primitive.action);

    private final Action action;

    private UtilityPulsePrimitive(Action action) {
        this.action = action;
    }

    @Override
    public void init(SpellRuntimeContext context, SpellStub stub, EffectSpec spec) {
        if (!(stub.level() instanceof ServerLevel level)) return;
        var center = stub.position();
        var size = spec.getStat(Stats.SIZE);
        if (!Double.isFinite(size) || size <= 0) {
            stub.discard();
            return;
        }
        var radius = Math.min(4.0 * size, 16.0);
        var bounds = new AABB(center, center).inflate(radius);
        for (var entity : level.getEntities(stub, bounds, entity -> entity.isAlive()
                && !entity.isSpectator() && !(entity instanceof BypassEntity)
                && entity.distanceToSqr(center) <= radius * radius)) {
            if (action == Action.GATHER) {
                if (entity instanceof ItemEntity item) {
                    var offset = center.subtract(item.position());
                    item.setDeltaMovement(offset.scale(0.6 / Math.max(1.0, offset.length())));
                    item.hasImpulse = true;
                }
                continue;
            }
            if (!(entity instanceof LivingEntity target)) continue;
            if (action == Action.HEAL) {
                target.heal(target.getMaxHealth() * 0.1F);
                continue;
            }
            if (target == context.owner || target.isInvulnerable()
                    || target instanceof Player player && player.getAbilities().invulnerable
                    || context.owner.isAlliedTo(target) || target.isAlliedTo(context.owner)) continue;
            if (context.owner instanceof ServerPlayer caster && target instanceof ServerPlayer player
                    && (!level.getServer().isPvpAllowed() || !caster.canHarmPlayer(player))) continue;
            if (action == Action.SLOW) {
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 3), context.owner);
            } else {
                var direction = target.position().subtract(center).multiply(1, 0, 1);
                if (direction.lengthSqr() < 1.0E-6) {
                    direction = stub.getInitialDirection().multiply(1, 0, 1);
                }
                if (direction.lengthSqr() < 1.0E-6) continue;
                target.knockback(1.2, -direction.x, -direction.z);
                if (target instanceof ServerPlayer player) {
                    player.connection.send(new ClientboundSetEntityMotionPacket(target));
                }
            }
        }
        var particle = switch (action) {
            case HEAL -> ParticleTypes.HAPPY_VILLAGER;
            case GATHER -> ParticleTypes.PORTAL;
            case REPEL -> ParticleTypes.CLOUD;
            case SLOW -> ParticleTypes.SNOWFLAKE;
        };
        level.sendParticles(particle, center.x, center.y, center.z, 24,
                radius * 0.5, radius * 0.25, radius * 0.5, 0.02);
        stub.discard();
    }

    @Override
    public PrimitiveType<UtilityPulsePrimitive> type() {
        return PGCSpellPrimitiveTypes.UTILITY_PULSE.get();
    }

    private enum Action implements StringRepresentable {
        HEAL, GATHER, REPEL, SLOW;

        @Override
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }
}
