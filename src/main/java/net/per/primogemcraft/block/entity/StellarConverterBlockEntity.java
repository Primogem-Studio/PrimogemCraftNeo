package net.per.primogemcraft.block.entity;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.nbt.NbtCompound;
import net.per.primogemcraft.compat.network.RegistryFriendlyByteBuf;
import net.minecraft.text.Text;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.block.BlockState;
import net.per.primogemcraft.registry.PGCBlockEntities;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.registry.PGCRecipeTypes;
import net.per.primogemcraft.recipe.StellarConverterRecipe;
import net.per.primogemcraft.system.menu.StellarConverterMenu;
import org.jetbrains.annotations.Nullable;

import java.util.stream.IntStream;

public class StellarConverterBlockEntity extends net.minecraft.block.entity.LootableContainerBlockEntity
        implements SidedInventory, net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory {
    public static final int SLOT_COUNT = 5;
    public static final int FUEL_SLOT = 0;
    public static final int FIRST_INPUT_SLOT = 1;
    public static final int FIRST_OUTPUT_SLOT = 2;
    public static final int SECOND_INPUT_SLOT = 3;
    public static final int SECOND_OUTPUT_SLOT = 4;
    public static final int MAX_CHARGE = 330;
    public static final int CHARGE_PER_DUST = 3;
    public static final int DATA_CHARGE = 0;
    public static final int DATA_FIRST_COST = 1;
    public static final int DATA_SECOND_COST = 2;
    public static final int DATA_FIRST_DISABLED = 3;
    public static final int DATA_SECOND_DISABLED = 4;
    public static final int DATA_COUNT = 5;

    private static final String CHARGE_TAG = "charge";
    private static final String FIRST_DISABLED_TAG = "first_disabled";
    private static final String SECOND_DISABLED_TAG = "second_disabled";
    private static final int[] SLOTS = IntStream.range(0, SLOT_COUNT).toArray();

    private DefaultedList<ItemStack> items = DefaultedList.ofSize(SLOT_COUNT, ItemStack.EMPTY);
    private int charge;
    private int firstCost;
    private int secondCost;
    private boolean firstDisabled;
    private boolean secondDisabled;
    public StellarConverterBlockEntity(BlockPos pos, BlockState state) {
        super(PGCBlockEntities.STELLAR_CONVERTER.get(), pos, state);
    }

    public static void serverTick(World level, BlockPos pos, BlockState state, StellarConverterBlockEntity converter) {
        converter.tick();
    }

    public static boolean isFuel(ItemStack stack) {
        return stack.isOf(PGCItems.DUST_OF_AZOTH.get());
    }

    public static boolean accepts(int index, ItemStack stack) {
        return switch (index) {
            case FUEL_SLOT -> isFuel(stack);
            case FIRST_OUTPUT_SLOT, SECOND_OUTPUT_SLOT -> false;
            default -> true;
        };
    }

    @Override
    public void readNbt(NbtCompound tag) {
        super.readNbt(tag);
        charge = tag.getInt(CHARGE_TAG);
        firstDisabled = tag.getBoolean(FIRST_DISABLED_TAG);
        secondDisabled = tag.getBoolean(SECOND_DISABLED_TAG);
    }

    @Override
    protected void writeNbt(NbtCompound tag) {
        super.writeNbt(tag);
        tag.putInt(CHARGE_TAG, charge);
        tag.putBoolean(FIRST_DISABLED_TAG, firstDisabled);
        tag.putBoolean(SECOND_DISABLED_TAG, secondDisabled);
    }

    @Override
    public void writeScreenOpeningData(net.minecraft.server.network.ServerPlayerEntity player, net.minecraft.network.PacketByteBuf buffer) {
        buffer.writeBlockPos(pos);
    }

    @Override
    protected Text getContainerName() {
        return Text.translatable("block.primogemcraft.stellar_converter");
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

    private boolean acceptsInput(int index, ItemStack stack) {
        return accepts(index, stack) && (index != FIRST_INPUT_SLOT || !firstDisabled)
                && (index != SECOND_INPUT_SLOT || !secondDisabled);
    }

    @Override
    protected ScreenHandler createScreenHandler(int id, PlayerInventory inventory) {
        return new StellarConverterMenu(id, inventory, this, propertyDelegate(), ScreenHandlerContext.create(world, pos));
    }

    public PropertyDelegate propertyDelegate() {
        return new PropertyDelegate() {
            @Override
            public int size() {
                return DATA_COUNT;
            }

            @Override
            public int get(int index) {
                return switch (index) {
                    case DATA_CHARGE -> charge;
                    case DATA_FIRST_COST -> firstCost;
                    case DATA_SECOND_COST -> secondCost;
                    case DATA_FIRST_DISABLED -> firstDisabled ? 1 : 0;
                    case DATA_SECOND_DISABLED -> secondDisabled ? 1 : 0;
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                switch (index) {
                    case DATA_CHARGE -> charge = value;
                    case DATA_FIRST_COST -> firstCost = value;
                    case DATA_SECOND_COST -> secondCost = value;
                    case DATA_FIRST_DISABLED -> firstDisabled = value != 0;
                    case DATA_SECOND_DISABLED -> secondDisabled = value != 0;
                }
                markDirty();
            }
        };
    }

    @Override
    public int[] getAvailableSlots(Direction side) {
        return SLOTS;
    }

    @Override
    public boolean canInsert(int index, ItemStack stack, @Nullable Direction direction) {
        return acceptsInput(index, stack) && (index == FUEL_SLOT || !isFuel(stack));
    }

    @Override
    public boolean canExtract(int index, ItemStack stack, Direction direction) {
        return index == FIRST_OUTPUT_SLOT || index == SECOND_OUTPUT_SLOT;
    }

    private void tick() {
        var changed = chargeFromFuel();
        changed |= convert(FIRST_INPUT_SLOT, FIRST_OUTPUT_SLOT);
        changed |= convert(SECOND_INPUT_SLOT, SECOND_OUTPUT_SLOT);
        if (changed) markDirty();
    }

    private boolean chargeFromFuel() {
        if (charge + CHARGE_PER_DUST > MAX_CHARGE) return false;
        var fuel = items.get(FUEL_SLOT);
        if (!isFuel(fuel)) return false;
        fuel.decrement(1);
        charge += CHARGE_PER_DUST;
        return true;
    }

    private boolean convert(int inputSlot, int outputSlot) {
        if (inputSlot == FIRST_INPUT_SLOT ? firstDisabled : secondDisabled) {
            if (inputSlot == FIRST_INPUT_SLOT) firstCost = 0;
            else secondCost = 0;
            return false;
        }
        var input = items.get(inputSlot);
        var conversion = findConversion(input);
        var cost = conversion == null ? 0 : conversion.cost();
        if (inputSlot == FIRST_INPUT_SLOT) firstCost = cost;
        else secondCost = cost;
        if (conversion == null || charge < cost) return false;
        var output = items.get(outputSlot);
        if (!output.isEmpty() && !ItemStack.canCombine(output, conversion.output())) return false;
        var stored = output.isEmpty() ? 0 : output.getCount();
        if (stored + conversion.output().getCount() > conversion.output().getMaxCount()) return false;
        input.decrement(conversion.input().getCount());
        if (output.isEmpty()) items.set(outputSlot, conversion.output().copy());
        else output.increment(conversion.output().getCount());
        charge -= cost;
        return true;
    }

    private StellarConverterRecipe.Conversion findConversion(ItemStack stack) {
        if (stack.isEmpty() || !(world instanceof ServerWorld server)) return null;
        for (var holder : server.getRecipeManager().listAllOfType(PGCRecipeTypes.STELLAR_CONVERTER.get())) {
            var conversion = holder.match(stack);
            if (conversion != null) return conversion;
        }
        return null;
    }

    @Override
    public boolean canPlayerUse(net.minecraft.entity.player.PlayerEntity player) {
        return net.minecraft.inventory.Inventory.canPlayerUse(this, player);
    }
}
