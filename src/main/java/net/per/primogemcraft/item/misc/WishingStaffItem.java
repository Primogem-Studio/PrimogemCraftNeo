package net.per.primogemcraft.item.misc;

import net.minecraft.text.Text;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.world.World;
import net.per.primogemcraft.entity.misc.WishEntity;

import java.util.List;

public class WishingStaffItem extends Item {
    private static final double RESOLVE_RADIUS = 16.0D;

    public WishingStaffItem(Settings properties) {
        super(properties);
    }

    @Override
    public TypedActionResult<ItemStack> use(World level, PlayerEntity player, Hand hand) {
        var stack = player.getStackInHand(hand);
        if (!(level instanceof ServerWorld sl))
            return TypedActionResult.success(stack, level.isClient());
        var wishes = sl.getEntitiesByClass(WishEntity.class, player.getBoundingBox().expand(RESOLVE_RADIUS), wish -> wish.isOwnedBy(player) && !wish.isCapturingRadiance());

        WishEntity loudest = null;
        for (var wish : wishes) {
            if (!wish.resolve(sl, false)) continue;
            if (loudest == null || wish.rarity().ordinal() > loudest.rarity().ordinal()) loudest = wish;
        }

        if (loudest == null) {
            if (player instanceof ServerPlayerEntity sp)
                sp.sendMessage(Text.translatable("message.primogemcraft.wish.nothing_to_resolve"), true);
        } else {
            sl.playSound(null, player.getX(), player.getY(), player.getZ(), loudest.rarity().sound(), SoundCategory.NEUTRAL, 1.0F, 1.0F);
        }
        return TypedActionResult.success(stack, false);
    }

    @Override
    public void appendTooltip(ItemStack stack, @org.jetbrains.annotations.Nullable World level, List<Text> tooltip, TooltipContext flag) {
        tooltip.add(Text.translatable("item.primogemcraft.wishing_staff.tooltip.0"));
    }
}
