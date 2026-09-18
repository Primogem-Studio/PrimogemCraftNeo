package net.per.primogemcraft.system.wish;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;

public final class WishTooltips {
    private WishTooltips() {
    }

    public static boolean showsDetails() {
        return FMLEnvironment.dist == Dist.CLIENT && Screen.hasShiftDown();
    }

    public static boolean showsTutorial() {
        return FMLEnvironment.dist == Dist.CLIENT && Screen.hasControlDown();
    }

    /**
     * The player the tooltip is being built for, or null on a dedicated server. Item tooltips are assembled
     * client-side, so a description that depends on what the viewer is carrying reads the local player here.
     */
    public static Player viewer() {
        return FMLEnvironment.dist == Dist.CLIENT ? Minecraft.getInstance().player : null;
    }
}
