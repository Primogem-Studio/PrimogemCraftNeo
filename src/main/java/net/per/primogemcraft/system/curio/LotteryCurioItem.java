package net.per.primogemcraft.system.curio;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.util.Advancements;
import net.per.primogemcraft.util.PGCTimer;
import net.per.primogemcraft.util.PlayerFlags;

import java.util.function.Consumer;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public class LotteryCurioItem extends CurioItem {
    private static final String GATE = "curio/lottery";
    private static final int GATE_TICKS = 5;
    private static final double BASE_GATE = 0.3D;
    private static final double LUCKY_GATE = 0.8D;
    private static final double LUCKY_BONUS = 0.2D;
    private static final double LUCKY_CAP = 0.8D;
    private static final double UNLUCKY_VOUCHER_CHANCE = 0.01D;
    private static final int THE_LOTTERYS_APPROVAL_BREAKS = 100;
    private static final String BANNED_KEY = "message.primogemcraft.curio.lottery.banned";

    public static final String BAN_TIMER = "curio/lottery_ban";
    public static final ResourceLocation BREAKS = ResourceLocation.fromNamespaceAndPath(MOD_ID, "lottery_breaks");

    private final double odds;
    private final Consumer<CurioContext> win;
    private final Consumer<CurioContext> lose;

    public LotteryCurioItem(CurioForm form, double odds, Consumer<CurioContext> win, Consumer<CurioContext> lose, Properties properties) {
        super(CurioTrigger.ACTIVE, form, 1, () -> new ItemStack(PGCItems.DAMAGED_COSMIC_BIG_LOTTO.get()), properties);
        this.odds = odds;
        this.win = win;
        this.lose = lose;
    }

    @Override
    public void impacted(CurioContext context, CurioImpact impact) {
        var signal = impact.signal();
        if (signal != CurioSignal.BLOCK_BROKEN && signal != CurioSignal.CONTAINER_BROKEN) return;
        if (!PGCTimer.isDone(context.player(), BAN_TIMER)) {
            context.announce(Component.translatable(BANNED_KEY));
            return;
        }
        if (!context.ready(GATE, GATE_TICKS)) return;
        var lucky = lucky(context.player());
        if (!context.chance(lucky ? LUCKY_GATE : BASE_GATE)) return;
        var output = signal == CurioSignal.CONTAINER_BROKEN ? context.withProduction(ContainerCurioItem.jarProduction(context.player())) : context;
        if (context.chance(lucky ? Math.min(odds + odds * LUCKY_BONUS, LUCKY_CAP) : odds)) {
            win.accept(output);
            context.player().level().playSound(null, context.player().getX(), context.player().getY(), context.player().getZ(), SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.PLAYERS, 1.0F, 1.0F);
            return;
        }
        lose.accept(output);
        var flags = PlayerFlags.of(context.player());
        if (flags.advance(BREAKS) >= THE_LOTTERYS_APPROVAL_BREAKS) Advancements.grant(context.player(), "the_lotterys_approval");
        if (context.chance(UNLUCKY_VOUCHER_CHANCE)) context.give(new ItemStack(PGCItems.UNLUCKY_VOUCHER.get()));
        context.damage(1);
    }

    private static boolean lucky(ServerPlayer player) {
        return player.getInventory().contains(stack -> !stack.isEmpty() && stack.is(PGCItems.UNLUCKY_VOUCHER.get()));
    }
}
