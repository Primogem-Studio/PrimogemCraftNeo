package net.per.primogemcraft.system.curio;

import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.Identifier;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.sound.SoundCategory;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.per.primogemcraft.item.curio.FissionCuckooClockItem;
import net.per.primogemcraft.registry.PGCEffects;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.system.curio.compat.CuriosIntegration;

import java.util.List;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public final class CuckooClockDuplicates {
    public static final Identifier DECORATION = Identifier.of(MOD_ID, "curio/fission_cuckoo_clock/decoration");
    private static final List<EquipmentSlot> SLOTS = List.of(EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND,
            EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD);
    private static final int TRICK_TICKS = 24000;
    private static final int SMOKE_COUNT = 40;
    private static final double SMOKE_HEIGHT = 2.0D;
    private static final double SMOKE_SPREAD = 0.1D;
    private static final float SOUND_VOLUME = 10.0F;
    private static final float NO_DROP = 0.0F;
    private static final int CLEAR_ALL = -1;

    private CuckooClockDuplicates() {
    }

    public static void spawn(CurioContext context, LivingEntity source, int tier) {
        var spawned = source.getType().spawn(context.getWorld(), source.getBlockPos(), SpawnReason.MOB_SUMMONED);
        if (!(spawned instanceof LivingEntity duplicate)) return;
        duplicate.addStatusEffect(new StatusEffectInstance(PGCEffects.CUCKOO_CLOCK_TRICK.get(), TRICK_TICKS, 0, false, false));
        equip(duplicate, tier);
    }

    public static boolean isDecoration(ItemStack stack) {
        return Curios.marked(stack, DECORATION);
    }

    public static void register() {
        ServerLivingEntityEvents.ALLOW_DEATH.register((entity, source, amount) -> {
            if (entity.getGroup() != net.minecraft.entity.EntityGroup.UNDEAD) return true;
            if (!entity.hasStatusEffect(PGCEffects.CUCKOO_CLOCK_TRICK.get())) return true;
            if (!(entity.getWorld() instanceof ServerWorld level)) return true;
            level.spawnParticles(ParticleTypes.LARGE_SMOKE, entity.getX(), entity.getY(), entity.getZ(), SMOKE_COUNT, 0.0D, SMOKE_HEIGHT, 0.0D, SMOKE_SPREAD);
            level.playSound(null, entity.getBlockPos(), SoundEvents.ENTITY_EVOKER_CAST_SPELL, SoundCategory.HOSTILE, SOUND_VOLUME, 1.0F);
            entity.discard();
            return false;
        });
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            if (!alive) FissionCuckooClockItem.forget(newPlayer);
        });
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (var player : server.getPlayerManager().getPlayerList()) {
                player.getInventory().remove(stack -> isDecoration(stack), CLEAR_ALL, player.playerScreenHandler.getCraftingInput());
                for (var stack : CuriosIntegration.equipped(player)) if (isDecoration(stack)) stack.setCount(0);
            }
        });
    }

    private static void equip(LivingEntity duplicate, int tier) {
        if (!(duplicate instanceof MobEntity mob)) return;
        mob.equipStack(EquipmentSlot.HEAD, decoration(new ItemStack(PGCItems.SPECIAL_01_HELMET.get())));
        mob.equipStack(EquipmentSlot.OFFHAND, decoration(new ItemStack(PGCItems.FISSION_CUCKOO_CLOCK_I.get())));
        if (tier >= 1) {
            mob.equipStack(EquipmentSlot.CHEST, new ItemStack(Items.IRON_CHESTPLATE));
            mob.equipStack(EquipmentSlot.LEGS, new ItemStack(Items.IRON_LEGGINGS));
            mob.equipStack(EquipmentSlot.FEET, new ItemStack(Items.IRON_BOOTS));
            mob.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.GOLDEN_SWORD));
        }
        if (tier >= 2) {
            mob.equipStack(EquipmentSlot.CHEST, new ItemStack(Items.DIAMOND_CHESTPLATE));
            mob.equipStack(EquipmentSlot.LEGS, new ItemStack(Items.DIAMOND_LEGGINGS));
            mob.equipStack(EquipmentSlot.FEET, new ItemStack(Items.DIAMOND_BOOTS));
            mob.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
        }
        if (tier >= 3) {
            mob.equipStack(EquipmentSlot.CHEST, new ItemStack(Items.NETHERITE_CHESTPLATE));
            mob.equipStack(EquipmentSlot.LEGS, new ItemStack(Items.NETHERITE_LEGGINGS));
            mob.equipStack(EquipmentSlot.FEET, new ItemStack(Items.NETHERITE_BOOTS));
            mob.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.DIAMOND_SWORD));
        }
        for (var slot : SLOTS) mob.setEquipmentDropChance(slot, NO_DROP);
    }

    private static ItemStack decoration(ItemStack stack) {
        Curios.mark(stack, DECORATION);
        return stack;
    }
}
