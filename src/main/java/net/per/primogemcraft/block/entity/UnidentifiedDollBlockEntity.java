package net.per.primogemcraft.block.entity;

import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.component.Fireworks;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.per.primogemcraft.block.UnidentifiedDollBlock;
import net.per.primogemcraft.enchantment.FoolsWrath;
import net.per.primogemcraft.registry.PGCBlockEntities;
import net.per.primogemcraft.registry.PGCSounds;

import java.util.List;

public class UnidentifiedDollBlockEntity extends BlockEntity {
    private static final String TICKING_KEY = "ticking";
    private static final String ARMED_KEY = "armed";
    private static final String DETONATION_KEY = "detonation";
    private static final String NEXT_FLIP_KEY = "next_flip";

    private static final int COUNTDOWN_TICKS = 600;
    private static final int FUSE_TICKS = 40;
    private static final int LIT_TICKS = 4;
    private static final long MEDIUM_REMAINING = 200L;
    private static final long FAST_REMAINING = 40L;

    private static final int BLAST_REACH = 1;
    private static final int BLAST_STEP = 17;
    private static final float BLAST_POWER = 40.0F;
    private static final double CLEANSE_RADIUS = 12.0D;

    private static final List<Shell> SHELLS = List.of(
            new Shell(3.0D, 1.0D, 0.0D, 5, List.of(
                    shell(FireworkExplosion.Shape.LARGE_BALL, 15882071, 3438841, false),
                    shell(FireworkExplosion.Shape.STAR, 16773394, 5701425, false))),
            new Shell(0.0D, 1.0D, 5.0D, 4, List.of(
                    shell(FireworkExplosion.Shape.BURST, 14873175, 3471673, true),
                    shell(FireworkExplosion.Shape.STAR, 16730642, 16009471, false))),
            new Shell(-4.0D, 1.0D, 0.0D, 4, List.of(
                    shell(FireworkExplosion.Shape.LARGE_BALL, 15095794, 16331918, true),
                    shell(FireworkExplosion.Shape.CREEPER, 2757375, 4784112, false))));

    private boolean ticking;
    private boolean armed;
    private long detonation;
    private long nextFlip;

    public UnidentifiedDollBlockEntity(BlockPos pos, BlockState state) {
        super(PGCBlockEntities.UNIDENTIFIED_DOLL.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, UnidentifiedDollBlockEntity doll) {
        if (level instanceof ServerLevel server) doll.tick(server, pos, state);
    }

    public boolean ticking() {
        return ticking;
    }

    public void arm(ServerLevel level, BlockPos pos, BlockState state, boolean armed) {
        if (ticking) return;
        var now = level.getGameTime();
        ticking = true;
        this.armed = armed;
        detonation = now + COUNTDOWN_TICKS;
        nextFlip = now + (armed ? LIT_TICKS : stageOf(now).interval());
        level.setBlock(pos, state.setValue(UnidentifiedDollBlock.LIT, armed), Block.UPDATE_ALL);
        setChanged();
    }

    private void tick(ServerLevel level, BlockPos pos, BlockState state) {
        if (!ticking) return;
        var now = level.getGameTime();
        if (now >= detonation + FUSE_TICKS) {
            detonate(level, pos);
            return;
        }
        if (now >= detonation) {
            if (now == detonation) {
                level.setBlock(pos, state.setValue(UnidentifiedDollBlock.LIT, true), Block.UPDATE_ALL);
                playSound(level, pos, PGCSounds.BOMB_FINAL_TICK.get());
            }
            return;
        }
        if (now >= nextFlip) flip(level, pos, state, now);
    }

    private void flip(ServerLevel level, BlockPos pos, BlockState state, long now) {
        var lit = !state.getValue(UnidentifiedDollBlock.LIT);
        level.setBlock(pos, state.setValue(UnidentifiedDollBlock.LIT, lit), Block.UPDATE_ALL);
        var stage = stageOf(now);
        nextFlip = now + (lit ? stage.interval() : LIT_TICKS);
        if (lit) playSound(level, pos, PGCSounds.BOMB_TICK.get(), 1.0F, stage.pitch());
        setChanged();
    }

    private Stage stageOf(long now) {
        var remaining = detonation - now;
        if (remaining > MEDIUM_REMAINING) return Stage.NORMAL;
        return remaining > FAST_REMAINING ? Stage.FAST : Stage.VERY_FAST;
    }

    private void detonate(ServerLevel level, BlockPos pos) {
        if (armed) blast(level, pos);
        else fizzle(level, pos);
        level.removeBlock(pos, false);
    }

    private static void blast(ServerLevel level, BlockPos pos) {
        for (var x = -BLAST_REACH; x <= BLAST_REACH; x++)
            for (var y = -BLAST_REACH; y <= BLAST_REACH; y++)
                for (var z = -BLAST_REACH; z <= BLAST_REACH; z++)
                    level.explode(null, pos.getX() + x * BLAST_STEP, pos.getY() + y * BLAST_STEP, pos.getZ() + z * BLAST_STEP, BLAST_POWER, Level.ExplosionInteraction.TNT);
    }

    private static void fizzle(ServerLevel level, BlockPos pos) {
        playSound(level, pos, SoundEvents.GENERIC_EXPLODE.value());
        FoolsWrath.cleanse(level, new AABB(pos).inflate(CLEANSE_RADIUS));
        for (var shell : SHELLS) level.addFreshEntity(rocket(level, pos, shell));
    }

    private static FireworkRocketEntity rocket(ServerLevel level, BlockPos pos, Shell shell) {
        var stack = new ItemStack(Items.FIREWORK_ROCKET);
        stack.set(DataComponents.FIREWORKS, new Fireworks(shell.flight(), shell.explosions()));
        return new FireworkRocketEntity(level, pos.getX() + shell.x(), pos.getY() + shell.y(), pos.getZ() + shell.z(), stack);
    }

    private static FireworkExplosion shell(FireworkExplosion.Shape shape, int color, int fadeColor, boolean trail) {
        return new FireworkExplosion(shape, IntList.of(color), IntList.of(fadeColor), trail, true);
    }

    private static void playSound(ServerLevel level, BlockPos pos, SoundEvent sound) {
        playSound(level, pos, sound, (float) 1.0, 1.0F);
    }

    private static void playSound(ServerLevel level, BlockPos pos, SoundEvent sound, float volume, float pitch) {
        level.playSound(null, pos, sound, SoundSource.BLOCKS, volume, pitch);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putBoolean(TICKING_KEY, ticking);
        tag.putBoolean(ARMED_KEY, armed);
        tag.putLong(DETONATION_KEY, detonation);
        tag.putLong(NEXT_FLIP_KEY, nextFlip);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        ticking = tag.getBoolean(TICKING_KEY);
        armed = tag.getBoolean(ARMED_KEY);
        detonation = tag.getLong(DETONATION_KEY);
        nextFlip = tag.getLong(NEXT_FLIP_KEY);
    }

    private record Shell(double x, double y, double z, int flight, List<FireworkExplosion> explosions) {
    }

    private enum Stage {
        NORMAL(20, 0.8F),
        FAST(12, 1.1F),
        VERY_FAST(8, 1.4F);

        private final int interval;
        private final float pitch;

        Stage(int interval, float pitch) {
            this.interval = interval;
            this.pitch = pitch;
        }

        public int interval() {
            return interval;
        }

        public float pitch() {
            return pitch;
        }
    }
}
