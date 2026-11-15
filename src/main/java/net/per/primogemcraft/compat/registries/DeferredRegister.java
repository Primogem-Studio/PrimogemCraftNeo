package net.per.primogemcraft.compat.registries;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 1.21 NeoForge {@code DeferredRegister} 的兼容包装，委托给 1.20.1 Forge 实现。
 *
 * <p>NeoForge 提供 {@code createItems}/{@code createBlocks} 便捷工厂与
 * {@code registerItem}/{@code registerSimpleBlock}/{@code registerSimpleBlockItem}
 * 等便捷注册方法；1.20.1 Forge 只有 {@code create(key, modid)} +
 * {@code register(name, Supplier)}。本类补齐这些方法，使注册类仅需替换 import。
 *
 * <p>方法签名逐项对照 NeoForge 21.1.250 实测结果，泛型返回类型保持一致，
 * 以保证 {@code DeferredItem<PrimogemItem>} 之类的声明可直接赋值。
 */
public class DeferredRegister<T> {

    protected final net.minecraftforge.registries.DeferredRegister<T> forge;
    protected final String modid;

    protected DeferredRegister(net.minecraftforge.registries.DeferredRegister<T> forge, String modid) {
        this.forge = forge;
        this.modid = modid;
    }

    // ------------------------------------------------------------ 工厂方法

    public static <B> DeferredRegister<B> create(ResourceKey<? extends Registry<B>> key, String modid) {
        return new DeferredRegister<>(net.minecraftforge.registries.DeferredRegister.create(key, modid), modid);
    }

    public static <B> DeferredRegister<B> create(Registry<B> registry, String modid) {
        return new DeferredRegister<>(net.minecraftforge.registries.DeferredRegister.create(registry.key(), modid), modid);
    }

    public static Items createItems(String modid) {
        return new Items(net.minecraftforge.registries.DeferredRegister.create(Registries.ITEM, modid), modid);
    }

    public static Blocks createBlocks(String modid) {
        return new Blocks(net.minecraftforge.registries.DeferredRegister.create(Registries.BLOCK, modid), modid);
    }

    // ------------------------------------------------------------ 基础注册

    public <I extends T> DeferredHolder<T, I> register(String name, Supplier<? extends I> supplier) {
        return DeferredHolder.of(forge.register(name, supplier));
    }

    public <I extends T> DeferredHolder<T, I> register(String name, Function<ResourceLocation, ? extends I> factory) {
        ResourceLocation id = new ResourceLocation(modid, name);
        return DeferredHolder.of(forge.register(name, () -> factory.apply(id)));
    }

    public void register(IEventBus bus) {
        forge.register(bus);
    }

    public Collection<DeferredHolder<T, ? extends T>> getEntries() {
        return forge.getEntries().stream()
                .<DeferredHolder<T, ? extends T>>map(DeferredHolder::of)
                .toList();
    }

    public ResourceKey<? extends Registry<T>> getRegistryKey() {
        return forge.getRegistryKey();
    }

    public ResourceLocation getRegistryName() {
        return forge.getRegistryName();
    }

    public String getModid() {
        return modid;
    }

    // ------------------------------------------------------------ Items

    /** 对应 NeoForge 的 {@code DeferredRegister.Items}。 */
    public static class Items extends DeferredRegister<Item> {
        protected Items(net.minecraftforge.registries.DeferredRegister<Item> forge, String modid) {
            super(forge, modid);
        }

        @Override
        public <I extends Item> DeferredItem<I> register(String name, Supplier<? extends I> supplier) {
            return DeferredItem.ofItem(forge.register(name, supplier));
        }

        public <I extends Item> DeferredItem<I> register(String name, Function<ResourceLocation, ? extends I> factory) {
            ResourceLocation id = new ResourceLocation(modid, name);
            return DeferredItem.ofItem(forge.register(name, () -> factory.apply(id)));
        }

        public <I extends Item> DeferredItem<I> registerItem(String name, Function<Item.Properties, ? extends I> factory) {
            return DeferredItem.ofItem(forge.register(name, () -> factory.apply(new Item.Properties())));
        }

        public <I extends Item> DeferredItem<I> registerItem(String name, Function<Item.Properties, ? extends I> factory,
                                                             Item.Properties properties) {
            return DeferredItem.ofItem(forge.register(name, () -> factory.apply(properties)));
        }

        public DeferredItem<Item> registerSimpleItem(String name) {
            return DeferredItem.ofItem(forge.register(name, () -> new Item(new Item.Properties())));
        }

        public DeferredItem<Item> registerSimpleItem(String name, Item.Properties properties) {
            return DeferredItem.ofItem(forge.register(name, () -> new Item(properties)));
        }

        public DeferredItem<BlockItem> registerSimpleBlockItem(String name, DeferredBlock<?> block) {
            return DeferredItem.ofItem(forge.register(name, () -> new BlockItem(block.get(), new Item.Properties())));
        }

        public DeferredItem<BlockItem> registerSimpleBlockItem(String name, DeferredBlock<?> block, Item.Properties properties) {
            return DeferredItem.ofItem(forge.register(name, () -> new BlockItem(block.get(), properties)));
        }

        public DeferredItem<BlockItem> registerSimpleBlockItem(String name, Supplier<? extends Block> block) {
            return DeferredItem.ofItem(forge.register(name, () -> new BlockItem(block.get(), new Item.Properties())));
        }
    }

    // ------------------------------------------------------------ Blocks

    /** 对应 NeoForge 的 {@code DeferredRegister.Blocks}。 */
    public static class Blocks extends DeferredRegister<Block> {
        protected Blocks(net.minecraftforge.registries.DeferredRegister<Block> forge, String modid) {
            super(forge, modid);
        }

        @Override
        public <B extends Block> DeferredBlock<B> register(String name, Supplier<? extends B> supplier) {
            return DeferredBlock.ofBlock(forge.register(name, supplier));
        }

        public <B extends Block> DeferredBlock<B> register(String name, Function<ResourceLocation, ? extends B> factory) {
            ResourceLocation id = new ResourceLocation(modid, name);
            return DeferredBlock.ofBlock(forge.register(name, () -> factory.apply(id)));
        }

        public <B extends Block> DeferredBlock<B> registerBlock(String name,
                                                                Function<BlockBehaviour.Properties, ? extends B> factory) {
            return DeferredBlock.ofBlock(forge.register(name, () -> factory.apply(BlockBehaviour.Properties.of())));
        }

        public <B extends Block> DeferredBlock<B> registerBlock(String name,
                                                                Function<BlockBehaviour.Properties, ? extends B> factory,
                                                                BlockBehaviour.Properties properties) {
            return DeferredBlock.ofBlock(forge.register(name, () -> factory.apply(properties)));
        }

        public DeferredBlock<Block> registerSimpleBlock(String name) {
            return DeferredBlock.ofBlock(forge.register(name, () -> new Block(BlockBehaviour.Properties.of())));
        }

        public DeferredBlock<Block> registerSimpleBlock(String name, BlockBehaviour.Properties properties) {
            return DeferredBlock.ofBlock(forge.register(name, () -> new Block(properties)));
        }
    }
}
