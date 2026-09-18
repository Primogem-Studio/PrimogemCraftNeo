package net.per.primogemcraft.item.curio;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.per.primogemcraft.registry.PGCDataComponents;
import net.per.primogemcraft.registry.PGCEffects;
import net.per.primogemcraft.registry.PGCSounds;
import net.per.primogemcraft.system.curio.*;
import net.per.primogemcraft.system.curio.effect.CurioEffects;

public class InvalidThoughtCodeMachineItem extends CurioItem {
    private static final String TRIGGER_KEY = "message.primogemcraft.curio.invalid_thought_code_machine.trigger";
    private static final String COOLDOWN = "curio/invalid_thought_code_machine";
    private static final int COOLDOWN_TICKS = 400;
    private static final float FOOD_RETAINED = 0.5F;
    private static final int TRIGGERED = 1;
    private static final int CLEANSED = 2;

    public InvalidThoughtCodeMachineItem(Properties properties) {
        super(CurioTrigger.ACTIVE, CurioForm.NEGATIVE, 1, properties);
    }

    @Override
    public void presence(CurioContext context) {
        var player = context.player();
        var counter = PGCDataComponents.CURIO_COUNTER.get();
        int state = context.stack().getOrDefault(counter, 0);
        if (state == CLEANSED) {
            CurioEffects.release(player, PGCEffects.INVALID_THOUGHT_CODE_MACHINE);
            return;
        }
        CurioEffects.hold(player, PGCEffects.INVALID_THOUGHT_CODE_MACHINE, MobEffectInstance.INFINITE_DURATION, 0);
        if (state == TRIGGERED) return;
        context.stack().set(counter, TRIGGERED);
        context.announce(Component.translatable(TRIGGER_KEY, context.stack().getHoverName()));
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), PGCSounds.CURIO_BROKEN.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    @Override
    public void impacted(CurioContext context, CurioImpact impact) {
        if (impact.signal() != CurioSignal.ATTACK) return;
        var player = context.player();
        if (!CurioEffects.active(player, PGCEffects.INVALID_THOUGHT_CODE_MACHINE)) return;
        if (!(impact.subject() instanceof LivingEntity victim)) return;
        if (victim.getHealth() < victim.getMaxHealth()) return;
        if (!context.ready(COOLDOWN, COOLDOWN_TICKS)) return;
        var food = player.getFoodData();
        food.setFoodLevel((int) (food.getFoodLevel() * FOOD_RETAINED));
    }

    public static void clear(ServerPlayer player) {
        var counter = PGCDataComponents.CURIO_COUNTER.get();
        for (var context : Curios.held(player))
            if (context.stack().getItem() instanceof InvalidThoughtCodeMachineItem) context.stack().set(counter, CLEANSED);
        CurioEffects.release(player, PGCEffects.INVALID_THOUGHT_CODE_MACHINE);
    }
}
