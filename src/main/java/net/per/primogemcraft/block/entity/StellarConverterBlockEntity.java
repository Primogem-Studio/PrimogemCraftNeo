package net.per.primogemcraft.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.per.primogemcraft.registry.PGCBlockEntities;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.registry.PGCRecipeTypes;
import net.per.primogemcraft.recipe.StellarConverterRecipe;
import net.per.primogemcraft.system.menu.StellarConverterMenu;
import org.jetbrains.annotations.Nullable;

import java.util.stream.IntStream;

public class StellarConverterBlockEntity extends BaseContainerBlockEntity implements ContainerData, WorldlyContainer {
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
    public static final int DATA_COUNT = 3;

    private static final String CHARGE_TAG = "charge";
    private static final int[] SLOTS = IntStream.range(0, SLOT_COUNT).toArray();

    private NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    private int charge;
    private int firstCost;
    private int secondCost;

    public StellarConverterBlockEntity(BlockPos pos, BlockState state) {
        super(PGCBlockEntities.STELLAR_CONVERTER.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, StellarConverterBlockEntity converter) {
        converter.tick();
    }

    public static boolean isFuel(ItemStack stack) {
        return stack.is(PGCItems.DUST_OF_AZOTH.get());
    }

    public static boolean accepts(int index, ItemStack stack) {
        return switch (index) {
            case FUEL_SLOT -> isFuel(stack);
            case FIRST_OUTPUT_SLOT, SECOND_OUTPUT_SLOT -> false;
            default -> true;
        };
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        charge = tag.getInt(CHARGE_TAG);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt(CHARGE_TAG, charge);
    }

    @Override
    public void writeClientSideData(AbstractContainerMenu menu, RegistryFriendlyByteBuf buffer) {
        buffer.writeBlockPos(worldPosition);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("block.primogemcraft.stellar_converter");
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
    public boolean canPlaceItem(int index, ItemStack stack) {
        return accepts(index, stack);
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
        return new StellarConverterMenu(id, inventory, this, this, ContainerLevelAccess.create(level, worldPosition));
    }

    @Override
    public int getCount() {
        return DATA_COUNT;
    }

    @Override
    public int get(int index) {
        return switch (index) {
            case DATA_CHARGE -> charge;
            case DATA_FIRST_COST -> firstCost;
            default -> secondCost;
        };
    }

    @Override
    public void set(int index, int value) {
        switch (index) {
            case DATA_CHARGE -> charge = value;
            case DATA_FIRST_COST -> firstCost = value;
            default -> secondCost = value;
        }
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return SLOTS;
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack stack, @Nullable Direction direction) {
        return accepts(index, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return index == FIRST_OUTPUT_SLOT || index == SECOND_OUTPUT_SLOT;
    }

    private void tick() {
        var changed = chargeFromFuel();
        changed |= convert(FIRST_INPUT_SLOT, FIRST_OUTPUT_SLOT);
        changed |= convert(SECOND_INPUT_SLOT, SECOND_OUTPUT_SLOT);
        if (changed) setChanged();
    }

    private boolean chargeFromFuel() {
        if (charge + CHARGE_PER_DUST > MAX_CHARGE) return false;
        var fuel = items.get(FUEL_SLOT);
        if (!isFuel(fuel)) return false;
        fuel.shrink(1);
        charge += CHARGE_PER_DUST;
        return true;
    }

    private boolean convert(int inputSlot, int outputSlot) {
        var input = items.get(inputSlot);
        var conversion = findConversion(input);
        var cost = conversion == null ? 0 : conversion.cost();
        if (inputSlot == FIRST_INPUT_SLOT) firstCost = cost;
        else secondCost = cost;
        if (conversion == null || charge < cost) return false;
        var output = items.get(outputSlot);
        if (!output.isEmpty() && !ItemStack.isSameItemSameComponents(output, conversion.output())) return false;
        var stored = output.isEmpty() ? 0 : output.getCount();
        if (stored + conversion.output().getCount() > conversion.output().getMaxStackSize()) return false;
        input.shrink(conversion.input().getCount());
        if (output.isEmpty()) items.set(outputSlot, conversion.output().copy());
        else output.grow(conversion.output().getCount());
        charge -= cost;
        return true;
    }

    private StellarConverterRecipe.Conversion findConversion(ItemStack stack) {
        if (stack.isEmpty() || !(level instanceof ServerLevel server)) return null;
        for (var holder : server.getRecipeManager().getAllRecipesFor(PGCRecipeTypes.STELLAR_CONVERTER.get())) {
            var conversion = holder.value().match(stack);
            if (conversion != null) return conversion;
        }
        return null;
    }
}
