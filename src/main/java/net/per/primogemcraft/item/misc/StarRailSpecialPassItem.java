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
import net.per.primogemcraft.registry.PGCSounds;
import net.per.primogemcraft.system.wish.WishBanner;
import net.per.primogemcraft.system.wish.WishRoller;

import java.util.List;

public class StarRailSpecialPassItem extends Item {
    public StarRailSpecialPassItem(Settings properties) {
        super(properties);
    }

    @Override
    public TypedActionResult<ItemStack> use(World level, PlayerEntity player, Hand hand) {
        var stack = player.getStackInHand(hand);
        if (!(level instanceof ServerWorld serverLevel) || !(player instanceof ServerPlayerEntity serverPlayer)) {
            return TypedActionResult.success(stack, level.isClient());
        }

        var pulls = player.isSneaking() ? stack.getCount() : 1;
        stack.decrement(pulls);

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                pulls > 1 ? PGCSounds.WISH_TEN.get() : PGCSounds.WISH_ROLL.get(), SoundCategory.PLAYERS, 1.0F, 1.0F);

        WishEntity.spawnRing(serverLevel, serverPlayer,
                WishRoller.rollGuaranteedGold(serverPlayer, WishBanner.INTERTWINED, pulls));
        return TypedActionResult.success(stack, false);
    }

    @Override
    public void appendTooltip(ItemStack stack, @org.jetbrains.annotations.Nullable World level, List<Text> tooltip, TooltipContext flag) {
        tooltip.add(Text.translatable("item.primogemcraft.star_rail_special_pass.tooltip.0"));
        tooltip.add(Text.translatable("item.primogemcraft.star_rail_special_pass.tooltip.1"));
        tooltip.add(Text.translatable("item.primogemcraft.star_rail_special_pass.tooltip.2"));
    }
}
