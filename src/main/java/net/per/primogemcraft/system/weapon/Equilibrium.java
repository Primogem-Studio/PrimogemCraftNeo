package net.per.primogemcraft.system.weapon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.player.Player;
import net.per.primogemcraft.config.PGCConfig;
import net.per.primogemcraft.registry.PGCAttachments;

public final class Equilibrium {
    public static final Codec<Equilibrium> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("tier").forGetter(Equilibrium::tier)
    ).apply(instance, Equilibrium::new));

    private static final double[] TIER_BONUS = {0.0D, 0.005D, 0.015D, 0.035D};
    private static final double MULTIPLIER_BONUS = 0.0005D;

    private int tier;

    public Equilibrium() {
    }

    private Equilibrium(int tier) {
        this.tier = Math.clamp(tier, 0, TIER_BONUS.length - 1);
    }

    public static Equilibrium of(Player player) {
        return player.getData(PGCAttachments.EQUILIBRIUM);
    }

    public int tier() {
        return tier;
    }

    public double bonus() {
        if (tier <= 0) return 0.0D;
        return TIER_BONUS[tier] + PGCConfig.WEAPON_DAMAGE_MULTIPLIER.get() * MULTIPLIER_BONUS;
    }

    public boolean advanceTo(int tier) {
        if (this.tier >= tier) return false;
        this.tier = tier;
        return true;
    }

    public void reset() {
        tier = 0;
    }
}
