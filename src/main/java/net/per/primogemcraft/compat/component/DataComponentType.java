package net.per.primogemcraft.compat.component;

import com.mojang.serialization.Codec;

/**
 * 1.21 {@code DataComponentType<T>} 的兼容替代。
 *
 * <p>1.20.1 没有数据组件系统，物品附加数据统一存放在 ItemStack 的 NBT 里。
 * 本类只承载「名称 + Codec」，真正的读写由 {@link StackComponents} 通过
 * {@code NbtOps} 完成，因此原有各组件的 {@code Codec} 定义可以原样复用。
 */
public final class DataComponentType<T> {

    private final String name;
    private final Codec<T> codec;
    private final boolean networkSynchronized;

    DataComponentType(String name, Codec<T> codec, boolean networkSynchronized) {
        this.name = name;
        this.codec = codec;
        this.networkSynchronized = networkSynchronized;
    }

    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    public String name() {
        return name;
    }

    public Codec<T> codec() {
        return codec;
    }

    /** 1.21 用它决定组件是否同步给客户端；1.20.1 里 NBT 随物品自然同步，此值仅作记录。 */
    public boolean isNetworkSynchronized() {
        return networkSynchronized;
    }

    @Override
    public String toString() {
        return "DataComponentType[" + name + "]";
    }

    /** 对应 1.21 的 {@code DataComponentType.Builder}。 */
    public static final class Builder<T> {
        private Codec<T> codec;
        private boolean networkSynchronized;
        private String name;

        private Builder() {
        }

        /** 设置组件名（NBT 子标签键）。 */
        public Builder<T> name(String name) {
            this.name = name;
            return this;
        }

        /** 1.21: {@code persistent(Codec)} —— 持久化到磁盘，这里记录用于 NBT 读写。 */
        public Builder<T> persistent(Codec<T> codec) {
            this.codec = codec;
            return this;
        }

        /**
         * 1.21: {@code networkSynchronized(StreamCodec)}。1.20.1 的 NBT 随物品自动同步，
         * 不再需要 StreamCodec，参数类型放宽为 Object 以便调用点原样保留编译。
         */
        public Builder<T> networkSynchronized(Object streamCodec) {
            this.networkSynchronized = true;
            return this;
        }

        public DataComponentType<T> build() {
            if (codec == null) {
                throw new IllegalStateException("DataComponentType requires persistent(codec): " + name);
            }
            return new DataComponentType<>(name, codec, networkSynchronized);
        }
    }
}
