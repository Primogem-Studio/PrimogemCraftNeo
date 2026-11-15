package net.per.primogemcraft.item.curio;

import net.per.primogemcraft.compat.component.StackComponents;
import net.minecraft.text.Text;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.LivingEntity;
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

    public InvalidThoughtCodeMachineItem(Settings properties) {
        super(CurioTrigger.ACTIVE, CurioForm.NEGATIVE, 1, properties);
    }

    @Override
    public void presence(CurioContext context) {
        var player = context.player();
        var counter = PGCDataComponents.CURIO_COUNTER.get();
        int state = StackComponents.getOrDefault(context.stack(), counter, 0);
        if (state == CLEANSED) {
            CurioEffects.release(player, PGCEffects.INVALID_THOUGHT_CODE_MACHINE.get());
            return;
        }
        CurioEffects.hold(player, PGCEffects.INVALID_THOUGHT_CODE_MACHINE.get(), StatusEffectInstance.INFINITE, 0);
        if (state == TRIGGERED) return;
        StackComponents.set(context.stack(), counter, TRIGGERED);
        context.announce(Text.translatable(TRIGGER_KEY, context.stack().getName()));
        player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), PGCSounds.CURIO_BROKEN.get(), SoundCategory.PLAYERS, 1.0F, 1.0F);
    }

    @Override
    public void impacted(CurioContext context, CurioImpact impact) {
        if (impact.signal() != CurioSignal.ATTACK) return;
        var player = context.player();
        if (!CurioEffects.active(player, PGCEffects.INVALID_THOUGHT_CODE_MACHINE.get())) return;
        if (!(impact.subject() instanceof LivingEntity victim)) return;
        if (victim.getHealth() < victim.getMaxHealth()) return;
        if (!context.ready(COOLDOWN, COOLDOWN_TICKS)) return;
        var food = player.getHungerManager();
        food.setFoodLevel((int) (food.getFoodLevel() * FOOD_RETAINED));
    }

    public static void clear(ServerPlayerEntity player) {
        var counter = PGCDataComponents.CURIO_COUNTER.get();
        for (var context : Curios.held(player))
            if (context.stack().getItem() instanceof InvalidThoughtCodeMachineItem) StackComponents.set(context.stack(), counter, CLEANSED);
        CurioEffects.release(player, PGCEffects.INVALID_THOUGHT_CODE_MACHINE.get());
    }
}
