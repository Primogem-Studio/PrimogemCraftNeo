package net.per.primogemcraft.system.curio;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.per.primogemcraft.item.curio.FissionCuckooClockItem;
import net.per.primogemcraft.registry.PGCEffects;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.system.curio.compat.CuriosIntegration;

import java.util.List;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

@EventBusSubscriber(modid = MOD_ID)
public final class CuckooClockDuplicates {
    public static final ResourceLocation DECORATION = ResourceLocation.fromNamespaceAndPath(MOD_ID, "curio/fission_cuckoo_clock/decoration");
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
        var spawned = source.getType().spawn(context.level(), source.blockPosition(), MobSpawnType.MOB_SUMMONED);
        if (!(spawned instanceof LivingEntity duplicate)) return;
        duplicate.addEffect(new MobEffectInstance(PGCEffects.CUCKOO_CLOCK_TRICK, TRICK_TICKS, 0, false, false));
        equip(duplicate, tier);
    }

    public static boolean isDecoration(ItemStack stack) {
        return Curios.marked(stack, DECORATION);
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        var entity = event.getEntity();
        if (!entity.getType().is(EntityTypeTags.UNDEAD)) return;
        if (!entity.hasEffect(PGCEffects.CUCKOO_CLOCK_TRICK)) return;
        if (!(entity.level() instanceof ServerLevel level)) return;
        event.setCanceled(true);
        level.sendParticles(ParticleTypes.LARGE_SMOKE, entity.getX(), entity.getY(), entity.getZ(), SMOKE_COUNT, 0.0D, SMOKE_HEIGHT, 0.0D, SMOKE_SPREAD);
        level.playSound(null, entity.blockPosition(), SoundEvents.EVOKER_CAST_SPELL, SoundSource.HOSTILE, SOUND_VOLUME, 1.0F);
        entity.discard();
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (!event.isWasDeath() || !(event.getEntity() instanceof ServerPlayer player)) return;
        FissionCuckooClockItem.forget(player);
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        player.getInventory().clearOrCountMatchingItems(CuckooClockDuplicates::isDecoration, CLEAR_ALL, player.inventoryMenu.getCraftSlots());
        for (var stack : CuriosIntegration.equipped(player)) if (isDecoration(stack)) stack.setCount(0);
    }

    private static void equip(LivingEntity duplicate, int tier) {
        if (!(duplicate instanceof Mob mob)) return;
        mob.setItemSlot(EquipmentSlot.HEAD, decoration(new ItemStack(PGCItems.SPECIAL_01_HELMET.get())));
        mob.setItemSlot(EquipmentSlot.OFFHAND, decoration(new ItemStack(PGCItems.FISSION_CUCKOO_CLOCK_I.get())));
        if (tier >= 1) {
            mob.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.IRON_CHESTPLATE));
            mob.setItemSlot(EquipmentSlot.LEGS, new ItemStack(Items.IRON_LEGGINGS));
            mob.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.IRON_BOOTS));
            mob.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.GOLDEN_SWORD));
        }
        if (tier >= 2) {
            mob.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.DIAMOND_CHESTPLATE));
            mob.setItemSlot(EquipmentSlot.LEGS, new ItemStack(Items.DIAMOND_LEGGINGS));
            mob.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.DIAMOND_BOOTS));
            mob.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
        }
        if (tier >= 3) {
            mob.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.NETHERITE_CHESTPLATE));
            mob.setItemSlot(EquipmentSlot.LEGS, new ItemStack(Items.NETHERITE_LEGGINGS));
            mob.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.NETHERITE_BOOTS));
            mob.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.DIAMOND_SWORD));
        }
        for (var slot : SLOTS) mob.setDropChance(slot, NO_DROP);
    }

    private static ItemStack decoration(ItemStack stack) {
        Curios.mark(stack, DECORATION);
        return stack;
    }
}
