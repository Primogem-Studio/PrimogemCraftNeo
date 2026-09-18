package net.per.primogemcraft.item.curio;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.per.primogemcraft.enchantment.EnchantCost;
import net.per.primogemcraft.enchantment.EnchantGrade;
import net.per.primogemcraft.enchantment.EnchantOption;
import net.per.primogemcraft.enchantment.EnchantReward;
import net.per.primogemcraft.enchantment.PGCEnchantments;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.system.curio.CurioContext;
import net.per.primogemcraft.system.curio.CurioEnchanting;
import net.per.primogemcraft.system.curio.CurioForm;
import net.per.primogemcraft.system.curio.CurioItem;
import net.per.primogemcraft.system.curio.CurioTrigger;

import java.util.List;

public class FoolsGelItem extends CurioItem {
    private static final String INVALID_KEY = "message.primogemcraft.curio.enchanting.invalid";
    private static final String HINT_TIMER = "curio/fools_gel_hint";
    private static final int HINT_TICKS = 600;
    private static final int CURSE_LEVEL = 5;

    public FoolsGelItem(Properties properties) {
        super(CurioTrigger.ACTIVE, CurioForm.FUSION, 1, () -> new ItemStack(PGCItems.UNIDENTIFIED_DOLL.get()), properties);
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
        var offhand = player.getOffhandItem();
        if (!offhand.isEnchantable()) {
            if (context.ready(HINT_TIMER, HINT_TICKS)) context.announce(Component.translatable(INVALID_KEY));
            return;
        }
        var enchantment = CurioEnchanting.randomEnchantment(player);
        var preview = CurioEnchanting.withEnchantment(offhand, enchantment);
        PGCEnchantments.apply(player.level(), preview, PGCEnchantments.FOOLS_WRATH, CURSE_LEVEL);
        context.damage(1);
        EnchantReward.open(player, List.of(EnchantOption.of(offhand, preview, EnchantGrade.of(enchantment.level), enchantment.level, EnchantCost.free())));
    }
}
