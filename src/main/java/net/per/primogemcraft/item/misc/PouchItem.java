package net.per.primogemcraft.item.misc;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.per.primogemcraft.item.tool.DescribedItem;
import net.per.primogemcraft.system.menu.ContainerWindowMenu;
import net.per.primogemcraft.system.menu.ContainerWindowSound;
import net.per.primogemcraft.system.menu.PouchContainer;

/**
 * A pouch holds a fixed number of slots inside the item stack itself, so its contents travel with it. Right-clicking
 * opens the shared container window on the pouch held in that hand.
 */
public class PouchItem extends DescribedItem {
    private final int capacity;

    public PouchItem(Properties properties, int capacity) {
        super(properties);
        this.capacity = capacity;
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        return 0.0F;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(new PouchMenuProvider(stack.getHoverName(), new PouchContainer(stack, capacity), hand, stack, capacity));
            ContainerWindowSound.POUCH_OPEN.play(level, player.getX(), player.getY(), player.getZ(), player.getRandom());
        }
        player.swing(hand, true);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    private record PouchMenuProvider(Component title, PouchContainer container, InteractionHand hand, ItemStack holding,
                                     int capacity) implements MenuProvider {
        @Override
        public Component getDisplayName() {
            return title;
        }

        @Override
        public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
            return new ContainerWindowMenu(id, inventory, container, hand, holding, ContainerLevelAccess.NULL,
                    ContainerWindowSound.POUCH_CLOSE);
        }

        @Override
        public void writeClientSideData(AbstractContainerMenu menu, RegistryFriendlyByteBuf buffer) {
            buffer.writeByte(hand == InteractionHand.MAIN_HAND ? ContainerWindowMenu.SOURCE_MAIN_HAND : ContainerWindowMenu.SOURCE_OFF_HAND);
            buffer.writeVarInt(capacity);
        }
    }
}
