package net.per.primogemcraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.util.PlayerItems;

public class ACakeForYouBlock extends PGCHorizontalBlock {
    public static final MapCodec<ACakeForYouBlock> CODEC = simpleCodec(properties -> new ACakeForYouBlock(properties, Shapes.block()));

    private static final int FOOD_RESTORE = 5;
    private static final float SATURATION_RESTORE = 4.0F;
    private static final int MAX_LEVEL = 20;
    private static final float VOLUME = 0.5F;
    private static final float PITCH = 1.0F;

    public ACakeForYouBlock(BlockBehaviour.Properties properties, VoxelShape shape) {
        super(properties, shape);
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResult.SUCCESS;
        var food = player.getFoodData();
        var hungry = food.getFoodLevel() < MAX_LEVEL;
        var unfilled = food.getSaturationLevel() < MAX_LEVEL;
        if ((hungry || unfilled) && PlayerItems.take(serverPlayer, PGCItems.MORA.get(), 1)) {
            if (hungry) food.setFoodLevel(food.getFoodLevel() + FOOD_RESTORE);
            else food.setSaturation(food.getSaturationLevel() + SATURATION_RESTORE);
            level.playSound(null, pos, SoundEvents.PLAYER_BURP, hungry ? SoundSource.NEUTRAL : SoundSource.PLAYERS, VOLUME, PITCH);
        }
        return InteractionResult.SUCCESS;
    }
}
