package net.per.primogemcraft.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
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
        setChanged();
    }

    public int retainedAmount() {
        return retainedAmount;
    }

    public void retainAmount(int amount) {
        retainedAmount = amount;
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        stored = Math.max(0, tag.getInt(STORED_KEY));
        retainedAmount = 0;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt(STORED_KEY, stored);
    }
}
