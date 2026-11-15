package net.per.primogemcraft.block;

import net.minecraft.util.math.BlockPos;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.util.math.Vec3d;
import net.per.primogemcraft.system.casket.CasketReward;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RewardCasketBlock extends Block {
    private static final float BREAK_EXHAUSTION = 0.005F;

    public RewardCasketBlock(Settings properties) {
        super(properties);
    }

    @Override
    public void afterBreak(World level, PlayerEntity player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool) {
        if (!(level instanceof ServerWorld server) || !(player instanceof ServerPlayerEntity serverPlayer)) {
            super.afterBreak(level, player, pos, state, blockEntity, tool);
            return;
        }
        player.incrementStat(Stats.MINED.getOrCreateStat(this));
        player.addExhaustion(BREAK_EXHAUSTION);
        var rewards = rewards(server, serverPlayer, pos, state, blockEntity, tool);
        if (!rewards.isEmpty()) CasketReward.open(serverPlayer, rewards);
    }

    private List<ItemStack> rewards(ServerWorld level, ServerPlayerEntity player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool) {
        var params = new LootContextParameterSet.Builder(level)
                .add(LootContextParameters.BLOCK_STATE, state)
                .add(LootContextParameters.ORIGIN, Vec3d.ofCenter(pos))
                .add(LootContextParameters.TOOL, tool)
                .addOptional(LootContextParameters.BLOCK_ENTITY, blockEntity)
                .addOptional(LootContextParameters.THIS_ENTITY, player)
                .build(LootContextTypes.BLOCK);
        return level.getServer().getLootManager().getLootTable(getLootTableId()).generateLoot(params);
    }
}
