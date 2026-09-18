package net.per.primogemcraft.item.curio;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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

    public EnigmataMagnetismItem(Properties properties) {
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
        if (!victim.getType().is(EntityTypeTags.UNDEAD)) return;
        var data = victim.getPersistentData();
        if (data.getBoolean(CONVERTED)) return;
        if (!context.chance(CHANCE)) return;
        data.putBoolean(CONVERTED, true);
        data.putLong(MARK, victim.level().getGameTime() + MARK_TICKS);
        impact.level().playSound(null, victim.getX(), victim.getY(), victim.getZ(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.0F, 20.0F);
        reinforce(victim);
        if (context.player().getInventory().contains(stack -> !stack.isEmpty() && stack.is(PGCItems.MAGNETIC_FLAME.get()))) {
            context.player().getPersistentData().putBoolean(FLAME_FLAG, true);
            duplicate(impact.level(), victim);
        }
        context.damage(1);
    }

    private static void fragments(CurioContext context, CurioImpact impact, LivingEntity victim) {
        if (!victim.getType().is(EntityTypeTags.UNDEAD)) return;
        if (victim.getPersistentData().getLong(MARK) <= victim.level().getGameTime()) return;
        CurioLoot.spawn(impact.level(), victim.position(), new ItemStack(PGCItems.COSMIC_FRAGMENT.get(), Mth.nextInt(context.random(), 2, 20)));
    }

    private static void reinforce(LivingEntity victim) {
        if (victim.getItemBySlot(EquipmentSlot.HEAD).isEmpty()) victim.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.GOLDEN_HELMET));
        if (victim.getMainHandItem().isEmpty()) victim.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_SWORD));
        applyBoost(victim);
    }

    private static void applyBoost(LivingEntity victim) {
        victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, MobEffectInstance.INFINITE_DURATION, SPEED_AMPLIFIER, false, false));
        victim.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, MobEffectInstance.INFINITE_DURATION, STRENGTH_AMPLIFIER, false, false));
        victim.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, MobEffectInstance.INFINITE_DURATION, RESISTANCE_AMPLIFIER, false, false));
        victim.addEffect(new MobEffectInstance(MobEffects.GLOWING, MobEffectInstance.INFINITE_DURATION, 0, false, false));
        victim.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, MobEffectInstance.INFINITE_DURATION, 0, false, false));
    }

    private static void duplicate(ServerLevel level, LivingEntity victim) {
        var spawned = victim.getType().spawn(level, BlockPos.containing(victim.position()), MobSpawnType.MOB_SUMMONED);
        if (!(spawned instanceof LivingEntity copy)) return;
        copy.getPersistentData().putBoolean(CONVERTED, true);
        copy.getPersistentData().putLong(MARK, level.getGameTime() + MARK_TICKS);
        reinforce(copy);
    }

    public static boolean consumeFlameFlag(Player player) {
        var data = player.getPersistentData();
        if (!data.getBoolean(FLAME_FLAG)) return false;
        data.putBoolean(FLAME_FLAG, false);
        return true;
    }
}
