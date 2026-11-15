package net.per.primogemcraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.util.math.BlockPos;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.sound.SoundCategory;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.Box;
import net.per.primogemcraft.block.entity.XiaoLanternLauncherBlockEntity;
import net.per.primogemcraft.entity.misc.XiaoLanternEntity;
import net.per.primogemcraft.registry.PGCBlocks;
import net.per.primogemcraft.registry.PGCEntities;
import net.per.primogemcraft.registry.PGCItems;

public class XiaoLanternLauncherBlock extends BlockWithEntity {
    private static final int TRIGGER_LIMIT = 11;
    private static final int DROP_PICKUP_DELAY = 10;
    private static final int DESTROY_EVENT = 2001;
    private static final float VOLUME = 1.0F;
    private static final float PITCH = 1.0F;

    public XiaoLanternLauncherBlock(AbstractBlock.Settings properties) {
        super(properties);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public void neighborUpdate(BlockState state, World level, BlockPos pos, Block neighborBlock, BlockPos fromPos, boolean movedByPiston) {
        super.neighborUpdate(state, level, pos, neighborBlock, fromPos, movedByPiston);
        if (level.isClient() || !level.isReceivingRedstonePower(pos)) return;
        var above = pos.up();
        if (!level.getBlockState(above).isAir() || !level.getEntitiesByClass(XiaoLanternEntity.class, new Box(above), e -> true).isEmpty()) {
            level.playSound(null, pos, SoundEvents.BLOCK_DISPENSER_FAIL, SoundCategory.BLOCKS, VOLUME, PITCH);
            return;
        }
        if (!(level instanceof ServerWorld server)) return;
        var lantern = PGCEntities.XIAO_LANTERN.get().spawn(server, above, SpawnReason.MOB_SUMMONED);
        if (lantern == null) return;
        lantern.setYaw(level.getRandom().nextFloat() * 360.0F);
        level.playSound(null, pos, SoundEvents.BLOCK_DISPENSER_DISPENSE, SoundCategory.BLOCKS, VOLUME, PITCH);
        if (state.isOf(PGCBlocks.CREATIVE_XIAO_LANTERN_LAUNCHER.get())) return;
        if (!(server.getBlockEntity(pos) instanceof XiaoLanternLauncherBlockEntity launcher)) return;
        if (launcher.increment() < TRIGGER_LIMIT) return;
        breakDown(server, pos, state);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new XiaoLanternLauncherBlockEntity(pos, state);
    }

    private static void breakDown(ServerWorld level, BlockPos pos, BlockState state) {
        var dropped = new ItemEntity(level, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                new ItemStack(PGCItems.XIAO_LANTERN_LAUNCHER.get()));
        dropped.setPickupDelay(DROP_PICKUP_DELAY);
        dropped.setNeverDespawn();
        level.spawnEntity(dropped);
        level.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
        level.syncWorldEvent(DESTROY_EVENT, pos, Block.getRawIdFromState(state));
        level.playSound(null, pos, SoundEvents.BLOCK_STONE_BREAK, SoundCategory.BLOCKS, VOLUME, PITCH);
    }
}
