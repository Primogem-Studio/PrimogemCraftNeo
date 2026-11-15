package net.per.primogemcraft.compat.registries;

import com.mojang.datafixers.util.Either;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryOwner;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class DeferredHolder<R, T extends R> implements RegistryEntry<R> {

    private final Registry<? super R> registry;
    private final RegistryKey<R> key;
    private final Supplier<T> valueSupplier;
    private final Identifier id;
    private RegistryEntry<R> resolved;

    protected DeferredHolder(Registry<? super R> registry, RegistryKey<R> key, Identifier id, Supplier<T> valueSupplier) {
        this.registry = registry;
        this.key = key;
        this.id = id;
        this.valueSupplier = valueSupplier;
    }

    protected DeferredHolder(Identifier id, Supplier<T> valueSupplier) {
        this.registry = null;
        this.key = null;
        this.id = id;
        this.valueSupplier = valueSupplier;
    }

    public static <R, T extends R> DeferredHolder<R, T> of(Registry<? super R> registry, RegistryKey<R> key, Identifier id, Supplier<T> valueSupplier) {
        return new DeferredHolder<>(registry, key, id, valueSupplier);
    }

    public static <R, T extends R> DeferredHolder<R, T> local(Identifier id, T value) {
        return new DeferredHolder<>(id, () -> value);
    }

    public boolean isLocal() {
        return registry == null;
    }

    @Override
    public T value() {
        return valueSupplier.get();
    }

    public T get() {
        return valueSupplier.get();
    }

    public Optional<T> asOptional() {
        return hasKeyAndValue() ? Optional.of(valueSupplier.get()) : Optional.empty();
    }

    public Identifier getId() {
        return id;
    }

    public RegistryKey<R> getRegistryKey() {
        if (key == null) {
            throw new UnsupportedOperationException("Local DeferredHolder has no RegistryKey: " + id);
        }
        return key;
    }

    @Override
    public boolean hasKeyAndValue() {
        return valueSupplier.get() != null;
    }

    @Override
    public boolean matchesId(Identifier location) {
        return id.equals(location);
    }

    @Override
    public boolean matchesKey(RegistryKey<R> other) {
        return key != null && key.equals(other);
    }

    @Override
    public boolean matches(Predicate<RegistryKey<R>> predicate) {
        return key != null && predicate.test(key);
    }

    @Override
    public boolean isIn(TagKey<R> tag) {
        return !isLocal() && getDelegate().isIn(tag);
    }

    public boolean is(RegistryEntry<R> other) {
        return getDelegate().equals(other) || (key != null && key.equals(other.getKey().orElse(null)));
    }

    @Override
    public Stream<TagKey<R>> streamTags() {
        return isLocal() ? Stream.of() : getDelegate().streamTags();
    }

    @Override
    public Either<RegistryKey<R>, R> getKeyOrValue() {
        return key != null ? Either.left(key) : Either.right(value());
    }

    @Override
    public Optional<RegistryKey<R>> getKey() {
        return Optional.ofNullable(key);
    }

    public Optional<RegistryKey<R>> unwrapKey() {
        return getKey();
    }

    @Override
    public Type getType() {
        return key != null ? Type.REFERENCE : Type.DIRECT;
    }

    @Override
    public boolean ownerEquals(RegistryEntryOwner<R> owner) {
        return isLocal() || getDelegate().ownerEquals(owner);
    }

    @SuppressWarnings("unchecked")
    public RegistryEntry<R> getDelegate() {
        if (isLocal()) {
            return RegistryEntry.of(value());
        }
        if (resolved == null) {
            resolved = ((Registry<R>) registry).getEntry(key).orElseThrow(
                    () -> new IllegalStateException("Registry entry not bound yet: " + id));
        }
        return resolved;
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