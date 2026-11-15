package net.per.primogemcraft.item.misc;

import net.per.primogemcraft.compat.component.StackComponents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.world.World;
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
    private static final Identifier FIRST_PRIZE_FLAG = Identifier.of(MOD_ID, "lucky_special_ticket/first");

    public LuckySpecialTicketItem(Settings properties) {
        super(properties);
    }

    @Override
    public TypedActionResult<ItemStack> use(World level, PlayerEntity player, Hand hand) {
        var stack = player.getStackInHand(hand);
        if (!(player instanceof ServerPlayerEntity serverPlayer)) return TypedActionResult.consume(stack);
        var waited = waitedTicks(stack);
        if (waited >= WAIT_TICKS) reveal(serverPlayer, stack);
        else if (waited == NOT_JOINED) join(serverPlayer, stack);
        else serverPlayer.sendMessage(Text.translatable(MESSAGE_PREFIX + "waiting"), true);
        return TypedActionResult.consume(stack);
    }

    public static void cashOut(ServerPlayerEntity player) {
        var stack = player.getMainHandStack();
        if (!player.isAlive() || player.isSpectator() || !stack.isOf(PGCItems.LUCKY_SPECIAL_TICKET.get())) return;
        if (waitedTicks(stack) != NOT_JOINED) {
            player.sendMessage(Text.translatable(MESSAGE_PREFIX + "regret"));
            return;
        }
        PlayerItems.give(player, jade(CASH_OUT_JADE));
        stack.decrement(1);
        draw(player);
        player.sendMessage(Text.translatable(MESSAGE_PREFIX + "cashout"), true);
    }

    @Override
    public void inventoryTick(ItemStack stack, World level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        if (level.isClient()) return;
        var waited = waitedTicks(stack);
        if (waited == NOT_JOINED || waited >= WAIT_TICKS) return;
        StackComponents.set(stack, PGCDataComponents.TICKET_PROGRESS, waited + 1);
    }

    @Override
    public void appendTooltip(ItemStack stack, @org.jetbrains.annotations.Nullable World level, List<Text> tooltip, TooltipContext flag) {
        appendDescription(stack, tooltip);
        var waited = waitedTicks(stack);
        if (waited == NOT_JOINED) return;
        if (!WishTooltips.showsDetails()) {
            tooltip.add(Text.translatable(STATE_PREFIX + "hint"));
            return;
        }
        if (waited >= WAIT_TICKS) {
            tooltip.add(Text.translatable(STATE_PREFIX + "revealed"));
            return;
        }
        appendRemaining(waited, tooltip);
    }

    private static void appendRemaining(int waited, List<Text> tooltip) {
        tooltip.add(Text.translatable(STATE_PREFIX + "remaining"));
        var seconds = (WAIT_TICKS - waited) / TICKS_PER_SECOND;
        if (seconds <= SECONDS_PER_MINUTE) {
            tooltip.add(Text.translatable(STATE_PREFIX + "seconds", seconds));
            return;
        }
        tooltip.add(Text.translatable(STATE_PREFIX + "minutes", seconds / SECONDS_PER_MINUTE));
    }

    private void join(ServerPlayerEntity player, ItemStack stack) {
        stack.decrement(1);
        var joined = new ItemStack(this);
        StackComponents.set(joined, PGCDataComponents.TICKET_PROGRESS, 0);
        var dropped = new ItemEntity(player.getWorld(), player.getX(), player.getY(), player.getZ(), joined);
        dropped.setPickupDelay(0);
        player.getWorld().spawnEntity(dropped);
        player.sendMessage(Text.translatable(MESSAGE_PREFIX + "joined"));
    }

    private void reveal(ServerPlayerEntity player, ItemStack stack) {
        var flags = PlayerFlags.of(player);
        if (flags.flag(FIRST_PRIZE_FLAG)) {
            roll(player, stack);
            return;
        }
        flags.setFlag(FIRST_PRIZE_FLAG, true);
        claim(player, stack, FIRST_PRIZE_JADE, "first_prize");
    }

    private void roll(ServerPlayerEntity player, ItemStack stack) {
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

    private static void claim(ServerPlayerEntity player, ItemStack stack, int jade, String message) {
        PlayerItems.give(player, jade(jade));
        stack.decrement(1);
        player.sendMessage(Text.translatable(MESSAGE_PREFIX + message));
    }

    private static ItemStack jade(int amount) {
        return new ItemStack(PGCItems.STELLAR_JADE.get(), amount);
    }

    private static void draw(ServerPlayerEntity player) {
        player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), PGCSounds.LOTTERY_DRAW.get(), SoundCategory.PLAYERS, VOLUME, PITCH);
    }

    private static int waitedTicks(ItemStack stack) {
        return StackComponents.getOrDefault(stack, PGCDataComponents.TICKET_PROGRESS, NOT_JOINED);
    }
}
