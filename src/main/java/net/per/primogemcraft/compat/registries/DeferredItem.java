package net.per.primogemcraft.compat.registries;

import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

import java.util.function.Supplier;

public class DeferredItem<T extends Item> extends DeferredHolder<Item, T> implements ItemConvertible {

    private DeferredItem(Registry<? super Item> registry, RegistryKey<Item> key, Identifier id, Supplier<T> valueSupplier) {
        super(registry, key, id, valueSupplier);
    }

    private DeferredItem(Identifier id, Supplier<T> valueSupplier) {
        super(id, valueSupplier);
    }

    public static <T extends Item> DeferredItem<T> ofItem(Registry<? super Item> registry, RegistryKey<Item> key, Identifier id, Supplier<T> valueSupplier) {
        return new DeferredItem<>(registry, key, id, valueSupplier);
    }

    public static <T extends Item> DeferredItem<T> localItem(Identifier id, T value) {
        return new DeferredItem<>(id, () -> value);
    }

    @Override
    public Item asItem() {
        return get();
    }

    public ItemStack toStack() {
        return new ItemStack(get());
    }

    public ItemStack toStack(int count) {
        return new ItemStack(get(), count);
    }
}