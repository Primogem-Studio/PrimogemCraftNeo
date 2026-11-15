package net.per.primogemcraft.compat.registries;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.RegistryObject;

/**
 * Compatibility wrapper for NeoForge 1.21 {@code DeferredBlock<T>}: equivalent to
 * {@code DeferredHolder<Block, T>} plus {@link ItemLike}.
 *
 * <p>The factory is named {@code ofBlock} to avoid an erasure clash with the
 * inherited {@code DeferredHolder.of(RegistryObject<T>)}.
 */
public class DeferredBlock<T extends Block> extends DeferredHolder<Block, T> implements ItemLike {

    private DeferredBlock(RegistryObject<T> delegate) {
        super(delegate);
    }

    public static <T extends Block> DeferredBlock<T> ofBlock(RegistryObject<T> delegate) {
        return new DeferredBlock<>(delegate);
    }

    @Override
    public Item asItem() {
        return get().asItem();
    }
}
