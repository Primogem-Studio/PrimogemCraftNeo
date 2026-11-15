package net.per.primogemcraft.system.abundance;

import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.minecraft.server.ServerTask;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.sound.SoundCategory;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.loot.context.LootContextParameters;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.per.primogemcraft.config.PGCConfig;
import net.per.primogemcraft.entity.mob.AbundanceBlightZombieEntity;
import net.per.primogemcraft.registry.PGCEffects;
import net.per.primogemcraft.registry.PGCEntities;
import net.per.primogemcraft.registry.PGCParticles;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public final class AbundanceEvents {
    private static final Identifier WITHER_LOOT = Identifier.of(MOD_ID, "entities/abundance_withering");

    private static final int SOURCE_TICKS = -1;
    private static final float DEATH_HEALTH_RATIO = 0.5F;
    private static final double DAMAGE_NEGATE_CHANCE = 0.25D;
    private static final int DEATH_BOOST_TICKS = 300;
    private static final int DEATH_BOOST_AMPLIFIER = 1;
    private static final double DEATH_PARTICLE_HEIGHT = 1.0D;
    private static final int DEATH_PARTICLE_COUNT = 100;
    private static final double DEATH_PARTICLE_SPEED = 0.7D;
    private static final float TOTEM_VOLUME = 0.2F;
    private static final float TOTEM_PITCH = 0.7F;
    private static final float SHIELD_VOLUME = 0.3F;
    private static final float SHIELD_PITCH = 1.0F;
    private static final int SPAWN_DELAY = 1;

    private AbundanceEvents() {
    }

    public static void register() {
        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (!(entity instanceof LivingEntity living)) return;
            if (world.getDifficulty() == net.minecraft.world.Difficulty.PEACEFUL) return;
            if (living.getGroup() != net.minecraft.entity.EntityGroup.UNDEAD) return;
            if (living.getMaxHealth() >= PGCConfig.MARA_HEALTH_THRESHOLD.get()) return;
            if (world.getRandom().nextDouble() >= chance(PGCConfig.ABUNDANCE_CHANCE.get())) return;
            living.addStatusEffect(new StatusEffectInstance(PGCEffects.ABUNDANCE.get(), SOURCE_TICKS, 0));
            if (living instanceof ZombieEntity zombie && !(living instanceof AbundanceBlightZombieEntity)
                    && world.getRandom().nextDouble() < chance(PGCConfig.BLIGHT_ZOMBIE_CHANCE.get()))
                convert(world, zombie);
        });
        ServerLivingEntityEvents.ALLOW_DEATH.register((entity, source, amount) -> {
            if (!entity.hasStatusEffect(PGCEffects.ABUNDANCE.get())) return true;
            if (entity.getOffHandStack().isOf(Items.TOTEM_OF_UNDYING)) return true;
            var instance = entity.getStatusEffect(PGCEffects.ABUNDANCE.get());
            if (instance == null) return true;
            var level = entity.getWorld();
            entity.setHealth(entity.getMaxHealth() * DEATH_HEALTH_RATIO);
            if (level instanceof ServerWorld server)
                server.spawnParticles(PGCParticles.MARA.get(), entity.getX(), entity.getY() + DEATH_PARTICLE_HEIGHT, entity.getZ(),
                        DEATH_PARTICLE_COUNT, 0.0D, 0.0D, 0.0D, DEATH_PARTICLE_SPEED);
            level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.ITEM_TOTEM_USE, SoundCategory.HOSTILE, TOTEM_VOLUME, TOTEM_PITCH);
            var amplifier = instance.getAmplifier();
            var duration = instance.getDuration();
            entity.removeStatusEffect(PGCEffects.ABUNDANCE.get());
            grantDeathBoosts(entity);
            if (amplifier > 0) {
                entity.addStatusEffect(new StatusEffectInstance(PGCEffects.ABUNDANCE.get(), duration, amplifier - 1));
                return false;
            }
            if (level instanceof ServerWorld server && !(entity instanceof PlayerEntity))
                dropWitherLoot(server, entity, source);
            return false;
        });
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            if (entity instanceof PlayerEntity) return true;
            if (!entity.hasStatusEffect(PGCEffects.ABUNDANCE.get())) return true;
            if (entity.getRandom().nextDouble() >= DAMAGE_NEGATE_CHANCE) return true;
            entity.getWorld().playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.ITEM_SHIELD_BLOCK, SoundCategory.HOSTILE, SHIELD_VOLUME, SHIELD_PITCH);
            return false;
        });
    }

    private static double chance(int percent) {
        return percent / 100.0D;
    }

    private static void grantDeathBoosts(LivingEntity entity) {
        if (entity instanceof PlayerEntity) return;
        entity.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, DEATH_BOOST_TICKS, DEATH_BOOST_AMPLIFIER, false, false));
        entity.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, DEATH_BOOST_TICKS, DEATH_BOOST_AMPLIFIER, false, false));
        entity.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, DEATH_BOOST_TICKS, DEATH_BOOST_AMPLIFIER, false, false));
        entity.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, DEATH_BOOST_TICKS, 0, false, false));
    }

    private static void convert(ServerWorld level, ZombieEntity zombie) {
        var type = PGCEntities.ABUNDANCE_BLIGHT_ZOMBIE.get();
        level.getServer().execute(new ServerTask(SPAWN_DELAY, () -> {
            if (zombie.isRemoved()) return;
            var spawned = type.spawn(level, zombie.getBlockPos(), SpawnReason.MOB_SUMMONED);
            if (spawned == null) return;
            spawned.setYaw(level.getRandom().nextFloat() * 360.0F);
            zombie.discard();
        }));
    }

    private static void dropWitherLoot(ServerWorld level, LivingEntity entity, DamageSource source) {
        var table = level.getServer().getLootManager().getLootTable(WITHER_LOOT);
        var params = new LootContextParameterSet.Builder(level)
                .add(LootContextParameters.THIS_ENTITY, entity)
                .add(LootContextParameters.ORIGIN, entity.getPos())
                .add(LootContextParameters.DAMAGE_SOURCE, source)
                .build(LootContextTypes.ENTITY);
        for (var stack : table.generateLoot(params)) entity.dropStack(stack);
    }
}
