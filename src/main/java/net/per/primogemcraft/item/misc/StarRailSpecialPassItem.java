package net.per.primogemcraft.item.misc;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.per.primogemcraft.entity.misc.WishEntity;
import net.per.primogemcraft.registry.PGCSounds;
import net.per.primogemcraft.system.wish.WishBanner;
import net.per.primogemcraft.system.wish.WishRoller;

import java.util.List;

public class StarRailSpecialPassItem extends Item {
    public StarRailSpecialPassItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        if (!(level instanceof ServerLevel serverLevel) || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }

        var pulls = player.isShiftKeyDown() ? stack.getCount() : 1;
        stack.shrink(pulls);

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                pulls > 1 ? PGCSounds.WISH_TEN.get() : PGCSounds.WISH_ROLL.get(), SoundSource.PLAYERS, 1.0F, 1.0F);

        WishEntity.spawnRing(serverLevel, serverPlayer,
                WishRoller.rollGuaranteedGold(serverPlayer, WishBanner.INTERTWINED, pulls));
        return InteractionResultHolder.sidedSuccess(stack, false);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.primogemcraft.star_rail_special_pass.tooltip.0"));
        tooltip.add(Component.translatable("item.primogemcraft.star_rail_special_pass.tooltip.1"));
        tooltip.add(Component.translatable("item.primogemcraft.star_rail_special_pass.tooltip.2"));
    }
}
