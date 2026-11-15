package net.per.primogemcraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.Identifier;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.ActionResult;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.world.World;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.util.shape.VoxelShape;
import net.per.primogemcraft.block.entity.ContainerWindowBlockEntity;
import net.per.primogemcraft.system.event.EventRegistry;
import net.per.primogemcraft.system.event.RandomEvents;
import net.per.primogemcraft.util.Advancements;
import net.per.primogemcraft.util.PlayerFlags;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;
import net.minecraft.util.Hand;

public class TrashCanBlock extends PGCHorizontalBlock implements BlockEntityProvider, ContainerWindowBlock {
    public static final int CONTAINER_SIZE = 27;

    private static final Identifier LOOTED_FLAG = Identifier.of(MOD_ID, "trash/goods");
    private static final double FIRST_CHANCE = 0.5D;
    private static final double REPEAT_CHANCE = 0.05D;
    private static final float VOLUME = 0.5F;
    private static final float PITCH = 0.5F;

    public TrashCanBlock(AbstractBlock.Settings properties, VoxelShape shape) {
        super(properties, shape);
    }

    @Override
    public int containerSize() {
        return CONTAINER_SIZE;
    }

    @Override
    public ActionResult onUse(BlockState state, World level, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        ContainerWindowBlocks.open(level, pos, player);
        if (player instanceof ServerPlayerEntity serverPlayer) Advancements.grant(serverPlayer, "once_you_set_foot_on_this_path");
        return ActionResult.SUCCESS;
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new ContainerWindowBlockEntity(pos, state);
    }

    @Override
    public void onStateReplaced(BlockState state, World level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.isOf(newState.getBlock())) ContainerWindowBlocks.dropContents(level, pos);
        super.onStateReplaced(state, level, pos, newState, movedByPiston);
    }

    @Override
    public boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    @Override
    public int getComparatorOutput(BlockState state, World level, BlockPos pos) {
        return ScreenHandler.calculateComparatorOutput(level.getBlockEntity(pos));
    }

    @Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        super.onBreak(world, pos, state, player);
        if (!world.isClient() && player instanceof ServerPlayerEntity serverPlayer) roll(serverPlayer, world, pos);
    }

    private static void roll(ServerPlayerEntity player, World level, BlockPos pos) {
        var flags = PlayerFlags.of(player);
        var chance = flags.flag(LOOTED_FLAG) ? REPEAT_CHANCE : FIRST_CHANCE;
        if (player.getRandom().nextDouble() >= chance) return;
        EventRegistry.trigger(player, RandomEvents.trashGoods);
        level.playSound(null, pos, SoundEvents.ENTITY_ENDER_DRAGON_SHOOT, SoundCategory.NEUTRAL, VOLUME, PITCH);
        flags.setFlag(LOOTED_FLAG, true);
    }
}
