package net.per.primogemcraft.item.curio;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.per.primogemcraft.registry.PGCDataComponents;
import net.per.primogemcraft.registry.PGCEffects;
import net.per.primogemcraft.registry.PGCSounds;
import net.per.primogemcraft.system.curio.*;
import net.per.primogemcraft.system.curio.effect.CurioEffects;

public class WhimsicalFanciesMachineryCrewItem extends CurioItem {
    private static final String TRIGGER_KEY = "message.primogemcraft.curio.whimsical_fancies_machinery_crew.trigger";
    private static final int MAX_FOOD = 20;
    private static final float MAX_SATURATION = 8.0F;
    private static final float FOOD_LOSS = 0.5F;
    private static final float SATURATION_LOSS = 0.8F;
    private static final int TRIGGERED = 1;
    private static final int CLEANSED = 2;

    public WhimsicalFanciesMachineryCrewItem(Properties properties) {
        super(CurioTrigger.ACTIVE, CurioForm.NEGATIVE, properties);
    }

    @Override
    public void presence(CurioContext context) {
        var player = context.player();
        var counter = PGCDataComponents.CURIO_COUNTER.get();
        int state = context.stack().getOrDefault(counter, 0);
        if (state == CLEANSED) {
            CurioEffects.release(player, PGCEffects.WHIMSICAL_FANCIES_MACHINERY_CREW);
            return;
        }
        CurioEffects.hold(player, PGCEffects.WHIMSICAL_FANCIES_MACHINERY_CREW, MobEffectInstance.INFINITE_DURATION, 0);
        if (state == TRIGGERED) return;
        context.stack().set(counter, TRIGGERED);
        context.announce(Component.translatable(TRIGGER_KEY, context.stack().getHoverName()));
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), PGCSounds.CURIO_BROKEN.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    @Override
    public void impacted(CurioContext context, CurioImpact impact) {
        if (impact.signal() != CurioSignal.ITEM_USED) return;
        var player = context.player();
        if (!CurioEffects.active(player, PGCEffects.WHIMSICAL_FANCIES_MACHINERY_CREW)) return;
        var food = impact.item().get(DataComponents.FOOD);
        if (food == null || food.nutrition() <= 0) return;
        var data = player.getFoodData();
        data.setFoodLevel(Math.min(MAX_FOOD, data.getFoodLevel() - (int) (food.nutrition() * FOOD_LOSS)));
        if (food.saturation() > 0.0F) data.setSaturation(Math.min(MAX_SATURATION, data.getSaturationLevel() - food.saturation() * SATURATION_LOSS));
    }

    public static void clear(ServerPlayer player) {
        var counter = PGCDataComponents.CURIO_COUNTER.get();
        for (var context : Curios.held(player))
            if (context.stack().getItem() instanceof WhimsicalFanciesMachineryCrewItem) context.stack().set(counter, CLEANSED);
        CurioEffects.release(player, PGCEffects.WHIMSICAL_FANCIES_MACHINERY_CREW);
    }
}
