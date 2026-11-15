package net.per.primogemcraft.system.event;

import net.minecraft.util.math.BlockPos;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.util.math.Vec3d;
import net.per.primogemcraft.config.PGCConfig;

import java.util.*;
import java.util.function.Consumer;

public final class EventCombat {
    public static final int LOOT_OPTIONS = 3;

    private static final double SPAWN_RADIUS = 10.0D;
    private static final int SPAWN_ATTEMPTS = 20;
    private static final int VERTICAL_SCAN = 8;
    private static final int NO_CHALLENGE = -1;
    private static final String TARGET_KILLED_KEY = "message.primogemcraft.event.target_killed";
    private static final String CHALLENGE_TIMEOUT_KEY = "message.primogemcraft.event.challenge_timeout";

    private static final Map<UUID, Challenge> CHALLENGES = new HashMap<>();
    private static final Map<UUID, LootDrop> LOOT_DROPS = new HashMap<>();

    private EventCombat() {
    }

    public static boolean summon(EventContext context, EntityType<?> type, int count, Consumer<LivingEntity> modifier) {
        return spawn(context, type, count, modifier, false, null, NO_CHALLENGE, null);
    }

    public static boolean guard(EventContext context, EntityType<?> type, int count, Consumer<LivingEntity> modifier) {
        return spawn(context, type, count, modifier, true, null, NO_CHALLENGE, null);
    }

    public static boolean challenge(EventContext context, EntityType<?> type, int count, int required, Consumer<LivingEntity> modifier, Consumer<EventContext> completion) {
        return spawn(context, type, count, modifier, true, null, Math.max(0, required), completion);
    }

    public static boolean loot(EventContext context, EntityType<?> type, int count, Identifier table) {
        return spawn(context, type, count, null, false, table, NO_CHALLENGE, null);
    }

    public static void killed(ServerPlayerEntity killer, LivingEntity dead) {
        var dropped = LOOT_DROPS.remove(dead.getUuid());
        if (dropped != null) dropped.context().lootTable(dropped.table(), LOOT_OPTIONS);
        var challenge = CHALLENGES.get(killer.getUuid());
        if (challenge == null || !challenge.targets.remove(dead.getUuid())) return;
        killer.sendMessage(Text.translatable(TARGET_KILLED_KEY), true);
        if (++challenge.killed < challenge.required) return;
        CHALLENGES.remove(killer.getUuid());
        if (challenge.completion != null) challenge.completion.accept(challenge.context);
    }

    public static void tick(MinecraftServer server) {
        if (CHALLENGES.isEmpty()) return;
        var time = server.getOverworld().getTime();
        var iterator = CHALLENGES.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            if (entry.getValue().deadline > time) continue;
            var player = server.getPlayerManager().getPlayer(entry.getKey());
            if (player != null) player.sendMessage(Text.translatable(CHALLENGE_TIMEOUT_KEY), true);
            iterator.remove();
        }
    }

    private static boolean spawn(EventContext context, EntityType<?> type, int count, Consumer<LivingEntity> modifier, boolean guard, Identifier table, int required, Consumer<EventContext> completion) {
        var level = context.getWorld();
        var player = context.player();
        var targets = new HashSet<UUID>();
        for (var index = 0; index < count; index++) {
            if (!(type.spawn(level, spawnPosition(level, player.getPos()), SpawnReason.MOB_SUMMONED) instanceof LivingEntity living)) continue;
            if (modifier != null) modifier.accept(living);
            if (guard) mark(living, player);
            if (table != null) LOOT_DROPS.put(living.getUuid(), new LootDrop(context, table));
            targets.add(living.getUuid());
        }
        if (targets.isEmpty()) return false;
        if (required != NO_CHALLENGE) {
            var deadline = level.getServer().getOverworld().getTime() + PGCConfig.EVENT_CHALLENGE_TICKS.get();
            CHALLENGES.put(player.getUuid(), new Challenge(context, targets, MathHelper.clamp(required, 1, targets.size()), completion, deadline));
        }
        return true;
    }

    private static void mark(LivingEntity living, ServerPlayerEntity player) {
        int ticks = PGCConfig.EVENT_CHALLENGE_TICKS.get();
        living.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, ticks, 0, false, false));
        living.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, ticks, 0, false, false));
        if (living instanceof MobEntity mob) mob.setTarget(player);
    }

    private static BlockPos spawnPosition(ServerWorld level, Vec3d center) {
        var random = level.random;
        for (var attempt = 0; attempt < SPAWN_ATTEMPTS; attempt++) {
            var angle = random.nextDouble() * Math.PI * 2.0D;
            var distance = SPAWN_RADIUS * Math.sqrt(random.nextDouble());
            var x = MathHelper.floor(center.x + distance * Math.cos(angle));
            var z = MathHelper.floor(center.z + distance * Math.sin(angle));
            var base = MathHelper.floor(center.y);
            for (var offset = 0; offset <= VERTICAL_SCAN; offset++) {
                if (suitable(level, new BlockPos(x, base + offset, z))) return new BlockPos(x, base + offset, z);
                if (offset > 0 && suitable(level, new BlockPos(x, base - offset, z))) return new BlockPos(x, base - offset, z);
            }
        }
        return BlockPos.ofFloored(center);
    }

    private static boolean suitable(ServerWorld level, BlockPos pos) {
        if (level.isOutOfHeightLimit(pos) || level.isOutOfHeightLimit(pos.up())) return false;
        if (!level.getBlockState(pos).isAir() || !level.getBlockState(pos.up()).isAir()) return false;
        var below = level.getBlockState(pos.down());
        return below.isSolidBlock(level, pos.down()) || below.isOpaqueFullCube(level, pos.down());
    }

    private record LootDrop(EventContext context, Identifier table) {
    }

    private static final class Challenge {
        private final EventContext context;
        private final Set<UUID> targets;
        private final int required;
        private final Consumer<EventContext> completion;
        private final long deadline;
        private int killed;

        private Challenge(EventContext context, Set<UUID> targets, int required, Consumer<EventContext> completion, long deadline) {
            this.context = context;
            this.targets = targets;
            this.required = required;
            this.completion = completion;
            this.deadline = deadline;
        }
    }
}
