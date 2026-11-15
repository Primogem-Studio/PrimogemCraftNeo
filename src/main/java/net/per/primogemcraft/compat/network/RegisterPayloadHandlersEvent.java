package net.per.primogemcraft.compat.network;

/**
 * 1.21 {@code RegisterPayloadHandlersEvent} 的兼容替代。
 *
 * <p>1.21 在模组事件总线上派发该事件来注册载荷；1.20.1 Forge 的
 * {@code SimpleChannel} 可以在任意时机注册，因此这里把事件退化为一个
 * 由模组入口主动触发的简单载体，{@code PGCNetwork.registerPayloads} 的
 * 方法体（registrar + playToServer/playToClient）可以原样保留。
 */
public final class RegisterPayloadHandlersEvent {

    /**
     * 对应 1.21 的 {@code event.registrar(version)}。返回的注册器就是
     * {@link PayloadRegistrar} 本身，其 playToServer/playToClient 为静态方法，
     * 但通过实例引用调用同样合法，因此调用点无需改写。
     */
    public PayloadRegistrar registrar(String version) {
        PayloadRegistrar.setProtocolVersion(version);
        return new PayloadRegistrar();
    }
}
