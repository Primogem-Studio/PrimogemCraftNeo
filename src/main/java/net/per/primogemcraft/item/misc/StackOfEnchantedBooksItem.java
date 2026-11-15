package net.per.primogemcraft.item.misc;

import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.SoundEvents;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Rarity;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.world.World;
import net.per.primogemcraft.item.tool.DescribedItem;

public class StackOfEnchantedBooksItem extends DescribedItem {
    private static final int MIN_BOOKS = 1;
    private static final int MAX_BOOKS = 5;
    private static final int MIN_LEVEL = 9;
    private static final int MAX_LEVEL = 45;
    private static final int PICKUP_DELAY = 10;
    private static final float VOLUME = 0.5F;

    public StackOfEnchantedBooksItem(Settings properties) {
        super(properties.rarity(Rarity.UNCOMMON));
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return true;
    }

    @Override
    public TypedActionResult<ItemStack> use(World level, PlayerEntity player, Hand hand) {
        var stack = player.getStackInHand(hand);
        if (level.isClient()) return TypedActionResult.success(stack, true);
        var random = player.getRandom();
        for (var index = MathHelper.nextInt(random, MIN_BOOKS, MAX_BOOKS); index > 0; index--) {
            var book = EnchantmentHelper.enchant(random, new ItemStack(Items.BOOK), MathHelper.nextInt(random, MIN_LEVEL, MAX_LEVEL), false);
            var entity = new ItemEntity(level, player.getX(), player.getY(), player.getZ(), book);
            entity.setPickupDelay(PICKUP_DELAY);
            level.spawnEntity(entity);
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_VILLAGER_WORK_LIBRARIAN, SoundCategory.PLAYERS, VOLUME, 1.0F);
        stack.decrement(1);
        player.swingHand(hand);
        return TypedActionResult.success(stack, false);
    }
}
