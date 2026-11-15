package net.per.primogemcraft.system.event;

import net.minecraft.registry.RegistryWrapper;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.PersistentState;
import net.per.primogemcraft.config.PGCConfig;
import net.per.primogemcraft.util.PGCTimer;
import net.per.primogemcraft.util.PlayerFlags;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public final class EventQuota {
    private static final String DATA_NAME = "primogemcraft_event_quota";
    private static final Identifier PLAYER_KEY = Identifier.of(MOD_ID, "event/quota");
    private static final String REFILL_GATE = "event/refill";
    private static final double PERCENT = 100.0D;

    private EventQuota() {
    }

    public static int available(ServerWorld level) {
        return data(level).available;
    }

    public static int limit(ServerWorld level) {
        return data(level).limit;
    }

    public static void addAvailable(ServerWorld level, int amount) {
        var data = data(level);
        data.available = net.minecraft.util.math.MathHelper.clamp(data.available + amount, 0, data.limit);
        data.setDirty(true);
    }

    public static void addLimit(ServerWorld level, int amount) {
        var data = data(level);
        data.limit = Math.max(0, data.limit + amount);
        data.available = Math.min(data.available, data.limit);
        data.setDirty(true);
    }

    public static int playerStored(ServerPlayerEntity player) {
        return PlayerFlags.of(player).counter(PLAYER_KEY);
    }

    public static void addPlayerStored(ServerPlayerEntity player, int amount) {
        PlayerFlags.of(player).set(PLAYER_KEY, Math.max(0, playerStored(player) + amount));
    }

    public static boolean roll(ServerWorld level, ServerPlayerEntity player) {
        refill(level, player);
        if (level.random.nextDouble() >= PGCConfig.EVENT_DROP_CHANCE.get() / PERCENT) return false;
        var stored = playerStored(player);
        if (stored > 0) {
            addPlayerStored(player, -1);
            return true;
        }
        var data = data(level);
        if (data.available <= 0) return false;
        data.available--;
        data.setDirty(true);
        return true;
    }

    private static void refill(ServerWorld level, ServerPlayerEntity player) {
        if (!PGCTimer.isDone(player, REFILL_GATE)) return;
        PGCTimer.set(player, REFILL_GATE, PGCConfig.EVENT_RECOVERY_TICKS.get());
        if (playerStored(player) <= 0) {
            addPlayerStored(player, 1);
            return;
        }
        addAvailable(level, 1);
    }

    private static Data data(ServerWorld level) {
        return level.getServer().getOverworld().getPersistentStateManager().getOrCreate(Data::load, Data::new, DATA_NAME);
    }

    public static final class Data extends PersistentState {
        private int limit = PGCConfig.EVENT_WORLD_LIMIT.get();
        private int available = limit;

        public static Data load(NbtCompound tag) {
            var data = new Data();
            data.limit = Math.max(0, tag.getInt("limit"));
            data.available = net.minecraft.util.math.MathHelper.clamp(tag.getInt("available"), 0, data.limit);
            return data;
        }

        @Override
        public NbtCompound writeNbt(NbtCompound tag) {
            tag.putInt("available", available);
            tag.putInt("limit", limit);
            return tag;
        }
    }
}
