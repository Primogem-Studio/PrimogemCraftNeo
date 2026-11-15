package net.per.primogemcraft.compat.network;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

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

    StreamCodec<FriendlyByteBuf, Integer> VAR_INT = StreamCodec.of(
            (buf, value) -> buf.writeVarInt(value), FriendlyByteBuf::readVarInt);

    StreamCodec<FriendlyByteBuf, Long> VAR_LONG = StreamCodec.of(
            (buf, value) -> buf.writeVarLong(value), FriendlyByteBuf::readVarLong);

    StreamCodec<FriendlyByteBuf, Boolean> BOOL = StreamCodec.of(
            (buf, value) -> buf.writeBoolean(value), FriendlyByteBuf::readBoolean);

    StreamCodec<FriendlyByteBuf, Double> DOUBLE = StreamCodec.of(
            (buf, value) -> buf.writeDouble(value), FriendlyByteBuf::readDouble);

    StreamCodec<FriendlyByteBuf, Float> FLOAT = StreamCodec.of(
            (buf, value) -> buf.writeFloat(value), FriendlyByteBuf::readFloat);

    StreamCodec<FriendlyByteBuf, Integer> INT = StreamCodec.of(
            (buf, value) -> buf.writeInt(value), FriendlyByteBuf::readInt);

    StreamCodec<FriendlyByteBuf, Byte> BYTE = StreamCodec.of(
            (buf, value) -> buf.writeByte(value), FriendlyByteBuf::readByte);

    StreamCodec<FriendlyByteBuf, Short> SHORT = StreamCodec.of(
            (buf, value) -> buf.writeShort(value), FriendlyByteBuf::readShort);

    StreamCodec<FriendlyByteBuf, String> STRING_UTF8 = StreamCodec.of(
            FriendlyByteBuf::writeUtf, FriendlyByteBuf::readUtf);

    StreamCodec<FriendlyByteBuf, ResourceLocation> RESOURCE_LOCATION = StreamCodec.of(
            FriendlyByteBuf::writeResourceLocation, FriendlyByteBuf::readResourceLocation);

    StreamCodec<FriendlyByteBuf, BlockPos> BLOCK_POS = StreamCodec.of(
            FriendlyByteBuf::writeBlockPos, FriendlyByteBuf::readBlockPos);

    StreamCodec<FriendlyByteBuf, UUID> UUID = StreamCodec.of(
            FriendlyByteBuf::writeUUID, FriendlyByteBuf::readUUID);

    /** 物品堆。1.21 同步组件数据，1.20.1 同步 NBT——组件数据已落在 NBT 中，语义等价。 */
    StreamCodec<FriendlyByteBuf, ItemStack> ITEM_STACK = StreamCodec.of(
            FriendlyByteBuf::writeItem, FriendlyByteBuf::readItem);

    StreamCodec<FriendlyByteBuf, CompoundTag> COMPOUND_TAG = StreamCodec.of(
            FriendlyByteBuf::writeNbt, FriendlyByteBuf::readNbt);

    // ------------------------------------------------------------ 集合 / 映射

    /**
     * 对应 1.21 的 {@code ByteBufCodecs.list()}：返回 {@code CodecOperation}，
     * 由 {@code elementCodec.apply(ByteBufCodecs.list())} 组合出集合编解码器。
     */
    static <V> StreamCodec.CodecOperation<FriendlyByteBuf, V, List<V>> list() {
        return collection(ArrayList::new);
    }

    /** 对应 1.21 的 {@code ByteBufCodecs.collection(factory)}（操作符形态）。 */
    static <V, C extends Collection<V>> StreamCodec.CodecOperation<FriendlyByteBuf, V, C> collection(
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
    static <V, C extends Collection<V>> StreamCodec<FriendlyByteBuf, C> collection(
            IntFunction<C> factory, StreamCodec<FriendlyByteBuf, V> elementCodec) {
        return elementCodec.apply(collection(factory));
    }

    /** 对应 1.21 的 {@code ByteBufCodecs.map(factory, keyCodec, valueCodec)}。 */
    static <K, V, M extends Map<K, V>> StreamCodec<FriendlyByteBuf, M> map(
            IntFunction<? extends M> factory,
            StreamCodec<FriendlyByteBuf, K> keyCodec,
            StreamCodec<FriendlyByteBuf, V> valueCodec) {
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
