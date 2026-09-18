package net.per.primogemcraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.per.primogemcraft.block.entity.MoraPileBlockEntity;
import net.per.primogemcraft.entity.misc.FallingMoraPileEntity;
import net.per.primogemcraft.registry.PGCBlocks;
import net.per.primogemcraft.registry.PGCItems;

import java.util.List;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public class MoraPileBlock extends FallingBlock implements EntityBlock {
    public static final MapCodec<MoraPileBlock> CODEC = simpleCodec(MoraPileBlock::new);
    public static final IntegerProperty TIER = IntegerProperty.create("tier", 0, 3);
    public static final IntegerProperty BASE_AMOUNT = IntegerProperty.create("base_amount", 1, 4);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public static final int MAX_TIER = 3;

    private static final int[] UPGRADE_STORED = {5, 8, 21, 26};
    private static final int[] TIER_AMOUNT = {0, 5, 13, 34};
    private static final float DEPOSIT_VOLUME = 0.5F;
    private static final float DEPOSIT_PITCH_MIN = 1.0F;
    private static final float DEPOSIT_PITCH_MAX = 1.1F;
    private static final TagKey<Block> MORA_PILES = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(MOD_ID, "mora_piles"));

    public MoraPileBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(TIER, 0).setValue(BASE_AMOUNT, 1));
    }

    @Override
    protected MapCodec<? extends FallingBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, TIER, BASE_AMOUNT);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite()).setValue(BASE_AMOUNT, 4);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        var offset = state.getOffset(level, pos);
        return shape(state).move(offset.x, offset.y, offset.z);
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @SuppressWarnings("deprecation")
    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return state.getValue(TIER) == MAX_TIER ? 2 : 1;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (stack.isEmpty() || !stack.is(PGCItems.MORA.get())) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if (level.isClientSide()) return ItemInteractionResult.SUCCESS;
        if (deposit(level, pos, state)) stack.consume(1, player);
        return ItemInteractionResult.SUCCESS;
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos fromPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, fromPos, movedByPiston);
        level.scheduleTick(pos, this, getDelayAfterPlace());
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        int tier = state.getValue(TIER);
        var blockEntity = level.getBlockEntity(pos);
        var downgrade = tier > 0 && !level.isClientSide() && !player.isCreative();
        var removed = super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
        if (downgrade && removed && blockEntity instanceof MoraPileBlockEntity pile) {
            var remaining = state.setValue(TIER, tier - 1);
            if (level.setBlock(pos, remaining, Block.UPDATE_ALL)) {
                pile.retainAmount(amount(remaining, 0));
            }
        }
        return removed;
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        var blockEntity = params.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        var count = blockEntity instanceof MoraPileBlockEntity pile
                ? amount(state, pile.stored()) - pile.retainedAmount() : amount(state, 0);
        return count > 0 ? List.of(new ItemStack(PGCItems.MORA.get(), count)) : List.of();
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MoraPileBlockEntity(pos, state);
    }

    public static boolean deposit(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide() || !level.getBlockState(pos).equals(state)) return false;
        if (!(level.getBlockEntity(pos) instanceof MoraPileBlockEntity pile)) return false;
        int tier = state.getValue(TIER);
        var stored = amount(state, pile.stored()) - amount(state, 0) + 1;
        level.playSound(null, pos, SoundEvents.CHAIN_PLACE, SoundSource.BLOCKS, DEPOSIT_VOLUME,
                Mth.nextFloat(level.getRandom(), DEPOSIT_PITCH_MIN, DEPOSIT_PITCH_MAX));
        if (stored < upgradeStored(state)) {
            pile.setStored(stored);
            return true;
        }
        pile.setStored(0);
        if (!level.setBlock(pos, tier < MAX_TIER ? state.setValue(TIER, tier + 1) : PGCBlocks.PACKED_MORA_PILE.get().defaultBlockState(), Block.UPDATE_ALL)) {
            pile.setStored(stored - 1);
            return false;
        }
        return true;
    }

    public static int amount(BlockState state, int stored) {
        return state.getValue(BASE_AMOUNT) + TIER_AMOUNT[state.getValue(TIER)] + Mth.clamp(stored, 0, upgradeStored(state) - 1);
    }

    private static int upgradeStored(BlockState state) {
        var tier = state.getValue(TIER);
        return UPGRADE_STORED[tier] + (tier == MAX_TIER ? 4 - state.getValue(BASE_AMOUNT) : 0);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        var below = level.getBlockState(pos.below());
        if (pos.getY() < level.getMinBuildHeight()) return;
        if (isFree(below) || below.is(MORA_PILES) && !below.is(PGCBlocks.PACKED_MORA_PILE.get())) {
            FallingMoraPileEntity.fall(level, pos, state);
        }
    }

    private static VoxelShape shape(BlockState state) {
        return switch (state.getValue(TIER)) {
            case 1 -> switch (state.getValue(FACING)) {
                case NORTH -> Block.box(2, 0, 2, 12, 6, 12);
                case EAST -> Block.box(4, 0, 2, 14, 6, 12);
                case WEST -> Block.box(2, 0, 4, 12, 6, 14);
                default -> Block.box(4, 0, 4, 14, 6, 14);
            };
            case 2 -> Block.box(2, 0, 2, 14, 12, 14);
            case 3 -> Block.box(1, 0, 1, 15, 13, 15);
            default -> switch (state.getValue(FACING)) {
                case NORTH -> Block.box(4, 0, 4, 14, 4, 14);
                case EAST -> Block.box(2, 0, 4, 12, 4, 14);
                case WEST -> Block.box(4, 0, 2, 14, 4, 12);
                default -> Block.box(2, 0, 2, 12, 4, 12);
            };
        };
    }
}
