package net.per.primogemcraft.block;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.sound.SoundCategory;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.Hand;
import net.minecraft.util.ActionResult;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.fluid.FluidState;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.shape.VoxelShape;
import net.per.primogemcraft.block.entity.MoraPileBlockEntity;
import net.per.primogemcraft.entity.misc.FallingMoraPileEntity;
import net.per.primogemcraft.registry.PGCBlocks;
import net.per.primogemcraft.registry.PGCItems;

import java.util.List;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public class MoraPileBlock extends FallingBlock implements BlockEntityProvider {
    public static final IntProperty TIER = IntProperty.of("tier", 0, 3);
    public static final IntProperty BASE_AMOUNT = IntProperty.of("base_amount", 1, 4);
    public static final DirectionProperty FACING = HorizontalFacingBlock.FACING;

    public static final int MAX_TIER = 3;

    private static final int[] UPGRADE_STORED = {5, 8, 21, 26};
    private static final int[] TIER_AMOUNT = {0, 5, 13, 34};
    private static final float DEPOSIT_VOLUME = 0.5F;
    private static final float DEPOSIT_PITCH_MIN = 1.0F;
    private static final float DEPOSIT_PITCH_MAX = 1.1F;
    private static final TagKey<Block> MORA_PILES = TagKey.of(RegistryKeys.BLOCK, new Identifier(MOD_ID, "mora_piles"));

    public MoraPileBlock(AbstractBlock.Settings settings) {
        super(settings);
        setDefaultState(getStateManager().getDefaultState().with(FACING, Direction.NORTH).with(TIER, 0).with(BASE_AMOUNT, 1));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, TIER, BASE_AMOUNT);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite()).with(BASE_AMOUNT, 4);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        var offset = state.getModelOffset(world, pos);
        return shape(state).offset(offset.x, offset.y, offset.z);
    }

    @Override
    public BlockState rotate(BlockState state, net.minecraft.util.BlockRotation rotation) {
        return state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    @SuppressWarnings("deprecation")
    @Override
    public BlockState mirror(BlockState state, net.minecraft.util.BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING)));
    }

    @Override
    public boolean emitsRedstonePower(BlockState state) {
        return true;
    }

    @Override
    public int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return state.get(TIER) == MAX_TIER ? 2 : 1;
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        var stack = player.getStackInHand(hand);
        if (stack.isEmpty() || !stack.isOf(PGCItems.MORA.get())) return ActionResult.PASS;
        if (world.isClient()) return ActionResult.SUCCESS;
        if (deposit(world, pos, state)) stack.decrement(1);
        return ActionResult.CONSUME;
    }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify) {
        super.neighborUpdate(state, world, pos, block, fromPos, notify);
        world.scheduleBlockTick(pos, this, 2);
    }

    @Override
    public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, net.minecraft.entity.ai.pathing.NavigationType type) {
        return false;
    }

    @Override
    public List<ItemStack> getDroppedStacks(BlockState state, LootContextParameterSet.Builder builder) {
        var blockEntity = builder.getOptional(LootContextParameters.BLOCK_ENTITY);
        var count = blockEntity instanceof MoraPileBlockEntity pile
                ? amount(state, pile.stored()) - pile.retainedAmount() : amount(state, 0);
        return count > 0 ? List.of(new ItemStack(PGCItems.MORA.get(), count)) : List.of();
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new MoraPileBlockEntity(pos, state);
    }

    public static boolean deposit(World world, BlockPos pos, BlockState state) {
        if (world.isClient() || !world.getBlockState(pos).equals(state)) return false;
        if (!(world.getBlockEntity(pos) instanceof MoraPileBlockEntity pile)) return false;
        int tier = state.get(TIER);
        var stored = amount(state, pile.stored()) - amount(state, 0) + 1;
        world.playSound(null, pos, SoundEvents.BLOCK_CHAIN_PLACE, SoundCategory.BLOCKS, DEPOSIT_VOLUME,
                MathHelper.nextFloat(world.getRandom(), DEPOSIT_PITCH_MIN, DEPOSIT_PITCH_MAX));
        if (stored < upgradeStored(state)) {
            pile.setStored(stored);
            return true;
        }
        pile.setStored(0);
        if (!world.setBlockState(pos, tier < MAX_TIER ? state.with(TIER, tier + 1) : PGCBlocks.PACKED_MORA_PILE.get().getDefaultState(), Block.NOTIFY_ALL)) {
            pile.setStored(stored - 1);
            return false;
        }
        return true;
    }

    public static int amount(BlockState state, int stored) {
        return state.get(BASE_AMOUNT) + TIER_AMOUNT[state.get(TIER)] + MathHelper.clamp(stored, 0, upgradeStored(state) - 1);
    }

    private static int upgradeStored(BlockState state) {
        var tier = state.get(TIER);
        return UPGRADE_STORED[tier] + (tier == MAX_TIER ? 4 - state.get(BASE_AMOUNT) : 0);
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        var below = world.getBlockState(pos.down());
        if (pos.getY() < world.getBottomY()) return;
        if (below.isAir() || below.isIn(MORA_PILES) && !below.isOf(PGCBlocks.PACKED_MORA_PILE.get())) {
            FallingMoraPileEntity.fall(world, pos, state);
        }
    }

    private static VoxelShape shape(BlockState state) {
        return switch (state.get(TIER)) {
            case 1 -> switch (state.get(FACING)) {
                case NORTH -> Block.createCuboidShape(2, 0, 2, 12, 6, 12);
                case EAST -> Block.createCuboidShape(4, 0, 2, 14, 6, 12);
                case WEST -> Block.createCuboidShape(2, 0, 4, 12, 6, 14);
                default -> Block.createCuboidShape(4, 0, 4, 14, 6, 14);
            };
            case 2 -> Block.createCuboidShape(2, 0, 2, 14, 12, 14);
            case 3 -> Block.createCuboidShape(1, 0, 1, 15, 13, 15);
            default -> switch (state.get(FACING)) {
                case NORTH -> Block.createCuboidShape(4, 0, 4, 14, 4, 14);
                case EAST -> Block.createCuboidShape(2, 0, 4, 12, 4, 14);
                case WEST -> Block.createCuboidShape(4, 0, 2, 14, 4, 12);
                default -> Block.createCuboidShape(2, 0, 2, 12, 4, 12);
            };
        };
    }
}
