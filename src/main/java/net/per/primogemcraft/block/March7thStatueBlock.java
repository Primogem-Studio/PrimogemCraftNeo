package net.per.primogemcraft.block;

import net.minecraft.util.math.BlockPos;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.ActionResult;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.shape.VoxelShape;

import net.per.primogemcraft.registry.PGCSounds;
import net.per.primogemcraft.util.Advancements;

public class March7thStatueBlock extends PGCHorizontalBlock {
    private static final float PLACE_VOLUME = 0.4F;
    private static final float BREAK_VOLUME = 0.3F;
    private static final float PITCH = 1.0F;
    private static final float ENCHANT_POWER = 8.0F;

    public March7thStatueBlock(AbstractBlock.Settings settings, VoxelShape shape) {
        super(settings, shape);
    }

    public float getEnchantingPowerBonus(BlockState state, WorldView world, BlockPos pos) {
        return ENCHANT_POWER;
    }

    public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.onPlaced(world, pos, state, placer, stack);
        puzzled(world, pos);
    }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify) {
        super.neighborUpdate(state, world, pos, block, fromPos, notify);
        puzzled(world, pos);
    }

    @Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        super.onBreak(world, pos, state, player);
        startled(world, pos);
    }

    @Override
    public void onProjectileHit(World world, BlockState state, BlockHitResult hit, ProjectileEntity projectile) {
        startled(world, hit.getBlockPos());
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient()) {
            world.playSound(null, pos, PGCSounds.MARCH_7TH_GIGGLE.get(), SoundCategory.BLOCKS, BREAK_VOLUME, PITCH);
            if (player instanceof ServerPlayerEntity serverPlayer) Advancements.grant(serverPlayer, "repeat_maiden");
        }
        return ActionResult.SUCCESS;
    }

    private static void puzzled(World world, BlockPos pos) {
        if (world.isClient()) return;
        world.playSound(null, pos, PGCSounds.MARCH_7TH_PUZZLED.get(), SoundCategory.BLOCKS, PLACE_VOLUME, PITCH);
    }

    private static void startled(World world, BlockPos pos) {
        if (world.isClient()) return;
        world.playSound(null, pos, PGCSounds.MARCH_7TH_STARTLED.get(), SoundCategory.BLOCKS, BREAK_VOLUME, PITCH);
    }
}
