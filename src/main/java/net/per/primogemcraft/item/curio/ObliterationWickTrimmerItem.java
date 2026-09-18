package net.per.primogemcraft.item.curio;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.per.primogemcraft.system.curio.*;

public class ObliterationWickTrimmerItem extends CurioItem {
    protected static final int TICKS_PER_HARDNESS = 100;
    protected static final int MAX_STRENGTH_LEVEL = 3;

    public ObliterationWickTrimmerItem(CurioForm form, Properties properties) {
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
        return impact.state().getDestroySpeed(impact.level(), BlockPos.containing(impact.position()));
    }

    protected static void strength(CurioContext context, float hardness) {
        var duration = (int) (hardness * TICKS_PER_HARDNESS);
        var level = Mth.nextInt(context.random(), 0, MAX_STRENGTH_LEVEL);
        context.player().addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, duration, level, false, false));
    }
}
