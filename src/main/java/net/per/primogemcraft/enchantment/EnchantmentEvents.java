package net.per.primogemcraft.enchantment;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.per.primogemcraft.system.curio.CurioLoot;

public final class EnchantmentEvents {
    private EnchantmentEvents() {
    }

    public static void register() {
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            if (!(player instanceof ServerPlayerEntity serverPlayer)) return;
            if (CurioLoot.isJar(state)) CanOpener.pryOpen(serverPlayer, state, pos.toCenterPos());
        });
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((victim, source, amount) -> {
            var attacker = source.getAttacker();
            if (attacker instanceof LivingEntity living) {
                FoolsWrath.punish(living, victim);
                TheHunt.harvest(living, victim);
            }
            var direct = source.getSource();
            if (direct instanceof LivingEntity living && direct != attacker) FoolsWrath.punish(living, victim);
            return true;
        });
    }
}