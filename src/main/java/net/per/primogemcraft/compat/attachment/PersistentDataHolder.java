package net.per.primogemcraft.compat.attachment;

import net.minecraft.nbt.NbtCompound;

/**
 * 实体持久化数据的 duck interface，由 {@code EntityPersistentDataMixin} 实现。
 *
 * <p>必须放在 mixin 包之外：Mixin 禁止直接引用 mixin 包内的类型，
 * 否则加载时会抛 IllegalClassLoadError。
 */
public interface PersistentDataHolder {
    NbtCompound primogemcraft$getPersistentData();
}