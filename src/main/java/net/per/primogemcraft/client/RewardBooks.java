package net.per.primogemcraft.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.per.primogemcraft.item.misc.RewardExperienceBookItem;

public final class RewardBooks {
    private RewardBooks() {
    }

    public static boolean isUnlearned(ItemStack stack) {
        var player = Minecraft.getInstance().player;
        return player == null || !RewardExperienceBookItem.isLearned(stack, player.getUUID());
    }
}
