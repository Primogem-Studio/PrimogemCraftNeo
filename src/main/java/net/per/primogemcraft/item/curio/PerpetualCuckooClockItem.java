package net.per.primogemcraft.item.curio;

import net.per.primogemcraft.compat.component.StackComponents;
import net.minecraft.text.Text;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.sound.SoundCategory;
import net.minecraft.item.ItemStack;
import net.per.primogemcraft.registry.PGCDataComponents;
import net.per.primogemcraft.registry.PGCEffects;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.registry.PGCSounds;
import net.per.primogemcraft.system.curio.*;
import net.per.primogemcraft.system.curio.effect.CurioEffects;
import net.per.primogemcraft.util.PlayerItems;

public class PerpetualCuckooClockItem extends CurioItem {
    private static final String START_KEY = "message.primogemcraft.curio.perpetual_cuckoo_clock.start";
    private static final String STOP_KEY = "message.primogemcraft.curio.perpetual_cuckoo_clock.stop";
    private static final String TICK_KEY = "message.primogemcraft.curio.perpetual_cuckoo_clock.tick";
    private static final String DRAIN_TIMER = "curio/perpetual_cuckoo_clock";
    private static final int INTEGRITY = 31;
    private static final int RUNNING_TICKS = 60;
    private static final int DRAIN_TICKS = 3600;
    private static final int CLEANSED = 1;
    private static final int ARMED = 2;
    private static final int STOPPED = 3;
    private static final double XP_LOSS = 0.05D;
    private static final int DROP_TICKS = 10;
    private static final int LARGE_DROPS = 3;
    private static final int MEDIUM_DROPS = 2;
    private static final double LARGE_CHANCE = 0.05D;
    private static final double MEDIUM_CHANCE = 0.1D;
    private static final double SMALL_CHANCE = 0.15D;
    private static final float START_VOLUME = 4.0F;
    private static final float START_PITCH = 0.5F;
    private static final float REPAIR_VOLUME = 0.2F;
    private static final float REPAIR_PITCH = 1.4F;
    private static final float DRAIN_VOLUME = 0.5F;
    private static final float DRAIN_PITCH = 0.5F;

    public PerpetualCuckooClockItem(Settings properties) {
        super(CurioTrigger.ACTIVE, CurioForm.FUSION, INTEGRITY, properties);
    }

    @Override
    public void pickedUp(ServerPlayerEntity player, ItemStack stack) {
        if (state(stack) == STOPPED) StackComponents.set(stack, PGCDataComponents.CURIO_COUNTER, 0);
        arm(player, stack);
    }

    @Override
    public void presence(CurioContext context) {
        var stack = context.stack();
        var player = context.player();
        var state = state(stack);
        if (state == CLEANSED || state == STOPPED) return;
        var bar = StackComponents.get(stack, PGCDataComponents.CUSTOM_BAR);
        if (bar == null) return;
        if (bar.numerator() <= 0) {
            if (state == ARMED) {
                stop(context);
                return;
            }
            arm(player, stack);
            return;
        }
        if (state != ARMED) StackComponents.set(stack, PGCDataComponents.CURIO_COUNTER, ARMED);
        CurioEffects.hold(player, PGCEffects.PERPETUAL_CUCKOO_CLOCK.get(), RUNNING_TICKS, 0);
        if (!context.ready(DRAIN_TIMER, DRAIN_TICKS)) return;
        if (player.totalExperience <= 0) return;
        context.announce(Text.translatable(TICK_KEY, stack.getName()));
        player.addExperience(-(int) (player.totalExperience * XP_LOSS));
        player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.PLAYERS, DRAIN_VOLUME, DRAIN_PITCH);
    }

    private static void arm(ServerPlayerEntity player, ItemStack stack) {
        if (state(stack) != 0) return;
        var bar = StackComponents.get(stack, PGCDataComponents.CUSTOM_BAR);
        if (bar == null || bar.numerator() > 0) return;
        CurioEffects.hold(player, PGCEffects.PERPETUAL_CUCKOO_CLOCK.get(), RUNNING_TICKS, 0);
        CurioContext.of(player, stack, CurioForm.FUSION).damage(INTEGRITY - 1);
        StackComponents.set(stack, PGCDataComponents.CURIO_COUNTER, ARMED);
        player.sendMessage(Text.translatable(START_KEY, stack.getName()), false);
        player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), PGCSounds.CURIO_BROKEN.get(), SoundCategory.PLAYERS, START_VOLUME, START_PITCH);
    }

    @Override
    public void impacted(CurioContext context, CurioImpact impact) {
        if (impact.signal() != CurioSignal.KILL) return;
        if (!CurioEffects.active(context.player(), PGCEffects.PERPETUAL_CUCKOO_CLOCK.get())) return;
        var drops = drops(context);
        for (var index = 0; index < drops; index++) context.give(new ItemStack(PGCItems.CUCKOO_CLOCK_PART.get()));
    }

    @Override
    public void activated(CurioContext context) {
        var wear = StackComponents.get(context.stack(), PGCDataComponents.CUSTOM_BAR);
        if (wear == null || wear.numerator() <= 0) return;
        var player = context.player();
        var part = PGCItems.CUCKOO_CLOCK_PART.get();
        var owned = PlayerItems.count(player, part);
        if (owned <= 0) return;
        var used = Math.min(owned, wear.numerator());
        PlayerItems.take(player, part, used);
        Curios.repair(player, context.stack(), used);
        player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BLOCK_ANVIL_USE, SoundCategory.PLAYERS, REPAIR_VOLUME, REPAIR_PITCH);
        if (used < wear.numerator()) return;
        stop(context);
    }

    private static int drops(CurioContext context) {
        if (context.chance(LARGE_CHANCE)) return LARGE_DROPS;
        if (context.chance(MEDIUM_CHANCE)) return MEDIUM_DROPS;
        if (context.chance(SMALL_CHANCE)) return 1;
        return 0;
    }

    public static void clear(ServerPlayerEntity player) {
        var counter = PGCDataComponents.CURIO_COUNTER.get();
        for (var context : Curios.held(player))
            if (context.stack().getItem() instanceof PerpetualCuckooClockItem) StackComponents.set(context.stack(), counter, CLEANSED);
        CurioEffects.release(player, PGCEffects.PERPETUAL_CUCKOO_CLOCK.get());
    }

    private static void stop(CurioContext context) {
        CurioEffects.release(context.player(), PGCEffects.PERPETUAL_CUCKOO_CLOCK.get());
        StackComponents.set(context.stack(), PGCDataComponents.CURIO_COUNTER, STOPPED);
        context.announce(Text.translatable(STOP_KEY, context.stack().getName()));
        var player = context.player();
        player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), PGCSounds.CURIO_BROKEN.get(), SoundCategory.PLAYERS, 1.0F, 1.0F);
    }

    private static int state(ItemStack stack) {
        return StackComponents.getOrDefault(stack, PGCDataComponents.CURIO_COUNTER, 0);
    }
}
