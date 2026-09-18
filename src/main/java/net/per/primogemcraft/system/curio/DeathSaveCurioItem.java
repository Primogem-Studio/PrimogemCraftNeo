package net.per.primogemcraft.system.curio;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.network.PacketDistributor;
import net.per.primogemcraft.network.ItemActivationPayload;
import net.per.primogemcraft.registry.PGCEffects;
import net.per.primogemcraft.registry.PGCParticles;
import net.per.primogemcraft.system.curio.effect.CurioEffects;
import net.per.primogemcraft.util.PGCTimer;
import net.per.primogemcraft.util.PlayerFlags;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public class DeathSaveCurioItem extends CurioItem {
    private static final String PUNISH_KEY = "message.primogemcraft.curio.death_save.punish";
    private static final ResourceLocation READY = key("death_save_ready");
    private static final ResourceLocation IMMUNITIES = key("death_save_immunities");
    private static final ResourceLocation PUNISH_EXEMPT = key("death_save_punish_exempt");
    private static final ResourceLocation REVENGE_ARMED = key("death_save_revenge_armed");
    private static final ResourceLocation PUNISHING = key("death_save_punishing");
    private static final String LOCK_TIMER = "curio/death_save_lock";
    private static final int LOCK_TICKS = 2400;
    private static final int PUNISH_DELAY = 20;
    private static final int WEAKNESS_AMPLIFIER = 9;
    private static final double PARTICLE_HEIGHT = 1.0D;
    private static final int PARTICLE_COUNT = 150;
    private static final double PARTICLE_SPEED = 1.0D;

    private final Spec spec;
    private final Consumer<CurioContext> onSave;
    private final BiConsumer<CurioContext, CurioImpact> onAbsorb;

    public DeathSaveCurioItem(CurioForm form, int integrity, Spec spec, Consumer<CurioContext> onSave, BiConsumer<CurioContext, CurioImpact> onAbsorb, Properties properties) {
        super(CurioTrigger.ACTIVE, form, integrity, properties);
        this.spec = spec;
        this.onSave = onSave;
        this.onAbsorb = onAbsorb;
    }

    @Override
    public void presence(CurioContext context) {
        var player = context.player();
        var flags = PlayerFlags.of(player);
        if (!flags.flag(READY)) return;
        flags.setFlag(READY, false);
        flags.set(IMMUNITIES, spec.immunities());
        CurioEffects.apply(player, PGCEffects.DEATHBED, spec.duration(), spec.immunities() - 1);
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, spec.duration(), spec.speedAmplifier(), false, false));
        player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, spec.duration(), WEAKNESS_AMPLIFIER, false, false));
        PacketDistributor.sendToPlayer(player, new ItemActivationPayload(context.stack()));
        if (spec.wears()) context.damage(1);
        if (onSave != null) onSave.accept(context);
    }

    @Override
    public boolean survivesDeath(CurioContext context, CurioImpact impact) {
        var player = context.player();
        if (PlayerFlags.of(player).flag(PUNISHING)) return false;
        if (!PGCTimer.isDone(player, lockTimer(context))) return false;
        if (player.getMainHandItem().is(Items.TOTEM_OF_UNDYING) && player.getOffhandItem().is(Items.TOTEM_OF_UNDYING)) return false;
        PlayerFlags.of(player).setFlag(READY, true);
        PGCTimer.set(player, lockTimer(context), LOCK_TICKS);
        player.setHealth(1.0F);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 0.5F, 1.0F);
        if (player.level() instanceof ServerLevel serverLevel)
            serverLevel.sendParticles(PGCParticles.MARA.get(), player.getX(), player.getY() + PARTICLE_HEIGHT, player.getZ(), PARTICLE_COUNT, 0.0D, 0.0D, 0.0D, PARTICLE_SPEED);
        return true;
    }

    private static String lockTimer(CurioContext context) {
        return LOCK_TIMER + "/" + Curios.idOf(context.stack().getItem()).getPath();
    }

    public static void endLife(ServerPlayer player) {
        var flags = PlayerFlags.of(player);
        flags.setFlag(READY, false);
        flags.set(IMMUNITIES, 0);
        flags.setFlag(PUNISH_EXEMPT, false);
        flags.setFlag(REVENGE_ARMED, false);
        flags.setFlag(PUNISHING, false);
        CurioEffects.discard(player, PGCEffects.DEATHBED);
        PGCTimer.clearMatching(player, LOCK_TIMER + "/");
    }

    public static void clearPunishment(ServerPlayer player) {
        PlayerFlags.of(player).setFlag(PUNISHING, false);
    }

    public static void punish(ServerPlayer player) {
        var flags = PlayerFlags.of(player);
        flags.set(IMMUNITIES, 0);
        if (flags.flag(PUNISH_EXEMPT)) {
            flags.setFlag(PUNISH_EXEMPT, false);
            return;
        }
        if (flags.flag(PUNISHING)) return;
        flags.setFlag(PUNISHING, true);
        CurioEffects.defer(player, PUNISH_DELAY, DeathSaveCurioItem::strike);
    }

    private static void strike(ServerPlayer player) {
        var flags = PlayerFlags.of(player);
        if (!flags.flag(PUNISHING)) return;
        player.displayClientMessage(Component.translatable(PUNISH_KEY), false);
        player.hurt(player.damageSources().genericKill(), (float) Math.pow(2.0D, 64.0D));
        flags.setFlag(PUNISHING, false);
    }

    @Override
    public boolean absorbsDamage(CurioContext context, CurioImpact impact) {
        var player = context.player();
        var instance = player.getEffect(PGCEffects.DEATHBED);
        if (instance == null) return false;
        var flags = PlayerFlags.of(player);
        if (flags.counter(IMMUNITIES) <= 0) return false;
        flags.set(IMMUNITIES, flags.counter(IMMUNITIES) - 1);
        CurioEffects.replace(player, PGCEffects.DEATHBED, instance.getDuration(), flags.counter(IMMUNITIES) - 1);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ANVIL_PLACE, SoundSource.PLAYERS, 0.2F, 20.0F);
        if (onAbsorb != null) onAbsorb.accept(context, impact);
        return true;
    }

    @Override
    public void impacted(CurioContext context, CurioImpact impact) {
        if (!spec.restoresOnXp() || impact.signal() != CurioSignal.XP_PICKED) return;
        var player = context.player();
        var instance = player.getEffect(PGCEffects.DEATHBED);
        if (instance == null) return;
        PlayerFlags.of(player).set(IMMUNITIES, spec.immunities());
        CurioEffects.replace(player, PGCEffects.DEATHBED, instance.getDuration(), spec.immunities() - 1);
    }

    public static void exemptNextPunishment(ServerPlayer player) {
        PlayerFlags.of(player).setFlag(PUNISH_EXEMPT, true);
    }

    public static void armRevenge(ServerPlayer player) {
        PlayerFlags.of(player).setFlag(REVENGE_ARMED, true);
    }

    public static boolean consumeRevenge(ServerPlayer player) {
        var flags = PlayerFlags.of(player);
        if (!flags.flag(REVENGE_ARMED)) return false;
        flags.setFlag(REVENGE_ARMED, false);
        return true;
    }

    public static void strike(LivingEntity attacker, float ratio) {
        attacker.hurt(attacker.damageSources().magic(), attacker.getMaxHealth() * ratio);
    }

    private static ResourceLocation key(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    public record Spec(boolean wears, int immunities, int duration, int speedAmplifier, boolean restoresOnXp) {
    }
}
