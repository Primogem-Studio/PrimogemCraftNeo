package net.per.primogemcraft.item.curio;

import net.minecraft.world.World;
import net.per.primogemcraft.compat.component.StackComponents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.ActionResult;
import net.minecraft.item.ItemStack;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.world.Heightmap;
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
    private static final Identifier CLAIMED_MARK = Identifier.of(MOD_ID, "lost_property_claimed");
    private static final int STEP_TICKS = 60;
    private static final int STEPS = 100;
    private static final int RANGE = 300;
    private static final int VERTICAL_RANGE = 16;

    private final int descriptionLines;

    protected LostPropertyItem(CurioForm form, int descriptionLines, Settings properties) {
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
            StackComponents.set(stack, PGCDataComponents.CURIO_COUNTER, progress);
            return;
        }
        generate(context);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        if (!(context.getPlayer() instanceof ServerPlayerEntity player)) return ActionResult.SUCCESS;
        var stack = context.getStack();
        if (stack.getItem() != this) return ActionResult.PASS;
        if (Curios.marked(stack, CLAIMED_MARK)) {
            player.sendMessage(Text.translatable(USED_KEY), false);
            return ActionResult.SUCCESS;
        }
        var position = position(stack);
        if (position == null || !position.equals(context.getBlockPos())) {
            player.sendMessage(Text.translatable(MISMATCH_KEY), false);
            return ActionResult.SUCCESS;
        }
        var level = player.getServerWorld();
        if (level.getBlockState(position).isOf(PGCBlocks.WONDROUS_ENCOUNTER_BLOCK.get())) level.breakBlock(position, true);
        var curio = CurioContext.of(player, stack, form());
        grant(curio);
        Curios.mark(stack, CLAIMED_MARK);
        Curios.broken(player, stack);
        stack.decrement(1);
        return ActionResult.SUCCESS;
    }

    @Override
    public void appendTooltip(ItemStack stack, @org.jetbrains.annotations.Nullable World level, List<Text> tooltip, TooltipContext flag) {
        appendTrigger(stack, tooltip);
        var prefix = stack.getTranslationKey() + TOOLTIP_SUFFIX;
        for (var index = 0; index < descriptionLines; index++) tooltip.add(Text.translatable(prefix + index));
        var position = position(stack);
        if (position == null) {
            tooltip.add(Text.translatable(stack.getTranslationKey() + PROGRESS_SUFFIX, progress(stack)));
            tooltip.add(Text.translatable(stack.getTranslationKey() + PENDING_SUFFIX));
            return;
        }
        tooltip.add(Text.translatable(stack.getTranslationKey() + POSITION_SUFFIX, position.getX(), position.getY(), position.getZ()));
        tooltip.add(Text.translatable(stack.getTranslationKey() + REVEALED_SUFFIX));
    }

    protected abstract void grant(CurioContext context);

    private static BlockPos position(ItemStack stack) {
        return StackComponents.get(stack, PGCDataComponents.ENCOUNTER_POSITION);
    }

    private static int progress(ItemStack stack) {
        return StackComponents.getOrDefault(stack, PGCDataComponents.CURIO_COUNTER, 0);
    }

    private static void generate(CurioContext context) {
        var player = context.player();
        var level = context.getWorld();
        var x = player.getBlockX() + MathHelper.nextInt(context.random(), -RANGE, RANGE);
        var z = player.getBlockZ() + MathHelper.nextInt(context.random(), -RANGE, RANGE);
        var limit = level.getTopY() - 1;
        var surface = level.getTopPosition(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, new BlockPos(x, 0, z)).getY();
        var y = MathHelper.clamp(surface + MathHelper.nextInt(context.random(), -VERTICAL_RANGE, VERTICAL_RANGE), level.getBottomY(), limit);
        var position = new BlockPos(x, y, z);
        while (position.getY() < limit && !level.getBlockState(position).isAir()) position = position.up();
        level.setBlockState(position, PGCBlocks.WONDROUS_ENCOUNTER_BLOCK.get().getDefaultState());
        StackComponents.set(context.stack(), PGCDataComponents.ENCOUNTER_POSITION, position);
        context.announce(Text.translatable(GENERATED_KEY));
    }
}
