package net.per.primogemcraft.enchantment;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public final class AmbrosialArborAttachment {
    private static final float RESTORE_PER_LEVEL = 0.2F;

    private AmbrosialArborAttachment() {
    }

    public static void restore(LivingEntity entity, ItemStack stack, int margin) {
        if (entity.level().isClientSide() || !stack.isDamageableItem()) return;
        var damage = stack.getDamageValue();
        if (damage <= stack.getMaxDamage() - margin) return;
        var level = PGCEnchantments.levelOf(entity.level(), stack, PGCEnchantments.AMBROSIAL_ARBOR_ATTACHMENT);
        if (level <= 0) return;
        var restored = damage - (int) (damage * level * RESTORE_PER_LEVEL);
        if (restored >= damage) return;
        stack.setDamageValue(restored);
        degrade(entity, stack, level);
        entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    private static void degrade(LivingEntity entity, ItemStack stack, int level) {
        PGCEnchantments.holder(entity.level(), PGCEnchantments.AMBROSIAL_ARBOR_ATTACHMENT)
                .ifPresent(enchantment -> EnchantmentHelper.updateEnchantments(stack, mutable -> mutable.set(enchantment, level - 1)));
    }
}
