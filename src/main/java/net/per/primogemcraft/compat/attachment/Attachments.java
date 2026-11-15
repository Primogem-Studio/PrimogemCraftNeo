package net.per.primogemcraft.compat.attachment;

import com.mojang.serialization.DataResult;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.per.primogemcraft.compat.registries.DeferredHolder;
import net.per.primogemcraft.compat.attachment.PersistentDataHolder;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class Attachments {

    public static final String ROOT = "primogemcraft_attachments";

    private static final Map<String, Object> TRANSIENT = new ConcurrentHashMap<>();

    private Attachments() {
    }

    public static void register() {
        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
            var from = persistent(oldPlayer).getCompound(ROOT);
            if (from.isEmpty()) return;
            var to = persistent(newPlayer).getCompound(ROOT);
            for (var name : from.getKeys()) {
                to.put(name, from.get(name).copy());
            }
            persistent(newPlayer).put(ROOT, to);
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            var prefix = handler.getPlayer().getUuid() + "|";
            TRANSIENT.keySet().removeIf(k -> k.startsWith(prefix));
        });
    }

    private static NbtCompound persistent(Entity entity) {
        return ((PersistentDataHolder) entity).primogemcraft$getPersistentData();
    }

    @SuppressWarnings("unchecked")
    public static <T> T get(Entity entity, AttachmentType<T> type) {
        if (entity == null || type == null) return type == null ? null : type.defaultValue().get();
        if (type.codec() == null) {
            var cached = TRANSIENT.get(key(entity, type));
            return cached == null ? type.defaultValue().get() : (T) cached;
        }
        var root = persistent(entity).getCompound(ROOT);
        if (!root.contains(type.name())) return type.defaultValue().get();
        DataResult<T> parsed = type.codec().parse(NbtOps.INSTANCE, root.get(type.name()));
        Optional<T> value = parsed.result();
        return value.orElseGet(() -> type.defaultValue().get());
    }

    public static <T> T get(Entity entity, DeferredHolder<AttachmentType<?>, AttachmentType<T>> holder) {
        return get(entity, holder.get());
    }

    public static <T> void set(Entity entity, AttachmentType<T> type, T value) {
        if (entity == null || type == null) return;
        if (type.codec() == null) {
            TRANSIENT.put(key(entity, type), value);
            return;
        }
        Optional<NbtElement> encoded = type.codec().encodeStart(NbtOps.INSTANCE, value).result();
        if (encoded.isEmpty()) return;
        var persistent = persistent(entity);
        var root = persistent.getCompound(ROOT);
        root.put(type.name(), encoded.get());
        persistent.put(ROOT, root);
    }

    public static <T> void set(Entity entity, DeferredHolder<AttachmentType<?>, AttachmentType<T>> holder, T value) {
        set(entity, holder.get(), value);
    }

    public static <T> boolean has(Entity entity, AttachmentType<T> type) {
        if (entity == null || type == null) return false;
        if (type.codec() == null) return TRANSIENT.containsKey(key(entity, type));
        return persistent(entity).getCompound(ROOT).contains(type.name());
    }

    private static String key(Entity entity, AttachmentType<?> type) {
        return entity.getUuid() + "|" + type.name();
    }

    public static NbtCompound rootOf(Entity entity) {
        return persistent(entity).getCompound(ROOT);
    }
}