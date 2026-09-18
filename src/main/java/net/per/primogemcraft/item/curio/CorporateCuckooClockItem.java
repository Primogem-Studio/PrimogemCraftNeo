package net.per.primogemcraft.item.curio;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.per.primogemcraft.registry.PGCDataComponents;
import net.per.primogemcraft.registry.PGCSounds;
import net.per.primogemcraft.system.curio.CurioContext;
import net.per.primogemcraft.system.curio.CurioForm;
import net.per.primogemcraft.system.curio.CurioItem;
import net.per.primogemcraft.system.curio.CurioTrigger;

public class CorporateCuckooClockItem extends CurioItem {
    private static final String DRAIN_KEY = "message.primogemcraft.curio.corporate_cuckoo_clock.drain";
    private static final int INTEGRITY = 2233;
    private static final double MIN_LOSS = 0.025D;
    private static final double MAX_LOSS = 0.75D;
    private static final int PERCENT_TENTHS = 1000;
    private static final float SOUND_VOLUME = 4.0F;
    private static final float SOUND_PITCH = 0.5F;

    public CorporateCuckooClockItem(Properties properties) {
        super(CurioTrigger.ACTIVE, CurioForm.FUSION, INTEGRITY, properties);
    }

    @Override
    public void pickedUp(ServerPlayer player, ItemStack stack) {
        arm(player, stack);
    }

    @Override
    public void presence(CurioContext context) {
        arm(context.player(), context.stack());
    }

    private static void arm(ServerPlayer player, ItemStack stack) {
        var bar = stack.get(PGCDataComponents.CUSTOM_BAR.get());
        if (bar == null || bar.numerator() > 0) return;
        if (player.experienceLevel < 1) return;
        var ratio = Mth.nextDouble(player.getRandom(), MIN_LOSS, MAX_LOSS);
        var drained = (int) (player.totalExperience * ratio);
        player.giveExperiencePoints(-drained);
        player.displayClientMessage(Component.translatable(DRAIN_KEY, stack.getHoverName(), drained, percent(ratio)), false);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), PGCSounds.CURIO_BROKEN.get(), SoundSource.PLAYERS, SOUND_VOLUME, SOUND_PITCH);
        CurioContext.of(player, stack, CurioForm.FUSION).damage(INTEGRITY - 1);
    }

    private static Component percent(double ratio) {
        var tenths = Math.round(ratio * PERCENT_TENTHS);
        var whole = tenths / 10;
        var fraction = tenths % 10;
        return Component.literal(whole + "." + fraction);
    }
}
