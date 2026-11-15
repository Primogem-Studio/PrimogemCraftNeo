package net.per.primogemcraft.item.misc;

import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.per.primogemcraft.enchantment.PGCEnchantments;
import net.per.primogemcraft.item.tool.DescribedItem;
import net.per.primogemcraft.util.PlayerItems;

public class CosmicFragmentItem extends DescribedItem {
    private static final TagKey<Item> CURIOS = TagKey.of(RegistryKeys.ITEM, Identifier.of("c", "curio"));
    private static final double REPAIR_RATIO_PER_LEVEL = 0.02D;
    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };

    public CosmicFragmentItem(Settings properties) {
        super(properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, World level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        if (!(entity instanceof ServerPlayerEntity player)) return;
        repair(player, player.getMainHandStack());
        repair(player, player.getOffHandStack());
        for (var armor : ARMOR_SLOTS) repair(player, player.getEquippedStack(armor));
    }

    private void repair(ServerPlayerEntity player, ItemStack target) {
        if (target.isEmpty() || !target.isDamageable() || target.isIn(CURIOS)) return;
        var enchantmentLevel = PGCEnchantments.levelOf(player.getWorld(), target, PGCEnchantments.COSMIC_FRAGMENT_MENDING);
        if (enchantmentLevel <= 0) return;
        var amount = Math.max(1, (int) (target.getMaxDamage() * REPAIR_RATIO_PER_LEVEL * enchantmentLevel));
        if (target.getDamage() <= amount) return;
        if (PlayerItems.count(player, this) <= 0) return;
        PlayerItems.take(player, this, 1);
        target.setDamage(target.getDamage() - amount);
    }
}
