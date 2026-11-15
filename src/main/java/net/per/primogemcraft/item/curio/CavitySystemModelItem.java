package net.per.primogemcraft.item.curio;

import net.minecraft.world.World;
import net.per.primogemcraft.compat.component.StackComponents;
import net.minecraft.text.Text;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.client.item.TooltipContext;
import net.per.primogemcraft.registry.PGCDataComponents;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.system.curio.CurioContext;
import net.per.primogemcraft.system.curio.CurioForm;
import net.per.primogemcraft.system.curio.CurioItem;
import net.per.primogemcraft.system.curio.CurioTrigger;
import net.per.primogemcraft.util.PlayerItems;

import java.util.List;

public class CavitySystemModelItem extends CurioItem {
    private static final String CONSUMED_KEY = "message.primogemcraft.curio.cavity_system_model.consumed";
    private static final int TOOLTIP_LINES = 10;
    private static final int INITIAL_DURATION = 60;
    private static final int FRAGMENTS_PER_LEVEL = 16;
    private static final int LEVEL_COST = 5;
    private static final int SECONDS_PER_MINUTE = 60;
    private static final int TICKS_PER_SECOND = 20;

    public CavitySystemModelItem(Settings properties) {
        super(CurioTrigger.RIGHT_CLICK, CurioForm.NORMAL, 1, properties);
    }

    @Override
    public void activated(CurioContext context) {
        var player = context.player();
        var stack = context.stack();
        var fragment = PGCItems.COSMIC_FRAGMENT.get();
        var fragments = PlayerItems.count(player, fragment);
        if (fragments <= 0) return;
        var duration = duration(stack);
        if (player.isSneaking()) {
            var cost = duration / SECONDS_PER_MINUTE;
            if (fragments < cost) return;
            PlayerItems.take(player, fragment, cost);
            StackComponents.set(stack, PGCDataComponents.CURIO_COUNTER, duration + 1);
            return;
        }
        PlayerItems.take(player, fragment, fragments);
        var level = fragments / FRAGMENTS_PER_LEVEL;
        if (level > 0) player.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, duration * TICKS_PER_SECOND, level - 1, false, false));
        var spent = duration - (int) Math.ceil(fragments / (double) FRAGMENTS_PER_LEVEL) * LEVEL_COST;
        StackComponents.set(stack, PGCDataComponents.CURIO_COUNTER, Math.max(0, spent));
        context.announce(Text.translatable(CONSUMED_KEY));
        if (spent < 1) context.damage(1);
    }

    @Override
    public void appendTooltip(ItemStack stack, @org.jetbrains.annotations.Nullable World level, List<Text> tooltip, TooltipContext flag) {
        appendTrigger(stack, tooltip);
        var prefix = stack.getTranslationKey() + ".tooltip.";
        for (var index = 0; index < TOOLTIP_LINES; index++) tooltip.add(Text.translatable(prefix + index));
        tooltip.add(Text.translatable(prefix + TOOLTIP_LINES, duration(stack)));
    }

    private static int duration(ItemStack stack) {
        return StackComponents.getOrDefault(stack, PGCDataComponents.CURIO_COUNTER, INITIAL_DURATION);
    }
}
