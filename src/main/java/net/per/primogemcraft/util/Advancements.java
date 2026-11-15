package net.per.primogemcraft.util;

import net.minecraft.util.Identifier;
import net.minecraft.server.network.ServerPlayerEntity;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public final class Advancements {
    private Advancements() {
    }

    public static void grant(ServerPlayerEntity player, String path) {
        var holder = player.server.getAdvancementLoader().get(Identifier.of(MOD_ID, path));
        if (holder == null) return;
        var progress = player.getAdvancementTracker().getProgress(holder);
        if (progress.isDone()) return;
        for (var criteria : progress.getUnobtainedCriteria()) player.getAdvancementTracker().grantCriterion(holder, criteria);
    }
}
