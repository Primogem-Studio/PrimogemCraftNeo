package net.per.primogemcraft.item.tool;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.system.curio.Curios;
import net.per.primogemcraft.system.weapon.WeaponTier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.per.primogemcraft.compat.item.WeaponAttributes120;

public class PrimogemSwordItem extends SwordItem {
    private static final Tier TIER = new WeaponTier(1000, 6.0F, 10, WeaponTier.IRON_INCORRECT, PGCItems.PRIMOGEM);
    private static final float ATTACK_DAMAGE = 6.0F;
    private static final float ATTACK_SPEED = -2.4F;
    private static final double EASTER_EGG_CHANCE = 0.05D;

    public PrimogemSwordItem(Properties properties) {
        super(TIER, 0, ATTACK_SPEED, properties.fireResistant());
    }

    @Override
    public void onCraftedBy(ItemStack stack, Level level, Player player) {
        super.onCraftedBy(stack, level, player);
        if (!(level instanceof ServerLevel server) || !(player instanceof ServerPlayer serverPlayer)) return;
        if (server.getRandom().nextDouble() >= EASTER_EGG_CHANCE) return;
        stack.hurtAndBreak(stack.getMaxDamage(), serverPlayer, item -> {
        });
        Curios.give(serverPlayer, new ItemStack(PGCItems.STRANGE_PRIMOGEM_SWORD.get()));
    }

    @Override
    public com.google.common.collect.Multimap<net.minecraft.world.entity.ai.attributes.Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        var mutable = com.google.common.collect.ArrayListMultimap.<net.minecraft.world.entity.ai.attributes.Attribute, AttributeModifier>create();
        mutable.putAll(super.getDefaultAttributeModifiers(slot));
        if (slot == EquipmentSlot.MAINHAND)
            WeaponAttributes120.putWeaponModifiers(mutable, ATTACK_DAMAGE, ATTACK_SPEED);
        return com.google.common.collect.ImmutableMultimap.copyOf(mutable);
    }
}
