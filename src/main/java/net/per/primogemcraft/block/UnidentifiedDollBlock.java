package net.per.primogemcraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.per.primogemcraft.block.entity.UnidentifiedDollBlockEntity;
import net.per.primogemcraft.registry.PGCBlockEntities;
import net.per.primogemcraft.registry.PGCSounds;

public class UnidentifiedDollBlock extends BaseEntityBlock {
    public static final MapCodec<UnidentifiedDollBlock> CODEC = simpleCodec(properties -> new UnidentifiedDollBlock(properties, Shapes.block()));
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final int LIT_LIGHT = 9;

    private static final float PLANTED_VOLUME = 0.5F;
    private static final float DEFUSE_VOLUME = 0.5F;
    private static final float PRESS_VOLUME = 1.0F;

    private final VoxelShape shape;

    public UnidentifiedDollBlock(BlockBehaviour.Properties properties, VoxelShape shape) {
        super(properties);
        this.shape = shape;
        registerDefaultState(stateDefinition.any().setValue(LIT, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        var offset = state.getOffset(level, pos);
        return shape.move(offset.x, offset.y, offset.z);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIT);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (level.isClientSide() || oldState.is(state.getBlock())) return;
        level.playSound(null, pos, PGCSounds.BOMB_PLANTED.get(), SoundSource.BLOCKS, PLANTED_VOLUME, 1.0F);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (level instanceof ServerLevel server && level.getBlockEntity(pos) instanceof UnidentifiedDollBlockEntity doll && !doll.ticking()) {
            doll.arm(server, pos, state, true);
            server.playSound(null, pos, PGCSounds.BOMB_TICK.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
            server.playSound(null, pos, SoundEvents.WOODEN_BUTTON_CLICK_OFF, SoundSource.BLOCKS, PRESS_VOLUME, 1.0F);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void attack(BlockState state, Level level, BlockPos pos, Player player) {
        if (level.isClientSide() || !ticking(level, pos)) return;
        level.playSound(null, pos, PGCSounds.BOMB_DEFUSING.get(), SoundSource.PLAYERS, DEFUSE_VOLUME, 1.0F);
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        if (!level.isClientSide() && ticking(level, pos))
            level.playSound(null, pos, PGCSounds.BOMB_DEFUSED.get(), SoundSource.PLAYERS, DEFUSE_VOLUME, 1.0F);
        return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new UnidentifiedDollBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) return null;
        return createTickerHelper(type, PGCBlockEntities.UNIDENTIFIED_DOLL.get(), UnidentifiedDollBlockEntity::tick);
    }

    private static boolean ticking(Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof UnidentifiedDollBlockEntity doll && doll.ticking();
    }
}
