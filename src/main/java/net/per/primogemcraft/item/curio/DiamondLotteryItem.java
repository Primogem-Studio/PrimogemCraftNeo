package net.per.primogemcraft.item.curio;

import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.per.primogemcraft.system.curio.CurioForm;
import net.per.primogemcraft.system.curio.LotteryCurioItem;
import net.per.primogemcraft.util.Advancements;

public class DiamondLotteryItem extends LotteryCurioItem {
    private static final int MIN_DIAMONDS = 1;
    private static final int MAX_DIAMONDS = 10;
    private static final float FOOD_RETAINED = 0.02F;

    public DiamondLotteryItem(Properties properties) {
        super(CurioForm.FUSION, 0.5D, context -> {
            context.give(new ItemStack(Items.DIAMOND, Mth.nextInt(context.random(), MIN_DIAMONDS, MAX_DIAMONDS)));
            context.announce(Component.translatable("message.primogemcraft.curio.lottery.diamonds_gained"));
            Advancements.grant(context.player(), "your_pity_is_gone");
        }, context -> {
            context.player().setHealth(1.0F);
            var data = context.player().getFoodData();
            data.setFoodLevel((int) (data.getFoodLevel() * FOOD_RETAINED));
        }, properties);
    }
}
