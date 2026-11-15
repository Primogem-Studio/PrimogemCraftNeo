package net.per.primogemcraft.block.entity;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.inventory.Inventories;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.item.ItemStack;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.block.BlockState;
import net.per.primogemcraft.block.ContainerWindowBlock;
import net.per.primogemcraft.block.TrashCanBlock;
import net.per.primogemcraft.registry.PGCBlockEntities;
import net.per.primogemcraft.system.menu.ContainerWindowMenu;
import net.per.primogemcraft.system.menu.ContainerWindowSound;

public class ContainerWindowBlockEntity extends LootableContainerBlockEntity
        implements net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory {
    private DefaultedList<ItemStack> items;

    public ContainerWindowBlockEntity(BlockPos pos, BlockState state) {
        super(PGCBlockEntities.CONTAINER_WINDOW.get(), pos, state);
        items = DefaultedList.ofSize(containerSize(state), ItemStack.EMPTY);
    }

    @Override
    protected Text getContainerName() {
        var block = getCachedState().getBlock();
        if (block instanceof TrashCanBlock) return Text.translatable(block.getTranslationKey() + ".window");
        return Text.translatable(block.getTranslationKey());
    }

    @Override
    public int size() {
        return items.size();
    }

    @Override
    protected DefaultedList<ItemStack> getInvStackList() {
        return items;
    }

    @Override
    protected void setInvStackList(DefaultedList<ItemStack> list) {
        this.items = list;
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        if (deserializeLootTable(nbt)) return;
        items = DefaultedList.ofSize(size(), ItemStack.EMPTY);
        Inventories.readNbt(nbt, items);
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        if (serializeLootTable(nbt)) return;
        Inventories.writeNbt(nbt, items);
    }

    @Override
    protected ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory) {
        return new ContainerWindowMenu(syncId, playerInventory, this, null, null,
                ScreenHandlerContext.create(world, pos), ContainerWindowSound.TRASH_CAN_CLOSE);
    }

    @Override
    public void writeScreenOpeningData(net.minecraft.server.network.ServerPlayerEntity player, net.minecraft.network.PacketByteBuf buffer) {
        buffer.writeByte(ContainerWindowMenu.SOURCE_BLOCK);
        buffer.writeVarInt(items.size());
        buffer.writeBlockPos(pos);
    }

    private static int containerSize(BlockState state) {
        return state.getBlock() instanceof ContainerWindowBlock window ? window.containerSize() : 0;
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return net.minecraft.inventory.Inventory.canPlayerUse(this, player);
    }
}
