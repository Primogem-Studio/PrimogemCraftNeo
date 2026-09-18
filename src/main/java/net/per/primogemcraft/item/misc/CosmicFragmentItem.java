package net.per.primogemcraft.item.misc;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.per.primogemcraft.enchantment.PGCEnchantments;
import net.per.primogemcraft.item.tool.DescribedItem;
import net.per.primogemcraft.util.PlayerItems;

public class CosmicFragmentItem extends DescribedItem {
    private static final TagKey<Item> CURIOS = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "curio"));
    private static final double REPAIR_RATIO_PER_LEVEL = 0.02D;
    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };

    public CosmicFragmentItem(Properties properties) {
        super(properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        if (!(entity instanceof ServerPlayer player)) return;
        repair(player, player.getMainHandItem());
        repair(player, player.getOffhandItem());
        for (var armor : ARMOR_SLOTS) repair(player, player.getItemBySlot(armor));
    }

    private void repair(ServerPlayer player, ItemStack target) {
        if (target.isEmpty() || !target.isDamageableItem() || target.is(CURIOS)) return;
        var enchantmentLevel = PGCEnchantments.levelOf(player.level(), target, PGCEnchantments.COSMIC_FRAGMENT_MENDING);
        if (enchantmentLevel <= 0) return;
        var amount = Math.max(1, (int) (target.getMaxDamage() * REPAIR_RATIO_PER_LEVEL * enchantmentLevel));
        if (target.getDamageValue() <= amount) return;
        if (PlayerItems.count(player, this) <= 0) return;
        PlayerItems.take(player, this, 1);
        target.setDamageValue(target.getDamageValue() - amount);
    }
}
