package net.per.primogemcraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.per.primogemcraft.block.entity.ContainerWindowBlockEntity;
import net.per.primogemcraft.system.event.EventRegistry;
import net.per.primogemcraft.system.event.RandomEvents;
import net.per.primogemcraft.util.Advancements;
import net.per.primogemcraft.util.PlayerFlags;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public class TrashCanBlock extends PGCHorizontalBlock implements EntityBlock, ContainerWindowBlock {
    public static final MapCodec<TrashCanBlock> CODEC = simpleCodec(properties -> new TrashCanBlock(properties, Shapes.block()));
    public static final int CONTAINER_SIZE = 27;

    private static final ResourceLocation LOOTED_FLAG = ResourceLocation.fromNamespaceAndPath(MOD_ID, "trash/goods");
    private static final double FIRST_CHANCE = 0.5D;
    private static final double REPEAT_CHANCE = 0.05D;
    private static final float VOLUME = 0.5F;
    private static final float PITCH = 0.5F;

    public TrashCanBlock(BlockBehaviour.Properties properties, VoxelShape shape) {
        super(properties, shape);
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    public int containerSize() {
        return CONTAINER_SIZE;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        ContainerWindowBlocks.open(level, pos, player);
        if (player instanceof ServerPlayer serverPlayer) Advancements.grant(serverPlayer, "once_you_set_foot_on_this_path");
        return InteractionResult.SUCCESS;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ContainerWindowBlockEntity(pos, state);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) ContainerWindowBlocks.dropContents(level, pos);
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return AbstractContainerMenu.getRedstoneSignalFromBlockEntity(level.getBlockEntity(pos));
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        var removed = super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
        if (removed && !level.isClientSide() && player instanceof ServerPlayer serverPlayer) roll(serverPlayer, level, pos);
        return removed;
    }

    private static void roll(ServerPlayer player, Level level, BlockPos pos) {
        var flags = PlayerFlags.of(player);
        var chance = flags.flag(LOOTED_FLAG) ? REPEAT_CHANCE : FIRST_CHANCE;
        if (player.getRandom().nextDouble() >= chance) return;
        EventRegistry.trigger(player, RandomEvents.trashGoods);
        level.playSound(null, pos, SoundEvents.DRAGON_FIREBALL_EXPLODE, SoundSource.NEUTRAL, VOLUME, PITCH);
        flags.setFlag(LOOTED_FLAG, true);
    }
}
