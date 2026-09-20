package net.per.primogemcraft.client;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.per.primogemcraft.entity.misc.ZiplineCarrierEntity;
import net.per.primogemcraft.registry.PGCSounds;

public final class ZiplineTravelSound extends AbstractTickableSoundInstance {
    private final ZiplineCarrierEntity carrier;

    public ZiplineTravelSound(ZiplineCarrierEntity carrier) {
        super(PGCSounds.ZIPLINE_TRAVEL.get(), SoundSource.NEUTRAL, RandomSource.create());
        this.carrier = carrier;
        looping = true;
        delay = 0;
        volume = 0.8F;
        tick();
    }

    @Override
    public void tick() {
        if (carrier.isRemoved() || !carrier.moving()) {
            stop();
            return;
        }
        x = carrier.getX();
        y = carrier.getY();
        z = carrier.getZ();
    }
}
