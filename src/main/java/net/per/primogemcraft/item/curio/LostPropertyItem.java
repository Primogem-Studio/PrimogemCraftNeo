package net.per.primogemcraft.item.curio;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.levelgen.Heightmap;
import net.per.primogemcraft.registry.PGCBlocks;
import net.per.primogemcraft.registry.PGCDataComponents;
import net.per.primogemcraft.system.curio.*;

import java.util.List;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public abstract class LostPropertyItem extends CurioItem {
    private static final String GENERATED_KEY = "message.primogemcraft.curio.lost_property.generated";
    private static final String MISMATCH_KEY = "message.primogemcraft.curio.lost_property.mismatch";
    private static final String USED_KEY = "message.primogemcraft.curio.lost_property.used";
    private static final String TOOLTIP_SUFFIX = ".tooltip.";
    private static final String PROGRESS_SUFFIX = ".tooltip.progress";
    private static final String PENDING_SUFFIX = ".tooltip.pending";
    private static final String POSITION_SUFFIX = ".tooltip.position";
    private static final String REVEALED_SUFFIX = ".tooltip.revealed";
    private static final ResourceLocation CLAIMED_MARK = ResourceLocation.fromNamespaceAndPath(MOD_ID, "lost_property_claimed");
    private static final int STEP_TICKS = 60;
    private static final int STEPS = 100;
    private static final int RANGE = 300;
    private static final int VERTICAL_RANGE = 16;

    private final int descriptionLines;

    protected LostPropertyItem(CurioForm form, int descriptionLines, Properties properties) {
        super(CurioTrigger.ACTIVE, form, properties);
        this.descriptionLines = descriptionLines;
    }

    @Override
    public int presenceInterval() {
        return STEP_TICKS;
    }

    @Override
    public void presence(CurioContext context) {
        var stack = context.stack();
        if (position(stack) != null) return;
        var progress = progress(stack) + 1;
        if (progress < STEPS) {
            stack.set(PGCDataComponents.CURIO_COUNTER.get(), progress);
            return;
        }
        generate(context);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!(context.getPlayer() instanceof ServerPlayer player)) return InteractionResult.SUCCESS;
        var stack = context.getItemInHand();
        if (stack.getItem() != this) return InteractionResult.PASS;
        if (Curios.marked(stack, CLAIMED_MARK)) {
            player.displayClientMessage(Component.translatable(USED_KEY), false);
            return InteractionResult.SUCCESS;
        }
        var position = position(stack);
        if (position == null || !position.equals(context.getClickedPos())) {
            player.displayClientMessage(Component.translatable(MISMATCH_KEY), false);
            return InteractionResult.SUCCESS;
        }
        var level = player.serverLevel();
        if (level.getBlockState(position).is(PGCBlocks.WONDROUS_ENCOUNTER_BLOCK.get())) level.destroyBlock(position, true);
        var curio = CurioContext.of(player, stack, form());
        grant(curio);
        Curios.mark(stack, CLAIMED_MARK);
        Curios.broken(player, stack);
        stack.shrink(1);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        appendTrigger(stack, tooltip);
        var prefix = stack.getDescriptionId() + TOOLTIP_SUFFIX;
        for (var index = 0; index < descriptionLines; index++) tooltip.add(Component.translatable(prefix + index));
        var position = position(stack);
        if (position == null) {
            tooltip.add(Component.translatable(stack.getDescriptionId() + PROGRESS_SUFFIX, progress(stack)));
            tooltip.add(Component.translatable(stack.getDescriptionId() + PENDING_SUFFIX));
            return;
        }
        tooltip.add(Component.translatable(stack.getDescriptionId() + POSITION_SUFFIX, position.getX(), position.getY(), position.getZ()));
        tooltip.add(Component.translatable(stack.getDescriptionId() + REVEALED_SUFFIX));
    }

    protected abstract void grant(CurioContext context);

    private static BlockPos position(ItemStack stack) {
        return stack.get(PGCDataComponents.ENCOUNTER_POSITION.get());
    }

    private static int progress(ItemStack stack) {
        return stack.getOrDefault(PGCDataComponents.CURIO_COUNTER.get(), 0);
    }

    private static void generate(CurioContext context) {
        var player = context.player();
        var level = context.level();
        var x = player.getBlockX() + Mth.nextInt(context.random(), -RANGE, RANGE);
        var z = player.getBlockZ() + Mth.nextInt(context.random(), -RANGE, RANGE);
        var limit = level.getMaxBuildHeight() - 1;
        var surface = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        var y = Mth.clamp(surface + Mth.nextInt(context.random(), -VERTICAL_RANGE, VERTICAL_RANGE), level.getMinBuildHeight(), limit);
        var position = new BlockPos(x, y, z);
        while (position.getY() < limit && !level.getBlockState(position).isAir()) position = position.above();
        level.setBlockAndUpdate(position, PGCBlocks.WONDROUS_ENCOUNTER_BLOCK.get().defaultBlockState());
        context.stack().set(PGCDataComponents.ENCOUNTER_POSITION.get(), position);
        context.announce(Component.translatable(GENERATED_KEY));
    }
}
