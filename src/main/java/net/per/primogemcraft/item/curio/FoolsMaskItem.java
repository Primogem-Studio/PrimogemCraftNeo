package net.per.primogemcraft.item.curio;
import net.per.primogemcraft.compat.component.VanillaComponents;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.enchantment.EnchantmentHelper;
import net.per.primogemcraft.enchantment.PGCEnchantments;
import net.per.primogemcraft.system.curio.*;

public class FoolsMaskItem extends CurioItem {
    private static final String INVALID_KEY = "message.primogemcraft.curio.enchanting.invalid";
    private static final int TABLE_LEVEL = 30;
    private static final int EFFECTS = 4;

    public FoolsMaskItem(Settings properties) {
        super(CurioTrigger.RIGHT_CLICK, CurioForm.NORMAL, 1, properties);
    }

    @Override
    public void activated(CurioContext context) {
        var player = context.player();
        var offhand = player.getOffHandStack();
        if (!offhand.hasEnchantments()) {
            context.announce(Text.translatable(INVALID_KEY));
            return;
        }
        switch (MathHelper.nextInt(player.getRandom(), 0, EFFECTS - 1)) {
            case 0 -> CurioEnchanting.tablePool(player, offhand, TABLE_LEVEL);
            case 1 -> context.give(enchantedBook(offhand));
            case 2 -> PGCEnchantments.apply(player.getWorld(), offhand, PGCEnchantments.FOOLS_WRATH,
                    PGCEnchantments.levelOf(player.getWorld(), offhand, PGCEnchantments.FOOLS_WRATH) + 1);
            default -> {
                if (offhand.isDamageable()) offhand.setDamage(offhand.getMaxDamage() - 1);
            }
        }
        context.damage(1);
    }

    private static ItemStack enchantedBook(ItemStack source) {
        var book = new ItemStack(Items.ENCHANTED_BOOK);
        VanillaComponents.setStoredEnchantments(book, source);
        return book;
    }
}
