package net.per.primogemcraft.system.curio.effect;

import net.minecraft.util.Identifier;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.per.primogemcraft.registry.PGCEffects;
import net.per.primogemcraft.util.PlayerFlags;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public class LottoPunishmentEffect extends CurioEffect {
    private static final Identifier MODIFIER = Identifier.of(MOD_ID, "effect/lotto_punishment");
    private static final Identifier CURED = Identifier.of(MOD_ID, "lotto_punishment_cured");
    private static final double HEALTH_PENALTY = -100.0D;
    private static final int REAPPLY_DELAY = 1;

    public LottoPunishmentEffect() {
        super(StatusEffectCategory.NEUTRAL, -39169);
        addAttributeModifier(EntityAttributes.GENERIC_MAX_HEALTH, net.per.primogemcraft.compat.attribute.AttributesCompat.uuid(MODIFIER).toString(), HEALTH_PENALTY, EntityAttributeModifier.Operation.MULTIPLY_TOTAL);
    }

    @Override
    public void finished(ServerPlayerEntity player, int amplifier) {
        restore(player);
    }

    @Override
    public void cleared(ServerPlayerEntity player, int amplifier) {
        restore(player);
    }

    public static void apply(ServerPlayerEntity player) {
        player.addStatusEffect(new StatusEffectInstance(PGCEffects.LOTTO_PUNISHMENT.get(), StatusEffectInstance.INFINITE, 0));
    }

    public static void cure(ServerPlayerEntity player) {
        PlayerFlags.of(player).setFlag(CURED, true);
        player.removeStatusEffect(PGCEffects.LOTTO_PUNISHMENT.get());
    }

    private static void restore(ServerPlayerEntity player) {
        var flags = PlayerFlags.of(player);
        if (flags.flag(CURED)) {
            flags.setFlag(CURED, false);
            return;
        }
        CurioEffects.defer(player, REAPPLY_DELAY, LottoPunishmentEffect::apply);
    }
}
