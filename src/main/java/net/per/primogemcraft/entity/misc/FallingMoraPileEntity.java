package net.per.primogemcraft.entity.misc;

import net.minecraft.util.math.BlockPos;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.per.primogemcraft.block.MoraPileBlock;
import net.per.primogemcraft.registry.PGCEntities;
import net.per.primogemcraft.registry.PGCItems;

public class FallingMoraPileEntity extends FallingBlockEntity {
    public FallingMoraPileEntity(EntityType<? extends FallingMoraPileEntity> type, World level) {
        super(type, level);
    }

    public static FallingMoraPileEntity fall(World level, BlockPos pos, BlockState state) {
        var entity = new FallingMoraPileEntity(PGCEntities.FALLING_MORA_PILE.get(), level);
        var data = new NbtCompound();
        data.put("BlockState", NbtHelper.fromBlockState(state));
        data.putBoolean("DropItem", true);
        var blockEntity = level.getBlockEntity(pos);
        if (blockEntity != null) data.put("TileEntityData", blockEntity.createNbt());
        entity.readCustomDataFromNbt(data);
        entity.setHurtEntities(0.0F, 0);
        entity.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        entity.prevX = entity.getX();
        entity.prevY = entity.getY();
        entity.prevZ = entity.getZ();
        entity.setFallingBlockPos(pos);
        if (level.setBlockState(pos, state.getFluidState().getBlockState(), Block.NOTIFY_ALL)) {
            level.spawnEntity(entity);
        }
        return entity;
    }

    @Override
    public ItemEntity dropStack(ItemStack stack, float offsetY) {
        if (stack.isOf(PGCItems.MORA_PILE.get())) {
            var stored = getBlockState().contains(MoraPileBlock.TIER) ? getBlockState().get(MoraPileBlock.TIER) : 0;
            stack = new ItemStack(PGCItems.MORA.get(), MoraPileBlock.amount(getBlockState(), stored));
        }
        return super.dropStack(stack, offsetY);
    }

    /** 1.21 用 DimensionTransition 承载传送信息；1.20.1 直接传目标 ServerLevel。 */
    @Override
    public Entity moveToWorld(net.minecraft.server.world.ServerWorld destination) {
        var entity = super.moveToWorld(destination);
        
        return entity;
    }
}
