package net.per.primogemcraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.util.math.BlockPos;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.block.Block;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.util.shape.VoxelShape;
import net.per.primogemcraft.registry.PGCItems;

import java.util.function.Function;

public class OtherworldCrystalClusterBlock extends PGCFaceAttachedBlock {
    private static final int PICKUP_DELAY = 10;

    public OtherworldCrystalClusterBlock(AbstractBlock.Settings properties, Function<BlockState, VoxelShape> shape) {
        super(properties, shape);
    }

    @Override
    public void neighborUpdate(BlockState state, World level, BlockPos pos, Block neighborBlock, BlockPos fromPos, boolean movedByPiston) {
        super.neighborUpdate(state, level, pos, neighborBlock, fromPos, movedByPiston);
        if (!(level instanceof ServerWorld server)) return;
        level.breakBlock(pos, false);
        var shard = new ItemEntity(server, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                new ItemStack(PGCItems.DIAMOND_SHARD.get()));
        shard.setPickupDelay(PICKUP_DELAY);
        server.spawnEntity(shard);
    }
}
