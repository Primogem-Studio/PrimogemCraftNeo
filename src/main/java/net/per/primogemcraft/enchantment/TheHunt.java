package net.per.primogemcraft.enchantment;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.sound.SoundCategory;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.enchantment.EnchantmentHelper;
import net.per.primogemcraft.item.weapon.BlackTasselItem;
import net.per.primogemcraft.registry.PGCEffects;
import net.per.primogemcraft.registry.PGCParticles;
import net.per.primogemcraft.system.curio.effect.CurioEffects;
import net.per.primogemcraft.system.weapon.WeaponEnhancement;
import net.per.primogemcraft.system.weapon.WeaponState;
import net.per.primogemcraft.util.PGCTimer;

public final class TheHunt {
    private static final int COOLDOWN = 500;
    private static final int COOLDOWN_PER_LEVEL = 100;
    private static final int TRANSFER_LEVEL = 5;
    private static final int TRANSFER_COOLDOWN = 6000;
    private static final int MAX_TRANSFER_TICKS = 6000;
    private static final double PARTICLE_HEIGHT = 1.0D;
    private static final int PARTICLE_COUNT = 100;
    private static final double PARTICLE_SPEED = 0.7D;
    private static final float SOUND_VOLUME = 0.2F;
    private static final float SOUND_PITCH = 0.7F;
    private static final String COOLDOWN_TIMER = "enchant/the_hunt";
    private static final String TRANSFER_TIMER = "enchant/the_hunt_transfer";

    private TheHunt() {
    }

    public static void harvest(LivingEntity attacker, LivingEntity victim) {
        if (attacker.getWorld().isClient() || attacker == victim) return;
        if (!victim.hasStatusEffect(PGCEffects.ABUNDANCE.get())) return;
        if (!PGCTimer.isDone(attacker, COOLDOWN_TIMER)) return;
        var level = levelOf(attacker);
        if (level <= 0) return;
        PGCTimer.set(attacker, COOLDOWN_TIMER, cooldownTicks(level));
        var instance = victim.getStatusEffect(PGCEffects.ABUNDANCE.get());
        if (instance != null && level >= TRANSFER_LEVEL && attacker instanceof ServerPlayerEntity player && PGCTimer.isDone(attacker, TRANSFER_TIMER)) {
            PGCTimer.set(attacker, TRANSFER_TIMER, TRANSFER_COOLDOWN);
            CurioEffects.apply(player, PGCEffects.ABUNDANCE.get(), Math.min(instance.getDuration(), MAX_TRANSFER_TICKS), Math.max(0, instance.getAmplifier() - 1));
            burst(player);
        }
        victim.removeStatusEffect(PGCEffects.ABUNDANCE.get());
    }

    public static int levelOf(LivingEntity attacker) {
        var stack = attacker.getMainHandStack();
        if (!(stack.getItem() instanceof BlackTasselItem)) return enchantLevel(stack);
        var refinement = attacker instanceof PlayerEntity player
                ? WeaponEnhancement.refinementOf(player, stack)
                : WeaponState.of(stack).refinements();
        return enchantLevel(stack) + refinement;
    }

    public static int level(ItemStack stack) {
        var level = enchantLevel(stack);
        if (!(stack.getItem() instanceof BlackTasselItem)) return level;
        return level + WeaponState.of(stack).refinements();
    }

    public static int cooldownTicks(int level) {
        return Math.max(0, COOLDOWN - COOLDOWN_PER_LEVEL * level);
    }

    private static int enchantLevel(ItemStack stack) {
        for (var entry : EnchantmentHelper.get(stack).entrySet())
            if (entry.getKey() == net.minecraft.registry.Registries.ENCHANTMENT.get(PGCEnchantments.THE_HUNT.getValue())) return entry.getValue();
        return 0;
    }

    private static void burst(ServerPlayerEntity player) {
        player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ITEM_TOTEM_USE, SoundCategory.HOSTILE, SOUND_VOLUME, SOUND_PITCH);
        if (!(player.getWorld() instanceof ServerWorld level)) return;
        level.spawnParticles(PGCParticles.MARA.get(), player.getX(), player.getY() + PARTICLE_HEIGHT, player.getZ(), PARTICLE_COUNT, 0.0D, 0.0D, 0.0D, PARTICLE_SPEED);
    }
}
