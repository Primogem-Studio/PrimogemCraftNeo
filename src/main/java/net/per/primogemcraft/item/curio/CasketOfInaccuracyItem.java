package net.per.primogemcraft.item.curio;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.per.primogemcraft.enchantment.EnchantCost;
import net.per.primogemcraft.enchantment.EnchantGrade;
import net.per.primogemcraft.enchantment.EnchantOption;
import net.per.primogemcraft.enchantment.EnchantReward;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.system.curio.*;

import java.util.List;

public class CasketOfInaccuracyItem extends CurioItem {
    private static final String INTERFERENCE_KEY = "message.primogemcraft.curio.casket_of_inaccuracy.interference";
    private static final String UNAVAILABLE_KEY = "message.primogemcraft.curio.casket_of_inaccuracy.unavailable";
    private static final String HINT_TIMER = "curio/casket_of_inaccuracy_hint";
    private static final int HINT_TICKS = 600;
    private static final double HIGH_LEVEL_CHANCE = 0.1D;

    public CasketOfInaccuracyItem(Properties properties) {
        super(CurioTrigger.ACTIVE, CurioForm.NORMAL, 1, () -> new ItemStack(PGCItems.DAMAGED_CASKET_OF_INACCURACY.get()), properties);
    }

    @Override
    public void pickedUp(ServerPlayer player, ItemStack stack) {
        trigger(CurioContext.of(player, stack, form()));
    }

    @Override
    public void presence(CurioContext context) {
        trigger(context);
    }

    private void trigger(CurioContext context) {
        var player = context.player();
        if (Curios.randomInventoryItem(player, CasketOfInaccuracyItem::workable).isEmpty()) {
            if (context.ready(HINT_TIMER, HINT_TICKS)) context.announce(Component.translatable(UNAVAILABLE_KEY));
            return;
        }
        if (Curios.randomInventoryItem(player, CasketOfInaccuracyItem::enchantable).isEmpty()) {
            if (context.ready(HINT_TIMER, HINT_TICKS)) context.announce(Component.translatable(INTERFERENCE_KEY));
            return;
        }
        context.damage(1);
    }

    @Override
    public void broken(CurioContext context) {
        var player = context.player();
        var target = Curios.randomInventoryItem(player, CasketOfInaccuracyItem::enchantable);
        if (target.isEmpty()) return;
        var level = context.chance(HIGH_LEVEL_CHANCE) ? Mth.nextInt(context.random(), 10, 30) : Mth.nextInt(context.random(), 1, 13);
        var preview = CurioEnchanting.everyPoolResult(player, target, level);
        EnchantReward.open(player, List.of(EnchantOption.of(target, preview, EnchantGrade.of(level), level, EnchantCost.free())));
    }

    private static boolean enchantable(ItemStack stack) {
        return stack.isEnchantable();
    }

    private static boolean workable(ItemStack stack) {
        return stack.getItem().isEnchantable(stack);
    }
}
