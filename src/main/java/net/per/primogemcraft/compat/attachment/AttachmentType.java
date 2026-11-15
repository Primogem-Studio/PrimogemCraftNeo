package net.per.primogemcraft.compat.attachment;

import com.mojang.serialization.Codec;

import java.util.function.Supplier;

/**
 * 1.21 NeoForge {@code AttachmentType<T>} 的兼容替代。
 *
 * <p>NeoForge 的附加数据系统（Attachment）在 1.20.1 Forge 不存在。本类承载
 * 「默认值工厂 + 序列化 Codec + 死亡是否保留」，实际存取由
 * {@link Attachments} 落在实体的 Forge 持久化 NBT（{@code getPersistentData}）上，
 * 因此玩家存档数据能真正保存与恢复，而不是内存态临时值。
 */
public final class AttachmentType<T> {

    private final String name;
    private final Supplier<T> defaultValue;
    private final Codec<T> codec;
    private final boolean copyOnDeath;

    private AttachmentType(String name, Supplier<T> defaultValue, Codec<T> codec, boolean copyOnDeath) {
        this.name = name;
        this.defaultValue = defaultValue;
        this.codec = codec;
        this.copyOnDeath = copyOnDeath;
    }

    public static <T> Builder<T> builder(Supplier<T> defaultValue) {
        return new Builder<>(defaultValue);
    }

    /** 由注册表或本地持有者填入名称。 */
    public AttachmentType<T> named(String name) {
        return new AttachmentType<>(name, defaultValue, codec, copyOnDeath);
    }

    public String name() {
        return name;
    }

    public Supplier<T> defaultValue() {
        return defaultValue;
    }

    /** 为 null 表示该附加数据不持久化（仅运行时内存态）。 */
    public Codec<T> codec() {
        return codec;
    }

    public boolean copyOnDeath() {
        return copyOnDeath;
    }

    @Override
    public String toString() {
        return "AttachmentType[" + name + "]";
    }

    /** 对应 1.21 的 {@code AttachmentType.Builder}。 */
    public static final class Builder<T> {
        private final Supplier<T> defaultValue;
        private Codec<T> codec;
        private boolean copyOnDeath;

        private Builder(Supplier<T> defaultValue) {
            this.defaultValue = defaultValue;
        }

        public Builder<T> serialize(Codec<T> codec) {
            this.codec = codec;
            return this;
        }

        /** 1.21 的 serialize(codec, ops) 重载。 */
        public Builder<T> serialize(Codec<T> codec, Object ops) {
            this.codec = codec;
            return this;
        }

        public Builder<T> copyOnDeath() {
            this.copyOnDeath = true;
            return this;
        }

        /**
         * 1.21 用 StreamCodec 把附加数据同步给客户端。1.20.1 没有该通道，
         * 参数放宽为 Object 以保持调用点原样编译；同步语义由业务侧按需处理。
         */
        public Builder<T> sync(Object streamCodec) {
            return this;
        }

        public AttachmentType<T> build() {
            return new AttachmentType<>(null, defaultValue, codec, copyOnDeath);
        }
    }
}
