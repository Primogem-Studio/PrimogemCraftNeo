package net.per.primogemcraft.system.weapon;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.per.primogemcraft.system.element.Element;
import net.per.primogemcraft.system.element.ElementDamage;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

/**
 * Tracks one thrust dash: it draws the piecewise glow trail from where the dash started to wherever
 * the player ends up, and deals the thrust's contact damage to every creature passed on the way.
 * Each creature is damaged at most once per dash.
 */
@EventBusSubscriber(modid = MOD_ID)
public final class ThrustDash {
    private static final Map<UUID, Dash> ACTIVE = new HashMap<>();
    private static final double STEP = 0.35D;
    private static final double HEIGHT = 1.0D;
    private static final double STOP_DISTANCE_SQUARED = 0.0025D;
    private static final double CONTACT_RADIUS = 1.5D;
    private static final int MAX_TICKS = 20;

    private ThrustDash() {
    }

    public static void start(ServerPlayer player, float damage) {
        ACTIVE.put(player.getUUID(), new Dash(player.position(), player.tickCount, damage, new HashSet<>()));
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        var dash = ACTIVE.get(player.getUUID());
        if (dash == null) return;
        var level = player.serverLevel();
        var current = player.position();
        line(level, dash.from(), current);
        strike(level, player, dash, dash.from(), current);
        if (current.distanceToSqr(dash.from()) < STOP_DISTANCE_SQUARED || player.tickCount - dash.startedAt() >= MAX_TICKS) {
            ACTIVE.remove(player.getUUID());
            return;
        }
        ACTIVE.put(player.getUUID(), new Dash(current, dash.startedAt(), dash.damage(), dash.struck()));
    }

    private static void line(ServerLevel level, Vec3 from, Vec3 to) {
        var steps = Math.max(1, (int) Math.ceil(from.distanceTo(to) / STEP));
        for (var index = 0; index <= steps; index++) point(level, from.lerp(to, (double) index / steps));
    }

    private static void point(ServerLevel level, Vec3 position) {
        level.sendParticles(ParticleTypes.GLOW, position.x, position.y + HEIGHT, position.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
    }

    private static void strike(ServerLevel level, ServerPlayer player, Dash dash, Vec3 from, Vec3 to) {
        for (var candidate : level.getEntitiesOfClass(LivingEntity.class, new AABB(from, to).inflate(CONTACT_RADIUS))) {
            if (candidate == player || !dash.struck().add(candidate.getUUID())) continue;
            WeaponDamage.extraHit(candidate, ElementDamage.of(Element.ANEMO, level.damageSources().sonicBoom(player)), dash.damage());
        }
    }

    private record Dash(Vec3 from, int startedAt, float damage, Set<UUID> struck) {
    }
}
