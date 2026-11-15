package net.per.primogemcraft.item.misc;

import net.per.primogemcraft.compat.component.StackComponents;
import net.minecraft.text.Text;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;
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

    public BlessingOfTheWelkinMoonItem(Settings properties) {
        super(properties);
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return net.minecraft.util.UseAction.BOW;
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        return USE_DURATION;
    }

    @Override
    public TypedActionResult<ItemStack> use(World level, PlayerEntity player, Hand hand) {
        player.setCurrentHand(hand);
        return TypedActionResult.consume(player.getStackInHand(hand));
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World level, LivingEntity entity) {
        var result = super.finishUsing(stack, level, entity);
        if (entity instanceof ServerPlayerEntity player) claim(player, stack);
        return result;
    }

    @Override
    public void appendTooltip(ItemStack stack, @org.jetbrains.annotations.Nullable World level, List<Text> tooltip, TooltipContext flag) {
        appendDescription(stack, tooltip);
        if (!WishTooltips.showsDetails()) return;
        tooltip.add(Text.translatable(REMAINING_KEY, remaining(stack)));
    }

    private int remaining(ItemStack stack) {
        return bar(stack).remaining();
    }

    private CustomBar bar(ItemStack stack) {
        return StackComponents.getOrDefault(stack, PGCDataComponents.CUSTOM_BAR, new CustomBar(0, CAPACITY, true));
    }

    private void claim(ServerPlayerEntity player, ItemStack stack) {
        if (player.getItemCooldownManager().isCoolingDown(this)) return;
        var fragments = MathHelper.nextInt(player.getRandom(), MIN_FRAGMENTS, MAX_FRAGMENTS);
        Curios.give(player, new ItemStack(PGCItems.PRIMOGEM_SHARD.get(), fragments));
        player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ITEM_TOTEM_USE, SoundCategory.PLAYERS, CLAIM_VOLUME, 1.0F);
        player.getItemCooldownManager().set(this, COOLDOWN_TICKS);
        if (remaining(stack) <= CLAIM_COST) {
            player.sendMessage(Text.translatable(EXPIRED_KEY), false);
            player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), PGCSounds.CURIO_BROKEN.get(), SoundCategory.PLAYERS, EXPIRED_VOLUME, EXPIRED_PITCH);
            stack.decrement(1);
            return;
        }
        StackComponents.set(stack, PGCDataComponents.CUSTOM_BAR, bar(stack).advancedBy(CLAIM_COST));
    }
}
