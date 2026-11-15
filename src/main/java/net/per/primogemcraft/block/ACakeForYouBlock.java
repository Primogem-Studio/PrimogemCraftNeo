package net.per.primogemcraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.util.math.BlockPos;
import net.minecraft.sound.SoundEvents;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.block.Block;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.world.event.GameEvent;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.util.shape.VoxelShape;
import net.per.primogemcraft.registry.PGCItems;

public class ACakeForYouBlock extends PGCHorizontalBlock {
    public static final int MAX_BITES = 8;
    public static final IntProperty BITES = IntProperty.of("bites", 0, MAX_BITES);

    private static final int FOOD_RESTORE = 5;
    private static final float SATURATION_RESTORE = 4.0F;
    private static final int MAX_LEVEL = 20;
    private static final float VOLUME = 0.5F;
    private static final float PITCH = 1.0F;

    public ACakeForYouBlock(AbstractBlock.Settings properties, VoxelShape shape) {
        super(properties, state -> state.get(BITES) == MAX_BITES ? Block.createCuboidShape(0, 0, 0, 16, 1, 16) : shape);
        setDefaultState(getDefaultState().with(BITES, 0));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(BITES);
    }

    /**
     * 1.21 把交互拆成 {@code useItemOn}（持物）与 {@code useWithoutItem}（空手）；
     * 1.20.1 只有单一的 {@code use}，这里按手持物品分派，行为与原版一致。
     */
    @Override
    public ActionResult onUse(BlockState state, World level, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        var stack = player.getStackInHand(hand);
        if (stack.isOf(PGCItems.MORA.get())) {
            var bites = state.get(BITES);
            if (!level.isClient() && bites > 0 && level.setBlockState(pos, state.with(BITES, bites - 1), Block.NOTIFY_ALL)) {
                stack.decrement(1);
                level.playSound(null, pos, SoundEvents.BLOCK_WOOL_PLACE, SoundCategory.BLOCKS, VOLUME, PITCH);
                level.emitGameEvent(player, GameEvent.BLOCK_CHANGE, pos);
            }
            return ActionResult.success(level.isClient());
        }
        if (state.get(BITES) == MAX_BITES) return ActionResult.PASS;
        var food = player.getHungerManager();
        var hungry = food.getFoodLevel() < MAX_LEVEL;
        if (!level.isClient() && level.setBlockState(pos, state.with(BITES, state.get(BITES) + 1), Block.NOTIFY_ALL)) {
            if (hungry) food.setFoodLevel(Math.min(MAX_LEVEL, food.getFoodLevel() + FOOD_RESTORE));
            else food.setSaturationLevel(Math.min(MAX_LEVEL, food.getSaturationLevel() + SATURATION_RESTORE));
            level.playSound(null, pos, SoundEvents.ENTITY_PLAYER_BURP, hungry ? SoundCategory.NEUTRAL : SoundCategory.PLAYERS, VOLUME, PITCH);
            level.emitGameEvent(player, GameEvent.EAT, pos);
        }
        return ActionResult.success(level.isClient());
    }
}
