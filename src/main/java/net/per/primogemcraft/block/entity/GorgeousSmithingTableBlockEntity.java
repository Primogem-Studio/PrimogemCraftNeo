package net.per.primogemcraft.block.entity;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.text.Text;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.item.ItemStack;
import net.minecraft.block.BlockState;
import net.per.primogemcraft.registry.PGCBlockEntities;
import net.per.primogemcraft.system.menu.GorgeousSmithingTableMenu;
import net.per.primogemcraft.system.menu.LinkedContainers;

public class GorgeousSmithingTableBlockEntity extends net.minecraft.block.entity.LootableContainerBlockEntity
        implements net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory {
    public static final int SLOT_COUNT = GorgeousSmithingTableMenu.MATERIAL_SLOT_COUNT;

    private DefaultedList<ItemStack> items = DefaultedList.ofSize(SLOT_COUNT, ItemStack.EMPTY);

    public GorgeousSmithingTableBlockEntity(BlockPos pos, BlockState state) {
        super(PGCBlockEntities.GORGEOUS_SMITHING_TABLE.get(), pos, state);
    }

    @Override
    protected Text getContainerName() {
        return Text.translatable("block.primogemcraft.gorgeous_smithing_table");
    }

    @Override
    public int size() {
        return SLOT_COUNT;
    }

    @Override
    protected DefaultedList<ItemStack> getInvStackList() {
        return items;
    }

    @Override
    protected void setInvStackList(DefaultedList<ItemStack> items) {
        this.items = items;
    }

    @Override
    protected ScreenHandler createScreenHandler(int id, PlayerInventory inventory) {
        var access = ScreenHandlerContext.create(world, pos);
        return new GorgeousSmithingTableMenu(id, inventory, this, access, LinkedContainers.scan(world, pos));
    }

    @Override
    public void writeScreenOpeningData(net.minecraft.server.network.ServerPlayerEntity player, net.minecraft.network.PacketByteBuf buffer) {
        buffer.writeBlockPos(pos);
        // 与 createScreenHandler 使用同一份扫描结果，保证客户端联动槽与服务端一致
        LinkedContainers.write(buffer, LinkedContainers.scan(world, pos));
    }

    @Override
    public boolean canPlayerUse(net.minecraft.entity.player.PlayerEntity player) {
        return net.minecraft.inventory.Inventory.canPlayerUse(this, player);
    }
}
