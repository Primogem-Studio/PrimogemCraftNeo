package net.per.primogemcraft.compat.network;

/**
 * 1.21 {@code StreamDecoder} 的兼容替代。
 */
public interface StreamDecoder<B, V> {
    V decode(B buffer);
}
