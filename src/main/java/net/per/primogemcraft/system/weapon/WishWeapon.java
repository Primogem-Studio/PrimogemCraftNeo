package net.per.primogemcraft.system.weapon;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public interface WishWeapon {
    TagKey<Item> FIVE_STAR = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MOD_ID, "weapon/five_star"));

    static boolean isFiveStar(ItemStack stack) {
        return stack.is(FIVE_STAR);
    }

    List<WeaponModifier> passives();

    List<WeaponDescription> descriptions(ItemStack stack);

    default WeaponStacks stacks(ItemStack stack) {
        return WeaponStacks.NONE;
    }

    default List<WeaponModifier> conditionalPassives(Player player, ItemStack stack, int slot, int refinement) {
        return List.of();
    }

    default boolean isHeld(Player player, int slot, ItemStack stack) {
        return slot != player.getInventory().selected && player.getOffhandItem() != stack;
    }

    default List<WeaponModifier> allPassives(Player player, ItemStack stack, int slot, int refinement) {
        var modifiers = new ArrayList<>(passives());
        modifiers.addAll(conditionalPassives(player, stack, slot, refinement));
        return modifiers;
    }
}
