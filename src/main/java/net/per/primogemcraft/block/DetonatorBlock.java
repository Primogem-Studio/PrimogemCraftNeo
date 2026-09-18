package net.per.primogemcraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.per.primogemcraft.block.entity.UnidentifiedDollBlockEntity;
import net.per.primogemcraft.registry.PGCBlocks;

public class DetonatorBlock extends PGCHorizontalBlock {
    public static final MapCodec<DetonatorBlock> CODEC = simpleCodec(properties -> new DetonatorBlock(properties, Shapes.block()));

    private static final int REACH = 16;
    private static final int REACH_DOWN = 4;
    private static final int REACH_UP = 5;
    private static final int SELF_DESTRUCT_TICKS = 10;

    public DetonatorBlock(BlockBehaviour.Properties properties, VoxelShape shape) {
        super(properties, shape);
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (level instanceof ServerLevel server) {
            for (var target : BlockPos.betweenClosed(pos.offset(-REACH, -REACH_DOWN, -REACH), pos.offset(REACH, REACH_UP, REACH)))
                arm(server, target);
            server.playSound(null, pos, SoundEvents.WOODEN_BUTTON_CLICK_OFF, SoundSource.BLOCKS, 1.0F, 1.0F);
            server.scheduleTick(pos, this, SELF_DESTRUCT_TICKS);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        level.destroyBlock(pos, false);
    }

    private static void arm(ServerLevel level, BlockPos pos) {
        var state = level.getBlockState(pos);
        if (!state.is(PGCBlocks.UNIDENTIFIED_DOLL.get())) return;
        if (level.getBlockEntity(pos) instanceof UnidentifiedDollBlockEntity doll) doll.arm(level, pos.immutable(), state, false);
    }
}
