package net.per.primogemcraft.compat.registries;

import com.mojang.datafixers.util.Either;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderOwner;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraftforge.registries.RegistryObject;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * 1.21 NeoForge {@code DeferredHolder<R, T>} 的兼容包装。
 *
 * <p>NeoForge 的 DeferredHolder 带两个类型参数（注册表元素类型 R 与实际类型 T）
 * 并且自身就是一个 {@link Holder}；1.20.1 Forge 的 {@link RegistryObject} 只有
 * 一个类型参数且不实现 Holder。本类保留 NeoForge 的形态，使项目中
 * 780 余处类型引用只需替换 import 即可编译。
 *
 * <p>支持两种来源：
 * <ul>
 *   <li><b>注册表模式</b>：委托给 Forge {@link RegistryObject}，值在注册事件后解析；</li>
 *   <li><b>本地模式</b>：由 {@link #local} 创建，值在构建期即确定。用于 1.20.1
 *       没有对应注册表的条目（例如数据组件类型）。</li>
 * </ul>
 *
 * <p>签名逐项对照 NeoForge 21.1.250 的 {@code DeferredHolder} 实测结果。
 */
public class DeferredHolder<R, T extends R> implements Holder<R> {

    private final RegistryObject<T> delegate;   // nullable，仅注册表模式有值
    private final ResourceKey<R> key;           // nullable，仅注册表模式有值
    private final Supplier<T> valueSupplier;    // 始终可用
    private final ResourceLocation id;

    /** 注册表模式。 */
    @SuppressWarnings("unchecked")
    protected DeferredHolder(RegistryObject<T> delegate) {
        this.delegate = delegate;
        this.key = (ResourceKey<R>) delegate.getKey();
        this.id = delegate.getId();
        this.valueSupplier = delegate;
    }

    /** 本地模式：不进入任何注册表，值在构建期确定。 */
    protected DeferredHolder(ResourceLocation id, Supplier<T> valueSupplier) {
        this.delegate = null;
        this.key = null;
        this.id = id;
        this.valueSupplier = valueSupplier;
    }

    public static <R, T extends R> DeferredHolder<R, T> of(RegistryObject<T> delegate) {
        return new DeferredHolder<>(delegate);
    }

    /**
     * 创建一个不隶属于任何注册表的持有者。
     *
     * @param id   标识（用于日志与调试）
     * @param value 构建期即可求值
     */
    public static <R, T extends R> DeferredHolder<R, T> local(ResourceLocation id, T value) {
        return new DeferredHolder<>(id, () -> value);
    }

    /** 暴露底层 Forge RegistryObject；本地模式下为 {@code null}。 */
    public RegistryObject<T> forge() {
        return delegate;
    }

    public boolean isLocal() {
        return delegate == null;
    }

    // ------------------------------------------------------------ NeoForge 语义

    @Override
    public T value() {
        return valueSupplier.get();
    }

    @Override
    public T get() {
        return valueSupplier.get();
    }

    public Optional<T> asOptional() {
        return isBound() ? Optional.of(valueSupplier.get()) : Optional.empty();
    }

    public ResourceLocation getId() {
        return id;
    }

    /** 本地模式没有 ResourceKey，此时抛出以暴露误用。 */
    public ResourceKey<R> getKey() {
        if (key == null) {
            throw new UnsupportedOperationException("Local DeferredHolder has no ResourceKey: " + id);
        }
        return key;
    }

    @Override
    public boolean isBound() {
        return delegate == null ? valueSupplier.get() != null : delegate.isPresent();
    }

    @Override
    public boolean is(ResourceLocation location) {
        return id.equals(location);
    }

    @Override
    public boolean is(ResourceKey<R> other) {
        return key != null && key.equals(other);
    }

    @Override
    public boolean is(Predicate<ResourceKey<R>> predicate) {
        return key != null && predicate.test(key);
    }

    @Override
    public boolean is(TagKey<R> tag) {
        return !isLocal() && getDelegate().is(tag);
    }

    public boolean is(Holder<R> other) {
        return getDelegate().equals(other) || (key != null && key.equals(other.unwrapKey().orElse(null)));
    }

    @Override
    public Stream<TagKey<R>> tags() {
        return isLocal() ? Stream.of() : getDelegate().tags();
    }

    @Override
    public Either<ResourceKey<R>, R> unwrap() {
        return key != null ? Either.left(key) : Either.right(value());
    }

    @Override
    public Optional<ResourceKey<R>> unwrapKey() {
        return Optional.ofNullable(key);
    }

    @Override
    public Kind kind() {
        return key != null ? Kind.REFERENCE : Kind.DIRECT;
    }

    @Override
    public boolean canSerializeIn(HolderOwner<R> owner) {
        return isLocal() || getDelegate().canSerializeIn(owner);
    }

    @SuppressWarnings("unchecked")
    public Holder<R> getDelegate() {
        if (isLocal()) {
            return Holder.direct(value());
        }
        return (Holder<R>) delegate.getHolder().orElseThrow(
                () -> new IllegalStateException("Registry entry not bound yet: " + id));
    }

    @Override
    public String toString() {
        return "DeferredHolder[" + id + "]";
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof DeferredHolder<?, ?> other)) return false;
        return id.equals(other.id);
    }
}
