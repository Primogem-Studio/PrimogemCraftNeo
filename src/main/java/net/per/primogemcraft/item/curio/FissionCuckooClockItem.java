package net.per.primogemcraft.item.curio;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.per.primogemcraft.registry.PGCEffects;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.registry.PGCSounds;
import net.per.primogemcraft.system.choice.ChoiceSupport;
import net.per.primogemcraft.system.curio.*;
import net.per.primogemcraft.system.curio.effect.CurioEffects;
import net.per.primogemcraft.util.PGCTimer;
import net.per.primogemcraft.util.PlayerFlags;

import java.util.List;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public class FissionCuckooClockItem extends CurioItem {
    private static final String START_KEY = "message.primogemcraft.curio.fission_cuckoo_clock.start";
    private static final String STOP_KEY = "message.primogemcraft.curio.fission_cuckoo_clock.stop";
    private static final String INSUFFICIENT_KEY = "message.primogemcraft.curio.fission_cuckoo_clock.insufficient";
    private static final String IMMUNE_KEY = "message.primogemcraft.curio.fission_cuckoo_clock.immune";
    private static final String SPLIT_KEY = "message.primogemcraft.curio.fission_cuckoo_clock.split";
    private static final String SPLIT_TIMER = "curio/fission_cuckoo_clock/split";
    private static final String DUPLICATE_TIMER = "curio/fission_cuckoo_clock/duplicate";
    private static final ResourceLocation ARMED = ResourceLocation.fromNamespaceAndPath(MOD_ID, "curio/fission_cuckoo_clock/armed");
    private static final ResourceLocation SETTLED = ResourceLocation.fromNamespaceAndPath(MOD_ID, "curio/fission_cuckoo_clock/settled");
    private static final ResourceLocation DEBT = ResourceLocation.fromNamespaceAndPath(MOD_ID, "curio/fission_cuckoo_clock/debt");
    private static final int EFFECT_TICKS = 100;
    private static final int SPLIT_INTERVAL = 6000;
    private static final int DUPLICATE_COOLDOWN = 20;
    private static final int XP_COST = 1395;
    private static final int MAX_REPLICAS = 3;
    private static final double DUPLICATE_CHANCE = 0.05D;
    private static final float START_VOLUME = 4.0F;
    private static final float START_PITCH = 0.5F;
    private static final float SPLIT_VOLUME = 10.0F;

    public FissionCuckooClockItem(Properties properties) {
        super(CurioTrigger.ACTIVE, CurioForm.FUSION, properties);
    }

    @Override
    public void pickedUp(ServerPlayer player, ItemStack stack) {
        PlayerFlags.of(player).setFlag(SETTLED, false);
        arm(player);
    }

    @Override
    public void presence(CurioContext context) {
        var player = context.player();
        arm(player);
        if (!PlayerFlags.of(player).flag(ARMED)) return;
        holdEffect(player);
        if (!context.ready(SPLIT_TIMER, SPLIT_INTERVAL)) return;
        if (replicas(player) >= MAX_REPLICAS) return;
        CurioReward.open(player, ChoiceSupport.CURIO_CARDS, List.of(context.reward(new ItemStack(PGCItems.FISSION_CUCKOO_CLOCK_I.get()))));
        context.announce(Component.translatable(SPLIT_KEY));
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.EVOKER_CAST_SPELL, SoundSource.PLAYERS, SPLIT_VOLUME, 1.0F);
    }

    @Override
    public void impacted(CurioContext context, CurioImpact impact) {
        if (impact.signal() != CurioSignal.ATTACK) return;
        if (!(impact.subject() instanceof LivingEntity victim)) return;
        if (!victim.getType().is(EntityTypeTags.UNDEAD)) return;
        var player = context.player();
        if (!PlayerFlags.of(player).flag(ARMED)) return;
        if (victim.getMaxHealth() > player.getMaxHealth()) return;
        var replicas = replicas(player);
        if (!context.chance(DUPLICATE_CHANCE * (1 + replicas))) return;
        if (!context.ready(DUPLICATE_TIMER, DUPLICATE_COOLDOWN)) return;
        CuckooClockDuplicates.spawn(context, victim, replicas);
    }

    @Override
    public void activated(CurioContext context) {
        var player = context.player();
        var flags = PlayerFlags.of(player);
        if (!flags.flag(ARMED)) {
            context.announce(Component.translatable(IMMUNE_KEY));
            return;
        }
        var debt = flags.counter(DEBT);
        var paid = Math.min(debt, player.totalExperience);
        if (paid > 0) {
            player.giveExperiencePoints(-paid);
            debt -= paid;
            flags.set(DEBT, debt);
        }
        if (debt > 0 || replicas(player) > 0) {
            context.announce(Component.translatable(INSUFFICIENT_KEY));
            return;
        }
        flags.setFlag(ARMED, false);
        flags.setFlag(SETTLED, true);
        CurioEffects.release(player, PGCEffects.FISSION_CUCKOO_CLOCK);
        context.announce(Component.translatable(STOP_KEY));
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), PGCSounds.CURIO_BROKEN.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    public static int replicas(ServerPlayer player) {
        var item = PGCItems.FISSION_CUCKOO_CLOCK_I.get();
        var count = 0;
        for (var context : Curios.held(player)) if (context.stack().is(item)) count++;
        return count;
    }

    public static void holdEffect(ServerPlayer player) {
        var effect = PGCEffects.FISSION_CUCKOO_CLOCK;
        var replicas = replicas(player);
        if (replicas <= 0 && !PlayerFlags.of(player).flag(ARMED)) {
            CurioEffects.release(player, effect);
            return;
        }
        if (CurioEffects.amplifier(player, effect) != replicas) CurioEffects.replace(player, effect, EFFECT_TICKS, replicas);
        CurioEffects.hold(player, effect, EFFECT_TICKS, replicas);
    }

    public static void resetSplit(ServerPlayer player) {
        PGCTimer.set(player, SPLIT_TIMER, SPLIT_INTERVAL);
    }

    public static void forget(ServerPlayer player) {
        var flags = PlayerFlags.of(player);
        flags.setFlag(ARMED, false);
        flags.setFlag(SETTLED, false);
        flags.set(DEBT, 0);
    }

    private static void arm(ServerPlayer player) {
        var flags = PlayerFlags.of(player);
        if (flags.flag(ARMED) || flags.flag(SETTLED)) return;
        flags.setFlag(ARMED, true);
        flags.set(DEBT, XP_COST);
        resetSplit(player);
        holdEffect(player);
        player.displayClientMessage(Component.translatable(START_KEY), false);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), PGCSounds.CURIO_BROKEN.get(), SoundSource.PLAYERS, START_VOLUME, START_PITCH);
    }
}
