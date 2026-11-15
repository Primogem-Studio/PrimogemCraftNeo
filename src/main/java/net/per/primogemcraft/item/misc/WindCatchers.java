package net.per.primogemcraft.item.misc;

import net.minecraft.sound.SoundEvents;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.Hand;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
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

    public static void use(World level, PlayerEntity player, boolean filled) {
        var held = player.getMainHandStack().getItem();
        if (filled) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ITEM_TRIDENT_RIPTIDE_2, SoundCategory.PLAYERS, VOLUME,
                    MathHelper.nextInt(player.getRandom(), MIN_PITCH, MAX_PITCH));
            player.setVelocity(0.0D, LAUNCH_VELOCITY, 0.0D);
            player.velocityModified = true;
            player.hurtTime = 10;
            if (!player.isCreative())
                player.setStackInHand(Hand.MAIN_HAND, new ItemStack(PGCItems.OTHERWORLD_WIND_CATCHER_EMPTY.get()));
            player.getItemCooldownManager().set(held, FILLED_COOLDOWN);
            return;
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ITEM_BOTTLE_FILL, SoundCategory.PLAYERS, 1.0F, 1.0F);
        player.setStackInHand(Hand.MAIN_HAND, new ItemStack(PGCItems.OTHERWORLD_WIND_CATCHER.get()));
        player.getItemCooldownManager().set(held, EMPTY_COOLDOWN);
    }
}
