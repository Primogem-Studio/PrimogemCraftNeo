package net.per.primogemcraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.per.primogemcraft.registry.PGCItems;

public class ACakeForYouBlock extends PGCHorizontalBlock {
    public static final MapCodec<ACakeForYouBlock> CODEC = simpleCodec(properties -> new ACakeForYouBlock(properties, Shapes.block()));
    public static final int MAX_BITES = 8;
    public static final IntegerProperty BITES = IntegerProperty.create("bites", 0, MAX_BITES);

    private static final int FOOD_RESTORE = 5;
    private static final float SATURATION_RESTORE = 4.0F;
    private static final int MAX_LEVEL = 20;
    private static final float VOLUME = 0.5F;
    private static final float PITCH = 1.0F;

    public ACakeForYouBlock(BlockBehaviour.Properties properties, VoxelShape shape) {
        super(properties, state -> state.getValue(BITES) == MAX_BITES ? Block.box(0, 0, 0, 16, 1, 16) : shape);
        registerDefaultState(defaultBlockState().setValue(BITES, 0));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(BITES);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!stack.is(PGCItems.MORA.get())) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        var bites = state.getValue(BITES);
        if (!level.isClientSide() && bites > 0 && level.setBlock(pos, state.setValue(BITES, bites - 1), Block.UPDATE_ALL)) {
            stack.shrink(1);
            level.playSound(null, pos, SoundEvents.WOOL_PLACE, SoundSource.BLOCKS, VOLUME, PITCH);
            level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
        }
        return ItemInteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (state.getValue(BITES) == MAX_BITES) return InteractionResult.PASS;
        var food = player.getFoodData();
        var hungry = food.getFoodLevel() < MAX_LEVEL;
        if (!level.isClientSide() && level.setBlock(pos, state.setValue(BITES, state.getValue(BITES) + 1), Block.UPDATE_ALL)) {
            if (hungry) food.setFoodLevel(Math.min(MAX_LEVEL, food.getFoodLevel() + FOOD_RESTORE));
            else food.setSaturation(Math.min(MAX_LEVEL, food.getSaturationLevel() + SATURATION_RESTORE));
            level.playSound(null, pos, SoundEvents.PLAYER_BURP, hungry ? SoundSource.NEUTRAL : SoundSource.PLAYERS, VOLUME, PITCH);
            level.gameEvent(player, GameEvent.EAT, pos);
        }
        return InteractionResult.SUCCESS;
    }
}
