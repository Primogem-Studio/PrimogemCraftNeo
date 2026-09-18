package net.per.primogemcraft.item.curio;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.per.primogemcraft.registry.PGCDataComponents;
import net.per.primogemcraft.system.curio.CurioContext;
import net.per.primogemcraft.system.curio.CurioForm;
import net.per.primogemcraft.system.curio.CurioItem;
import net.per.primogemcraft.system.curio.CurioTrigger;
import net.per.primogemcraft.system.curio.effect.CurioEffects;
import net.per.primogemcraft.system.curio.effect.PrescriptionEffect;

public class PrescriptionCurioItem extends CurioItem {
    private static final int APPLIED = 1;

    private final DeferredHolder<MobEffect, PrescriptionEffect> effect;

    public PrescriptionCurioItem(DeferredHolder<MobEffect, PrescriptionEffect> effect, Properties properties) {
        super(CurioTrigger.ACTIVE, CurioForm.NEGATIVE, properties);
        this.effect = effect;
    }

    @Override
    public void presence(CurioContext context) {
        var counter = PGCDataComponents.CURIO_COUNTER.get();
        if (context.stack().getOrDefault(counter, 0) == APPLIED) return;
        var ticks = effect.value().prescription().duration();
        CurioEffects.ensure(context.player(), effect, ticks > 0 ? ticks : MobEffectInstance.INFINITE_DURATION, 0);
        context.stack().set(counter, APPLIED);
    }
}
