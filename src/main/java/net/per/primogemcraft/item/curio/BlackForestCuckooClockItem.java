package net.per.primogemcraft.item.curio;

import net.per.primogemcraft.compat.component.StackComponents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.item.ItemStack;
import net.per.primogemcraft.registry.PGCDataComponents;
import net.per.primogemcraft.registry.PGCEffects;
import net.per.primogemcraft.registry.PGCSounds;
import net.per.primogemcraft.system.curio.*;
import net.per.primogemcraft.system.curio.effect.CurioEffects;

public class BlackForestCuckooClockItem extends CurioItem {
    private static final String COOLDOWN = "curio/black_forest_cuckoo_clock_spawn";
    private static final int DURATION = 24000;
    private static final int COOLDOWN_TICKS = 20;
    private static final int CLEANSED = 1;
    private static final double RADIUS = 16.0D;
    private static final double SPAWN_CHANCE = 0.1D;
    private static final double TYPE_WEIGHT = 0.4D;
    private static final int SPAWNS = 3;
    private static final float SOUND_VOLUME = 4.0F;
    private static final float SOUND_PITCH = 0.5F;
    private static final float YAW_RANGE = 360.0F;

    public BlackForestCuckooClockItem(Settings properties) {
        super(CurioTrigger.ACTIVE, CurioForm.FUSION, properties);
    }

    @Override
    public void pickedUp(ServerPlayerEntity player, ItemStack stack) {
        arm(player, stack);
    }

    @Override
    public void presence(CurioContext context) {
        var player = context.player();
        if (cleansed(context.stack())) return;
        if (!CurioEffects.active(player, PGCEffects.BLACK_FOREST_CUCKOO_CLOCK.get())) {
            arm(player, context.stack());
            return;
        }
        CurioEffects.hold(player, PGCEffects.BLACK_FOREST_CUCKOO_CLOCK.get(), DURATION, 0);
        for (var mob : player.getServerWorld().getEntitiesByClass(MobEntity.class, player.getBoundingBox().expand(RADIUS), e -> true)) mob.setTarget(player);
    }

    private static void arm(ServerPlayerEntity player, ItemStack stack) {
        if (cleansed(stack)) return;
        if (CurioEffects.active(player, PGCEffects.BLACK_FOREST_CUCKOO_CLOCK.get())) return;
        CurioEffects.hold(player, PGCEffects.BLACK_FOREST_CUCKOO_CLOCK.get(), DURATION, 0);
        player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), PGCSounds.CURIO_BROKEN.get(), SoundCategory.PLAYERS, SOUND_VOLUME, SOUND_PITCH);
    }

    public static void clear(ServerPlayerEntity player) {
        var counter = PGCDataComponents.CURIO_COUNTER.get();
        for (var context : Curios.held(player))
            if (context.stack().getItem() instanceof BlackForestCuckooClockItem) StackComponents.set(context.stack(), counter, CLEANSED);
        CurioEffects.release(player, PGCEffects.BLACK_FOREST_CUCKOO_CLOCK.get());
    }

    private static boolean cleansed(ItemStack stack) {
        return StackComponents.getOrDefault(stack, PGCDataComponents.CURIO_COUNTER, 0) == CLEANSED;
    }

    @Override
    public void impacted(CurioContext context, CurioImpact impact) {
        if (impact.signal() != CurioSignal.ATTACK) return;
        if (!(impact.subject() instanceof MobEntity victim)) return;
        var player = context.player();
        if (!CurioEffects.active(player, PGCEffects.BLACK_FOREST_CUCKOO_CLOCK.get())) return;
        if (victim.getTarget() != player) return;
        if (victim.getMaxHealth() > player.getMaxHealth()) return;
        if (!context.ready(COOLDOWN, COOLDOWN_TICKS)) return;
        if (!context.chance(SPAWN_CHANCE)) return;
        for (var index = 0; index < SPAWNS; index++) spawn(context, victim);
    }

    private static void spawn(CurioContext context, MobEntity victim) {
        var level = context.getWorld();
        var type = context.chance(TYPE_WEIGHT) ? EntityType.ZOMBIE : context.chance(TYPE_WEIGHT) ? EntityType.SKELETON : EntityType.WITHER_SKELETON;
        var spawned = type.spawn(level, victim.getBlockPos(), SpawnReason.MOB_SUMMONED);
        if (spawned == null) return;
        spawned.setYaw(level.getRandom().nextFloat() * YAW_RANGE);
    }
}
