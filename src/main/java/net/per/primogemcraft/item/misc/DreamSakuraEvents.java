package net.per.primogemcraft.item.misc;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.per.primogemcraft.registry.PGCEffects;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.system.element.effect.ThePastEffect;
import net.per.primogemcraft.util.Advancements;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

@EventBusSubscriber(modid = MOD_ID)
public final class DreamSakuraEvents {
    private static final String SAVED_KEY = "message.primogemcraft.dream_sakura.saved";

    private DreamSakuraEvents() {
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!player.hasEffect(PGCEffects.THE_PAST)) return;
        var sakura = carried(player);
        if (sakura.isEmpty()) return;
        event.setCanceled(true);
        ThePastEffect.rewind(player);
        player.removeEffect(PGCEffects.THE_PAST);
        if (sakura.getItem() instanceof DreamSakuraItem dreamSakura) dreamSakura.onSaved(player, sakura);
        Advancements.grant(player, "back_from_the_dead");
        player.server.getPlayerList().broadcastSystemMessage(Component.translatable(SAVED_KEY, player.getDisplayName()), false);
    }

    private static ItemStack carried(ServerPlayer player) {
        for (var stack : player.getInventory().items) if (stack.is(PGCItems.DREAM_SAKURA.get())) return stack;
        for (var stack : player.getInventory().offhand) if (stack.is(PGCItems.DREAM_SAKURA.get())) return stack;
        return ItemStack.EMPTY;
    }
}
