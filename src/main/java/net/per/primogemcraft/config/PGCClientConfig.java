package net.per.primogemcraft.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class PGCClientConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue SKY_TARGET_OVERLAY = BUILDER
            .translation("primogemcraft.configuration.sky_target_overlay")
            .define("tacz.sky_target_overlay", true);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private PGCClientConfig() {
    }
}
