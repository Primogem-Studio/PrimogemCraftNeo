package net.per.primogemcraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.util.TriState;
import net.per.primogemcraft.registry.PGCSounds;
import net.per.primogemcraft.util.Advancements;

public class March7thStatueBlock extends PGCHorizontalBlock {
    public static final MapCodec<March7thStatueBlock> CODEC = simpleCodec(properties -> new March7thStatueBlock(properties, Shapes.block()));

    private static final float PLACE_VOLUME = 0.4F;
    private static final float BREAK_VOLUME = 0.3F;
    private static final float PITCH = 1.0F;
    private static final float ENCHANT_POWER = 8.0F;

    public March7thStatueBlock(BlockBehaviour.Properties properties, VoxelShape shape) {
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
    public float getEnchantPowerBonus(BlockState state, LevelReader level, BlockPos pos) {
        return ENCHANT_POWER;
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        puzzled(level, pos);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos fromPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, fromPos, movedByPiston);
        puzzled(level, pos);
    }

    @Override
    public void attack(BlockState state, Level level, BlockPos pos, Player player) {
        puzzled(level, pos);
        if (player instanceof ServerPlayer serverPlayer) Advancements.grant(serverPlayer, "enough_stop_repeating");
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        var removed = super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
        startled(level, pos);
        return removed;
    }

    @Override
    public void wasExploded(Level level, BlockPos pos, Explosion explosion) {
        super.wasExploded(level, pos, explosion);
        startled(level, pos);
    }

    @Override
    protected void onProjectileHit(Level level, BlockState state, BlockHitResult hit, Projectile projectile) {
        startled(level, hit.getBlockPos());
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide()) {
            level.playSound(null, pos, PGCSounds.MARCH_7TH_GIGGLE.get(), SoundSource.BLOCKS, BREAK_VOLUME, PITCH);
            if (player instanceof ServerPlayer serverPlayer) Advancements.grant(serverPlayer, "repeat_maiden");
        }
        return InteractionResult.SUCCESS;
    }

    private static void puzzled(Level level, BlockPos pos) {
        if (level.isClientSide()) return;
        level.playSound(null, pos, PGCSounds.MARCH_7TH_PUZZLED.get(), SoundSource.BLOCKS, PLACE_VOLUME, PITCH);
    }

    private static void startled(Level level, BlockPos pos) {
        if (level.isClientSide()) return;
        level.playSound(null, pos, PGCSounds.MARCH_7TH_STARTLED.get(), SoundSource.BLOCKS, BREAK_VOLUME, PITCH);
    }
}
