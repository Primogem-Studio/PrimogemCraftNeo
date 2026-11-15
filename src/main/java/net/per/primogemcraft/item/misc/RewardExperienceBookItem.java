package net.per.primogemcraft.item.misc;

import net.per.primogemcraft.compat.component.StackComponents;
import net.minecraft.text.Text;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
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

    public RewardExperienceBookItem(Settings properties, int highMin, int highMax, int lowMin, int lowMax) {
        super(properties);
        this.highMin = highMin;
        this.highMax = highMax;
        this.lowMin = lowMin;
        this.lowMax = lowMax;
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        // 未学习过的经验书显示附魔光效；客户端由调用方保证
        return learners(stack).isEmpty();
    }

    @Override
    public TypedActionResult<ItemStack> use(World level, PlayerEntity player, Hand hand) {
        var stack = player.getStackInHand(hand);
        if (!(player instanceof ServerPlayerEntity serverPlayer)) return TypedActionResult.success(stack, true);
        if (isLearned(stack, player.getUuid())) {
            serverPlayer.sendMessage(Text.translatable("message.primogemcraft.reward_book.claimed"), true);
            return TypedActionResult.success(stack, false);
        }
        player.swingHand(hand);
        var random = player.getRandom();
        player.addExperience(random.nextDouble() <= 0.5D ? MathHelper.nextInt(random, highMin, highMax) : MathHelper.nextInt(random, lowMin, lowMax));
        level.playSound(null, player.getX(), player.getY(), player.getZ(), PGCSounds.EXPERIENCE_BOOK.get(), SoundCategory.NEUTRAL, 0.5F, 1.0F);
        learn(stack, player.getUuid());
        return TypedActionResult.success(stack, false);
    }

    public static boolean isLearned(ItemStack stack, UUID playerId) {
        return learners(stack).contains(playerId);
    }

    private static List<UUID> learners(ItemStack stack) {
        return StackComponents.getOrDefault(stack, PGCDataComponents.REWARD_BOOK_LEARNERS, List.of());
    }

    private static void learn(ItemStack stack, UUID playerId) {
        var learners = new ArrayList<>(learners(stack));
        learners.add(playerId);
        StackComponents.set(stack, PGCDataComponents.REWARD_BOOK_LEARNERS, List.copyOf(learners));
    }
}
