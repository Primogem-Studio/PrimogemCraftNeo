package net.per.primogemcraft.entity.mob;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.per.primogemcraft.registry.PGCEffects;
import net.per.primogemcraft.registry.PGCItems;

import java.util.Set;

public class AbundanceBlightZombieEntity extends Monster {
    private static final int BLIGHT_AMPLIFIER = 2;
    private static final float BLACK_SWORD_HEALTH_RATIO = 0.2F;

    private static final Set<ResourceKey<DamageType>> IMMUNE_DAMAGE_TYPES = Set.of(
            DamageTypes.IN_FIRE,
            DamageTypes.CACTUS,
            DamageTypes.DROWN,
            DamageTypes.LIGHTNING_BOLT,
            DamageTypes.FALLING_ANVIL,
            DamageTypes.WITHER,
            DamageTypes.WITHER_SKULL);

    public AbundanceBlightZombieEntity(EntityType<? extends AbundanceBlightZombieEntity> entityType, Level level) {
        super(entityType, level);
        xpReward = 8;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 40.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.ARMOR, 2.0D)
                .add(Attributes.ATTACK_DAMAGE, 5.0D)
                .add(Attributes.FOLLOW_RANGE, 16.0D)
                .add(Attributes.STEP_HEIGHT, 0.8D)
                .add(Attributes.ATTACK_KNOCKBACK, 1.0D);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new MeleeAttackGoal(this, 0.9D, true));
        goalSelector.addGoal(2, new RandomStrollGoal(this, 0.6D));
        goalSelector.addGoal(3, new RandomLookAroundGoal(this));
        goalSelector.addGoal(4, new FloatGoal(this));
        targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true, false));
    }

    @Override
    public boolean hurt(DamageSource damageSource, float amount) {
        equipBlackSword();
        if (isImmuneTo(damageSource)) return false;
        return super.hurt(damageSource, amount);
    }

    @SuppressWarnings("deprecation")
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, SpawnGroupData spawnGroupData) {
        setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(PGCItems.FRUIT_OF_THE_ALIEN_TREE.get()));
        addEffect(new MobEffectInstance(PGCEffects.ABUNDANCE, MobEffectInstance.INFINITE_DURATION, BLIGHT_AMPLIFIER));
        return super.finalizeSpawn(level, difficulty, reason, spawnGroupData);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.HUSK_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.HUSK_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.HUSK_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        playSound(SoundEvents.HUSK_STEP, 0.15F, 1.0F);
    }

    private void equipBlackSword() {
        if (!getMainHandItem().isEmpty()) return;
        if (getHealth() > getMaxHealth() * BLACK_SWORD_HEALTH_RATIO) return;
        setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(PGCItems.THE_BLACK_SWORD.get()));
    }

    private static boolean isImmuneTo(DamageSource damageSource) {
        for (var damageType : IMMUNE_DAMAGE_TYPES) if (damageSource.is(damageType)) return true;
        return damageSource.getDirectEntity() instanceof ThrownPotion
                || damageSource.getDirectEntity() instanceof AreaEffectCloud
                || damageSource.typeHolder().is(NeoForgeMod.POISON_DAMAGE);
    }
}
