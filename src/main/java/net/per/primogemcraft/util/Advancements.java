package net.per.primogemcraft.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public final class Advancements {
    private Advancements() {
    }

    public static void grant(ServerPlayer player, String path) {
        var holder = player.server.getAdvancements().get(ResourceLocation.fromNamespaceAndPath(MOD_ID, path));
        if (holder == null) return;
        var progress = player.getAdvancements().getOrStartProgress(holder);
        if (progress.isDone()) return;
        for (var criteria : progress.getRemainingCriteria()) player.getAdvancements().award(holder, criteria);
    }
}
