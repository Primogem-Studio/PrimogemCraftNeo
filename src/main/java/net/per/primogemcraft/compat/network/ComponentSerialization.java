package net.per.primogemcraft.compat.network;

import net.minecraft.network.chat.Component;
import net.minecraft.network.FriendlyByteBuf;

/**
 * 1.21 {@code ComponentSerialization} 的兼容替代。
 *
 * <p>1.21 用 {@code TRUSTED_STREAM_CODEC} 读写聊天组件；1.20.1 的
 * {@link FriendlyByteBuf} 已内置 {@code writeComponent}/{@code readComponent}，
 * 这里包装成 StreamCodec 形态，使 14 处调用点只需替换 import。
 *
 * <p>1.21 的 TRUSTED 变体跳过组件长度上限校验；1.20.1 的
 * {@code writeComponent} 内部会做 {@code MAX_COMPONENT_STRING_LENGTH} 校验，
 * 因此本实现的行为比 1.21 略严格。对本项目的用途（界面文本，长度远小于上限）无影响。
 */
public interface ComponentSerialization {

    StreamCodec<RegistryFriendlyByteBuf, Component> TRUSTED_STREAM_CODEC = StreamCodec.of(
            (buffer, value) -> buffer.writeComponent(value),
            FriendlyByteBuf::readComponent);

    StreamCodec<RegistryFriendlyByteBuf, Component> STREAM_CODEC = TRUSTED_STREAM_CODEC;
}
