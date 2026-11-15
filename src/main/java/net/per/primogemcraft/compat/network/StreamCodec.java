package net.per.primogemcraft.compat.network;

import com.mojang.datafixers.util.Function3;
import com.mojang.datafixers.util.Function4;
import com.mojang.datafixers.util.Function5;
import com.mojang.datafixers.util.Function6;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * 1.21 {@code StreamCodec} 的兼容实现。
 *
 * <p>1.20.1 Forge 的网络是 {@code SimpleChannel} + 手写 encode/decode，没有
 * StreamCodec 体系。本类复刻 1.21 的接口形状（{@code composite}/{@code of}/
 * {@code unit}/{@code map}/{@code apply}），使项目中 19 个 {@code STREAM_CODEC}
 * 定义和 10 个载荷类可以原样保留，实际收发由 {@link SimpleChannelBridge}
 * 在 Forge 的 {@code FriendlyByteBuf} 上执行。
 *
 * <p>签名逐项对照 1.21.1 NeoForge 的 {@code StreamCodec} 实测结果，
 * 其中 {@code Function3}~{@code Function6} 来自 datafixerupper（已确认可用）。
 */
public interface StreamCodec<B, V> extends StreamDecoder<B, V>, StreamEncoder<B, V> {

    // ------------------------------------------------------------ 组合子

    /** 对应 1.21 的 {@code StreamCodec.of(encoder, decoder)}。 */
    static <B, V> StreamCodec<B, V> of(StreamEncoder<B, V> encoder, StreamDecoder<B, V> decoder) {
        return new StreamCodec<B, V>() {
            @Override
            public void encode(B buffer, V value) {
                encoder.encode(buffer, value);
            }

            @Override
            public V decode(B buffer) {
                return decoder.decode(buffer);
            }
        };
    }

    /** 对应 1.21 的 {@code StreamCodec.unit(value)}：不占任何字节。 */
    static <B, V> StreamCodec<B, V> unit(V value) {
        return new StreamCodec<B, V>() {
            @Override
            public void encode(B buffer, V ignored) {
            }

            @Override
            public V decode(B buffer) {
                return value;
            }
        };
    }

    // ------------------------------------------------------------ map / apply

    default <O> StreamCodec<B, O> map(Function<? super V, ? extends O> decoder,
                                      Function<? super O, ? extends V> encoder) {
        StreamCodec<B, V> self = this;
        return new StreamCodec<B, O>() {
            @Override
            public void encode(B buffer, O value) {
                self.encode(buffer, encoder.apply(value));
            }

            @Override
            public O decode(B buffer) {
                return decoder.apply(self.decode(buffer));
            }
        };
    }

    /** 对应 1.21 的 {@code apply(CodecOperation)}，用于 {@code list()} / {@code map()}。 */
    default <O> StreamCodec<B, O> apply(CodecOperation<B, V, O> operation) {
        return operation.apply(this);
    }

    /** 对应 1.21 的 {@code StreamCodec.CodecOperation}。 */
    @FunctionalInterface
    interface CodecOperation<B, V, O> {
        StreamCodec<B, O> apply(StreamCodec<B, V> codec);
    }

    // ------------------------------------------------------------ composite

    static <B, C, T1> StreamCodec<B, C> composite(
            StreamCodec<? super B, T1> codec1, Function<C, T1> getter1,
            Function<T1, C> factory) {
        return new StreamCodec<B, C>() {
            @Override
            public void encode(B buffer, C value) {
                write(codec1, buffer, getter1.apply(value));
            }

            @Override
            public C decode(B buffer) {
                T1 v1 = read(codec1, buffer);
                return factory.apply(v1);
            }
        };
    }

    static <B, C, T1, T2> StreamCodec<B, C> composite(
            StreamCodec<? super B, T1> codec1, Function<C, T1> getter1,
            StreamCodec<? super B, T2> codec2, Function<C, T2> getter2,
            BiFunction<T1, T2, C> factory) {
        return new StreamCodec<B, C>() {
            @Override
            public void encode(B buffer, C value) {
                write(codec1, buffer, getter1.apply(value));
                write(codec2, buffer, getter2.apply(value));
            }

            @Override
            public C decode(B buffer) {
                T1 v1 = read(codec1, buffer);
                T2 v2 = read(codec2, buffer);
                return factory.apply(v1, v2);
            }
        };
    }

    static <B, C, T1, T2, T3> StreamCodec<B, C> composite(
            StreamCodec<? super B, T1> codec1, Function<C, T1> getter1,
            StreamCodec<? super B, T2> codec2, Function<C, T2> getter2,
            StreamCodec<? super B, T3> codec3, Function<C, T3> getter3,
            Function3<T1, T2, T3, C> factory) {
        return new StreamCodec<B, C>() {
            @Override
            public void encode(B buffer, C value) {
                write(codec1, buffer, getter1.apply(value));
                write(codec2, buffer, getter2.apply(value));
                write(codec3, buffer, getter3.apply(value));
            }

            @Override
            public C decode(B buffer) {
                T1 v1 = read(codec1, buffer);
                T2 v2 = read(codec2, buffer);
                T3 v3 = read(codec3, buffer);
                return factory.apply(v1, v2, v3);
            }
        };
    }

