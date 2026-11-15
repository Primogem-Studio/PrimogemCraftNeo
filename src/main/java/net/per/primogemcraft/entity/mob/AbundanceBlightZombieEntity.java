package net.per.primogemcraft.entity.mob;

import net.minecraft.util.math.BlockPos;
import net.minecraft.registry.RegistryKey;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.WanderAroundGoal;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.block.BlockState;
import net.per.primogemcraft.registry.PGCEffects;
import net.per.primogemcraft.registry.PGCItems;

import java.util.Set;

public class AbundanceBlightZombieEntity extends HostileEntity {
    private static final int BLIGHT_AMPLIFIER = 2;
    private static final float BLACK_SWORD_HEALTH_RATIO = 0.2F;

    private static final Set<RegistryKey<DamageType>> IMMUNE_DAMAGE_TYPES = Set.of(
            DamageTypes.IN_FIRE,
            DamageTypes.CACTUS,
            DamageTypes.DROWN,
            DamageTypes.LIGHTNING_BOLT,
            DamageTypes.FALLING_ANVIL,
            DamageTypes.WITHER,
            DamageTypes.WITHER_SKULL);

    public AbundanceBlightZombieEntity(EntityType<? extends AbundanceBlightZombieEntity> entityType, World level) {
        super(entityType, level);
        experiencePoints = 8;
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 40.0D)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3D)
                .add(EntityAttributes.GENERIC_ARMOR, 2.0D)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 5.0D)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 16.0D)
                // STEP_HEIGHT removed (Forge-only)
                .add(EntityAttributes.GENERIC_ATTACK_KNOCKBACK, 1.0D);
    }

    @Override
    protected void initGoals() {
        goalSelector.add(1, new MeleeAttackGoal(this, 0.9D, true));
        goalSelector.add(2, new WanderAroundGoal(this, 0.6D));
        goalSelector.add(3, new LookAroundGoal(this));
        goalSelector.add(4, new SwimGoal(this));
        targetSelector.add(1, new ActiveTargetGoal<>(this, PlayerEntity.class, true, false));
    }

    @Override
    public boolean damage(DamageSource damageSource, float amount) {
        equipBlackSword();
        if (isImmuneTo(damageSource)) return false;
        return super.damage(damageSource, amount);
    }

    @SuppressWarnings("deprecation")
    @Override
    public net.minecraft.entity.EntityData initialize(net.minecraft.world.ServerWorldAccess world, net.minecraft.world.LocalDifficulty difficulty, net.minecraft.entity.SpawnReason reason, net.minecraft.entity.EntityData entityData, net.minecraft.nbt.NbtCompound tag) {
        equipStack(EquipmentSlot.OFFHAND, new ItemStack(PGCItems.FRUIT_OF_THE_ALIEN_TREE.get()));
        addStatusEffect(new StatusEffectInstance(PGCEffects.ABUNDANCE.get(), StatusEffectInstance.INFINITE, BLIGHT_AMPLIFIER));
        return super.initialize(world, difficulty, reason, entityData, tag);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENTITY_HUSK_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.ENTITY_HUSK_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_HUSK_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        playSound(SoundEvents.ENTITY_HUSK_STEP, 0.15F, 1.0F);
    }

    private void equipBlackSword() {
        if (!getMainHandStack().isEmpty()) return;
        if (getHealth() > getMaxHealth() * BLACK_SWORD_HEALTH_RATIO) return;
        equipStack(EquipmentSlot.MAINHAND, new ItemStack(PGCItems.THE_BLACK_SWORD.get()));
    }

    private static boolean isImmuneTo(DamageSource damageSource) {
        for (var damageType : IMMUNE_DAMAGE_TYPES) if (damageSource.isOf(damageType)) return true;
        return damageSource.getSource() instanceof PotionEntity
                || damageSource.getSource() instanceof net.minecraft.entity.AreaEffectCloudEntity
                || damageSource.isOf(net.minecraft.entity.damage.DamageTypes.MAGIC);
    }
}
