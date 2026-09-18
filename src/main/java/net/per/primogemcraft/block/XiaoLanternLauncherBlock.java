package net.per.primogemcraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.per.primogemcraft.block.entity.XiaoLanternLauncherBlockEntity;
import net.per.primogemcraft.entity.misc.XiaoLanternEntity;
import net.per.primogemcraft.registry.PGCBlocks;
import net.per.primogemcraft.registry.PGCEntities;
import net.per.primogemcraft.registry.PGCItems;

public class XiaoLanternLauncherBlock extends BaseEntityBlock {
    public static final MapCodec<XiaoLanternLauncherBlock> CODEC = simpleCodec(XiaoLanternLauncherBlock::new);

    private static final int TRIGGER_LIMIT = 11;
    private static final int DROP_PICKUP_DELAY = 10;
    private static final int DESTROY_EVENT = 2001;
    private static final float VOLUME = 1.0F;
    private static final float PITCH = 1.0F;

    public XiaoLanternLauncherBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos fromPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, fromPos, movedByPiston);
        if (level.isClientSide() || !level.hasNeighborSignal(pos)) return;
        var above = pos.above();
        if (!level.getBlockState(above).isAir() || !level.getEntitiesOfClass(XiaoLanternEntity.class, new AABB(above)).isEmpty()) {
            level.playSound(null, pos, SoundEvents.DISPENSER_FAIL, SoundSource.BLOCKS, VOLUME, PITCH);
            return;
        }
        if (!(level instanceof ServerLevel server)) return;
        var lantern = PGCEntities.XIAO_LANTERN.get().spawn(server, above, MobSpawnType.MOB_SUMMONED);
        if (lantern == null) return;
        lantern.setYRot(level.getRandom().nextFloat() * 360.0F);
        level.playSound(null, pos, SoundEvents.DISPENSER_DISPENSE, SoundSource.BLOCKS, VOLUME, PITCH);
        if (state.is(PGCBlocks.CREATIVE_XIAO_LANTERN_LAUNCHER.get())) return;
        if (!(server.getBlockEntity(pos) instanceof XiaoLanternLauncherBlockEntity launcher)) return;
        if (launcher.increment() < TRIGGER_LIMIT) return;
        breakDown(server, pos, state);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new XiaoLanternLauncherBlockEntity(pos, state);
    }

    private static void breakDown(ServerLevel level, BlockPos pos, BlockState state) {
        var dropped = new ItemEntity(level, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                new ItemStack(PGCItems.XIAO_LANTERN_LAUNCHER.get()));
        dropped.setPickUpDelay(DROP_PICKUP_DELAY);
        dropped.setUnlimitedLifetime();
        level.addFreshEntity(dropped);
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        level.levelEvent(DESTROY_EVENT, pos, Block.getId(state));
        level.playSound(null, pos, SoundEvents.STONE_BREAK, SoundSource.BLOCKS, VOLUME, PITCH);
    }
}
