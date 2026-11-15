package net.per.primogemcraft.item.curio;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.per.primogemcraft.system.curio.*;

public class ObliterationWickTrimmerItem extends CurioItem {
    protected static final int TICKS_PER_HARDNESS = 100;
    protected static final int MAX_STRENGTH_LEVEL = 3;

    public ObliterationWickTrimmerItem(CurioForm form, Settings properties) {
        super(CurioTrigger.ACTIVE, form, properties);
    }

    @Override
    public void impacted(CurioContext context, CurioImpact impact) {
        if (impact.signal() != CurioSignal.BLOCK_BROKEN) return;
        var hardness = hardness(impact);
        if (hardness <= 0.0F) return;
        strength(context, hardness);
        bonus(context, impact, hardness);
    }

    protected void bonus(CurioContext context, CurioImpact impact, float hardness) {
    }

    protected static float hardness(CurioImpact impact) {
        return impact.state().getHardness(impact.level(), BlockPos.ofFloored(impact.position()));
    }

    protected static void strength(CurioContext context, float hardness) {
        var duration = (int) (hardness * TICKS_PER_HARDNESS);
        var level = MathHelper.nextInt(context.random(), 0, MAX_STRENGTH_LEVEL);
        context.player().addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, duration, level, false, false));
    }
}
