package net.per.primogemcraft.item.curio;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.ItemStack;
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

    public BlackForestCuckooClockItem(Properties properties) {
        super(CurioTrigger.ACTIVE, CurioForm.FUSION, properties);
    }

    @Override
    public void pickedUp(ServerPlayer player, ItemStack stack) {
        arm(player, stack);
    }

    @Override
    public void presence(CurioContext context) {
        var player = context.player();
        if (cleansed(context.stack())) return;
        if (!CurioEffects.active(player, PGCEffects.BLACK_FOREST_CUCKOO_CLOCK)) {
            arm(player, context.stack());
            return;
        }
        CurioEffects.hold(player, PGCEffects.BLACK_FOREST_CUCKOO_CLOCK, DURATION, 0);
        for (var mob : player.serverLevel().getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(RADIUS))) mob.setTarget(player);
    }

    private static void arm(ServerPlayer player, ItemStack stack) {
        if (cleansed(stack)) return;
        if (CurioEffects.active(player, PGCEffects.BLACK_FOREST_CUCKOO_CLOCK)) return;
        CurioEffects.hold(player, PGCEffects.BLACK_FOREST_CUCKOO_CLOCK, DURATION, 0);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), PGCSounds.CURIO_BROKEN.get(), SoundSource.PLAYERS, SOUND_VOLUME, SOUND_PITCH);
    }

    public static void clear(ServerPlayer player) {
        var counter = PGCDataComponents.CURIO_COUNTER.get();
        for (var context : Curios.held(player))
            if (context.stack().getItem() instanceof BlackForestCuckooClockItem) context.stack().set(counter, CLEANSED);
        CurioEffects.release(player, PGCEffects.BLACK_FOREST_CUCKOO_CLOCK);
    }

    private static boolean cleansed(ItemStack stack) {
        return stack.getOrDefault(PGCDataComponents.CURIO_COUNTER.get(), 0) == CLEANSED;
    }

    @Override
    public void impacted(CurioContext context, CurioImpact impact) {
        if (impact.signal() != CurioSignal.ATTACK) return;
        if (!(impact.subject() instanceof Mob victim)) return;
        var player = context.player();
        if (!CurioEffects.active(player, PGCEffects.BLACK_FOREST_CUCKOO_CLOCK)) return;
        if (victim.getTarget() != player) return;
        if (victim.getMaxHealth() > player.getMaxHealth()) return;
        if (!context.ready(COOLDOWN, COOLDOWN_TICKS)) return;
        if (!context.chance(SPAWN_CHANCE)) return;
        for (var index = 0; index < SPAWNS; index++) spawn(context, victim);
    }

    private static void spawn(CurioContext context, Mob victim) {
        var level = context.level();
        var type = context.chance(TYPE_WEIGHT) ? EntityType.ZOMBIE : context.chance(TYPE_WEIGHT) ? EntityType.SKELETON : EntityType.WITHER_SKELETON;
        var spawned = type.spawn(level, victim.blockPosition(), MobSpawnType.MOB_SUMMONED);
        if (spawned == null) return;
        spawned.setYRot(level.getRandom().nextFloat() * YAW_RANGE);
    }
}
