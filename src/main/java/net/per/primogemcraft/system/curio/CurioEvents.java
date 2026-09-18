package net.per.primogemcraft.system.curio;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerXpEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.per.primogemcraft.enchantment.EnchantReward;
import net.per.primogemcraft.util.PGCTimer;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

@EventBusSubscriber(modid = MOD_ID)
public final class CurioEvents {
    private CurioEvents() {
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        Curios.tickEquipped(player);
        CurioAttributes.sweep(player);
        CurioReward.tick(player);
        EnchantReward.tick(player);
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        CurioAttributes.clear(player);
        DeathSaveCurioItem.clearPunishment(player);
        CurioReward.clear(player);
        EnchantReward.clear(player);
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        PGCTimer.clear(event.getOriginal());
        CurioAttributes.clear(event.getOriginal());
        if (event.getEntity() instanceof ServerPlayer player) DeathSaveCurioItem.endLife(player);
    }

    @SubscribeEvent
    public static void onBlockBroken(BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;
        var state = event.getState();
        var container = CurioLoot.isJar(state);
        var signal = container ? CurioSignal.CONTAINER_BROKEN : CurioSignal.BLOCK_BROKEN;
        Curios.signal(player, CurioImpact.ofBlock(signal, player.serverLevel(), event.getPos().getCenter(), state));
    }

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (event.getEntity() instanceof ServerPlayer victim) {
            var impact = CurioImpact.ofEntity(CurioSignal.HURT, victim.serverLevel(), victim.position(), event.getSource().getEntity(), event.getAmount());
            if (Curios.absorbsDamage(victim, impact)) {
                event.setCanceled(true);
                return;
            }
            Curios.signal(victim, impact);
        }
        if (event.getSource().getEntity() instanceof ServerPlayer attacker)
            Curios.signal(attacker, CurioImpact.ofEntity(CurioSignal.ATTACK, attacker.serverLevel(), attacker.position(), event.getEntity(), event.getAmount()));
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            var impact = CurioImpact.ofEntity(CurioSignal.DEATH, player.serverLevel(), player.position(), event.getSource().getEntity(), 0.0F);
            if (Curios.survivesDeath(player, impact)) {
                event.setCanceled(true);
                return;
            }
            Curios.signal(player, impact);
        }
        if (event.getSource().getEntity() instanceof ServerPlayer killer)
            Curios.signal(killer, CurioImpact.ofEntity(CurioSignal.KILL, killer.serverLevel(), killer.position(), event.getEntity(), 0.0F));
    }

    @SubscribeEvent
    public static void onItemUsed(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        Curios.signal(player, CurioImpact.ofItem(CurioSignal.ITEM_USED, player.serverLevel(), player.position(), event.getItem()));
    }

    @SubscribeEvent
    public static void onPickupXp(PlayerXpEvent.PickupXp event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        Curios.signal(player, CurioImpact.ofEntity(CurioSignal.XP_PICKED, player.serverLevel(), player.position(), event.getOrb(), 0.0F));
    }

    @SubscribeEvent
    public static void onItemPickup(ItemEntityPickupEvent.Pre event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;
        var stack = event.getItemEntity().getItem();
        if (stack.getItem() instanceof CurioItem curio) curio.pickedUp(player, stack);
    }
}
