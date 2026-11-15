package net.per.primogemcraft.system.weapon;

import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
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
public final class ThrustDash {
    private static final Map<UUID, Dash> ACTIVE = new HashMap<>();
    private static final double STEP = 0.35D;
    private static final double HEIGHT = 1.0D;
    private static final double STOP_DISTANCE_SQUARED = 0.0025D;
    private static final double CONTACT_RADIUS = 1.5D;
    private static final int MAX_TICKS = 20;

    private ThrustDash() {
    }

    public static void start(ServerPlayerEntity player, float damage) {
        ACTIVE.put(player.getUuid(), new Dash(player.getPos(), player.age, damage, new HashSet<>()));
    }

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (var player : server.getPlayerManager().getPlayerList()) {
                var dash = ACTIVE.get(player.getUuid());
                if (dash == null) continue;
                var level = player.getServerWorld();
                var current = player.getPos();
                line(level, dash.from(), current);
                strike(level, player, dash, dash.from(), current);
                if (current.squaredDistanceTo(dash.from()) < STOP_DISTANCE_SQUARED || player.age - dash.startedAt() >= MAX_TICKS) {
                    ACTIVE.remove(player.getUuid());
                    continue;
                }
                ACTIVE.put(player.getUuid(), new Dash(current, dash.startedAt(), dash.damage(), dash.struck()));
            }
        });
    }

    private static void line(ServerWorld level, Vec3d from, Vec3d to) {
        var steps = Math.max(1, (int) Math.ceil(from.distanceTo(to) / STEP));
        for (var index = 0; index <= steps; index++) point(level, from.lerp(to, (double) index / steps));
    }

    private static void point(ServerWorld level, Vec3d position) {
        level.spawnParticles(ParticleTypes.GLOW, position.x, position.y + HEIGHT, position.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
    }

    private static void strike(ServerWorld level, ServerPlayerEntity player, Dash dash, Vec3d from, Vec3d to) {
        for (var candidate : level.getEntitiesByClass(LivingEntity.class, new Box(from, to).expand(CONTACT_RADIUS), e -> true)) {
            if (candidate == player || !dash.struck().add(candidate.getUuid())) continue;
            WeaponDamage.extraHit(candidate, ElementDamage.of(Element.ANEMO, level.getDamageSources().sonicBoom(player)), dash.damage());
        }
    }

    private record Dash(Vec3d from, int startedAt, float damage, Set<UUID> struck) {
    }
}
