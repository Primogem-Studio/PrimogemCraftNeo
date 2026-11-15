package net.per.primogemcraft.item.misc;

import net.minecraft.text.Text;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.per.primogemcraft.registry.PGCEffects;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.system.element.effect.ThePastEffect;
import net.per.primogemcraft.util.Advancements;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public final class DreamSakuraEvents {
    private static final String SAVED_KEY = "message.primogemcraft.dream_sakura.saved";

    private DreamSakuraEvents() {
    }

    public static void register() {
        ServerLivingEntityEvents.ALLOW_DEATH.register((entity, source, amount) -> {
            if (!(entity instanceof ServerPlayerEntity player)) return true;
            if (!player.hasStatusEffect(PGCEffects.THE_PAST.get())) return true;
            var sakura = carried(player);
            if (sakura.isEmpty()) return true;
            ThePastEffect.rewind(player);
            player.removeStatusEffect(PGCEffects.THE_PAST.get());
            if (sakura.getItem() instanceof DreamSakuraItem dreamSakura) dreamSakura.onSaved(player, sakura);
            Advancements.grant(player, "back_from_the_dead");
            player.server.getPlayerManager().broadcast(Text.translatable(SAVED_KEY, player.getName()), false);
            return false;
        });
    }

    private static ItemStack carried(ServerPlayerEntity player) {
        for (var stack : player.getInventory().main) if (stack.isOf(PGCItems.DREAM_SAKURA.get())) return stack;
        for (var stack : player.getInventory().offHand) if (stack.isOf(PGCItems.DREAM_SAKURA.get())) return stack;
        return ItemStack.EMPTY;
    }
}
