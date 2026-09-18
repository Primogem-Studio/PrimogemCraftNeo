package net.per.primogemcraft.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.per.primogemcraft.block.ContainerWindowBlock;
import net.per.primogemcraft.block.TrashCanBlock;
import net.per.primogemcraft.registry.PGCBlockEntities;
import net.per.primogemcraft.system.menu.ContainerWindowMenu;
import net.per.primogemcraft.system.menu.ContainerWindowSound;

public class ContainerWindowBlockEntity extends RandomizableContainerBlockEntity {
    private NonNullList<ItemStack> items;

    public ContainerWindowBlockEntity(BlockPos pos, BlockState state) {
        super(PGCBlockEntities.CONTAINER_WINDOW.get(), pos, state);
        items = NonNullList.withSize(containerSize(state), ItemStack.EMPTY);
    }

    @Override
    protected Component getDefaultName() {
        var block = getBlockState().getBlock();
        if (block instanceof TrashCanBlock) return Component.translatable(block.getDescriptionId() + ".window");
        return Component.translatable(block.getDescriptionId());
    }

    @Override
    public int getContainerSize() {
        return items.size();
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tryLoadLootTable(tag)) return;
        items = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items, registries);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (trySaveLootTable(tag)) return;
        ContainerHelper.saveAllItems(tag, items, registries);
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
        return new ContainerWindowMenu(id, inventory, this, null, null,
                ContainerLevelAccess.create(level, worldPosition), ContainerWindowSound.TRASH_CAN_CLOSE);
    }

    @Override
    public void writeClientSideData(AbstractContainerMenu menu, RegistryFriendlyByteBuf buffer) {
        buffer.writeByte(ContainerWindowMenu.SOURCE_BLOCK);
        buffer.writeVarInt(items.size());
        buffer.writeBlockPos(worldPosition);
    }

    private static int containerSize(BlockState state) {
        return state.getBlock() instanceof ContainerWindowBlock window ? window.containerSize() : 0;
    }
}
