package net.per.primogemcraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.util.math.BlockPos;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.ActionResult;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.block.ShapeContext;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.util.shape.VoxelShape;
import net.per.primogemcraft.block.entity.UnidentifiedDollBlockEntity;
import net.per.primogemcraft.registry.PGCBlockEntities;
import net.per.primogemcraft.registry.PGCSounds;
import net.minecraft.util.Hand;

public class UnidentifiedDollBlock extends BlockWithEntity {
    public static final BooleanProperty LIT = Properties.LIT;
    public static final int LIT_LIGHT = 9;

    private static final float PLANTED_VOLUME = 0.5F;
    private static final float DEFUSE_VOLUME = 0.5F;
    private static final float PRESS_VOLUME = 1.0F;

    private final VoxelShape shape;

    public UnidentifiedDollBlock(AbstractBlock.Settings properties, VoxelShape shape) {
        super(properties);
        this.shape = shape;
        setDefaultState(getStateManager().getDefaultState().with(LIT, false));
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView level, BlockPos pos, ShapeContext context) {
        var offset = state.getModelOffset(level, pos);
        return shape.offset(offset.x, offset.y, offset.z);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(LIT);
    }

    public void onPlaced(World level, BlockPos pos, BlockState state, net.minecraft.entity.LivingEntity placer, net.minecraft.item.ItemStack stack) {
        if (level.isClient()) return;
        level.playSound(null, pos, PGCSounds.BOMB_PLANTED.get(), SoundCategory.BLOCKS, PLANTED_VOLUME, 1.0F);
    }

    @Override
    public ActionResult onUse(BlockState state, World level, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (level.isClient()) return ActionResult.SUCCESS;
        if (level instanceof ServerWorld server && level.getBlockEntity(pos) instanceof UnidentifiedDollBlockEntity doll && !doll.ticking()) {
            doll.arm(server, pos, state, true);
            server.playSound(null, pos, PGCSounds.BOMB_TICK.get(), SoundCategory.BLOCKS, 1.0F, 1.0F);
            server.playSound(null, pos, SoundEvents.BLOCK_WOODEN_BUTTON_CLICK_OFF, SoundCategory.BLOCKS, PRESS_VOLUME, 1.0F);
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (!world.isClient() && ticking(world, pos))
            world.playSound(null, pos, PGCSounds.BOMB_DEFUSED.get(), SoundCategory.PLAYERS, DEFUSE_VOLUME, 1.0F);
        super.onBreak(world, pos, state, player);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new UnidentifiedDollBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World level, BlockState state, BlockEntityType<T> type) {
        if (level.isClient()) return null;
        return net.minecraft.block.BlockWithEntity.checkType(type, PGCBlockEntities.UNIDENTIFIED_DOLL.get(), UnidentifiedDollBlockEntity::tick);
    }

    private static boolean ticking(World level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof UnidentifiedDollBlockEntity doll && doll.ticking();
    }
}
