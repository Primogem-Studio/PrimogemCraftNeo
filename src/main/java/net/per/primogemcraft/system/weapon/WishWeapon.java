package net.per.primogemcraft.system.weapon;

import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public interface WishWeapon {
    TagKey<Item> FIVE_STAR = TagKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, "weapon/five_star"));

    static boolean isFiveStar(ItemStack stack) {
        return stack.isIn(FIVE_STAR);
    }

    List<WeaponModifier> passives();

    List<WeaponDescription> descriptions(ItemStack stack);

    default WeaponStacks stacks(ItemStack stack) {
        return WeaponStacks.NONE;
    }

    default List<WeaponModifier> conditionalPassives(PlayerEntity player, ItemStack stack, int slot, int refinement) {
        return List.of();
    }

    default boolean isHeld(PlayerEntity player, int slot, ItemStack stack) {
        return slot != player.getInventory().selectedSlot && player.getOffHandStack() != stack;
    }

    default List<WeaponModifier> allPassives(PlayerEntity player, ItemStack stack, int slot, int refinement) {
        var modifiers = new ArrayList<>(passives());
        modifiers.addAll(conditionalPassives(player, stack, slot, refinement));
        return modifiers;
    }
}
