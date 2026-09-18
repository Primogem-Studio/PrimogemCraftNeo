package net.per.primogemcraft.enchantment;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.per.primogemcraft.system.curio.CurioLoot;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

@EventBusSubscriber(modid = MOD_ID)
public final class EnchantmentEvents {
    private EnchantmentEvents() {
    }

    @SubscribeEvent
    public static void onBlockBroken(BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;
        var state = event.getState();
        if (CurioLoot.isJar(state)) CanOpener.pryOpen(player, state, event.getPos().getCenter());
    }

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        var victim = event.getEntity();
        var source = event.getSource();
        var attacker = source.getEntity();
        if (attacker instanceof LivingEntity living) {
            FoolsWrath.punish(living, victim);
            TheHunt.harvest(living, victim);
        }
        var direct = source.getDirectEntity();
        if (direct instanceof LivingEntity living && direct != attacker) FoolsWrath.punish(living, victim);
    }
}
