package net.per.primogemcraft.item.misc;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.per.primogemcraft.entity.misc.WishEntity;
import net.per.primogemcraft.registry.PGCSounds;
import net.per.primogemcraft.system.wish.*;

import java.util.List;

public class WishFateItem extends Item {
    private static final int USE_DURATION = 32;

    private final WishBanner banner;

    public WishFateItem(Properties properties, WishBanner banner) {
        super(properties);
        this.banner = banner;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return USE_DURATION;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!(level instanceof ServerLevel serverLevel) || !(entity instanceof ServerPlayer serverPlayer)) {
            return stack;
        }

        var pulls = serverPlayer.isShiftKeyDown() ? stack.getCount() : 1;
        var wishValue = WishValue.get(stack);
        stack.shrink(pulls);

        level.playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(), pulls > 1 ? PGCSounds.WISH_TEN.get() : PGCSounds.WISH_ROLL.get(), SoundSource.PLAYERS, 1.0F, 1.0F);

        WishEntity.spawnRing(serverLevel, serverPlayer, WishRoller.roll(serverPlayer, banner, wishValue, pulls));
        return stack;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(tooltipKey(0)));
        tooltip.add(Component.translatable(tooltipKey(1)));
        if (!banner.countsPity()) {
            tooltip.add(Component.translatable(tooltipKey(2)));
            return;
        }

        var wishValue = WishValue.get(stack);
        if (WishTooltips.showsDetails()) {
            tooltip.add(Component.translatable(tooltipKey(3), WishReports.percent(WishRoller.purpleChance(banner, wishValue), ChatFormatting.LIGHT_PURPLE)));
            tooltip.add(Component.translatable(tooltipKey(4), WishReports.percent(WishRoller.goldChance(banner, wishValue), ChatFormatting.GOLD)));
            return;
        }
        tooltip.add(Component.translatable(tooltipKey(2)));
    }

    private String tooltipKey(int index) {
        return "item.primogemcraft." + banner.id() + "_fate.tooltip." + index;
    }
}
