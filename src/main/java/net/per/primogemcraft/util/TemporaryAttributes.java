package net.per.primogemcraft.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.TickEvent;
import net.per.primogemcraft.compat.attribute.AttributesCompat;

import java.util.HashMap;
import java.util.Map;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

/**
 * 有时限的临时属性加成。
 *
 * <p>1.21 用 {@code addOrUpdateTransientModifier(ResourceLocation 版修饰器)}；
 * 1.20.1 改为经 {@link AttributesCompat} 把 id 映射为 UUID 后先移除再添加，
 * 保证同一 id 不叠加。到期由服务端 tick 统一回收。
 */
@Mod.EventBusSubscriber(modid = MOD_ID)
public final class TemporaryAttributes {
    private static final Map<LivingEntity, Map<ResourceLocation, Applied>> APPLIED = new HashMap<>();

    private TemporaryAttributes() {
    }

    public static void apply(LivingEntity entity, ResourceLocation id, Attribute attribute, double amount, AttributeModifier.Operation operation, int ticks) {
        var instance = entity.getAttribute(attribute);
        if (instance == null) return;
        AttributesCompat.addOrUpdateTransient(instance, AttributesCompat.modifier(id, amount, operation));
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
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        APPLIED.entrySet().removeIf(entityEntry -> {
            var entity = entityEntry.getKey();
            entityEntry.getValue().entrySet().removeIf(entry -> {
                if (--entry.getValue().remaining > 0) return false;
                detach(entity, entry.getKey(), entry.getValue().attribute);
                return true;
            });
            return entityEntry.getValue().isEmpty();
        });
    }

    private static void detach(LivingEntity entity, ResourceLocation id, Attribute attribute) {
        var instance = entity.getAttribute(attribute);
        if (instance != null) AttributesCompat.removeModifier(instance, id);
    }

    private static final class Applied {
        private final Attribute attribute;
        private int remaining;

        private Applied(Attribute attribute, int remaining) {
            this.attribute = attribute;
            this.remaining = remaining;
        }
    }
}
