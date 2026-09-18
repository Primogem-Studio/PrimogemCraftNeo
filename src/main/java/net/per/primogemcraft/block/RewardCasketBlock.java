package net.per.primogemcraft.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.per.primogemcraft.system.casket.CasketReward;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RewardCasketBlock extends Block {
    private static final float BREAK_EXHAUSTION = 0.005F;

    public RewardCasketBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool) {
        if (!(level instanceof ServerLevel server) || !(player instanceof ServerPlayer serverPlayer)) {
            super.playerDestroy(level, player, pos, state, blockEntity, tool);
            return;
        }
        player.awardStat(Stats.BLOCK_MINED.get(this));
        player.causeFoodExhaustion(BREAK_EXHAUSTION);
        var rewards = rewards(server, serverPlayer, pos, state, blockEntity, tool);
        if (!rewards.isEmpty()) CasketReward.open(serverPlayer, rewards);
    }

    private List<ItemStack> rewards(ServerLevel level, ServerPlayer player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool) {
        var params = new LootParams.Builder(level)
                .withParameter(LootContextParams.BLOCK_STATE, state)
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
                .withParameter(LootContextParams.TOOL, tool)
                .withOptionalParameter(LootContextParams.BLOCK_ENTITY, blockEntity)
                .withOptionalParameter(LootContextParams.THIS_ENTITY, player)
                .create(LootContextParamSets.BLOCK);
        return level.getServer().reloadableRegistries().getLootTable(getLootTable()).getRandomItems(params);
    }
}
