package net.per.primogemcraft.config;

/**
 * 客户端配置（Fabric）。
 *
 * <p>Forge 侧用 {@code ForgeConfigSpec} 声明并注册规格；这里复用 {@link PGCConfig}
 * 的 JSON 存储，取值方式与其余配置项一致（{@code SKY_TARGET_OVERLAY.get()}）。
 */
public final class PGCClientConfig {
    public static final PGCConfig.BoolValue SKY_TARGET_OVERLAY =
            new PGCConfig.BoolValue("tacz.sky_target_overlay", true);

    private PGCClientConfig() {
    }
}