package net.per.primogemcraft.item.misc;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.per.primogemcraft.item.tool.DescribedItem;

public class DreamSakuraItem extends DescribedItem {
    private static final String BROKEN_KEY = "message.primogemcraft.dream_sakura.broken";
    private static final int CAPACITY = 648;
    private static final int WEAR_AMOUNT = 647;
    private static final double BREAK_CHANCE = 0.01D;
    private static final float VOLUME = 1.0F;
    private static final float PITCH = 1.0F;

    public DreamSakuraItem(Properties properties) {
        super(properties.durability(CAPACITY).fireResistant().rarity(Rarity.EPIC));
    }

    public void onSaved(ServerPlayer player, ItemStack stack) {
        if (player.getRandom().nextDouble() < BREAK_CHANCE) {
            stack.hurtAndBreak(WEAR_AMOUNT, player.serverLevel(), player, broken -> {
            });
            player.displayClientMessage(Component.translatable(BROKEN_KEY), false);
        }
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.TOTEM_USE, SoundSource.PLAYERS, VOLUME, PITCH);
    }
}
