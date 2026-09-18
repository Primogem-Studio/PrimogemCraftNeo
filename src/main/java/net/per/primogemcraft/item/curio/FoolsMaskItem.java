package net.per.primogemcraft.item.curio;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.per.primogemcraft.enchantment.PGCEnchantments;
import net.per.primogemcraft.system.curio.*;

public class FoolsMaskItem extends CurioItem {
    private static final String INVALID_KEY = "message.primogemcraft.curio.enchanting.invalid";
    private static final int TABLE_LEVEL = 30;
    private static final int EFFECTS = 4;

    public FoolsMaskItem(Properties properties) {
        super(CurioTrigger.RIGHT_CLICK, CurioForm.NORMAL, 1, properties);
    }

    @Override
    public void activated(CurioContext context) {
        var player = context.player();
        var offhand = player.getOffhandItem();
        if (!offhand.isEnchanted()) {
            context.announce(Component.translatable(INVALID_KEY));
            return;
        }
        switch (Mth.nextInt(player.getRandom(), 0, EFFECTS - 1)) {
            case 0 -> CurioEnchanting.tablePool(player, offhand, TABLE_LEVEL);
            case 1 -> context.give(enchantedBook(offhand));
            case 2 -> PGCEnchantments.apply(player.level(), offhand, PGCEnchantments.FOOLS_WRATH,
                    PGCEnchantments.levelOf(player.level(), offhand, PGCEnchantments.FOOLS_WRATH) + 1);
            default -> {
                if (offhand.isDamageableItem()) offhand.setDamageValue(offhand.getMaxDamage() - 1);
            }
        }
        context.damage(1);
    }

    private static ItemStack enchantedBook(ItemStack source) {
        var book = new ItemStack(Items.ENCHANTED_BOOK);
        book.set(DataComponents.STORED_ENCHANTMENTS, EnchantmentHelper.getEnchantmentsForCrafting(source));
        return book;
    }
}
