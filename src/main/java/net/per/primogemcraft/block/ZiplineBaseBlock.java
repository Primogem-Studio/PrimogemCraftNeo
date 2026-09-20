package net.per.primogemcraft.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.per.primogemcraft.entity.misc.ZiplineAnchorEntity;

public class ZiplineBaseBlock extends Block {
    public static final IntegerProperty PART = IntegerProperty.create("part", 0, 8);

    public ZiplineBaseBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(PART, 4));
    }

    public static BlockPos center(BlockState state, BlockPos position) {
        var part = state.getValue(PART);
        return position.offset(1 - part % 3, 0, 1 - part / 3);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PART);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos position, Player player, BlockHitResult hit) {
        if (!level.isClientSide()) {
            var anchor = ZiplineAnchorEntity.find(level, center(state, position));
            if (anchor != null) anchor.attach(player);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos position, BlockState replacement, boolean moving) {
        super.onRemove(state, level, position, replacement, moving);
        if (level.isClientSide() || state.is(replacement.getBlock())) return;
        var center = center(state, position);
        for (var index = 0; index < 9; index++) {
            var part = center.offset(index % 3 - 1, 0, index / 3 - 1);
            var other = level.getBlockState(part);
            if (other.is(this) && other.getValue(PART) == index) level.removeBlock(part, false);
        }
        var anchor = ZiplineAnchorEntity.find(level, center);
        if (anchor != null) anchor.discard();
    }
}
