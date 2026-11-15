package net.per.primogemcraft.item.misc;

import net.minecraft.util.Formatting;
import net.minecraft.text.Text;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;
import net.per.primogemcraft.entity.misc.WishEntity;
import net.per.primogemcraft.registry.PGCSounds;
import net.per.primogemcraft.system.wish.*;

import java.util.List;

public class WishFateItem extends Item {
    private static final int USE_DURATION = 32;

    private final WishBanner banner;

    public WishFateItem(Settings properties, WishBanner banner) {
        super(properties);
        this.banner = banner;
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        return USE_DURATION;
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return net.minecraft.util.UseAction.BOW;
    }

    @Override
    public TypedActionResult<ItemStack> use(World level, PlayerEntity player, Hand hand) {
        player.setCurrentHand(hand);
        return TypedActionResult.consume(player.getStackInHand(hand));
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World level, LivingEntity entity) {
        if (!(level instanceof ServerWorld serverLevel) || !(entity instanceof ServerPlayerEntity serverPlayer)) {
            return stack;
        }

        var pulls = serverPlayer.isSneaking() ? stack.getCount() : 1;
        var wishValue = WishValue.get(stack);
        stack.decrement(pulls);

        level.playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(), pulls > 1 ? PGCSounds.WISH_TEN.get() : PGCSounds.WISH_ROLL.get(), SoundCategory.PLAYERS, 1.0F, 1.0F);

        WishEntity.spawnRing(serverLevel, serverPlayer, WishRoller.roll(serverPlayer, banner, wishValue, pulls));
        return stack;
    }

    @Override
    public void appendTooltip(ItemStack stack, @org.jetbrains.annotations.Nullable World level, List<Text> tooltip, TooltipContext flag) {
        tooltip.add(Text.translatable(tooltipKey(0)));
        tooltip.add(Text.translatable(tooltipKey(1)));
        if (!banner.countsPity()) {
            tooltip.add(Text.translatable(tooltipKey(2)));
            return;
        }

        var wishValue = WishValue.get(stack);
        if (WishTooltips.showsDetails()) {
            tooltip.add(Text.translatable(tooltipKey(3), WishReports.percent(WishRoller.purpleChance(banner, wishValue), Formatting.LIGHT_PURPLE)));
            tooltip.add(Text.translatable(tooltipKey(4), WishReports.percent(WishRoller.goldChance(banner, wishValue), Formatting.GOLD)));
            return;
        }
        tooltip.add(Text.translatable(tooltipKey(2)));
    }

    private String tooltipKey(int index) {
        return "item.primogemcraft." + banner.id() + "_fate.tooltip." + index;
    }
}
