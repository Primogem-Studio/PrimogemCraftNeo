package net.per.primogemcraft.item.misc;

import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.sound.SoundCategory;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Rarity;
import net.minecraft.world.World;
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

    public EnigmataGiftBoxItem(Settings properties) {
        super(properties.maxCount(1).rarity(Rarity.EPIC).maxDamage(COUNTDOWN));
    }

    @Override
    public TypedActionResult<ItemStack> use(World level, PlayerEntity player, Hand hand) {
        var stack = player.getStackInHand(hand);
        if (!(player instanceof ServerPlayerEntity serverPlayer)) return TypedActionResult.success(stack, true);
        open(serverPlayer, stack);
        serverPlayer.getItemCooldownManager().set(this, COOLDOWN_TICKS);
        serverPlayer.swingHand(hand);
        return TypedActionResult.success(stack, false);
    }

    @Override
    public void inventoryTick(ItemStack stack, World level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        if (level.isClient() || !(entity instanceof ServerPlayerEntity player)) return;
        var elapsed = stack.getDamage() + 1;
        if (elapsed < COUNTDOWN) {
            stack.setDamage(elapsed);
            return;
        }
        open(player, stack);
    }

    private void open(ServerPlayerEntity player, ItemStack stack) {
        var random = player.getRandom();
        if (random.nextDouble() < COMMON_CHANCE) {
            for (var count = MathHelper.nextInt(random, MIN_COMMON, MAX_COMMON); count > 0; count--) drop(player, pick(random, COMMON_POOL));
        } else {
            drop(player, pick(random, RARE_POOL));
        }
        stack.decrement(1);
        player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_GLOW_ITEM_FRAME_BREAK, SoundCategory.PLAYERS, VOLUME, PITCH);
    }

    private static void drop(ServerPlayerEntity player, ItemStack reward) {
        if (reward.isEmpty()) return;
        var dropped = new ItemEntity(player.getWorld(), player.getX(), player.getY() + DROP_HEIGHT, player.getZ(), reward);
        dropped.setPickupDelay(0);
        player.getWorld().spawnEntity(dropped);
    }

    private static ItemStack pick(net.minecraft.util.math.random.Random random, TagKey<Item> tag) {
        var candidates = new ArrayList<RegistryEntry<Item>>();
        for (var holder : Registries.ITEM.getOrCreateEntryList(tag)) candidates.add(holder);
        if (candidates.isEmpty()) return ItemStack.EMPTY;
        return new ItemStack(candidates.get(random.nextInt(candidates.size())));
    }

    private static TagKey<Item> tag(String name) {
        return TagKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, name));
    }
}
