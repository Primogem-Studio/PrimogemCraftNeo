package net.per.primogemcraft.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.per.primogemcraft.system.curio.CurioChoice;
import net.per.primogemcraft.system.curio.CurioForm;
import net.per.primogemcraft.system.curio.CurioGrade;
import net.per.primogemcraft.system.curio.Curios;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CurioCasketBlock extends Block {
    private static final int OPTION_COUNT = 3;
    private static final int ROLL_ATTEMPTS = 10;
    private static final double S_CHANCE = 0.1D;
    private static final double A_CHANCE = 0.3D;

    public CurioCasketBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool) {
        super.playerDestroy(level, player, pos, state, blockEntity, tool);
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        var options = options(serverPlayer);
        if (!options.isEmpty()) CurioChoice.open(serverPlayer, options, curio -> Curios.give(serverPlayer, curio));
    }

    private static List<ItemStack> options(ServerPlayer player) {
        var random = player.getRandom();
        var grade = grade(random);
        var options = new ArrayList<ItemStack>();
        for (var attempt = 0; attempt < ROLL_ATTEMPTS && options.size() < OPTION_COUNT; attempt++) {
            var candidate = Curios.randomCurio(random, grade.tag(CurioForm.NORMAL));
            if (candidate.isEmpty()) break;
            if (contains(options, candidate)) continue;
            options.add(candidate);
        }
        return options;
    }

    private static boolean contains(List<ItemStack> options, ItemStack candidate) {
        for (var option : options) if (option.getItem() == candidate.getItem()) return true;
        return false;
    }

    private static CurioGrade grade(RandomSource random) {
        if (random.nextDouble() < S_CHANCE) return CurioGrade.S;
        if (random.nextDouble() < A_CHANCE) return CurioGrade.A;
        return CurioGrade.B;
    }
}
