package net.per.primogemcraft.block;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.BlockMirror;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.DirectionProperty;
import net.per.primogemcraft.block.entity.ZiplineBaseBlockEntity;
import net.per.primogemcraft.registry.PGCBlockEntities;
import net.minecraft.util.ActionResult;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.hit.BlockHitResult;
import net.per.primogemcraft.entity.misc.ZiplineAnchorEntity;
import net.minecraft.util.Hand;

public class ZiplineBaseBlock extends Block implements BlockEntityProvider {
    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
    public static final IntProperty PART = IntProperty.of("part", 0, 8);

    public ZiplineBaseBlock(Settings properties) {
        super(properties);
        setDefaultState(getStateManager().getDefaultState().with(PART, 4).with(FACING, Direction.SOUTH));
    }

    public static BlockPos center(BlockState state, BlockPos position) {
        var part = state.get(PART);
        return position.add(1 - part % 3, 0, 1 - part / 3);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(PART, FACING);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos position, BlockState state) {
        return state.get(PART) == 4 ? new ZiplineBaseBlockEntity(position, state) : null;
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World level, BlockState state, BlockEntityType<T> type) {
        return !level.isClient() && type == PGCBlockEntities.ZIPLINE_BASE.get()
                ? (world, position, blockState, entity) -> ZiplineBaseBlockEntity.tick(world, position, blockState, (ZiplineBaseBlockEntity) entity) : null;
    }

    @Override
    public BlockState rotate(BlockState state, net.minecraft.util.BlockRotation rotation) {
        var part = state.get(PART);
        var offset = new BlockPos(part % 3 - 1, 0, part / 3 - 1).rotate(rotation);
        return state.with(PART, (offset.getZ() + 1) * 3 + offset.getX() + 1)
                .with(FACING, rotation.rotate(state.get(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, net.minecraft.util.BlockMirror mirror) {
        var part = state.get(PART);
        var x = part % 3 - 1;
        var z = part / 3 - 1;
        if (mirror == net.minecraft.util.BlockMirror.LEFT_RIGHT) z = -z;
        if (mirror == net.minecraft.util.BlockMirror.FRONT_BACK) x = -x;
        var facing = state.get(FACING);
        if (mirror == net.minecraft.util.BlockMirror.LEFT_RIGHT) facing = facing.rotateYCounterclockwise();
        else if (mirror == net.minecraft.util.BlockMirror.FRONT_BACK) facing = facing.rotateYClockwise();
        return state.with(PART, (z + 1) * 3 + x + 1).with(FACING, facing);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.INVISIBLE;
    }

    @Override
    public ActionResult onUse(BlockState state, World level, BlockPos position, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!level.isClient()) {
            var anchor = ZiplineAnchorEntity.find(level, center(state, position));
            if (anchor != null) anchor.attach(player);
        }
        return ActionResult.success(level.isClient());
    }

    @Override
    public void onStateReplaced(BlockState state, World level, BlockPos position, BlockState replacement, boolean moving) {
        super.onStateReplaced(state, level, position, replacement, moving);
        if (level.isClient() || false || state.isOf(replacement.getBlock())) return;
        var center = center(state, position);
        for (var index = 0; index < 9; index++) {
            var part = center.add(index % 3 - 1, 0, index / 3 - 1);
            var other = level.getBlockState(part);
            if (other.isOf(this) && other.get(PART) == index) level.removeBlock(part, false);
        }
        var anchor = ZiplineAnchorEntity.find(level, center);
        if (anchor != null) anchor.discard();
    }
}
