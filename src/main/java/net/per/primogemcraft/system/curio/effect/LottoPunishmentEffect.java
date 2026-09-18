package net.per.primogemcraft.system.curio.effect;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.per.primogemcraft.registry.PGCEffects;
import net.per.primogemcraft.util.PlayerFlags;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public class LottoPunishmentEffect extends CurioEffect {
    private static final ResourceLocation MODIFIER = ResourceLocation.fromNamespaceAndPath(MOD_ID, "effect/lotto_punishment");
    private static final ResourceLocation CURED = ResourceLocation.fromNamespaceAndPath(MOD_ID, "lotto_punishment_cured");
    private static final double HEALTH_PENALTY = -100.0D;
    private static final int REAPPLY_DELAY = 1;

    public LottoPunishmentEffect() {
        super(MobEffectCategory.NEUTRAL, -39169);
        addAttributeModifier(Attributes.MAX_HEALTH, MODIFIER, HEALTH_PENALTY, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    }

    @Override
    public void finished(ServerPlayer player, int amplifier) {
        restore(player);
    }

    @Override
    public void cleared(ServerPlayer player, int amplifier) {
        restore(player);
    }

    public static void apply(ServerPlayer player) {
        player.addEffect(new MobEffectInstance(PGCEffects.LOTTO_PUNISHMENT, MobEffectInstance.INFINITE_DURATION, 0));
    }

    public static void cure(ServerPlayer player) {
        PlayerFlags.of(player).setFlag(CURED, true);
        player.removeEffect(PGCEffects.LOTTO_PUNISHMENT);
    }

    private static void restore(ServerPlayer player) {
        var flags = PlayerFlags.of(player);
        if (flags.flag(CURED)) {
            flags.setFlag(CURED, false);
            return;
        }
        CurioEffects.defer(player, REAPPLY_DELAY, LottoPunishmentEffect::apply);
    }
}
