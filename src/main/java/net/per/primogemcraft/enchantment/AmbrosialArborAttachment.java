package net.per.primogemcraft.enchantment;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.sound.SoundCategory;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.ItemStack;
import net.minecraft.enchantment.EnchantmentHelper;
import org.jetbrains.annotations.Nullable;

public final class AmbrosialArborAttachment {
    private AmbrosialArborAttachment() {
    }

    public static boolean preventBreak(ServerWorld world, @Nullable LivingEntity entity, ItemStack stack) {
        if (!stack.isDamageable()) return false;
        var maximumDamage = stack.getMaxDamage();
        var elytra = stack.getItem() instanceof ElytraItem;
        var breakThreshold = maximumDamage - (elytra ? 1 : 0);
        if (stack.getDamage() < breakThreshold) return false;
        var enchantment = PGCEnchantments.holder(world, PGCEnchantments.AMBROSIAL_ARBOR_ATTACHMENT);
        if (enchantment.isEmpty()) return false;
        var level = net.per.primogemcraft.compat.item.Enchantments120.level(stack, enchantment.get());
        if (level <= 0) return false;
        var minimumRestored = Math.min(maximumDamage, elytra ? 2 : 1);
        var restored = Math.max(minimumRestored, (int) ((long) maximumDamage * Math.min(level, 5) / 5));
        stack.setDamage(maximumDamage - restored);
        net.per.primogemcraft.compat.item.Enchantments120.update(stack, mutable -> mutable.set(enchantment.get(), level - 1));
        if (entity != null) {
            world.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.ENTITY_ITEM_BREAK, SoundCategory.PLAYERS, 1.0F, 1.0F);
        }
        return true;
    }
}
