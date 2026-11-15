package net.per.primogemcraft.item.curio;

import net.minecraft.util.math.BlockPos;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.sound.SoundCategory;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.Hand;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.system.curio.*;

public class EnigmataMagnetismItem extends CurioItem {
    private static final String MARK = "primogemcraft:enigmata_magnetism";
    private static final String CONVERTED = "primogemcraft:enigmata_magnetism_converted";
    private static final String FLAME_FLAG = "primogemcraft:magnetic_flame";
    private static final int MARK_TICKS = 80;
    private static final int STRENGTH_AMPLIFIER = 4;
    private static final int RESISTANCE_AMPLIFIER = 2;
    private static final int SPEED_AMPLIFIER = 1;
    private static final float CHANCE = 0.3F;

    public EnigmataMagnetismItem(Settings properties) {
        super(CurioTrigger.ACTIVE, CurioForm.NORMAL, 5, properties);
    }

    @Override
    public void impacted(CurioContext context, CurioImpact impact) {
        if (!(impact.subject() instanceof LivingEntity victim)) return;
        if (impact.signal() == CurioSignal.KILL) {
            fragments(context, impact, victim);
            return;
        }
        if (impact.signal() != CurioSignal.ATTACK) return;
        if (victim.getGroup() != net.minecraft.entity.EntityGroup.UNDEAD) return;
        var data = ((net.per.primogemcraft.compat.attachment.PersistentDataHolder) victim).primogemcraft$getPersistentData();
        if (data.getBoolean(CONVERTED)) return;
        if (!context.chance(CHANCE)) return;
        data.putBoolean(CONVERTED, true);
        data.putLong(MARK, victim.getWorld().getTime() + MARK_TICKS);
        impact.level().playSound(null, victim.getX(), victim.getY(), victim.getZ(), SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.PLAYERS, 1.0F, 20.0F);
        reinforce(victim);
        if (net.per.primogemcraft.compat.item.Inventories120.any(context.player().getInventory(), stack -> !stack.isEmpty() && stack.isOf(PGCItems.MAGNETIC_FLAME.get()))) {
            ((net.per.primogemcraft.compat.attachment.PersistentDataHolder) context.player()).primogemcraft$getPersistentData().putBoolean(FLAME_FLAG, true);
            duplicate(impact.level(), victim);
        }
        context.damage(1);
    }

    private static void fragments(CurioContext context, CurioImpact impact, LivingEntity victim) {
        if (victim.getGroup() != net.minecraft.entity.EntityGroup.UNDEAD) return;
        if (((net.per.primogemcraft.compat.attachment.PersistentDataHolder) victim).primogemcraft$getPersistentData().getLong(MARK) <= victim.getWorld().getTime()) return;
        CurioLoot.spawn(impact.level(), victim.getPos(), new ItemStack(PGCItems.COSMIC_FRAGMENT.get(), MathHelper.nextInt(context.random(), 2, 20)));
    }

    private static void reinforce(LivingEntity victim) {
        if (victim.getEquippedStack(EquipmentSlot.HEAD).isEmpty()) victim.equipStack(EquipmentSlot.HEAD, new ItemStack(Items.GOLDEN_HELMET));
        if (victim.getMainHandStack().isEmpty()) victim.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.IRON_SWORD));
        applyBoost(victim);
    }

    private static void applyBoost(LivingEntity victim) {
        victim.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, StatusEffectInstance.INFINITE, SPEED_AMPLIFIER, false, false));
        victim.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, StatusEffectInstance.INFINITE, STRENGTH_AMPLIFIER, false, false));
        victim.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, StatusEffectInstance.INFINITE, RESISTANCE_AMPLIFIER, false, false));
        victim.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, StatusEffectInstance.INFINITE, 0, false, false));
        victim.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, StatusEffectInstance.INFINITE, 0, false, false));
    }

    private static void duplicate(ServerWorld level, LivingEntity victim) {
        var spawned = victim.getType().spawn(level, BlockPos.ofFloored(victim.getPos()), SpawnReason.MOB_SUMMONED);
        if (!(spawned instanceof LivingEntity copy)) return;
        ((net.per.primogemcraft.compat.attachment.PersistentDataHolder) copy).primogemcraft$getPersistentData().putBoolean(CONVERTED, true);
        ((net.per.primogemcraft.compat.attachment.PersistentDataHolder) copy).primogemcraft$getPersistentData().putLong(MARK, level.getTime() + MARK_TICKS);
        reinforce(copy);
    }

    public static boolean consumeFlameFlag(PlayerEntity player) {
        var data = ((net.per.primogemcraft.compat.attachment.PersistentDataHolder) player).primogemcraft$getPersistentData();
        if (!data.getBoolean(FLAME_FLAG)) return false;
        data.putBoolean(FLAME_FLAG, false);
        return true;
    }
}
