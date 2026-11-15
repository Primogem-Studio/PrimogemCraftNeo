package net.per.primogemcraft.system.zipline;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.registry.tag.DamageTypeTags;
import net.per.primogemcraft.entity.misc.ZiplineCarrierEntity;

public final class ZiplineEvents {
    public static void register() {
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            if (entity.getVehicle() instanceof ZiplineCarrierEntity && source.isIn(DamageTypeTags.IS_FALL)) {
                return false;
            }
            return true;
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            if (handler.getPlayer().getVehicle() instanceof ZiplineCarrierEntity carrier) carrier.release();
        });
    }
}