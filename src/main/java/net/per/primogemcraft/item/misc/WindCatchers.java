package net.per.primogemcraft.item.misc;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.per.primogemcraft.registry.PGCItems;

public final class WindCatchers {
    private static final int FILLED_COOLDOWN = 70;
    private static final int EMPTY_COOLDOWN = 40;
    private static final double LAUNCH_VELOCITY = 1.35D;
    private static final float VOLUME = 0.5F;
    private static final int MIN_PITCH = 1;
    private static final int MAX_PITCH = 3;

    private WindCatchers() {
    }

    public static void use(Level level, Player player, boolean filled) {
        var held = player.getMainHandItem().getItem();
        if (filled) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.TRIDENT_RIPTIDE_2, SoundSource.PLAYERS, VOLUME,
                    Mth.nextInt(player.getRandom(), MIN_PITCH, MAX_PITCH));
            player.setDeltaMovement(0.0D, LAUNCH_VELOCITY, 0.0D);
            player.hasImpulse = true;
            player.hurtMarked = true;
            if (!player.isCreative())
                player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(PGCItems.OTHERWORLD_WIND_CATCHER_EMPTY.get()));
            player.getCooldowns().addCooldown(held, FILLED_COOLDOWN);
            return;
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BOTTLE_FILL, SoundSource.PLAYERS, 1.0F, 1.0F);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(PGCItems.OTHERWORLD_WIND_CATCHER.get()));
        player.getCooldowns().addCooldown(held, EMPTY_COOLDOWN);
    }
}
