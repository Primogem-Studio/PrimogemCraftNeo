package net.per.primogemcraft.system.zipline;

import net.minecraft.world.damagesource.DamageTypes;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.per.primogemcraft.entity.misc.ZiplineCarrierEntity;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

@Mod.EventBusSubscriber(modid = MOD_ID)
public final class ZiplineEvents {
    @SubscribeEvent
    public static void incomingDamage(LivingHurtEvent event) {
        if (event.getEntity().getVehicle() instanceof ZiplineCarrierEntity && event.getSource().is(DamageTypes.IN_WALL))
            event.setCanceled(true);
    }

    @SubscribeEvent
    public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity().getVehicle() instanceof ZiplineCarrierEntity carrier) carrier.release();
    }
}
