package net.per.primogemcraft.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.per.primogemcraft.registry.PGCBlockEntities;

public class XiaoLanternLauncherBlockEntity extends BlockEntity {
    private static final String TRIGGERS_KEY = "triggers";

    private int triggers;

    public XiaoLanternLauncherBlockEntity(BlockPos pos, BlockState state) {
        super(PGCBlockEntities.XIAO_LANTERN_LAUNCHER.get(), pos, state);
    }

    public int increment() {
        setChanged();
        return ++triggers;
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        triggers = tag.getInt(TRIGGERS_KEY);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt(TRIGGERS_KEY, triggers);
    }
}
