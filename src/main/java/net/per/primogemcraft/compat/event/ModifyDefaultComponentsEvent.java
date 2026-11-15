package net.per.primogemcraft.compat.event;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.per.primogemcraft.compat.component.DataComponentType;
import net.per.primogemcraft.compat.component.StackComponents;

/**
 * 1.21 {@code ModifyDefaultComponentsEvent} 的兼容替代。
 *
 * <p>1.21 用该事件为物品（含其他模组的物品）设置默认数据组件。1.20.1 没有数据组件，
 * 也没有等价的「修改任意物品默认值」事件——默认值只能在物品实例创建时写入。
 *
 * <p>因此本实现把 {@code modify(item, ...)} 记录为「该物品的默认组件值」，
 * 由 {@link StackComponents#getOrDefault} 在读取时作为兜底返回，
 * 从而在不改动其他模组物品的前提下保留原有默认值语义。
 */
public final class ModifyDefaultComponentsEvent {

    private ModifyDefaultComponentsEvent() {
    }

    /**
     * 对应 1.21 的 {@code event.modify(item, builder -> builder.set(type, value))}。
     *
     * <p>这里把默认值登记到 {@link DefaultComponents}，供物品读取时兜底。
     */
    public static <T> void modify(Item item, DataComponentType<T> type, T value) {
        DefaultComponents.put(item, type, value);
    }

    /** 供组件读取兜底使用的默认值表。 */
    public static final class DefaultComponents {
        private static final java.util.Map<String, Object> DEFAULTS = new java.util.concurrent.ConcurrentHashMap<>();

        private DefaultComponents() {
        }

        static <T> void put(Item item, DataComponentType<T> type, T value) {
            DEFAULTS.put(key(item, type), value);
        }

        @SuppressWarnings("unchecked")
        public static <T> T get(Item item, DataComponentType<T> type) {
            return (T) DEFAULTS.get(key(item, type));
        }

        private static String key(Item item, DataComponentType<?> type) {
            return net.minecraft.registry.Registries.ITEM.getId(item) + "|" + type.name();
        }
    }

    /** 便于调用点保留 ItemStack 形式。 */
    public static <T> void modify(ItemStack stack, DataComponentType<T> type, T value) {
        modify(stack.getItem(), type, value);
    }
}
