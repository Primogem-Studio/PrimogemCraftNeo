package net.per.primogemcraft.compat.attribute;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public final class AttributesCompat {

    /** Forge-only attributes stubbed for 1.20.1 Fabric (no equivalent in vanilla). */
    public static final java.util.function.Supplier<EntityAttribute> SWIM_SPEED = () -> net.minecraft.entity.attribute.EntityAttributes.GENERIC_MOVEMENT_SPEED;
    public static final java.util.function.Supplier<EntityAttribute> ENTITY_REACH = () -> net.minecraft.entity.attribute.EntityAttributes.GENERIC_ATTACK_DAMAGE;
    public static final java.util.function.Supplier<EntityAttribute> STEP_HEIGHT_ADDITION = () -> net.minecraft.entity.attribute.EntityAttributes.GENERIC_MOVEMENT_SPEED;

    private AttributesCompat() {
    }

    public static UUID uuid(Identifier id) {
        return UUID.nameUUIDFromBytes(id.toString().getBytes(StandardCharsets.UTF_8));
    }

    public static EntityAttributeModifier modifier(Identifier id, double amount, EntityAttributeModifier.Operation operation) {
        return new EntityAttributeModifier(uuid(id), id.toString(), amount, operation);
    }

    public static EntityAttributeModifier modifier(String name, double amount, EntityAttributeModifier.Operation operation) {
        return new EntityAttributeModifier(name, amount, operation);
    }

    public static void addOrUpdateTransient(EntityAttributeInstance instance, EntityAttributeModifier modifier) {
        instance.removeModifier(modifier.getId());
        instance.addTemporaryModifier(modifier);
    }

    public static void removeModifier(EntityAttributeInstance instance, Identifier id) {
        instance.removeModifier(uuid(id));
    }

    public static EntityAttribute attribute(RegistryEntry<EntityAttribute> holder) {
        return holder.value();
    }

    public static EntityAttributeInstance getAttribute(LivingEntity entity, RegistryEntry<EntityAttribute> attribute) {
        return entity.getAttributeInstance(attribute.value());
    }
}