package net.per.primogemcraft.util;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.util.Identifier;
import net.per.primogemcraft.compat.attribute.AttributesCompat;

import java.util.HashMap;
import java.util.Map;

public final class TemporaryAttributes {
    private static final Map<LivingEntity, Map<Identifier, Applied>> APPLIED = new HashMap<>();

    private TemporaryAttributes() {
    }

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            APPLIED.entrySet().removeIf(entityEntry -> {
                var entity = entityEntry.getKey();
                entityEntry.getValue().entrySet().removeIf(entry -> {
                    if (--entry.getValue().remaining > 0) return false;
                    detach(entity, entry.getKey(), entry.getValue().attribute);
                    return true;
                });
                return entityEntry.getValue().isEmpty();
            });
        });
    }

    public static void apply(LivingEntity entity, Identifier id, EntityAttribute attribute, double amount, EntityAttributeModifier.Operation operation, int ticks) {
        var instance = entity.getAttributeInstance(attribute);
        if (instance == null) return;
        AttributesCompat.addOrUpdateTransient(instance, AttributesCompat.modifier(id, amount, operation));
        APPLIED.computeIfAbsent(entity, key -> new HashMap<>()).put(id, new Applied(attribute, ticks));
    }

    public static void remove(LivingEntity entity, Identifier id) {
        var applied = APPLIED.get(entity);
        if (applied == null) return;
        var removed = applied.remove(id);
        if (removed == null) return;
        detach(entity, id, removed.attribute);
        if (applied.isEmpty()) APPLIED.remove(entity);
    }

    public static void clear(LivingEntity entity) {
        var applied = APPLIED.remove(entity);
        if (applied == null) return;
        for (var entry : applied.entrySet()) detach(entity, entry.getKey(), entry.getValue().attribute);
    }

    private static void detach(LivingEntity entity, Identifier id, EntityAttribute attribute) {
        var instance = entity.getAttributeInstance(attribute);
        if (instance != null) AttributesCompat.removeModifier(instance, id);
    }

    private static final class Applied {
        private final EntityAttribute attribute;
        private int remaining;

        private Applied(EntityAttribute attribute, int remaining) {
            this.attribute = attribute;
            this.remaining = remaining;
        }
    }
}