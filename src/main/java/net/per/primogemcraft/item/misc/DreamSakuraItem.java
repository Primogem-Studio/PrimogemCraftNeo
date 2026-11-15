package net.per.primogemcraft.item.misc;

import net.minecraft.text.Text;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.sound.SoundCategory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Rarity;
import net.per.primogemcraft.item.tool.DescribedItem;

public class DreamSakuraItem extends DescribedItem {
    private static final String BROKEN_KEY = "message.primogemcraft.dream_sakura.broken";
    private static final int CAPACITY = 648;
    private static final int WEAR_AMOUNT = 647;
    private static final double BREAK_CHANCE = 0.01D;
    private static final float VOLUME = 1.0F;
    private static final float PITCH = 1.0F;

    public DreamSakuraItem(Settings properties) {
        super(properties.maxDamage(CAPACITY).fireproof().rarity(Rarity.EPIC));
    }

    public void onSaved(ServerPlayerEntity player, ItemStack stack) {
        if (player.getRandom().nextDouble() < BREAK_CHANCE) {
            stack.damage(WEAR_AMOUNT, player, broken -> {
            });
            player.sendMessage(Text.translatable(BROKEN_KEY), false);
        }
        player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ITEM_TOTEM_USE, SoundCategory.PLAYERS, VOLUME, PITCH);
    }
}
