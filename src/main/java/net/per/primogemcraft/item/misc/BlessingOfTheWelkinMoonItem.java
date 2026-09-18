package net.per.primogemcraft.item.misc;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.per.primogemcraft.component.CustomBar;
import net.per.primogemcraft.item.tool.DescribedItem;
import net.per.primogemcraft.registry.PGCDataComponents;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.registry.PGCSounds;
import net.per.primogemcraft.system.curio.Curios;
import net.per.primogemcraft.system.wish.WishTooltips;

import java.util.List;

public class BlessingOfTheWelkinMoonItem extends DescribedItem {
    public static final int CAPACITY = 30;

    private static final int USE_DURATION = 16;
    private static final int COOLDOWN_TICKS = 24000;
    private static final int MIN_FRAGMENTS = 10;
    private static final int MAX_FRAGMENTS = 90;
    private static final int CLAIM_COST = 1;
    private static final float CLAIM_VOLUME = 0.3F;
    private static final float EXPIRED_VOLUME = 0.3F;
    private static final float EXPIRED_PITCH = 0.5F;
    private static final String REMAINING_KEY = "item.primogemcraft.blessing_of_the_welkin_moon.remaining";
    private static final String EXPIRED_KEY = "message.primogemcraft.blessing_of_the_welkin_moon.expired";

    public BlessingOfTheWelkinMoonItem(Properties properties) {
        super(properties);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return USE_DURATION;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        var result = super.finishUsingItem(stack, level, entity);
        if (entity instanceof ServerPlayer player) claim(player, stack);
        return result;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        appendDescription(stack, tooltip);
        if (!WishTooltips.showsDetails()) return;
        tooltip.add(Component.translatable(REMAINING_KEY, remaining(stack)));
    }

    private int remaining(ItemStack stack) {
        return bar(stack).remaining();
    }

    private CustomBar bar(ItemStack stack) {
        return stack.getOrDefault(PGCDataComponents.CUSTOM_BAR.get(), new CustomBar(0, CAPACITY, true));
    }

    private void claim(ServerPlayer player, ItemStack stack) {
        if (player.getCooldowns().isOnCooldown(this)) return;
        var fragments = Mth.nextInt(player.getRandom(), MIN_FRAGMENTS, MAX_FRAGMENTS);
        Curios.give(player, new ItemStack(PGCItems.PRIMOGEM_SHARD.get(), fragments));
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, CLAIM_VOLUME, 1.0F);
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
        if (remaining(stack) <= CLAIM_COST) {
            player.displayClientMessage(Component.translatable(EXPIRED_KEY), false);
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(), PGCSounds.CURIO_BROKEN.get(), SoundSource.PLAYERS, EXPIRED_VOLUME, EXPIRED_PITCH);
            stack.shrink(1);
            return;
        }
        stack.set(PGCDataComponents.CUSTOM_BAR.get(), bar(stack).advancedBy(CLAIM_COST));
    }
}
