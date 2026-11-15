package net.per.primogemcraft.item.misc;

import net.per.primogemcraft.compat.network.RegistryFriendlyByteBuf;
import net.minecraft.text.Text;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.block.BlockState;
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

    public PouchItem(Settings properties, int capacity) {
        super(properties);
        this.capacity = capacity;
    }

    @Override
    public float getMiningSpeedMultiplier(ItemStack stack, BlockState state) {
        return 0.0F;
    }

    @Override
    public TypedActionResult<ItemStack> use(World level, PlayerEntity player, Hand hand) {
        var stack = player.getStackInHand(hand);
        if (player instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.openHandledScreen(new PouchMenuProvider(stack.getName(), new PouchContainer(stack, capacity), hand, stack, capacity));
            ContainerWindowSound.POUCH_OPEN.play(level, player.getX(), player.getY(), player.getZ(), player.getRandom());
        }
        player.swingHand(hand);
        return TypedActionResult.success(stack, level.isClient());
    }

    private record PouchMenuProvider(Text title, PouchContainer container, Hand hand, ItemStack holding,
                                     int capacity) implements net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory {
        public Text getDisplayName() {
            return title;
        }

        @Override
        public ScreenHandler createMenu(int id, PlayerInventory inventory, PlayerEntity player) {
            return new ContainerWindowMenu(id, inventory, container, hand, holding, ScreenHandlerContext.EMPTY,
                    ContainerWindowSound.POUCH_CLOSE);
        }

        @Override
        public void writeScreenOpeningData(net.minecraft.server.network.ServerPlayerEntity player, net.minecraft.network.PacketByteBuf buffer) {
            buffer.writeByte(hand == Hand.MAIN_HAND ? ContainerWindowMenu.SOURCE_MAIN_HAND : ContainerWindowMenu.SOURCE_OFF_HAND);
            buffer.writeVarInt(capacity);
        }
    }
}
