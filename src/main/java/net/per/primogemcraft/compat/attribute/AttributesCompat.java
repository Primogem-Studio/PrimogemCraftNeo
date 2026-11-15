package net.per.primogemcraft.compat.attribute;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * 1.21 属性修饰器 API 到 1.20.1 的桥接。
 *
 * <p>1.21 用 {@code ResourceLocation} 标识修饰器、枚举名为
 * {@code ADD_VALUE}/{@code ADD_MULTIPLIED_BASE}/{@code ADD_MULTIPLIED_TOTAL}，
 * 并提供 {@code addOrUpdateTransientModifier}；1.20.1 用 {@code UUID} 标识、
 * 枚举名为 {@code ADDITION}/{@code MULTIPLY_BASE}/{@code MULTIPLY_TOTAL}，
 * 且只有 {@code addTransientModifier}。
 *
 * <p>本类把 ResourceLocation 确定性地映射为 UUID（同一 id 始终得到同一 UUID），
 * 使「按 id 移除/更新修饰器」的语义与 1.21 一致。
 */
public final class AttributesCompat {

    private AttributesCompat() {
    }

    /** 把 1.21 的修饰器 id 确定性映射为 1.20.1 需要的 UUID。 */
    public static UUID uuid(ResourceLocation id) {
        return UUID.nameUUIDFromBytes(id.toString().getBytes(StandardCharsets.UTF_8));
    }

    /** 对应 1.21 的 {@code new AttributeModifier(ResourceLocation, double, Operation)}。 */
    public static AttributeModifier modifier(ResourceLocation id, double amount, AttributeModifier.Operation operation) {
        return new AttributeModifier(uuid(id), id.toString(), amount, operation);
    }

    /** 对应 1.21 的 {@code new AttributeModifier(String, double, Operation)}（无 id 的匿名修饰器）。 */
    public static AttributeModifier modifier(String name, double amount, AttributeModifier.Operation operation) {
        return new AttributeModifier(name, amount, operation);
    }

    /**
     * 对应 1.21 的 {@code instance.addOrUpdateTransientModifier(modifier)}：
     * 先按 UUID 移除旧值再添加，保证同一 id 不会叠加。
     */
    public static void addOrUpdateTransient(AttributeInstance instance, AttributeModifier modifier) {
        instance.removeModifier(modifier.getId());
        instance.addTransientModifier(modifier);
    }

    /** 对应 1.21 的 {@code instance.removeModifier(ResourceLocation)}。 */
    public static void removeModifier(AttributeInstance instance, ResourceLocation id) {
        instance.removeModifier(uuid(id));
    }

    /** 1.21 的 {@code Attributes.X} 是 {@code Holder<Attribute>}，1.20.1 是裸 {@code Attribute}。 */
    public static Attribute attribute(Holder<Attribute> holder) {
        return holder.value();
    }

    /** 1.20.1 的 {@code getAttribute} 接受裸 Attribute；此重载兼容传入 Holder 的调用点。 */
    public static AttributeInstance getAttribute(net.minecraft.world.entity.LivingEntity entity, Holder<Attribute> attribute) {
        return entity.getAttribute(attribute.value());
    }
}
