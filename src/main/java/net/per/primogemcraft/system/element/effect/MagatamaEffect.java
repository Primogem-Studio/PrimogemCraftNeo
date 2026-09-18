package net.per.primogemcraft.system.element.effect;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForgeMod;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public class MagatamaEffect extends MobEffect {
    private static final double ATTACK_DAMAGE = 0.3D;
    private static final double ATTACK_SPEED = 0.3D;
    private static final double SWIM_SPEED = 0.3D;
    private static final double ARMOR = 0.3D;
    private static final int FIRE_RESISTANCE_DURATION = 60;
    private static final int FIRE_RESISTANCE_AMPLIFIER = 1;
    private static final float THUNDER_VOLUME = 3.0F;
    private static final float THUNDER_PITCH = 0.5F;

    public MagatamaEffect() {
        super(MobEffectCategory.NEUTRAL, -1878800);
        addAttributeModifier(Attributes.ATTACK_DAMAGE, id("attack_damage"), ATTACK_DAMAGE, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        addAttributeModifier(Attributes.ATTACK_SPEED, id("attack_speed"), ATTACK_SPEED, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        addAttributeModifier(NeoForgeMod.SWIM_SPEED, id("swim_speed"), SWIM_SPEED, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        addAttributeModifier(Attributes.ARMOR, id("armor"), ARMOR, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
    }

    @Override
    public void onEffectStarted(LivingEntity entity, int amplifier) {
        var level = entity.level();
        if (level instanceof ServerLevel serverLevel) {
            var bolt = EntityType.LIGHTNING_BOLT.create(serverLevel);
            if (bolt != null) {
                bolt.moveTo(Vec3.atBottomCenterOf(entity.blockPosition()));
                bolt.setVisualOnly(true);
                serverLevel.addFreshEntity(bolt);
            }
        }
        level.playSound(null, entity.blockPosition(), SoundEvents.TRIDENT_THUNDER.value(), SoundSource.PLAYERS, THUNDER_VOLUME, THUNDER_PITCH);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        entity.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, FIRE_RESISTANCE_DURATION, FIRE_RESISTANCE_AMPLIFIER, false, false));
        return true;
    }

    private static ResourceLocation id(String name) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, "effect/magatama_" + name);
    }
}
