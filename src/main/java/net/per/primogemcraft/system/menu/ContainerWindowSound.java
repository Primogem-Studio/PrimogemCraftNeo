package net.per.primogemcraft.system.menu;

import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public record ContainerWindowSound(SoundEvent event, SoundCategory source, float volume, float minPitch, float maxPitch) {
    public static final ContainerWindowSound POUCH_OPEN =
            new ContainerWindowSound(SoundEvents.ENTITY_PIG_SADDLE, SoundCategory.PLAYERS, 0.3F, 0.8F, 1.1F);
    public static final ContainerWindowSound POUCH_CLOSE =
            new ContainerWindowSound(SoundEvents.BLOCK_WOOL_BREAK, SoundCategory.PLAYERS, 0.3F, 0.2F, 0.5F);
    public static final ContainerWindowSound TRASH_CAN_OPEN =
            new ContainerWindowSound(SoundEvents.BLOCK_SHULKER_BOX_OPEN, SoundCategory.BLOCKS, 0.5F, 2.0F, 2.0F);
    public static final ContainerWindowSound TRASH_CAN_CLOSE =
            new ContainerWindowSound(SoundEvents.BLOCK_SHULKER_BOX_CLOSE, SoundCategory.BLOCKS, 0.5F, 1.5F, 1.5F);

    public void play(World level, double x, double y, double z, Random random) {
        level.playSound(null, x, y, z, event, source, volume, MathHelper.nextFloat(random, minPitch, maxPitch));
    }
}
