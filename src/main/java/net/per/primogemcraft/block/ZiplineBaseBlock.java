package net.per.primogemcraft.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.per.primogemcraft.block.entity.ZiplineBaseBlockEntity;
import net.per.primogemcraft.registry.PGCBlockEntities;
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

public class ZiplineBaseBlock extends Block implements EntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final IntegerProperty PART = IntegerProperty.create("part", 0, 8);

    public ZiplineBaseBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(PART, 4).setValue(FACING, Direction.SOUTH));
    }

    public static BlockPos center(BlockState state, BlockPos position) {
        var part = state.getValue(PART);
        return position.offset(1 - part % 3, 0, 1 - part / 3);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PART, FACING);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos position, BlockState state) {
        return state.getValue(PART) == 4 ? new ZiplineBaseBlockEntity(position, state) : null;
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return !level.isClientSide() && type == PGCBlockEntities.ZIPLINE_BASE.get()
                ? (world, position, blockState, entity) -> ZiplineBaseBlockEntity.tick(world, position, blockState, (ZiplineBaseBlockEntity) entity) : null;
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        var part = state.getValue(PART);
        var offset = new BlockPos(part % 3 - 1, 0, part / 3 - 1).rotate(rotation);
        return state.setValue(PART, (offset.getZ() + 1) * 3 + offset.getX() + 1)
                .setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        var part = state.getValue(PART);
        var x = part % 3 - 1;
        var z = part / 3 - 1;
        if (mirror == Mirror.LEFT_RIGHT) z = -z;
        if (mirror == Mirror.FRONT_BACK) x = -x;
        return state.setValue(PART, (z + 1) * 3 + x + 1).setValue(FACING, mirror.mirror(state.getValue(FACING)));
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
        if (level.isClientSide() || level.captureBlockSnapshots || state.is(replacement.getBlock())) return;
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
