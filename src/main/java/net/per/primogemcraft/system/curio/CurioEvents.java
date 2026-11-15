package net.per.primogemcraft.system.curio;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.per.primogemcraft.enchantment.EnchantReward;
import net.per.primogemcraft.util.PGCTimer;

public final class CurioEvents {
    private CurioEvents() {
    }

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (var player : server.getPlayerManager().getPlayerList()) {
                Curios.tickEquipped(player);
                CurioAttributes.sweep(player);
                CurioReward.tick(player);
                EnchantReward.tick(player);
            }
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            var player = handler.getPlayer();
            CurioAttributes.clear(player);
            DeathSaveCurioItem.clearPunishment(player);
            CurioReward.clear(player);
            EnchantReward.clear(player);
        });
        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
            PGCTimer.clear(oldPlayer);
            CurioAttributes.clear(oldPlayer);
            DeathSaveCurioItem.endLife(newPlayer);
        });
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            if (!(player instanceof ServerPlayerEntity serverPlayer)) return;
            var container = CurioLoot.isJar(state);
            var signal = container ? CurioSignal.CONTAINER_BROKEN : CurioSignal.BLOCK_BROKEN;
            Curios.signal(serverPlayer, CurioImpact.ofBlock(signal, serverPlayer.getServerWorld(), pos.toCenterPos(), state));
        });
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            if (entity instanceof ServerPlayerEntity victim) {
                var impact = CurioImpact.ofEntity(CurioSignal.HURT, victim.getServerWorld(), victim.getPos(), source.getAttacker(), amount);
                if (Curios.absorbsDamage(victim, impact)) {
                    return false;
                }
                Curios.signal(victim, impact);
            }
            if (source.getAttacker() instanceof ServerPlayerEntity attacker) {
                Curios.signal(attacker, CurioImpact.ofEntity(CurioSignal.ATTACK, attacker.getServerWorld(), attacker.getPos(), entity, amount));
            }
            return true;
        });
        ServerLivingEntityEvents.ALLOW_DEATH.register((entity, source, amount) -> {
            if (entity instanceof ServerPlayerEntity player) {
                var impact = CurioImpact.ofEntity(CurioSignal.DEATH, player.getServerWorld(), player.getPos(), source.getAttacker(), 0.0F);
                if (Curios.survivesDeath(player, impact)) {
                    return false;
                }
                Curios.signal(player, impact);
            }
            return true;
        });
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
            if (source.getAttacker() instanceof ServerPlayerEntity killer) {
                Curios.signal(killer, CurioImpact.ofEntity(CurioSignal.KILL, killer.getServerWorld(), killer.getPos(), entity, 0.0F));
            }
        });
    }
}