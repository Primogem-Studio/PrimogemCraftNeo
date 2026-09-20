package net.per.primogemcraft.system.zipline;

import net.minecraft.world.damagesource.DamageTypes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.per.primogemcraft.entity.misc.ZiplineCarrierEntity;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

@EventBusSubscriber(modid = MOD_ID)
public final class ZiplineEvents {
    @SubscribeEvent
    public static void incomingDamage(LivingIncomingDamageEvent event) {
        if (event.getEntity().getVehicle() instanceof ZiplineCarrierEntity && event.getSource().is(DamageTypes.IN_WALL))
            event.setCanceled(true);
    }

    @SubscribeEvent
    public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity().getVehicle() instanceof ZiplineCarrierEntity carrier) carrier.release();
    }
}
