package net.per.primogemcraft.item.misc;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.per.primogemcraft.client.RewardBooks;
import net.per.primogemcraft.item.tool.DescribedItem;
import net.per.primogemcraft.registry.PGCDataComponents;
import net.per.primogemcraft.registry.PGCSounds;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RewardExperienceBookItem extends DescribedItem {
    private final int highMin;
    private final int highMax;
    private final int lowMin;
    private final int lowMax;

    public RewardExperienceBookItem(Properties properties, int highMin, int highMax, int lowMin, int lowMax) {
        super(properties);
        this.highMin = highMin;
        this.highMax = highMax;
        this.lowMin = lowMin;
        this.lowMax = lowMax;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        if (FMLEnvironment.dist != Dist.CLIENT) return false;
        return RewardBooks.isUnlearned(stack);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResultHolder.sidedSuccess(stack, true);
        if (isLearned(stack, player.getUUID())) {
            serverPlayer.displayClientMessage(Component.translatable("message.primogemcraft.reward_book.claimed"), true);
            return InteractionResultHolder.sidedSuccess(stack, false);
        }
        player.swing(hand, true);
        var random = player.getRandom();
        player.giveExperiencePoints(random.nextDouble() <= 0.5D ? Mth.nextInt(random, highMin, highMax) : Mth.nextInt(random, lowMin, lowMax));
        level.playSound(null, player.getX(), player.getY(), player.getZ(), PGCSounds.EXPERIENCE_BOOK.get(), SoundSource.NEUTRAL, 0.5F, 1.0F);
        learn(stack, player.getUUID());
        return InteractionResultHolder.sidedSuccess(stack, false);
    }

    public static boolean isLearned(ItemStack stack, UUID playerId) {
        return learners(stack).contains(playerId);
    }

    private static List<UUID> learners(ItemStack stack) {
        return stack.getOrDefault(PGCDataComponents.REWARD_BOOK_LEARNERS.get(), List.of());
    }

    private static void learn(ItemStack stack, UUID playerId) {
        var learners = new ArrayList<>(learners(stack));
        learners.add(playerId);
        stack.set(PGCDataComponents.REWARD_BOOK_LEARNERS.get(), List.copyOf(learners));
    }
}
