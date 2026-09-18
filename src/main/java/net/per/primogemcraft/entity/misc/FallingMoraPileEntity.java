package net.per.primogemcraft.entity.misc;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.DimensionTransition;
import net.per.primogemcraft.block.MoraPileBlock;
import net.per.primogemcraft.registry.PGCEntities;
import net.per.primogemcraft.registry.PGCItems;

public class FallingMoraPileEntity extends FallingBlockEntity {
    public FallingMoraPileEntity(EntityType<? extends FallingMoraPileEntity> type, Level level) {
        super(type, level);
    }

    public static FallingMoraPileEntity fall(Level level, BlockPos pos, BlockState state) {
        var entity = new FallingMoraPileEntity(PGCEntities.FALLING_MORA_PILE.get(), level);
        var data = new CompoundTag();
        data.put("BlockState", NbtUtils.writeBlockState(state));
        data.putBoolean("DropItem", true);
        var blockEntity = level.getBlockEntity(pos);
        if (blockEntity != null) data.put("TileEntityData", blockEntity.saveWithoutMetadata(level.registryAccess()));
        entity.readAdditionalSaveData(data);
        entity.blocksBuilding = true;
        entity.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        entity.xo = entity.getX();
        entity.yo = entity.getY();
        entity.zo = entity.getZ();
        entity.setStartPos(pos);
        if (level.setBlock(pos, state.getFluidState().createLegacyBlock(), Block.UPDATE_ALL)) {
            level.addFreshEntity(entity);
        }
        return entity;
    }

    @Override
    public ItemEntity spawnAtLocation(ItemStack stack, float offsetY) {
        if (stack.is(PGCItems.MORA_PILE.get())) {
            var stored = blockData == null ? 0 : blockData.getInt("stored");
            stack = new ItemStack(PGCItems.MORA.get(), MoraPileBlock.amount(getBlockState(), stored));
        }
        return super.spawnAtLocation(stack, offsetY);
    }

    @Override
    public Entity changeDimension(DimensionTransition transition) {
        var entity = super.changeDimension(transition);
        forceTickAfterTeleportToDuplicate = false;
        return entity;
    }
}
