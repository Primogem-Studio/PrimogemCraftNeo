package net.per.primogemcraft.item.misc;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.per.primogemcraft.item.tool.DescribedItem;
import net.per.primogemcraft.registry.PGCDataComponents;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.util.PlayerItems;

public class StackOfCosmicBigLottoItem extends DescribedItem {
    private static final int MIN_LOTTOS = 1;
    private static final int MAX_LOTTOS = 5;
    private static final float VOLUME = 0.5F;

    public StackOfCosmicBigLottoItem(Properties properties) {
        super(properties.stacksTo(1).rarity(Rarity.RARE));
    }

    public static ItemStack rolled(RandomSource random) {
        var stack = new ItemStack(PGCItems.STACK_OF_COSMIC_BIG_LOTTO.get());
        stack.set(PGCDataComponents.LOTTO_COUNT.get(), Mth.nextInt(random, MIN_LOTTOS, MAX_LOTTOS));
        return stack;
    }

    public static int lottos(ItemStack stack) {
        return stack.getOrDefault(PGCDataComponents.LOTTO_COUNT.get(), MAX_LOTTOS);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        if (!(player instanceof ServerPlayer server)) return InteractionResultHolder.success(stack);
        PlayerItems.give(server, new ItemStack(PGCItems.COSMIC_BIG_LOTTO.get()));
        var remaining = lottos(stack) - 1;
        if (remaining < MIN_LOTTOS) stack.shrink(1);
        else stack.set(PGCDataComponents.LOTTO_COUNT.get(), remaining);
        player.swing(hand, true);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.VILLAGER_WORK_LIBRARIAN, SoundSource.PLAYERS, VOLUME, 1.0F);
        return InteractionResultHolder.success(stack);
    }
}
