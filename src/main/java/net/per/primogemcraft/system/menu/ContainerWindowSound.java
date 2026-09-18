package net.per.primogemcraft.system.menu;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;

public record ContainerWindowSound(SoundEvent event, SoundSource source, float volume, float minPitch, float maxPitch) {
    public static final ContainerWindowSound POUCH_OPEN =
            new ContainerWindowSound(SoundEvents.PIG_SADDLE, SoundSource.PLAYERS, 0.3F, 0.8F, 1.1F);
    public static final ContainerWindowSound POUCH_CLOSE =
            new ContainerWindowSound(SoundEvents.WOOL_BREAK, SoundSource.PLAYERS, 0.3F, 0.2F, 0.5F);
    public static final ContainerWindowSound TRASH_CAN_OPEN =
            new ContainerWindowSound(SoundEvents.SHULKER_BOX_OPEN, SoundSource.BLOCKS, 0.5F, 2.0F, 2.0F);
    public static final ContainerWindowSound TRASH_CAN_CLOSE =
            new ContainerWindowSound(SoundEvents.SHULKER_BOX_CLOSE, SoundSource.BLOCKS, 0.5F, 1.5F, 1.5F);

    public void play(Level level, double x, double y, double z, RandomSource random) {
        level.playSound(null, x, y, z, event, source, volume, Mth.nextFloat(random, minPitch, maxPitch));
    }
}
