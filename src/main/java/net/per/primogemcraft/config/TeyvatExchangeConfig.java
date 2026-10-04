package net.per.primogemcraft.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class TeyvatExchangeConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec.BooleanValue ENABLED = BUILDER.define("enabled", true);
    public static final ModConfigSpec.IntValue PRIMOGEM_CRAFT_AMOUNT = BUILDER.defineInRange("primogem.primogemcraft_amount", 3, 1, 64);
    public static final ModConfigSpec.IntValue PRIMOGEM_TEYVAT_AMOUNT = BUILDER.defineInRange("primogem.teyvatdelight_amount", 1, 1, 64);
    public static final ModConfigSpec.IntValue MORA_CRAFT_AMOUNT = BUILDER.defineInRange("mora.primogemcraft_amount", 21, 1, 64);
    public static final ModConfigSpec.IntValue MORA_TEYVAT_AMOUNT = BUILDER.defineInRange("mora.teyvatdelight_amount", 3, 1, 64);
    public static final ModConfigSpec.IntValue PRIMOGEM_DAILY_LIMIT = BUILDER.defineInRange("primogem.daily_limit", 3, 0, 1000000);
    public static final ModConfigSpec.IntValue MORA_DAILY_LIMIT = BUILDER.defineInRange("mora.daily_limit", 5, 0, 1000000);
    public static final ModConfigSpec SPEC = BUILDER.build();

    private TeyvatExchangeConfig() {
    }
}