    static <B, C, T1, T2, T3, T4> StreamCodec<B, C> composite(
            StreamCodec<? super B, T1> codec1, Function<C, T1> getter1,
            StreamCodec<? super B, T2> codec2, Function<C, T2> getter2,
            StreamCodec<? super B, T3> codec3, Function<C, T3> getter3,
            StreamCodec<? super B, T4> codec4, Function<C, T4> getter4,
            Function4<T1, T2, T3, T4, C> factory) {
        return new StreamCodec<B, C>() {
            @Override
            public void encode(B buffer, C value) {
                write(codec1, buffer, getter1.apply(value));
                write(codec2, buffer, getter2.apply(value));
                write(codec3, buffer, getter3.apply(value));
                write(codec4, buffer, getter4.apply(value));
            }

            @Override
            public C decode(B buffer) {
                T1 v1 = read(codec1, buffer);
                T2 v2 = read(codec2, buffer);
                T3 v3 = read(codec3, buffer);
                T4 v4 = read(codec4, buffer);
                return factory.apply(v1, v2, v3, v4);
            }
        };
    }

    static <B, C, T1, T2, T3, T4, T5> StreamCodec<B, C> composite(
            StreamCodec<? super B, T1> codec1, Function<C, T1> getter1,
            StreamCodec<? super B, T2> codec2, Function<C, T2> getter2,
            StreamCodec<? super B, T3> codec3, Function<C, T3> getter3,
            StreamCodec<? super B, T4> codec4, Function<C, T4> getter4,
            StreamCodec<? super B, T5> codec5, Function<C, T5> getter5,
            Function5<T1, T2, T3, T4, T5, C> factory) {
        return new StreamCodec<B, C>() {
            @Override
            public void encode(B buffer, C value) {
                write(codec1, buffer, getter1.apply(value));
                write(codec2, buffer, getter2.apply(value));
                write(codec3, buffer, getter3.apply(value));
                write(codec4, buffer, getter4.apply(value));
                write(codec5, buffer, getter5.apply(value));
            }

            @Override
            public C decode(B buffer) {
                T1 v1 = read(codec1, buffer);
                T2 v2 = read(codec2, buffer);
                T3 v3 = read(codec3, buffer);
                T4 v4 = read(codec4, buffer);
                T5 v5 = read(codec5, buffer);
                return factory.apply(v1, v2, v3, v4, v5);
            }
        };
    }

    static <B, C, T1, T2, T3, T4, T5, T6> StreamCodec<B, C> composite(
            StreamCodec<? super B, T1> codec1, Function<C, T1> getter1,
            StreamCodec<? super B, T2> codec2, Function<C, T2> getter2,
            StreamCodec<? super B, T3> codec3, Function<C, T3> getter3,
            StreamCodec<? super B, T4> codec4, Function<C, T4> getter4,
            StreamCodec<? super B, T5> codec5, Function<C, T5> getter5,
            StreamCodec<? super B, T6> codec6, Function<C, T6> getter6,
            Function6<T1, T2, T3, T4, T5, T6, C> factory) {
        return new StreamCodec<B, C>() {
            @Override
            public void encode(B buffer, C value) {
                write(codec1, buffer, getter1.apply(value));
                write(codec2, buffer, getter2.apply(value));
                write(codec3, buffer, getter3.apply(value));
                write(codec4, buffer, getter4.apply(value));
                write(codec5, buffer, getter5.apply(value));
                write(codec6, buffer, getter6.apply(value));
            }

            @Override
            public C decode(B buffer) {
                T1 v1 = read(codec1, buffer);
                T2 v2 = read(codec2, buffer);
                T3 v3 = read(codec3, buffer);
                T4 v4 = read(codec4, buffer);
                T5 v5 = read(codec5, buffer);
                T6 v6 = read(codec6, buffer);
                return factory.apply(v1, v2, v3, v4, v5, v6);
            }
        };
    }

    // ------------------------------------------------------------ 通配捕获辅助

    @SuppressWarnings("unchecked")
    private static <B, T> void write(StreamCodec<? super B, T> codec, B buffer, T value) {
        ((StreamCodec<B, T>) codec).encode(buffer, value);
    }

    @SuppressWarnings("unchecked")
    private static <B, T> T read(StreamCodec<? super B, T> codec, B buffer) {
        return ((StreamCodec<B, T>) codec).decode(buffer);
    }
}
