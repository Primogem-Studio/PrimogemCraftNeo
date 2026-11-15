package net.per.primogemcraft.system.wish;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;

public final class WishTooltips {
    private WishTooltips() {
    }

    public static boolean showsDetails() {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT && Screen.hasShiftDown();
    }

    public static boolean showsTutorial() {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT && Screen.hasControlDown();
    }

    public static PlayerEntity viewer() {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT ? MinecraftClient.getInstance().player : null;
    }
}
