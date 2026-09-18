package net.per.primogemcraft.system.event;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;
import net.per.primogemcraft.config.PGCConfig;
import net.per.primogemcraft.util.PGCTimer;
import net.per.primogemcraft.util.PlayerFlags;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public final class EventQuota {
    private static final String DATA_NAME = "primogemcraft_event_quota";
    private static final ResourceLocation PLAYER_KEY = ResourceLocation.fromNamespaceAndPath(MOD_ID, "event/quota");
    private static final String REFILL_GATE = "event/refill";
    private static final double PERCENT = 100.0D;

    private EventQuota() {
    }

    public static int available(ServerLevel level) {
        return data(level).available;
    }

    public static int limit(ServerLevel level) {
        return data(level).limit;
    }

    public static void addAvailable(ServerLevel level, int amount) {
        var data = data(level);
        data.available = Math.clamp(data.available + amount, 0, data.limit);
        data.setDirty();
    }

    public static void addLimit(ServerLevel level, int amount) {
        var data = data(level);
        data.limit = Math.max(0, data.limit + amount);
        data.available = Math.min(data.available, data.limit);
        data.setDirty();
    }

    public static int playerStored(ServerPlayer player) {
        return PlayerFlags.of(player).counter(PLAYER_KEY);
    }

    public static void addPlayerStored(ServerPlayer player, int amount) {
        PlayerFlags.of(player).set(PLAYER_KEY, Math.max(0, playerStored(player) + amount));
    }

    public static boolean roll(ServerLevel level, ServerPlayer player) {
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
        data.setDirty();
        return true;
    }

    private static void refill(ServerLevel level, ServerPlayer player) {
        if (!PGCTimer.isDone(player, REFILL_GATE)) return;
        PGCTimer.set(player, REFILL_GATE, PGCConfig.EVENT_RECOVERY_TICKS.get());
        if (playerStored(player) <= 0) {
            addPlayerStored(player, 1);
            return;
        }
        addAvailable(level, 1);
    }

    private static Data data(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(new SavedData.Factory<>(Data::new, Data::load, null), DATA_NAME);
    }

    public static final class Data extends SavedData {
        private int limit = PGCConfig.EVENT_WORLD_LIMIT.get();
        private int available = limit;

        public static Data load(CompoundTag tag, HolderLookup.Provider registries) {
            var data = new Data();
            data.limit = Math.max(0, tag.getInt("limit"));
            data.available = Math.clamp(tag.getInt("available"), 0, data.limit);
            return data;
        }

        @Override
        public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
            tag.putInt("available", available);
            tag.putInt("limit", limit);
            return tag;
        }
    }
}
