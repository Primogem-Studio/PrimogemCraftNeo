package net.per.primogemcraft.network;

import net.minecraft.server.network.ServerPlayerEntity;
import net.per.primogemcraft.compat.network.RegisterPayloadHandlersEvent;
import net.per.primogemcraft.entity.misc.ZiplineCarrierEntity;
import net.per.primogemcraft.item.misc.LuckySpecialTicketItem;
import net.per.primogemcraft.system.choice.ChoiceRegistry;
import net.per.primogemcraft.system.element.AnemoEffectMode;

/**
 * 网络注册（Fabric 1.20.1）。
 * <p>通过兼容层 {@link RegisterPayloadHandlersEvent} + {@link net.per.primogemcraft.compat.network.PayloadRegistrar}
 * 桥接到 Fabric 的 ServerPlayNetworking / ClientPlayNetworking。
 */
public final class PGCNetwork {
    private static final String PROTOCOL_VERSION = "1.1";

    private PGCNetwork() {}

    /**
     * 双端共同部分：注册所有 payload 的编解码器 + C2S 收包器。
     *
     * <p>专用服务端只能调用本方法：S2C 收包器依赖 ClientPlayNetworking，
     * 在服务端加载会触发客户端类缺失。
     */
    public static void registerCommon() {
        var event = new RegisterPayloadHandlersEvent();
        var registrar = event.registrar(PROTOCOL_VERSION);

        // C2S
        registrar.playToServer(ZiplineControlPayload.TYPE, ZiplineControlPayload.STREAM_CODEC, (payload, context) -> {
            if (context.player().getVehicle() instanceof ZiplineCarrierEntity carrier) {
                if (payload.targetId() == -1) carrier.release();
                else carrier.depart((ServerPlayerEntity) context.player(), payload.targetId());
            }
        });
        registrar.playToServer(ChoiceSelectPayload.TYPE, ChoiceSelectPayload.STREAM_CODEC, (payload, context) -> {
            if (context.player() instanceof ServerPlayerEntity player)
                ChoiceRegistry.respond(player, payload.requestId(), payload.index());
        });
        registrar.playToServer(AnemoEffectPayload.TYPE, AnemoEffectPayload.STREAM_CODEC, (payload, context) -> {
            if (context.player() instanceof ServerPlayerEntity player) AnemoEffectMode.cycle(player);
        });
        registrar.playToServer(LuckySpecialTicketCashOutPayload.TYPE, LuckySpecialTicketCashOutPayload.STREAM_CODEC, (payload, context) -> {
            if (context.player() instanceof ServerPlayerEntity player) LuckySpecialTicketItem.cashOut(player);
        });

        // S2C 的编解码器（发送侧必需，双端都要注册；收包器在客户端另行注册）
        net.per.primogemcraft.compat.network.PayloadRegistrar.registerCodecs(
                WishDropsPayload.TYPE, WishDropsPayload.STREAM_CODEC,
                WishMaterialsPayload.TYPE, WishMaterialsPayload.STREAM_CODEC,
                ChoiceOpenPayload.TYPE, ChoiceOpenPayload.STREAM_CODEC,
                ChoiceResultPayload.TYPE, ChoiceResultPayload.STREAM_CODEC,
                ParticleBurstPayload.TYPE, ParticleBurstPayload.STREAM_CODEC,
                ItemActivationPayload.TYPE, ItemActivationPayload.STREAM_CODEC);
    }

    /** 客户端专用：注册 S2C 收包器。 */
    public static void registerClient() {
        var event = new RegisterPayloadHandlersEvent();
        var registrar = event.registrar(PROTOCOL_VERSION);

        registrar.playToClient(WishDropsPayload.TYPE, WishDropsPayload.STREAM_CODEC, (payload, context) ->
                net.per.primogemcraft.client.WishLootItems.update(payload.drops()));
        registrar.playToClient(WishMaterialsPayload.TYPE, WishMaterialsPayload.STREAM_CODEC, (payload, context) ->
                net.per.primogemcraft.client.WishMaterialValues.update(payload.values()));
        registrar.playToClient(ChoiceOpenPayload.TYPE, ChoiceOpenPayload.STREAM_CODEC, (payload, context) ->
                net.per.primogemcraft.client.gui.ChoiceClientHandler.open(payload.request()));
        registrar.playToClient(ChoiceResultPayload.TYPE, ChoiceResultPayload.STREAM_CODEC, (payload, context) ->
                net.per.primogemcraft.client.gui.ChoiceClientHandler.result(payload));
        registrar.playToClient(ParticleBurstPayload.TYPE, ParticleBurstPayload.STREAM_CODEC, (payload, context) ->
                net.per.primogemcraft.client.ChoiceParticles.handle(payload));
        registrar.playToClient(ItemActivationPayload.TYPE, ItemActivationPayload.STREAM_CODEC, (payload, context) ->
                net.per.primogemcraft.client.ItemActivationFx.show(payload.stack()));
    }
}
