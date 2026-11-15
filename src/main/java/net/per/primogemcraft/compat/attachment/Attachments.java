package net.per.primogemcraft.compat.attachment;

import com.mojang.serialization.DataResult;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.per.primogemcraft.compat.registries.DeferredHolder;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

/**
 * 附加数据在 1.20.1 上的实现。
 *
 * <p>NeoForge 的 {@code entity.getData(type)} / {@code setData(type, value)} 在 1.20.1
 * 不存在。本类把数据存进实体的 Forge 持久化 NBT（{@code getPersistentData()}），
 * 用各 {@link AttachmentType} 自带的 {@code Codec} 经 {@link NbtOps} 编解码，
 * 因此数据会随实体一起写入存档并在读取时恢复。
 *
 * <p>没有 Codec 的附加数据（如仅运行时的计时器）保存在内存映射中，语义与 1.21
 * 未声明 serialize 时一致：不跨存档持久化。
 */
@Mod.EventBusSubscriber(modid = MOD_ID)
public final class Attachments {

    /** 实体持久化 NBT 中存放附加数据的子标签。 */
    public static final String ROOT = "primogemcraft_attachments";

    /** 无 Codec 的附加数据：实体 UUID + 组件名 -> 值。 */
    private static final Map<String, Object> TRANSIENT = new ConcurrentHashMap<>();

    private Attachments() {
    }

    // ------------------------------------------------------------------ 读取

    @SuppressWarnings("unchecked")
    public static <T> T get(Entity entity, AttachmentType<T> type) {
        if (entity == null || type == null) return type == null ? null : type.defaultValue().get();
        if (type.codec() == null) {
            var cached = TRANSIENT.get(key(entity, type));
            return cached == null ? type.defaultValue().get() : (T) cached;
        }
        var root = entity.getPersistentData().getCompound(ROOT);
        if (!root.contains(type.name())) return type.defaultValue().get();
        DataResult<T> parsed = type.codec().parse(NbtOps.INSTANCE, root.get(type.name()));
        Optional<T> value = parsed.result();
        return value.orElseGet(() -> type.defaultValue().get());
    }

    public static <T> T get(Entity entity, DeferredHolder<AttachmentType<?>, AttachmentType<T>> holder) {
        return get(entity, holder.get());
    }

    // ------------------------------------------------------------------ 写入

    public static <T> void set(Entity entity, AttachmentType<T> type, T value) {
        if (entity == null || type == null) return;
        if (type.codec() == null) {
            TRANSIENT.put(key(entity, type), value);
            return;
        }
        Optional<Tag> encoded = type.codec().encodeStart(NbtOps.INSTANCE, value).result();
        if (encoded.isEmpty()) return;
        // getCompound 在标签缺失时返回副本，必须显式写回持久化标签
        var persistent = entity.getPersistentData();
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
        return entity.getPersistentData().getCompound(ROOT).contains(type.name());
    }

    private static String key(Entity entity, AttachmentType<?> type) {
        return entity.getUUID() + "|" + type.name();
    }

    // ------------------------------------------------------------------ 死亡保留

    /**
     * 玩家死亡重建实体时，把声明了 copyOnDeath 的附加数据迁移到新实体，
     * 复现 1.21 {@code copyOnDeath()} 的语义。
     */
    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        Player original = event.getOriginal();
        Player current = event.getEntity();
        var from = original.getPersistentData().getCompound(ROOT);
        if (from.isEmpty()) return;
        var to = current.getPersistentData().getCompound(ROOT);
        for (var name : from.getAllKeys()) {
            to.put(name, from.get(name).copy());
        }
        current.getPersistentData().put(ROOT, to);
    }

    /** 玩家登出时清理其运行时态附加数据，避免内存泄漏。 */
    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        var prefix = event.getEntity().getUUID() + "|";
        TRANSIENT.keySet().removeIf(k -> k.startsWith(prefix));
    }

    /** 供 neoforge 风格的 getData/setData 调用点使用的 CompoundTag 访问入口。 */
    public static CompoundTag rootOf(Entity entity) {
        return entity.getPersistentData().getCompound(ROOT);
    }
}
