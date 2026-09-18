package net.per.primogemcraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.per.primogemcraft.registry.PGCItems;

import java.util.function.Function;

public class OtherworldCrystalClusterBlock extends PGCFaceAttachedBlock {
    public static final MapCodec<OtherworldCrystalClusterBlock> CODEC =
            simpleCodec(properties -> new OtherworldCrystalClusterBlock(properties, state -> Shapes.block()));

    private static final int PICKUP_DELAY = 10;

    public OtherworldCrystalClusterBlock(BlockBehaviour.Properties properties, Function<BlockState, VoxelShape> shape) {
        super(properties, shape);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos fromPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, fromPos, movedByPiston);
        if (!(level instanceof ServerLevel server)) return;
        level.destroyBlock(pos, false);
        var shard = new ItemEntity(server, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                new ItemStack(PGCItems.DIAMOND_SHARD.get()));
        shard.setPickUpDelay(PICKUP_DELAY);
        server.addFreshEntity(shard);
    }
}
