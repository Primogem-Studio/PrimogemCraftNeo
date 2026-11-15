package net.per.primogemcraft.system.curio;

import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.ItemStack;
import net.per.primogemcraft.compat.attribute.AttributesCompat;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public final class CurioAttributes {
    private static final Map<UUID, Map<Identifier, Applied>> APPLIED = new HashMap<>();

    private CurioAttributes() {
    }

    public static void push(LivingEntity entity, ItemStack source, int index, EntityAttribute attribute, double amount, EntityAttributeModifier.Operation operation) {
        var instance = entity.getAttributeInstance(attribute);
        if (instance == null || source.isEmpty()) return;
        var id = idOf(source, index);
        AttributesCompat.addOrUpdateTransient(instance, AttributesCompat.modifier(id, amount, operation));
        APPLIED.computeIfAbsent(entity.getUuid(), key -> new HashMap<>()).put(id, new Applied(attribute, entity.age));
    }

    public static void sweep(LivingEntity entity) {
        var applied = APPLIED.get(entity.getUuid());
        if (applied == null) return;
        var tick = entity.age;
        applied.entrySet().removeIf(entry -> {
            if (entry.getValue().tick() == tick) return false;
            detach(entity, entry.getKey(), entry.getValue().attribute());
            return true;
        });
        if (applied.isEmpty()) APPLIED.remove(entity.getUuid());
    }

    public static void clear(LivingEntity entity) {
        var applied = APPLIED.remove(entity.getUuid());
        if (applied == null) return;
        for (var entry : applied.entrySet()) detach(entity, entry.getKey(), entry.getValue().attribute());
    }

    private static void detach(LivingEntity entity, Identifier id, EntityAttribute attribute) {
        var instance = entity.getAttributeInstance(attribute);
        if (instance != null) AttributesCompat.removeModifier(instance, id);
    }

    private static Identifier idOf(ItemStack source, int index) {
        return Identifier.of(MOD_ID, "curio/" + Registries.ITEM.getId(source.getItem()).getPath() + "/" + index);
    }

    private record Applied(EntityAttribute attribute, int tick) {
    }
}
