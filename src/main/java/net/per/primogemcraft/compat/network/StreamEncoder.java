package net.per.primogemcraft.compat.network;

/**
 * 1.21 {@code StreamEncoder} 的兼容替代。
 */
public interface StreamEncoder<B, V> {
    void encode(B buffer, V value);
}
