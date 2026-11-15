package net.per.primogemcraft.block;

import net.minecraft.util.math.BlockPos;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.random.Random;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.BlockState;
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

    public CurioCasketBlock(Settings properties) {
        super(properties);
    }

    @Override
    public void afterBreak(World level, PlayerEntity player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool) {
        super.afterBreak(level, player, pos, state, blockEntity, tool);
        if (!(player instanceof ServerPlayerEntity serverPlayer)) return;
        var options = options(serverPlayer);
        if (!options.isEmpty()) CurioChoice.open(serverPlayer, options, curio -> Curios.give(serverPlayer, curio));
    }

    private static List<ItemStack> options(ServerPlayerEntity player) {
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

    private static CurioGrade grade(Random random) {
        if (random.nextDouble() < S_CHANCE) return CurioGrade.S;
        if (random.nextDouble() < A_CHANCE) return CurioGrade.A;
        return CurioGrade.B;
    }
}
