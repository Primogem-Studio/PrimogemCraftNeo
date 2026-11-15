package net.per.primogemcraft.enchantment;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.per.primogemcraft.system.curio.CurioLoot;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

@Mod.EventBusSubscriber(modid = MOD_ID)
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
    public static void onIncomingDamage(LivingHurtEvent event) {
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
