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

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public class MagatamaEffect extends MobEffect implements net.per.primogemcraft.compat.event.EffectCallbacks.Started {
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
        addAttributeModifier(Attributes.ATTACK_DAMAGE, net.per.primogemcraft.compat.attribute.AttributesCompat.uuid(id("attack_damage")).toString(), ATTACK_DAMAGE, AttributeModifier.Operation.MULTIPLY_BASE);
        addAttributeModifier(Attributes.ATTACK_SPEED, net.per.primogemcraft.compat.attribute.AttributesCompat.uuid(id("attack_speed")).toString(), ATTACK_SPEED, AttributeModifier.Operation.MULTIPLY_BASE);
        addAttributeModifier(net.minecraftforge.common.ForgeMod.SWIM_SPEED.get(), net.per.primogemcraft.compat.attribute.AttributesCompat.uuid(id("swim_speed")).toString(), SWIM_SPEED, AttributeModifier.Operation.MULTIPLY_BASE);
        addAttributeModifier(Attributes.ARMOR, net.per.primogemcraft.compat.attribute.AttributesCompat.uuid(id("armor")).toString(), ARMOR, AttributeModifier.Operation.MULTIPLY_BASE);
    }

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
        level.playSound(null, entity.blockPosition(), SoundEvents.TRIDENT_THUNDER, SoundSource.PLAYERS, THUNDER_VOLUME, THUNDER_PITCH);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier)
    {
        entity.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, FIRE_RESISTANCE_DURATION, FIRE_RESISTANCE_AMPLIFIER, false, false));
        return;
    }

    private static ResourceLocation id(String name) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, "effect/magatama_" + name);
    }
}
