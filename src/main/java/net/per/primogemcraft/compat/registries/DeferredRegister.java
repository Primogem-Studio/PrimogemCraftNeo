package net.per.primogemcraft.compat.registries;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class DeferredRegister<T> {

    protected final Registry<T> registry;
    protected final String modid;
    private final List<DeferredHolder<T, ? extends T>> entries = new ArrayList<>();

    protected DeferredRegister(Registry<T> registry, String modid) {
        this.registry = registry;
        this.modid = modid;
    }

    public static <B> DeferredRegister<B> create(RegistryKey<? extends Registry<B>> key, String modid) {
        return new DeferredRegister<>(resolve(key), modid);
    }

    public static <B> DeferredRegister<B> create(Registry<B> registry, String modid) {
        return new DeferredRegister<>(registry, modid);
    }

    public static Items createItems(String modid) {
        return new Items(Registries.ITEM, modid);
    }

    public static Blocks createBlocks(String modid) {
        return new Blocks(Registries.BLOCK, modid);
    }

    @SuppressWarnings("unchecked")
    private static <B> Registry<B> resolve(RegistryKey<? extends Registry<B>> key) {
        var resolved = Registries.REGISTRIES.get(key.getValue());
        if (resolved == null) return null;
        return (Registry<B>) resolved;
    }

    public <I extends T> DeferredHolder<T, I> register(String name, Supplier<? extends I> supplier) {
        var id = new Identifier(modid, name);
        if (registry == null) {
            DeferredHolder<T, I> local = DeferredHolder.local(id, supplier.get());
            entries.add(local);
            return local;
        }
        I value = Registry.register(registry, id, supplier.get());
        DeferredHolder<T, I> holder = DeferredHolder.of(registry, RegistryKey.of(registry.getKey(), id), id, () -> value);
        entries.add(holder);
        return holder;
    }

    public <I extends T> DeferredHolder<T, I> register(String name, Function<Identifier, ? extends I> factory) {
        var id = new Identifier(modid, name);
        return register(name, () -> factory.apply(id));
    }

    public void register(Object bus) {
    }

    public Collection<DeferredHolder<T, ? extends T>> getEntries() {
        return List.copyOf(entries);
    }

    public RegistryKey<? extends Registry<T>> getRegistryKey() {
        return registry.getKey();
    }

    public Identifier getRegistryName() {
        return registry.getKey().getValue();
    }

    public String getModid() {
        return modid;
    }

    protected void track(DeferredHolder<T, ? extends T> holder) {
        entries.add(holder);
    }

    public static class Items extends DeferredRegister<Item> {
        protected Items(Registry<Item> registry, String modid) {
            super(registry, modid);
        }

        @Override
        public <I extends Item> DeferredItem<I> register(String name, Supplier<? extends I> supplier) {
            var id = new Identifier(modid, name);
            if (registry == null) {
                var local = DeferredItem.localItem(id, supplier.get());
                track(local);
                return local;
            }
            var value = Registry.register(registry, id, supplier.get());
            var holder = DeferredItem.ofItem(registry, RegistryKey.of(registry.getKey(), id), id, () -> value);
            track(holder);
            return holder;
        }

        public <I extends Item> DeferredItem<I> register(String name, Function<Identifier, ? extends I> factory) {
            var id = new Identifier(modid, name);
            return register(name, () -> factory.apply(id));
        }

        public <I extends Item> DeferredItem<I> registerItem(String name, Function<Item.Settings, ? extends I> factory) {
            return register(name, () -> factory.apply(new Item.Settings()));
        }

        public <I extends Item> DeferredItem<I> registerItem(String name, Function<Item.Settings, ? extends I> factory,
                                                             Item.Settings properties) {
            return register(name, () -> factory.apply(properties));
        }

        public DeferredItem<Item> registerSimpleItem(String name) {
            return register(name, () -> new Item(new Item.Settings()));
        }

        public DeferredItem<Item> registerSimpleItem(String name, Item.Settings properties) {
            return register(name, () -> new Item(properties));
        }

        public DeferredItem<BlockItem> registerSimpleBlockItem(String name, DeferredBlock<?> block) {
            return register(name, () -> new BlockItem(block.get(), new Item.Settings()));
        }

        public DeferredItem<BlockItem> registerSimpleBlockItem(String name, DeferredBlock<?> block, Item.Settings properties) {
            return register(name, () -> new BlockItem(block.get(), properties));
        }

        public DeferredItem<BlockItem> registerSimpleBlockItem(String name, Supplier<? extends Block> block) {
            return register(name, () -> new BlockItem(block.get(), new Item.Settings()));
        }
    }

    public static class Blocks extends DeferredRegister<Block> {
        protected Blocks(Registry<Block> registry, String modid) {
            super(registry, modid);
        }

        @Override
        public <B extends Block> DeferredBlock<B> register(String name, Supplier<? extends B> supplier) {
            var id = new Identifier(modid, name);
            if (registry == null) {
                var local = DeferredBlock.localBlock(id, supplier.get());
                track(local);
                return local;
            }
            var value = Registry.register(registry, id, supplier.get());
            var holder = DeferredBlock.ofBlock(registry, RegistryKey.of(registry.getKey(), id), id, () -> value);
            track(holder);
            return holder;
        }

        public <B extends Block> DeferredBlock<B> register(String name, Function<Identifier, ? extends B> factory) {
            var id = new Identifier(modid, name);
            return register(name, () -> factory.apply(id));
        }

        public <B extends Block> DeferredBlock<B> registerBlock(String name,
                                                                Function<AbstractBlock.Settings, ? extends B> factory) {
            return register(name, () -> factory.apply(AbstractBlock.Settings.create()));
        }

        public <B extends Block> DeferredBlock<B> registerBlock(String name,
                                                                Function<AbstractBlock.Settings, ? extends B> factory,
                                                                AbstractBlock.Settings properties) {
            return register(name, () -> factory.apply(properties));
        }

        public DeferredBlock<Block> registerSimpleBlock(String name) {
            return register(name, () -> new Block(AbstractBlock.Settings.create()));
        }

        public DeferredBlock<Block> registerSimpleBlock(String name, AbstractBlock.Settings properties) {
            return register(name, () -> new Block(properties));
        }
    }
}