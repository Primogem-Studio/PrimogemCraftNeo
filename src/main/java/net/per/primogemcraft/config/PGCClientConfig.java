package net.per.primogemcraft.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class PGCClientConfig {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.BooleanValue SKY_TARGET_OVERLAY = BUILDER
            .translation("primogemcraft.configuration.sky_target_overlay")
            .define("tacz.sky_target_overlay", true);

    public static final ForgeConfigSpec SPEC = BUILDER.build();

    private PGCClientConfig() {
    }
}
