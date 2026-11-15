package net.per.primogemcraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.util.math.BlockPos;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.world.World;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.util.hit.BlockHitResult;
import net.per.primogemcraft.block.entity.StellarConverterBlockEntity;
import net.per.primogemcraft.registry.PGCBlockEntities;
import net.minecraft.util.Hand;

public class StellarConverterBlock extends BlockWithEntity {
    public StellarConverterBlock(AbstractBlock.Settings properties) {
        super(properties);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public ActionResult onUse(BlockState state, World level, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (level.isClient()) return ActionResult.SUCCESS;
        if (player instanceof ServerPlayerEntity serverPlayer && level.getBlockEntity(pos) instanceof StellarConverterBlockEntity converter)
            serverPlayer.openHandledScreen(converter);
        return ActionResult.SUCCESS;
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new StellarConverterBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World level, BlockState state, BlockEntityType<T> type) {
        return level.isClient() ? null : net.minecraft.block.BlockWithEntity.checkType(type, PGCBlockEntities.STELLAR_CONVERTER.get(), StellarConverterBlockEntity::serverTick);
    }

    @Override
    public boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    @Override
    public int getComparatorOutput(BlockState state, World level, BlockPos pos) {
        return ScreenHandler.calculateComparatorOutput(level.getBlockEntity(pos));
    }
}
