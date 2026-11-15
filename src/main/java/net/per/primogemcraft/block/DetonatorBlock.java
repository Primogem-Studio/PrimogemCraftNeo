package net.per.primogemcraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.util.math.BlockPos;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.ActionResult;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.util.shape.VoxelShape;
import net.per.primogemcraft.block.entity.UnidentifiedDollBlockEntity;
import net.per.primogemcraft.registry.PGCBlocks;
import net.minecraft.util.Hand;

public class DetonatorBlock extends PGCHorizontalBlock {
    private static final int REACH = 16;
    private static final int REACH_DOWN = 4;
    private static final int REACH_UP = 5;
    private static final int SELF_DESTRUCT_TICKS = 10;

    public DetonatorBlock(AbstractBlock.Settings properties, VoxelShape shape) {
        super(properties, shape);
    }

    @Override
    public ActionResult onUse(BlockState state, World level, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (level.isClient()) return ActionResult.SUCCESS;
        if (level instanceof ServerWorld server) {
            for (var target : BlockPos.iterate(pos.add(-REACH, -REACH_DOWN, -REACH), pos.add(REACH, REACH_UP, REACH)))
                arm(server, target);
            server.playSound(null, pos, SoundEvents.BLOCK_WOODEN_BUTTON_CLICK_OFF, SoundCategory.BLOCKS, 1.0F, 1.0F);
            server.scheduleBlockTick(pos, this, SELF_DESTRUCT_TICKS);
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld level, BlockPos pos, Random random) {
        level.breakBlock(pos, false);
    }

    private static void arm(ServerWorld level, BlockPos pos) {
        var state = level.getBlockState(pos);
        if (!state.isOf(PGCBlocks.UNIDENTIFIED_DOLL.get())) return;
        if (level.getBlockEntity(pos) instanceof UnidentifiedDollBlockEntity doll) doll.arm(level, pos.toImmutable(), state, false);
    }
}
