package net.per.primogemcraft.compat.network;

import net.minecraft.util.math.BlockPos;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.IntFunction;

/**
 * 1.21 {@code ByteBufCodecs} 的兼容实现。
 *
 * <p>底层缓冲区统一为 1.20.1 的 {@link FriendlyByteBuf}，读写方法名取自该版本的
 * 实际 API。只实现项目用到的编解码器。
 *
 * <p>{@link #list()} 与 {@link #collection(IntFunction)} 按 1.21 的真实签名返回
 * {@code CodecOperation}，因此 {@code ELEMENT_CODEC.apply(ByteBufCodecs.list())}
 * 这类写法可以原样保留；线格式与 1.21 一致（varint 长度 + 逐个元素），
 * 元素编解码器由 {@code apply} 显式传入，无需运行时类型分派。
 */
public interface ByteBufCodecs {

    StreamCodec<PacketByteBuf, Integer> VAR_INT = StreamCodec.of(
            (buf, value) -> buf.writeVarInt(value), PacketByteBuf::readVarInt);

    StreamCodec<PacketByteBuf, Long> VAR_LONG = StreamCodec.of(
            (buf, value) -> buf.writeVarLong(value), PacketByteBuf::readVarLong);

    StreamCodec<PacketByteBuf, Boolean> BOOL = StreamCodec.of(
            (buf, value) -> buf.writeBoolean(value), PacketByteBuf::readBoolean);

    StreamCodec<PacketByteBuf, Double> DOUBLE = StreamCodec.of(
            (buf, value) -> buf.writeDouble(value), PacketByteBuf::readDouble);

    StreamCodec<PacketByteBuf, Float> FLOAT = StreamCodec.of(
            (buf, value) -> buf.writeFloat(value), PacketByteBuf::readFloat);

    StreamCodec<PacketByteBuf, Integer> INT = StreamCodec.of(
            (buf, value) -> buf.writeInt(value), PacketByteBuf::readInt);

    StreamCodec<PacketByteBuf, Byte> BYTE = StreamCodec.of(
            (buf, value) -> buf.writeByte(value), PacketByteBuf::readByte);

    StreamCodec<PacketByteBuf, Short> SHORT = StreamCodec.of(
            (buf, value) -> buf.writeShort(value), PacketByteBuf::readShort);

    StreamCodec<PacketByteBuf, String> STRING_UTF8 = StreamCodec.of(
            PacketByteBuf::writeString, PacketByteBuf::readString);

    StreamCodec<PacketByteBuf, Identifier> RESOURCE_LOCATION = StreamCodec.of(
            PacketByteBuf::writeIdentifier, PacketByteBuf::readIdentifier);

    StreamCodec<PacketByteBuf, BlockPos> BLOCK_POS = StreamCodec.of(
            PacketByteBuf::writeBlockPos, PacketByteBuf::readBlockPos);

    StreamCodec<PacketByteBuf, UUID> UUID = StreamCodec.of(
            PacketByteBuf::writeUuid, PacketByteBuf::readUuid);

    /** 物品堆。1.21 同步组件数据，1.20.1 同步 NBT——组件数据已落在 NBT 中，语义等价。 */
    StreamCodec<PacketByteBuf, ItemStack> ITEM_STACK = StreamCodec.of(
            PacketByteBuf::writeItemStack, PacketByteBuf::readItemStack);

    StreamCodec<PacketByteBuf, NbtCompound> COMPOUND_TAG = StreamCodec.of(
            PacketByteBuf::writeNbt, PacketByteBuf::readNbt);

    // ------------------------------------------------------------ 集合 / 映射

    /**
     * 对应 1.21 的 {@code ByteBufCodecs.list()}：返回 {@code CodecOperation}，
     * 由 {@code elementCodec.apply(ByteBufCodecs.list())} 组合出集合编解码器。
     */
    static <V> StreamCodec.CodecOperation<PacketByteBuf, V, List<V>> list() {
        return collection(ArrayList::new);
    }

    static <V, C extends Collection<V>> StreamCodec.CodecOperation<PacketByteBuf, V, C> collection(
            IntFunction<C> factory) {
        return elementCodec -> StreamCodec.of(
                (buf, value) -> {
                    buf.writeVarInt(value.size());
                    for (V element : value) elementCodec.encode(buf, element);
                },
                buf -> {
                    int size = buf.readVarInt();
                    C result = factory.apply(size);
                    for (int i = 0; i < size; i++) result.add(elementCodec.decode(buf));
                    return result;
                });
    }

    /** 对应 1.21 的 {@code ByteBufCodecs.collection(factory, elementCodec)}。 */
    static <V, C extends Collection<V>> StreamCodec<PacketByteBuf, C> collection(
            IntFunction<C> factory, StreamCodec<PacketByteBuf, V> elementCodec) {
        return elementCodec.apply(collection(factory));
    }

    static <K, V, M extends Map<K, V>> StreamCodec<PacketByteBuf, M> map(
            IntFunction<? extends M> factory,
            StreamCodec<PacketByteBuf, K> keyCodec,
            StreamCodec<PacketByteBuf, V> valueCodec) {
        return StreamCodec.of(
                (buf, value) -> {
                    buf.writeVarInt(value.size());
                    for (Map.Entry<K, V> entry : value.entrySet()) {
                        keyCodec.encode(buf, entry.getKey());
                        valueCodec.encode(buf, entry.getValue());
                    }
                },
                buf -> {
                    int size = buf.readVarInt();
                    M result = factory.apply(size);
                    for (int i = 0; i < size; i++) {
                        K key = keyCodec.decode(buf);
                        result.put(key, valueCodec.decode(buf));
                    }
                    return result;
                });
    }

    /** {@code map(LinkedHashMap::new, k, v)} 的等价写法辅助。 */
    static <K, V> IntFunction<Map<K, V>> linkedHashMap() {
        return size -> new LinkedHashMap<>();
    }
}
