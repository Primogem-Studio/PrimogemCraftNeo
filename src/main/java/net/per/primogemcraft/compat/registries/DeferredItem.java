package net.per.primogemcraft.compat.registries;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.RegistryObject;

/**
 * Compatibility wrapper for NeoForge 1.21 {@code DeferredItem<T>}: equivalent to
 * {@code DeferredHolder<Item, T>} plus {@link ItemLike}.
 *
 * <p>The factory is named {@code ofItem} rather than {@code of} because a static
 * {@code of(RegistryObject<T>)} here would have the same erasure as the inherited
 * {@code DeferredHolder.of(RegistryObject<T>)} without hiding it, which Java rejects.
 */
public class DeferredItem<T extends Item> extends DeferredHolder<Item, T> implements ItemLike {

    private DeferredItem(RegistryObject<T> delegate) {
        super(delegate);
    }

    public static <T extends Item> DeferredItem<T> ofItem(RegistryObject<T> delegate) {
        return new DeferredItem<>(delegate);
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
