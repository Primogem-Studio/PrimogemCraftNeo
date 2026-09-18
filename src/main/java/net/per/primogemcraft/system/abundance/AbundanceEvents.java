package net.per.primogemcraft.system.abundance;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.per.primogemcraft.config.PGCConfig;
import net.per.primogemcraft.entity.mob.AbundanceBlightZombieEntity;
import net.per.primogemcraft.registry.PGCEffects;
import net.per.primogemcraft.registry.PGCEntities;
import net.per.primogemcraft.registry.PGCParticles;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

@EventBusSubscriber(modid = MOD_ID)
public final class AbundanceEvents {
    private static final ResourceKey<LootTable> WITHER_LOOT = ResourceKey.create(Registries.LOOT_TABLE,
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "entities/abundance_withering"));

    private static final int SOURCE_TICKS = 2000000;
    private static final double EASY_CHANCE = 0.05D;
    private static final double NORMAL_CHANCE = 0.1D;
    private static final double HARD_CHANCE = 0.18D;
    private static final double BLIGHT_CHANCE = 0.01D;
    private static final float DEATH_HEALTH_RATIO = 0.5F;
    private static final double DAMAGE_NEGATE_CHANCE = 0.25D;
    private static final int GLOWING_TICKS = 1200;
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

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.loadedFromDisk() || !(event.getLevel() instanceof ServerLevel level)) return;
        if (!(event.getEntity() instanceof LivingEntity living)) return;
        if (living instanceof Zombie zombie && !(living instanceof AbundanceBlightZombieEntity)
                && level.getRandom().nextDouble() < scaled(BLIGHT_CHANCE)) {
            convert(level, zombie);
            return;
        }
        if (!living.getType().is(EntityTypeTags.UNDEAD)) return;
        if (living.getMaxHealth() >= PGCConfig.MARA_HEALTH_THRESHOLD.get()) return;
        if (level.getRandom().nextDouble() >= scaled(sourceChance(level))) return;
        living.addEffect(new MobEffectInstance(PGCEffects.ABUNDANCE, SOURCE_TICKS, 0));
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        var entity = event.getEntity();
        if (!entity.hasEffect(PGCEffects.ABUNDANCE)) return;
        if (entity.getOffhandItem().is(Items.TOTEM_OF_UNDYING)) return;
        var instance = entity.getEffect(PGCEffects.ABUNDANCE);
        if (instance == null) return;
        var level = entity.level();
        entity.setHealth(entity.getMaxHealth() * DEATH_HEALTH_RATIO);
        event.setCanceled(true);
        if (level instanceof ServerLevel server)
            server.sendParticles(PGCParticles.MARA.get(), entity.getX(), entity.getY() + DEATH_PARTICLE_HEIGHT, entity.getZ(),
                    DEATH_PARTICLE_COUNT, 0.0D, 0.0D, 0.0D, DEATH_PARTICLE_SPEED);
        level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.TOTEM_USE, SoundSource.HOSTILE, TOTEM_VOLUME, TOTEM_PITCH);
        var amplifier = instance.getAmplifier();
        var duration = instance.getDuration();
        entity.removeEffect(PGCEffects.ABUNDANCE);
        if (amplifier > 0) {
            entity.addEffect(new MobEffectInstance(PGCEffects.ABUNDANCE, duration, amplifier - 1));
            return;
        }
        if (level instanceof ServerLevel server && !(entity instanceof Player))
            dropWitherLoot(server, entity, event.getSource());
        entity.addEffect(new MobEffectInstance(MobEffects.GLOWING, GLOWING_TICKS, 0, false, false));
    }

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        var entity = event.getEntity();
        if (entity instanceof Player) return;
        if (!entity.hasEffect(PGCEffects.ABUNDANCE)) return;
        if (entity.getRandom().nextDouble() >= DAMAGE_NEGATE_CHANCE) return;
        event.setCanceled(true);
        entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.SHIELD_BLOCK, SoundSource.HOSTILE, SHIELD_VOLUME, SHIELD_PITCH);
    }

    private static double scaled(double chance) {
        return Math.min(1.0D, chance * PGCConfig.MARA_SPAWN_MULTIPLIER.get());
    }

    private static double sourceChance(ServerLevel level) {
        var difficulty = level.getDifficulty();
        if (difficulty == Difficulty.EASY) return EASY_CHANCE;
        if (difficulty == Difficulty.NORMAL) return NORMAL_CHANCE;
        if (difficulty == Difficulty.HARD) return HARD_CHANCE;
        return 0.0D;
    }

    private static void convert(ServerLevel level, Zombie zombie) {
        var type = PGCEntities.ABUNDANCE_BLIGHT_ZOMBIE.get();
        level.getServer().tell(new TickTask(SPAWN_DELAY, () -> {
            if (zombie.isRemoved()) return;
            var spawned = type.spawn(level, zombie.blockPosition(), MobSpawnType.MOB_SUMMONED);
            if (spawned == null) return;
            spawned.setYRot(level.getRandom().nextFloat() * 360.0F);
            zombie.discard();
        }));
    }

    private static void dropWitherLoot(ServerLevel level, LivingEntity entity, DamageSource source) {
        var table = level.getServer().reloadableRegistries().getLootTable(WITHER_LOOT);
        var params = new LootParams.Builder(level)
                .withParameter(LootContextParams.THIS_ENTITY, entity)
                .withParameter(LootContextParams.ORIGIN, entity.position())
                .withParameter(LootContextParams.DAMAGE_SOURCE, source)
                .create(LootContextParamSets.ENTITY);
        for (var stack : table.getRandomItems(params)) entity.spawnAtLocation(stack);
    }
}
