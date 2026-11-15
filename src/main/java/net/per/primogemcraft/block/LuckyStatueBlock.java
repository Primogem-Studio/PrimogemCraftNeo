package net.per.primogemcraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.ActionResult;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.Hand;


public class LuckyStatueBlock extends PGCHorizontalBlock {
    private static final int LUCK_AMPLIFIER = 2;
    private static final int BEACON_COLOR = 0xFFFFFF00;

    public LuckyStatueBlock(AbstractBlock.Settings properties, VoxelShape shape) {
        super(properties, shape);
    }

    public float[] getBeaconColorMultiplier(BlockState state, WorldView level, BlockPos pos, BlockPos beaconPos) {
        return new float[]{1.0F, 1.0F, 0.0F};
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient()) player.addStatusEffect(new StatusEffectInstance(StatusEffects.LUCK, StatusEffectInstance.INFINITE, LUCK_AMPLIFIER));
        return ActionResult.SUCCESS;
    }
}
