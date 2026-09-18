package net.per.primogemcraft.system.event;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

@EventBusSubscriber(modid = MOD_ID)
public final class EventEvents {
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof Player) return;
        if (!(event.getEntity().level() instanceof ServerLevel level)) return;
        if (!(event.getSource().getEntity() instanceof ServerPlayer killer)) return;
        EventCombat.killed(killer, event.getEntity());
        EventRegistry.drop(level, event.getEntity().position(), killer);
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        EventCombat.tick(event.getServer());
        EventChain.tick(event.getServer());
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        EventChain.clear(event.getEntity().getUUID());
    }
}
