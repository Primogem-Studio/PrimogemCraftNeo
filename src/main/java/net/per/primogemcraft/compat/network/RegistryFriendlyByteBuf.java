package net.per.primogemcraft.compat.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketByteBuf;

/**
 * 1.21 {@code RegistryFriendlyByteBuf} 的兼容替代。
 *
 * <p>1.21 引入该子类是为了在编解码时携带注册表访问上下文（用于数据组件与
 * 注册表同步字段的解析）。1.20.1 的 {@link FriendlyByteBuf} 直接读写 NBT 与
 * 注册名，不需要注册表上下文，因此这里退化为一个薄包装，仅为了让项目中
 * 55 处 {@code StreamCodec<RegistryFriendlyByteBuf, ...>} 的类型声明保持不变。
 */
public class RegistryFriendlyByteBuf extends PacketByteBuf {

    public RegistryFriendlyByteBuf(ByteBuf source) {
        super(source);
    }

    /** 若已经是包装类型则原样返回，否则包装一层，便于在网络桥接处使用。 */
    public static RegistryFriendlyByteBuf wrap(PacketByteBuf buffer) {
        return buffer instanceof RegistryFriendlyByteBuf typed
                ? typed
                : new RegistryFriendlyByteBuf(buffer);
    }
}
