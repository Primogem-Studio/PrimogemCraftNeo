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

import java.util.List;

public class WishingStaffItem extends Item {
    private static final double RESOLVE_RADIUS = 16.0D;

    public WishingStaffItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        if (!(level instanceof ServerLevel sl))
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        var wishes = sl.getEntitiesOfClass(WishEntity.class, player.getBoundingBox().inflate(RESOLVE_RADIUS), wish -> wish.isOwnedBy(player) && !wish.isCapturingRadiance());

        WishEntity loudest = null;
        for (var wish : wishes) {
            if (!wish.resolve(sl, false)) continue;
            if (loudest == null || wish.rarity().ordinal() > loudest.rarity().ordinal()) loudest = wish;
        }

        if (loudest == null) {
            if (player instanceof ServerPlayer sp)
                sp.displayClientMessage(Component.translatable("message.primogemcraft.wish.nothing_to_resolve"), true);
        } else {
            sl.playSound(null, player.getX(), player.getY(), player.getZ(), loudest.rarity().sound(), SoundSource.NEUTRAL, 1.0F, 1.0F);
        }
        return InteractionResultHolder.sidedSuccess(stack, false);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.primogemcraft.wishing_staff.tooltip.0"));
    }
}
