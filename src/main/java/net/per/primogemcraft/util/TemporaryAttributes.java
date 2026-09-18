package net.per.primogemcraft.util;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.HashMap;
import java.util.Map;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

@EventBusSubscriber(modid = MOD_ID)
public final class TemporaryAttributes {
    private static final Map<LivingEntity, Map<ResourceLocation, Applied>> APPLIED = new HashMap<>();

    private TemporaryAttributes() {
    }

    public static void apply(LivingEntity entity, ResourceLocation id, Holder<Attribute> attribute, double amount, AttributeModifier.Operation operation, int ticks) {
        var instance = entity.getAttribute(attribute);
        if (instance == null) return;
        instance.addOrUpdateTransientModifier(new AttributeModifier(id, amount, operation));
        APPLIED.computeIfAbsent(entity, key -> new HashMap<>()).put(id, new Applied(attribute, ticks));
    }

    public static void remove(LivingEntity entity, ResourceLocation id) {
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

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        APPLIED.entrySet().removeIf(entity -> {
            entity.getValue().entrySet().removeIf(entry -> {
                if (--entry.getValue().remaining > 0) return false;
                detach(entity.getKey(), entry.getKey(), entry.getValue().attribute);
                return true;
            });
            return entity.getValue().isEmpty();
        });
    }

    private static void detach(LivingEntity entity, ResourceLocation id, Holder<Attribute> attribute) {
        var instance = entity.getAttribute(attribute);
        if (instance != null) instance.removeModifier(id);
    }

    private static final class Applied {
        private final Holder<Attribute> attribute;
        private int remaining;

        private Applied(Holder<Attribute> attribute, int remaining) {
            this.attribute = attribute;
            this.remaining = remaining;
        }
    }
}
