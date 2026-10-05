package net.per.primogemcraft.client;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.per.primogemcraft.collab.genshincraft.LivingItemProjectMigration;

import static net.per.primogemcraft.PrimogemCraft.LOGGER;
import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

@EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT)
public final class LivingItemBlueprintMigration {
    private LivingItemBlueprintMigration() {
    }

    @SubscribeEvent
    public static void opening(ScreenEvent.Opening event) {
        var screen = event.getNewScreen();
        if (screen == null || !screen.getClass().getName().equals("net.hackermdch.genshincraft.gui.BlueprintDesignScreen")) return;
        var minecraft = Minecraft.getInstance();
        try {
            LivingItemProjectMigration.migrate(minecraft.gameDirectory.toPath().resolve("genshincraft/blueprint_projects.json"));
        } catch (Exception exception) {
            event.setCanceled(true);
            LOGGER.error("Unable to migrate legacy living-item blueprint projects", exception);
            if (minecraft.player != null) {
                minecraft.player.displayClientMessage(Component.translatable("message.primogemcraft.living_item_project_migration_failed"), false);
            }
        }
    }
}
