package net.per.primogemcraft.item.misc;

import net.per.primogemcraft.compat.component.StackComponents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Rarity;
import net.minecraft.world.World;
import net.per.primogemcraft.item.tool.DescribedItem;
import net.per.primogemcraft.registry.PGCDataComponents;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.util.PlayerItems;

public class StackOfCosmicBigLottoItem extends DescribedItem {
    private static final int MIN_LOTTOS = 1;
    private static final int MAX_LOTTOS = 5;
    private static final float VOLUME = 0.5F;

    public StackOfCosmicBigLottoItem(Settings properties) {
        super(properties.maxCount(1).rarity(Rarity.RARE));
    }

    public static ItemStack rolled(Random random) {
        var stack = new ItemStack(PGCItems.STACK_OF_COSMIC_BIG_LOTTO.get());
        StackComponents.set(stack, PGCDataComponents.LOTTO_COUNT, MathHelper.nextInt(random, MIN_LOTTOS, MAX_LOTTOS));
        return stack;
    }

    public static int lottos(ItemStack stack) {
        return StackComponents.getOrDefault(stack, PGCDataComponents.LOTTO_COUNT, MAX_LOTTOS);
    }

    @Override
    public TypedActionResult<ItemStack> use(World level, PlayerEntity player, Hand hand) {
        var stack = player.getStackInHand(hand);
        if (!(player instanceof ServerPlayerEntity server)) return TypedActionResult.success(stack);
        PlayerItems.give(server, new ItemStack(PGCItems.COSMIC_BIG_LOTTO.get()));
        var remaining = lottos(stack) - 1;
        if (remaining < MIN_LOTTOS) stack.decrement(1);
        else StackComponents.set(stack, PGCDataComponents.LOTTO_COUNT, remaining);
        player.swingHand(hand);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_VILLAGER_WORK_LIBRARIAN, SoundCategory.PLAYERS, VOLUME, 1.0F);
        return TypedActionResult.success(stack);
    }
}
