package net.per.primogemcraft.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.per.primogemcraft.compat.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.per.primogemcraft.registry.PGCBlockEntities;
import net.per.primogemcraft.system.menu.GorgeousSmithingTableMenu;
import net.per.primogemcraft.system.menu.LinkedContainers;

import java.util.List;

public class GorgeousSmithingTableBlockEntity extends net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity {
    public static final int SLOT_COUNT = GorgeousSmithingTableMenu.MATERIAL_SLOT_COUNT;

    private NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);

    public GorgeousSmithingTableBlockEntity(BlockPos pos, BlockState state) {
        super(PGCBlockEntities.GORGEOUS_SMITHING_TABLE.get(), pos, state);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("block.primogemcraft.gorgeous_smithing_table");
    }

    @Override
    public int getContainerSize() {
        return SLOT_COUNT;
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
    protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
        var access = ContainerLevelAccess.create(level, worldPosition);
        return new GorgeousSmithingTableMenu(id, inventory, this, access, LinkedContainers.scan(level, worldPosition));
    }

    public void writeOpeningData(net.minecraft.network.FriendlyByteBuf buffer) {
        buffer.writeBlockPos(worldPosition);
        LinkedContainers.write(buffer, LinkedContainers.scan(level, worldPosition));
    }

    @Override
    public boolean stillValid(net.minecraft.world.entity.player.Player player) {
        return net.minecraft.world.Container.stillValidBlockEntity(this, player);
    }
}
