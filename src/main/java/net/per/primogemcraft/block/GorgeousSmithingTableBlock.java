package net.per.primogemcraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.util.math.BlockPos;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.util.hit.BlockHitResult;
import net.per.primogemcraft.block.entity.GorgeousSmithingTableBlockEntity;
import net.minecraft.util.Hand;

public class GorgeousSmithingTableBlock extends BlockWithEntity {
    public GorgeousSmithingTableBlock(AbstractBlock.Settings properties) {
        super(properties);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public ActionResult onUse(BlockState state, World level, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (level.isClient()) return ActionResult.SUCCESS;
        if (player instanceof ServerPlayerEntity serverPlayer && level.getBlockEntity(pos) instanceof GorgeousSmithingTableBlockEntity table)
            serverPlayer.openHandledScreen(table);
        return ActionResult.SUCCESS;
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new GorgeousSmithingTableBlockEntity(pos, state);
    }
}
