package net.per.primogemcraft.item.tool;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.world.World;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.system.curio.Curios;
import net.per.primogemcraft.system.weapon.WeaponTier;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.per.primogemcraft.compat.item.WeaponAttributes120;

public class PrimogemSwordItem extends SwordItem {
    private static final ToolMaterial TIER = new WeaponTier(1000, 6.0F, 10, WeaponTier.IRON_INCORRECT, PGCItems.PRIMOGEM);
    private static final float ATTACK_DAMAGE = 6.0F;
    private static final float ATTACK_SPEED = -2.4F;
    private static final double EASTER_EGG_CHANCE = 0.05D;

    public PrimogemSwordItem(Settings properties) {
        super(TIER, 0, ATTACK_SPEED, properties.fireproof());
    }

    @Override
    public void onCraft(ItemStack stack, World level, PlayerEntity player) {
        super.onCraft(stack, level, player);
        if (!(level instanceof ServerWorld server) || !(player instanceof ServerPlayerEntity serverPlayer)) return;
        if (server.getRandom().nextDouble() >= EASTER_EGG_CHANCE) return;
        stack.damage(stack.getMaxDamage(), serverPlayer, item -> {
        });
        Curios.give(serverPlayer, new ItemStack(PGCItems.STRANGE_PRIMOGEM_SWORD.get()));
    }

    @Override
    public com.google.common.collect.Multimap<net.minecraft.entity.attribute.EntityAttribute, EntityAttributeModifier> getAttributeModifiers(EquipmentSlot slot) {
        var mutable = com.google.common.collect.ArrayListMultimap.<net.minecraft.entity.attribute.EntityAttribute, EntityAttributeModifier>create();
        mutable.putAll(super.getAttributeModifiers(slot));
        if (slot == EquipmentSlot.MAINHAND)
            WeaponAttributes120.putWeaponModifiers(mutable, ATTACK_DAMAGE, ATTACK_SPEED);
        return com.google.common.collect.ImmutableMultimap.copyOf(mutable);
    }
}
