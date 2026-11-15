package net.per.primogemcraft.compat.network;

import net.minecraft.util.Identifier;

/**
 * 1.21 {@code CustomPacketPayload} 的兼容替代。
 *
 * <p>1.21 要求每个载荷实现该接口并提供 {@code type()} 与 {@code StreamCodec}；
 * 1.20.1 Forge 的 {@code SimpleChannel} 按 {@code Class} 注册与派发载荷，
 * 因此 {@link Type} 除标识外还携带载荷类型，供 {@link PayloadRegistrar} 注册时使用。
 * 除此之外本接口只保留形态，使 10 个载荷 record 的 {@code implements} 子句可以原样保留。
 */
public interface CustomPacketPayload {

    /** 对应 1.21 的 {@code CustomPacketPayload.Type<T>}，额外携带载荷 Class。 */
    final class Type<T extends CustomPacketPayload> {
        private final Identifier id;
        private final Class<T> payloadClass;

        public Type(Identifier id, Class<T> payloadClass) {
            this.id = id;
            this.payloadClass = payloadClass;
        }

        public Identifier id() {
            return id;
        }

        /** Forge 的 SimpleChannel 以 Class 作为派发键。 */
        public Class<T> payloadClass() {
            return payloadClass;
        }

        @Override
        public String toString() {
            return "PayloadType[" + id + "]";
        }

        @Override
        public int hashCode() {
            return id.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof Type<?> other && id.equals(other.id);
        }
    }

    Type<? extends CustomPacketPayload> type();
}
