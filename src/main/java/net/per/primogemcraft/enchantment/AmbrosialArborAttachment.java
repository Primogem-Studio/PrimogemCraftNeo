package net.per.primogemcraft.enchantment;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ElytraItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.jetbrains.annotations.Nullable;

public final class AmbrosialArborAttachment {
    private AmbrosialArborAttachment() {
    }

    public static boolean preventBreak(ServerLevel world, @Nullable LivingEntity entity, ItemStack stack) {
        if (!stack.isDamageableItem()) return false;
        var maximumDamage = stack.getMaxDamage();
        var elytra = stack.getItem() instanceof ElytraItem;
        var breakThreshold = maximumDamage - (elytra ? 1 : 0);
        if (stack.getDamageValue() < breakThreshold) return false;
        var enchantment = PGCEnchantments.holder(world, PGCEnchantments.AMBROSIAL_ARBOR_ATTACHMENT);
        if (enchantment.isEmpty()) return false;
        var level = stack.getEnchantmentLevel(enchantment.get());
        if (level <= 0) return false;
        var minimumRestored = Math.min(maximumDamage, elytra ? 2 : 1);
        var restored = Math.max(minimumRestored, (int) ((long) maximumDamage * Math.min(level, 5) / 5));
        stack.setDamageValue(maximumDamage - restored);
        EnchantmentHelper.updateEnchantments(stack, mutable -> mutable.set(enchantment.get(), level - 1));
        if (entity != null) {
            world.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 1.0F, 1.0F);
        }
        return true;
    }
}
