package net.per.primogemcraft.item.misc;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.per.primogemcraft.item.tool.DescribedItem;

public class StackOfEnchantedBooksItem extends DescribedItem {
    private static final int MIN_BOOKS = 1;
    private static final int MAX_BOOKS = 5;
    private static final int MIN_LEVEL = 9;
    private static final int MAX_LEVEL = 45;
    private static final int PICKUP_DELAY = 10;
    private static final float VOLUME = 0.5F;

    public StackOfEnchantedBooksItem(Properties properties) {
        super(properties.rarity(Rarity.UNCOMMON));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        if (level.isClientSide()) return InteractionResultHolder.sidedSuccess(stack, true);
        var random = player.getRandom();
        var candidates = level.registryAccess().registryOrThrow(Registries.ENCHANTMENT).holders()
                .map(reference -> (Holder<Enchantment>) reference).toList();
        for (var index = Mth.nextInt(random, MIN_BOOKS, MAX_BOOKS); index > 0; index--) {
            var book = EnchantmentHelper.enchantItem(random, new ItemStack(Items.BOOK), Mth.nextInt(random, MIN_LEVEL, MAX_LEVEL), candidates.stream());
            var entity = new ItemEntity(level, player.getX(), player.getY(), player.getZ(), book);
            entity.setPickUpDelay(PICKUP_DELAY);
            level.addFreshEntity(entity);
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.VILLAGER_WORK_LIBRARIAN, SoundSource.PLAYERS, VOLUME, 1.0F);
        stack.shrink(1);
        player.swing(hand, true);
        return InteractionResultHolder.sidedSuccess(stack, false);
    }
}
