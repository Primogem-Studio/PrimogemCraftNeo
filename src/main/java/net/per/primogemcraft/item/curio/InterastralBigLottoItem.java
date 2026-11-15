package net.per.primogemcraft.item.curio;

import net.minecraft.text.Text;
import net.per.primogemcraft.system.curio.CurioEnchanting;
import net.per.primogemcraft.system.curio.CurioForm;
import net.per.primogemcraft.system.curio.LotteryCurioItem;

public class InterastralBigLottoItem extends LotteryCurioItem {
    private static final int HIGH_LEVEL = 20;
    private static final int LOW_LEVEL = 9;
    private static final float HIGH_LEVEL_CHANCE = 0.4F;

    public InterastralBigLottoItem(Settings properties) {
        super(CurioForm.NORMAL, 0.5D, context -> {
            var mainhand = context.player().getMainHandStack();
            if (!mainhand.isEnchantable()) {
                context.announce(Text.translatable("message.primogemcraft.curio.lottery.enchant_invalid"));
                return;
            }
            CurioEnchanting.tablePool(context.player(), mainhand, context.chance(HIGH_LEVEL_CHANCE) ? HIGH_LEVEL : LOW_LEVEL);
            context.announce(Text.translatable("message.primogemcraft.curio.lottery.enchant_gained"));
        }, context -> {
            var levels = context.player().experienceLevel;
            context.player().addExperienceLevels(-levels);
            context.announce(Text.translatable("message.primogemcraft.curio.lottery.experience_removed", levels));
        }, properties);
    }
}
