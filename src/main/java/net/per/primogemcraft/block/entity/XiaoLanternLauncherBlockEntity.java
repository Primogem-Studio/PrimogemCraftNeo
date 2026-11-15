package net.per.primogemcraft.block.entity;

import net.minecraft.util.math.BlockPos;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.BlockState;
import net.per.primogemcraft.registry.PGCBlockEntities;

public class XiaoLanternLauncherBlockEntity extends BlockEntity {
    private static final String TRIGGERS_KEY = "triggers";

    private int triggers;

    public XiaoLanternLauncherBlockEntity(BlockPos pos, BlockState state) {
        super(PGCBlockEntities.XIAO_LANTERN_LAUNCHER.get(), pos, state);
    }

    public int increment() {
        markDirty();
        return ++triggers;
    }

    @Override
    public void readNbt(NbtCompound tag) {
        super.readNbt(tag);
        triggers = tag.getInt(TRIGGERS_KEY);
    }

    @Override
    protected void writeNbt(NbtCompound tag) {
        super.writeNbt(tag);
        tag.putInt(TRIGGERS_KEY, triggers);
    }
}
