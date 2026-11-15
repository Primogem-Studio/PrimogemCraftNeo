package net.per.primogemcraft.system.element.effect;

import net.minecraft.util.Identifier;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.sound.SoundCategory;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.util.math.Vec3d;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public class MagatamaEffect extends StatusEffect implements net.per.primogemcraft.compat.event.EffectCallbacks.Started {
    private static final double ATTACK_DAMAGE = 0.3D;
    private static final double ATTACK_SPEED = 0.3D;
    private static final double SWIM_SPEED = 0.3D;
    private static final double ARMOR = 0.3D;
    private static final int FIRE_RESISTANCE_DURATION = 60;
    private static final int FIRE_RESISTANCE_AMPLIFIER = 1;
    private static final float THUNDER_VOLUME = 3.0F;
    private static final float THUNDER_PITCH = 0.5F;

    public MagatamaEffect() {
        super(StatusEffectCategory.NEUTRAL, -1878800);
        addAttributeModifier(EntityAttributes.GENERIC_ATTACK_DAMAGE, net.per.primogemcraft.compat.attribute.AttributesCompat.uuid(id("attack_damage")).toString(), ATTACK_DAMAGE, EntityAttributeModifier.Operation.MULTIPLY_BASE);
        addAttributeModifier(EntityAttributes.GENERIC_ATTACK_SPEED, net.per.primogemcraft.compat.attribute.AttributesCompat.uuid(id("attack_speed")).toString(), ATTACK_SPEED, EntityAttributeModifier.Operation.MULTIPLY_BASE);
        addAttributeModifier(net.per.primogemcraft.compat.attribute.AttributesCompat.SWIM_SPEED.get(), net.per.primogemcraft.compat.attribute.AttributesCompat.uuid(id("swim_speed")).toString(), SWIM_SPEED, EntityAttributeModifier.Operation.MULTIPLY_BASE);
        addAttributeModifier(EntityAttributes.GENERIC_ARMOR, net.per.primogemcraft.compat.attribute.AttributesCompat.uuid(id("armor")).toString(), ARMOR, EntityAttributeModifier.Operation.MULTIPLY_BASE);
    }

    public void onEffectStarted(LivingEntity entity, int amplifier) {
        var level = entity.getWorld();
        if (level instanceof ServerWorld serverLevel) {
            var bolt = EntityType.LIGHTNING_BOLT.create(serverLevel);
            if (bolt != null) {
                bolt.refreshPositionAfterTeleport(Vec3d.ofBottomCenter(entity.getBlockPos()));
                bolt.setCosmetic(true);
                serverLevel.spawnEntity(bolt);
            }
        }
        level.playSound(null, entity.getBlockPos(), SoundEvents.ITEM_TRIDENT_THUNDER, SoundCategory.PLAYERS, THUNDER_VOLUME, THUNDER_PITCH);
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return true;
    }

    @Override
    public void applyUpdateEffect(LivingEntity entity, int amplifier)
    {
        entity.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, FIRE_RESISTANCE_DURATION, FIRE_RESISTANCE_AMPLIFIER, false, false));
        return;
    }

    private static Identifier id(String name) {
        return Identifier.of(MOD_ID, "effect/magatama_" + name);
    }
}
