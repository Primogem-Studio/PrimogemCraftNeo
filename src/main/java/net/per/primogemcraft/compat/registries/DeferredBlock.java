package net.per.primogemcraft.compat.registries;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

import java.util.function.Supplier;

public class DeferredBlock<T extends Block> extends DeferredHolder<Block, T> implements ItemConvertible {

    private DeferredBlock(Registry<? super Block> registry, RegistryKey<Block> key, Identifier id, Supplier<T> valueSupplier) {
        super(registry, key, id, valueSupplier);
    }

    private DeferredBlock(Identifier id, Supplier<T> valueSupplier) {
        super(id, valueSupplier);
    }

    public static <T extends Block> DeferredBlock<T> ofBlock(Registry<? super Block> registry, RegistryKey<Block> key, Identifier id, Supplier<T> valueSupplier) {
        return new DeferredBlock<>(registry, key, id, valueSupplier);
    }

    public static <T extends Block> DeferredBlock<T> localBlock(Identifier id, T value) {
        return new DeferredBlock<>(id, () -> value);
    }

    @Override
    public Item asItem() {
        return get().asItem();
    }
}