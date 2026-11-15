package net.per.primogemcraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import net.per.primogemcraft.block.entity.GorgeousSmithingTableBlockEntity;
import net.per.primogemcraft.system.menu.LinkedContainers;
import net.minecraft.world.InteractionHand;

public class GorgeousSmithingTableBlock extends BaseEntityBlock {
    public GorgeousSmithingTableBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (player instanceof ServerPlayer serverPlayer && level.getBlockEntity(pos) instanceof GorgeousSmithingTableBlockEntity table)
            NetworkHooks.openScreen(serverPlayer, table, buffer -> {
                buffer.writeBlockPos(pos);
                LinkedContainers.write(buffer, LinkedContainers.scan(level, pos));
            });
        return InteractionResult.SUCCESS;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GorgeousSmithingTableBlockEntity(pos, state);
    }
}
