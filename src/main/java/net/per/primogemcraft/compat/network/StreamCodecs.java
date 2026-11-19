package net.per.primogemcraft.compat.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

/**
 * 原版类上的静态 {@code STREAM_CODEC} 字段在 1.20.1 不存在
 * （{@code ResourceLocation.STREAM_CODEC}、{@code ItemStack.STREAM_CODEC}、
 * {@code BlockPos.STREAM_CODEC}、{@code UUIDUtil.STREAM_CODEC}），
 * 且无法通过 import 重定向到原版类，故集中在此提供等价实现。
 *
 * <p>线格式与 1.21 保持一致，因此客户端与服务端只要都用本模组即可互通。
 */
public interface StreamCodecs {

    StreamCodec<RegistryFriendlyByteBuf, ResourceLocation> RESOURCE_LOCATION = StreamCodec.of(
            (buffer, value) -> buffer.writeResourceLocation(value),
            FriendlyByteBuf::readResourceLocation);

    StreamCodec<RegistryFriendlyByteBuf, BlockPos> BLOCK_POS = StreamCodec.of(
            (buffer, value) -> buffer.writeBlockPos(value),
            FriendlyByteBuf::readBlockPos);

    StreamCodec<RegistryFriendlyByteBuf, UUID> UUID = StreamCodec.of(
            (buffer, value) -> buffer.writeUUID(value),
            FriendlyByteBuf::readUUID);

    StreamCodec<RegistryFriendlyByteBuf, ItemStack> ITEM_STACK = StreamCodec.of(
            FriendlyByteBuf::writeItem,
            FriendlyByteBuf::readItem);

    /**
     * 对应 1.21 的 {@code ItemStack.OPTIONAL_STREAM_CODEC}：允许编码 {@code null}。
     * 用一个布尔前缀表示存在性，随后按普通物品堆写入。
     */
    StreamCodec<RegistryFriendlyByteBuf, ItemStack> OPTIONAL_ITEM_STACK = StreamCodec.of(
            (buffer, value) -> {
                buffer.writeBoolean(value != null);
                if (value != null) buffer.writeItem(value);
            },
            buffer -> buffer.readBoolean() ? buffer.readItem() : null);

    static <V> StreamCodec<RegistryFriendlyByteBuf, java.util.List<V>> list(StreamCodec<RegistryFriendlyByteBuf, V> element) {
        return StreamCodec.of(
                (buffer, value) -> {
                    buffer.writeVarInt(value.size());
                    for (var item : value) element.encode(buffer, item);
                },
                buffer -> {
                    var size = buffer.readVarInt();
                    var result = new java.util.ArrayList<V>(size);
                    for (var index = 0; index < size; index++) result.add(element.decode(buffer));
                    return java.util.List.copyOf(result);
                });
    }
}
