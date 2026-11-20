package net.per.primogemcraft.compat.event;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public final class EventDispatch {

    public interface DamageModifier {
        float modify(LivingEntity entity, DamageSource source, float amount);
    }

    public interface EffectExpired {
        void onExpired(LivingEntity entity, StatusEffectInstance instance);
    }

    public interface ItemPickup {
        void onPickup(net.minecraft.entity.player.PlayerEntity player, net.minecraft.entity.ItemEntity item);
    }

    /** 替代 Forge 的 LivingDropsEvent：向生物掉落列表追加物品。 */
    public interface LivingDrops {
        void onDrops(LivingEntity entity, DamageSource source, List<net.minecraft.entity.ItemEntity> drops);
    }

    private static final List<EffectExpired> EFFECT_EXPIRED = new CopyOnWriteArrayList<>();
    private static final List<DamageModifier> DAMAGE_MODIFIERS = new CopyOnWriteArrayList<>();
    private static final List<ItemPickup> ITEM_PICKUPS = new CopyOnWriteArrayList<>();
    private static final List<LivingDrops> LIVING_DROPS = new CopyOnWriteArrayList<>();

    private EventDispatch() {
    }

    public static void onEffectExpired(EffectExpired listener) {
        EFFECT_EXPIRED.add(listener);
    }

    public static void onDamageModify(DamageModifier modifier) {
        DAMAGE_MODIFIERS.add(modifier);
    }

    public static void fireEffectExpired(LivingEntity entity, StatusEffectInstance instance) {
        for (var listener : EFFECT_EXPIRED) listener.onExpired(entity, instance);
    }

    public static float fireDamageModify(LivingEntity entity, DamageSource source, float amount) {
        var result = amount;
        for (var modifier : DAMAGE_MODIFIERS) result = modifier.modify(entity, source, result);
        return result;
    }

    public static void onItemPickup(ItemPickup listener) {
        ITEM_PICKUPS.add(listener);
    }

    public static void fireItemPickup(net.minecraft.entity.player.PlayerEntity player, net.minecraft.entity.ItemEntity item) {
        for (var listener : ITEM_PICKUPS) listener.onPickup(player, item);
    }

    public static void onLivingDrops(LivingDrops listener) {
        LIVING_DROPS.add(listener);
    }

    public static void fireLivingDrops(LivingEntity entity, DamageSource source, List<net.minecraft.entity.ItemEntity> drops) {
        for (var listener : LIVING_DROPS) listener.onDrops(entity, source, drops);
    }
}