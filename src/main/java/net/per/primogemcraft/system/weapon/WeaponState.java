package net.per.primogemcraft.system.weapon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.per.primogemcraft.registry.PGCDataComponents;

public record WeaponState(int level, int refinements, int xp, int duplicateRefinements, int superimposerRefinements,
                          int temporaryRefinements) {
    public static final WeaponState INITIAL = new WeaponState(1, 1, 0, 0, 0, 0);

    public static final Codec<WeaponState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("level").forGetter(WeaponState::level),
            Codec.INT.fieldOf("refinement").forGetter(WeaponState::refinements),
            Codec.INT.optionalFieldOf("xp", 0).forGetter(WeaponState::xp),
            Codec.INT.optionalFieldOf("duplicate_refinements", 0).forGetter(WeaponState::duplicateRefinements),
            Codec.INT.optionalFieldOf("superimposer_refinements", 0).forGetter(WeaponState::superimposerRefinements),
            Codec.INT.optionalFieldOf("temporary_refinements", 0).forGetter(WeaponState::temporaryRefinements)
    ).apply(instance, WeaponState::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, WeaponState> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, WeaponState::level,
            ByteBufCodecs.VAR_INT, WeaponState::refinements,
            ByteBufCodecs.VAR_INT, WeaponState::xp,
            ByteBufCodecs.VAR_INT, WeaponState::duplicateRefinements,
            ByteBufCodecs.VAR_INT, WeaponState::superimposerRefinements,
            ByteBufCodecs.VAR_INT, WeaponState::temporaryRefinements,
            WeaponState::new);

    public static WeaponState of(ItemStack stack) {
        return stack.getOrDefault(PGCDataComponents.WEAPON_STATE.get(), INITIAL).normalized();
    }

    public static void set(ItemStack stack, WeaponState state) {
        stack.set(PGCDataComponents.WEAPON_STATE.get(), state);
    }

    public int spentRefinements() {
        return Math.max(0, refinements - 1);
    }

    public int duplicateLayers() {
        return spentRefinements() - superimposerLayers();
    }

    public int superimposerLayers() {
        return Mth.clamp(superimposerRefinements, 0, spentRefinements());
    }

    public WeaponState duplicatedBy(int amount) {
        return new WeaponState(level, refinements + amount, xp, duplicateRefinements + amount, superimposerRefinements,
                temporaryRefinements);
    }

    public WeaponState superimposedBy(int amount) {
        return new WeaponState(level, refinements + amount, xp, duplicateRefinements, superimposerRefinements + amount,
                temporaryRefinements);
    }

    public WeaponState temporarilyBy(int amount) {
        return new WeaponState(level, refinements, xp, duplicateRefinements, superimposerRefinements,
                Mth.clamp(temporaryRefinements + amount, 0, WeaponEnhancement.MAX_TEMPORARY_REFINEMENT));
    }

    public WeaponState leveledTo(int target, int remainingXp) {
        return new WeaponState(target, refinements, remainingXp, duplicateRefinements, superimposerRefinements,
                temporaryRefinements);
    }

    private WeaponState normalized() {
        var excess = refinements - WeaponEnhancement.MAX_REFINEMENT;
        if (excess <= 0) return this;
        return new WeaponState(level, WeaponEnhancement.MAX_REFINEMENT, xp, duplicateRefinements, superimposerRefinements,
                Mth.clamp(temporaryRefinements + excess, 0, WeaponEnhancement.MAX_TEMPORARY_REFINEMENT));
    }
}
