package net.per.primogemcraft.item.misc;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.per.primogemcraft.item.tool.DescribedItem;

import java.util.ArrayList;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public class EnigmataGiftBoxItem extends DescribedItem {
    private static final TagKey<Item> COMMON_POOL = tag("gift_box_common");
    private static final TagKey<Item> RARE_POOL = tag("gift_box_rare");

    private static final int COUNTDOWN = 1220;
    private static final double COMMON_CHANCE = 0.5D;
    private static final int MIN_COMMON = 1;
    private static final int MAX_COMMON = 3;
    private static final float VOLUME = 0.5F;
    private static final float PITCH = 1.5F;
    private static final double DROP_HEIGHT = 0.8D;
    private static final int COOLDOWN_TICKS = 20;

    public EnigmataGiftBoxItem(Properties properties) {
        super(properties.stacksTo(1).rarity(Rarity.EPIC).durability(COUNTDOWN));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResultHolder.sidedSuccess(stack, true);
        open(serverPlayer, stack);
        serverPlayer.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
        serverPlayer.swing(hand, true);
        return InteractionResultHolder.sidedSuccess(stack, false);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        if (level.isClientSide() || !(entity instanceof ServerPlayer player)) return;
        var elapsed = stack.getDamageValue() + 1;
        if (elapsed < COUNTDOWN) {
            stack.setDamageValue(elapsed);
            return;
        }
        open(player, stack);
    }

    private void open(ServerPlayer player, ItemStack stack) {
        var random = player.getRandom();
        if (random.nextDouble() < COMMON_CHANCE) {
            for (var count = Mth.nextInt(random, MIN_COMMON, MAX_COMMON); count > 0; count--) drop(player, pick(random, COMMON_POOL));
        } else {
            drop(player, pick(random, RARE_POOL));
        }
        stack.shrink(1);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GLOW_ITEM_FRAME_BREAK, SoundSource.PLAYERS, VOLUME, PITCH);
    }

    private static void drop(ServerPlayer player, ItemStack reward) {
        if (reward.isEmpty()) return;
        var dropped = new ItemEntity(player.level(), player.getX(), player.getY() + DROP_HEIGHT, player.getZ(), reward);
        dropped.setPickUpDelay(0);
        player.level().addFreshEntity(dropped);
    }

    private static ItemStack pick(net.minecraft.util.RandomSource random, TagKey<Item> tag) {
        var candidates = new ArrayList<Holder<Item>>();
        for (var holder : BuiltInRegistries.ITEM.getTagOrEmpty(tag)) candidates.add(holder);
        if (candidates.isEmpty()) return ItemStack.EMPTY;
        return new ItemStack(candidates.get(random.nextInt(candidates.size())).value());
    }

    private static TagKey<Item> tag(String name) {
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MOD_ID, name));
    }
}
