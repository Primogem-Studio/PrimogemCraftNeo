package net.per.primogemcraft.item.misc;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.per.primogemcraft.item.tool.DescribedItem;
import net.per.primogemcraft.registry.PGCDataComponents;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.registry.PGCSounds;
import net.per.primogemcraft.system.wish.WishTooltips;
import net.per.primogemcraft.util.Advancements;
import net.per.primogemcraft.util.PlayerFlags;
import net.per.primogemcraft.util.PlayerItems;

import java.util.List;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public class LuckySpecialTicketItem extends DescribedItem {
    private static final int NOT_JOINED = -1;
    private static final int WAIT_TICKS = 24000;
    private static final int TICKS_PER_SECOND = 20;
    private static final int SECONDS_PER_MINUTE = 60;
    private static final int CASH_OUT_JADE = 10;
    private static final int TOP_PRIZE_JADE = 5000;
    private static final int FIRST_PRIZE_JADE = 60;
    private static final int SECOND_PRIZE_JADE = 5;
    private static final double TOP_PRIZE_CHANCE = 0.0001D;
    private static final double FIRST_PRIZE_CHANCE = 0.1D;
    private static final float VOLUME = 0.3F;
    private static final float PITCH = 1.0F;
    private static final String MESSAGE_PREFIX = "message.primogemcraft.ticket.";
    private static final String STATE_PREFIX = "item.primogemcraft.lucky_special_ticket.state.";
    private static final String TOP_PRIZE_ADVANCEMENT = "galaxy_lucky_star";
    private static final ResourceLocation FIRST_PRIZE_FLAG = ResourceLocation.fromNamespaceAndPath(MOD_ID, "lucky_special_ticket/first");

    public LuckySpecialTicketItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        var waited = waitedTicks(stack);
        if (waited >= WAIT_TICKS) reveal(serverPlayer, stack);
        else if (waited == NOT_JOINED) join(serverPlayer, stack);
        else serverPlayer.displayClientMessage(Component.translatable(MESSAGE_PREFIX + "waiting"), true);
        return InteractionResultHolder.sidedSuccess(stack, false);
    }

    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity, InteractionHand hand) {
        if (!(entity instanceof ServerPlayer player)) return super.onEntitySwing(stack, entity, hand);
        if (waitedTicks(stack) != NOT_JOINED) {
            player.sendSystemMessage(Component.translatable(MESSAGE_PREFIX + "regret"));
            return false;
        }
        PlayerItems.give(player, jade(CASH_OUT_JADE));
        stack.shrink(1);
        draw(player);
        player.displayClientMessage(Component.translatable(MESSAGE_PREFIX + "cashout"), true);
        return false;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        if (level.isClientSide()) return;
        var waited = waitedTicks(stack);
        if (waited == NOT_JOINED || waited >= WAIT_TICKS) return;
        stack.set(PGCDataComponents.TICKET_PROGRESS.get(), waited + 1);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        appendDescription(stack, tooltip);
        var waited = waitedTicks(stack);
        if (waited == NOT_JOINED) return;
        if (!WishTooltips.showsDetails()) {
            tooltip.add(Component.translatable(STATE_PREFIX + "hint"));
            return;
        }
        if (waited >= WAIT_TICKS) {
            tooltip.add(Component.translatable(STATE_PREFIX + "revealed"));
            return;
        }
        appendRemaining(waited, tooltip);
    }

    private static void appendRemaining(int waited, List<Component> tooltip) {
        tooltip.add(Component.translatable(STATE_PREFIX + "remaining"));
        var seconds = (WAIT_TICKS - waited) / TICKS_PER_SECOND;
        if (seconds <= SECONDS_PER_MINUTE) {
            tooltip.add(Component.translatable(STATE_PREFIX + "seconds", seconds));
            return;
        }
        tooltip.add(Component.translatable(STATE_PREFIX + "minutes", seconds / SECONDS_PER_MINUTE));
    }

    private void join(ServerPlayer player, ItemStack stack) {
        stack.shrink(1);
        var joined = new ItemStack(this);
        joined.set(PGCDataComponents.TICKET_PROGRESS.get(), 0);
        var dropped = new ItemEntity(player.level(), player.getX(), player.getY(), player.getZ(), joined);
        dropped.setPickUpDelay(0);
        player.level().addFreshEntity(dropped);
        player.sendSystemMessage(Component.translatable(MESSAGE_PREFIX + "joined"));
    }

    private void reveal(ServerPlayer player, ItemStack stack) {
        var flags = PlayerFlags.of(player);
        if (flags.flag(FIRST_PRIZE_FLAG)) {
            roll(player, stack);
            return;
        }
        flags.setFlag(FIRST_PRIZE_FLAG, true);
        claim(player, stack, FIRST_PRIZE_JADE, "first_prize");
    }

    private void roll(ServerPlayer player, ItemStack stack) {
        draw(player);
        var random = player.getRandom();
        if (random.nextDouble() < TOP_PRIZE_CHANCE) {
            PlayerItems.give(player, new ItemStack(PGCItems.LUCKY_STATUE.get()));
            claim(player, stack, TOP_PRIZE_JADE, "top_prize");
            Advancements.grant(player, TOP_PRIZE_ADVANCEMENT);
            return;
        }
        if (random.nextDouble() < FIRST_PRIZE_CHANCE) {
            claim(player, stack, FIRST_PRIZE_JADE, "first_prize");
            return;
        }
        claim(player, stack, SECOND_PRIZE_JADE, "second_prize");
    }

    private static void claim(ServerPlayer player, ItemStack stack, int jade, String message) {
        PlayerItems.give(player, jade(jade));
        stack.shrink(1);
        player.sendSystemMessage(Component.translatable(MESSAGE_PREFIX + message));
    }

    private static ItemStack jade(int amount) {
        return new ItemStack(PGCItems.STELLAR_JADE.get(), amount);
    }

    private static void draw(ServerPlayer player) {
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), PGCSounds.LOTTERY_DRAW.get(), SoundSource.PLAYERS, VOLUME, PITCH);
    }

    private static int waitedTicks(ItemStack stack) {
        return stack.getOrDefault(PGCDataComponents.TICKET_PROGRESS.get(), NOT_JOINED);
    }
}
