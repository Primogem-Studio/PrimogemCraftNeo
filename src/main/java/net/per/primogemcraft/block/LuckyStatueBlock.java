package net.per.primogemcraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.FastColor;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.util.TriState;

public class LuckyStatueBlock extends PGCHorizontalBlock {
    public static final MapCodec<LuckyStatueBlock> CODEC = simpleCodec(properties -> new LuckyStatueBlock(properties, Shapes.block()));

    private static final int LUCK_AMPLIFIER = 2;
    private static final int BEACON_COLOR = FastColor.ARGB32.opaque(-256);

    public LuckyStatueBlock(BlockBehaviour.Properties properties, VoxelShape shape) {
        super(properties, shape);
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    public TriState canSustainPlant(BlockState state, BlockGetter level, BlockPos pos, Direction facing, BlockState plant) {
        return TriState.TRUE;
    }

    @Override
    public Integer getBeaconColorMultiplier(BlockState state, LevelReader level, BlockPos pos, BlockPos beaconPos) {
        return BEACON_COLOR;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide()) player.addEffect(new MobEffectInstance(MobEffects.LUCK, MobEffectInstance.INFINITE_DURATION, LUCK_AMPLIFIER));
        return InteractionResult.SUCCESS;
    }
}
