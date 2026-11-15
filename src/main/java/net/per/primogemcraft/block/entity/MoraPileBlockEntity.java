package net.per.primogemcraft.block.entity;

import net.minecraft.util.math.BlockPos;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.BlockState;
import net.per.primogemcraft.registry.PGCBlockEntities;

public class MoraPileBlockEntity extends BlockEntity {
    private static final String STORED_KEY = "stored";

    private int stored;
    private int retainedAmount;

    public MoraPileBlockEntity(BlockPos pos, BlockState state) {
        super(PGCBlockEntities.MORA_PILE.get(), pos, state);
    }

    public int stored() {
        return stored;
    }

    public void setStored(int stored) {
        this.stored = Math.max(0, stored);
        markDirty();
    }

    public int retainedAmount() {
        return retainedAmount;
    }

    public void retainAmount(int amount) {
        retainedAmount = amount;
    }

    @Override
    public void readNbt(NbtCompound tag) {
        super.readNbt(tag);
        stored = Math.max(0, tag.getInt(STORED_KEY));
        retainedAmount = 0;
    }

    @Override
    protected void writeNbt(NbtCompound tag) {
        super.writeNbt(tag);
        tag.putInt(STORED_KEY, stored);
    }
}
