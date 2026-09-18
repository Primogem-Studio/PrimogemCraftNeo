package net.per.primogemcraft.system.curio;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public final class CurioAttributes {
    private static final Map<UUID, Map<ResourceLocation, Applied>> APPLIED = new HashMap<>();

    private CurioAttributes() {
    }

    public static void push(LivingEntity entity, ItemStack source, int index, Holder<Attribute> attribute, double amount, AttributeModifier.Operation operation) {
        var instance = entity.getAttribute(attribute);
        if (instance == null || source.isEmpty()) return;
        var id = idOf(source, index);
        instance.addOrUpdateTransientModifier(new AttributeModifier(id, amount, operation));
        APPLIED.computeIfAbsent(entity.getUUID(), key -> new HashMap<>()).put(id, new Applied(attribute, entity.tickCount));
    }

    public static void sweep(LivingEntity entity) {
        var applied = APPLIED.get(entity.getUUID());
        if (applied == null) return;
        var tick = entity.tickCount;
        applied.entrySet().removeIf(entry -> {
            if (entry.getValue().tick() == tick) return false;
            detach(entity, entry.getKey(), entry.getValue().attribute());
            return true;
        });
        if (applied.isEmpty()) APPLIED.remove(entity.getUUID());
    }

    public static void clear(LivingEntity entity) {
        var applied = APPLIED.remove(entity.getUUID());
        if (applied == null) return;
        for (var entry : applied.entrySet()) detach(entity, entry.getKey(), entry.getValue().attribute());
    }

    private static void detach(LivingEntity entity, ResourceLocation id, Holder<Attribute> attribute) {
        var instance = entity.getAttribute(attribute);
        if (instance != null) instance.removeModifier(id);
    }

    private static ResourceLocation idOf(ItemStack source, int index) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, "curio/" + BuiltInRegistries.ITEM.getKey(source.getItem()).getPath() + "/" + index);
    }

    private record Applied(Holder<Attribute> attribute, int tick) {
    }
}
