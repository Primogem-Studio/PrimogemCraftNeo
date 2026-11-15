package net.per.primogemcraft.compat.component;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtElement;
import net.minecraft.item.ItemStack;

import java.util.Optional;

/**
 * 数据组件在 1.20.1 上的 NBT 实现。
 *
 * <p>1.21 的 {@code stack.get(type)} / {@code stack.set(type, value)} 在 1.20.1 不存在，
 * 统一改为本类的静态方法。数据存放在物品 NBT 的 {@value #ROOT} 子标签下，
 * 每个组件用其注册名作为键，值由各组件自己的 {@link Codec} 经 {@link NbtOps} 编解码。
 *
 * <p>这样做的好处是原有 19 个组件的 {@code Codec} 定义可以原样复用，
 * 不必为每种数据手写 NBT 读写；且配方 JSON 里已转换好的
 * {@code "nbt": "{primogemcraft_components:{...}}"} 与此处的键路径一致。
 */
public final class StackComponents {

    /** 物品 NBT 中存放组件数据的子标签名。 */
    public static final String ROOT = "primogemcraft_components";

    private StackComponents() {
    }

    // ------------------------------------------------------------------ 读取

    /**
     * 对应 1.21 的 {@code stack.get(type)}：不存在时返回 {@code null}。
     */
    public static <T> T get(ItemStack stack, DataComponentType<T> type) {
        if (stack == null || stack.isEmpty() || type == null) return null;
        NbtCompound root = stack.getSubNbt(ROOT);
        if (root == null || !root.contains(type.name())) return null;
        return decode(type, root.get(type.name()));
    }

    /**
     * 对应 1.21 的 {@code stack.has(type)}。
     */
    public static <T> boolean has(ItemStack stack, DataComponentType<T> type) {
        if (stack == null || stack.isEmpty() || type == null) return false;
        NbtCompound root = stack.getSubNbt(ROOT);
        return root != null && root.contains(type.name());
    }

    /**
     * 对应 1.21 的 {@code stack.getOrDefault(type, fallback)}。
     *
     * <p>与 1.21 行为一致：键不存在或解码失败时返回 fallback，不抛异常。
     */
    public static <T> T getOrDefault(ItemStack stack, DataComponentType<T> type, T fallback) {
        T value = get(stack, type);
        return value == null ? fallback : value;
    }

    // ------------------------------------------------------------------ 写入

    /**
     * 对应 1.21 的 {@code stack.set(type, value)}：原地修改物品堆。
     *
     * <p>1.21 的 set 返回修改后的新堆；这里沿用 Forge 常见的原地写入语义，
     * 并返回同一个 ItemStack，使 {@code stack.set(...)} 形式的调用点无需改写。
     */
    public static <T> ItemStack set(ItemStack stack, DataComponentType<T> type, T value) {
        if (stack == null || stack.isEmpty() || type == null) return stack;
        if (value == null) return remove(stack, type);
        Optional<NbtElement> encoded = type.codec().encodeStart(NbtOps.INSTANCE, value).result();
        if (encoded.isEmpty()) {
            throw new IllegalStateException("Failed to encode component '" + type.name() + "'");
        }
        stack.getOrCreateSubNbt(ROOT).put(type.name(), encoded.get());
        return stack;
    }

    /**
     * 对应 1.21 的 {@code stack.remove(type)}。
     */
    public static <T> ItemStack remove(ItemStack stack, DataComponentType<T> type) {
        if (stack == null || stack.isEmpty() || type == null) return stack;
        NbtCompound root = stack.getSubNbt(ROOT);
        if (root == null) return stack;
        root.remove(type.name());
        if (root.isEmpty()) stack.removeSubNbt(ROOT);
        return stack;
    }

    // ------------------------------------------------------------------ 内部

    private static <T> T decode(DataComponentType<T> type, NbtElement tag) {
        if (tag == null) return null;
        DataResult<T> result = type.codec().parse(NbtOps.INSTANCE, tag);
        Optional<T> value = result.result();
        if (value.isEmpty()) {
            // 与 1.21 的 getOrDefault 语义一致：数据损坏时当作不存在，避免崩溃
            return null;
        }
        return value.get();
    }

    /** 供需要直接读取原始 NBT 的场合使用（例如战利品修饰器）。 */
    public static Optional<NbtCompound> rawRoot(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return Optional.empty();
        return Optional.ofNullable(stack.getSubNbt(ROOT));
    }

    public static DynamicOps<NbtElement> ops() {
        return NbtOps.INSTANCE;
    }

    // ------------------------------------------------------------------ holder 閲嶈浇

    /** 调用点常直接传注册持有者（PGCDataComponents.X），这里提供等价重载。 */
    public static <T> T get(ItemStack stack,
                            net.per.primogemcraft.compat.registries.DeferredHolder<DataComponentType<?>, DataComponentType<T>> holder) {
        return get(stack, holder.get());
    }

    public static <T> boolean has(ItemStack stack,
                                  net.per.primogemcraft.compat.registries.DeferredHolder<DataComponentType<?>, DataComponentType<T>> holder) {
        return has(stack, holder.get());
    }

    public static <T> T getOrDefault(ItemStack stack,
                                     net.per.primogemcraft.compat.registries.DeferredHolder<DataComponentType<?>, DataComponentType<T>> holder,
                                     T fallback) {
        return getOrDefault(stack, holder.get(), fallback);
    }

    public static <T> ItemStack set(ItemStack stack,
                                    net.per.primogemcraft.compat.registries.DeferredHolder<DataComponentType<?>, DataComponentType<T>> holder,
                                    T value) {
        return set(stack, holder.get(), value);
    }

    public static <T> ItemStack remove(ItemStack stack,
                                       net.per.primogemcraft.compat.registries.DeferredHolder<DataComponentType<?>, DataComponentType<T>> holder) {
        return remove(stack, holder.get());
    }
}