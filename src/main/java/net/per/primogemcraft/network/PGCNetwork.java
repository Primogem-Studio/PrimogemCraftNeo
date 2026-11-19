package net.per.primogemcraft.network;

import net.minecraft.server.level.ServerPlayer;
import net.per.primogemcraft.compat.network.PayloadRegistrar;
import net.per.primogemcraft.compat.network.RegisterPayloadHandlersEvent;
import net.per.primogemcraft.client.ChoiceParticles;
import net.per.primogemcraft.client.ItemActivationFx;
import net.per.primogemcraft.client.WishMaterialValues;
import net.per.primogemcraft.client.WishLootItems;
import net.per.primogemcraft.client.SkyTargetOverlay;
import net.per.primogemcraft.client.gui.ChoiceClientHandler;
import net.per.primogemcraft.entity.misc.ZiplineCarrierEntity;
import net.per.primogemcraft.item.misc.LuckySpecialTicketItem;
import net.per.primogemcraft.item.weapon.ThunderingPulseItem;
import net.per.primogemcraft.system.choice.ChoiceRegistry;
import net.per.primogemcraft.system.element.AnemoEffectMode;
import net.per.primogemcraft.system.wish.WishCoreTransfer;

public final class PGCNetwork {
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        net.per.primogemcraft.collab.teyvatdelight.StellarShopNetwork.register(event);
        var registrar = event.registrar(PayloadRegistrar.protocolVersion());
        registrar.playToClient(SkyTargetPayload.TYPE, SkyTargetPayload.STREAM_CODEC, (payload, context) -> SkyTargetOverlay.update(payload));
        registrar.playToClient(SkyBulletPayload.TYPE, SkyBulletPayload.STREAM_CODEC, (payload, context) -> SkyTargetOverlay.updateBullet(payload));
        registrar.playToServer(ThunderingPulsePayload.TYPE, ThunderingPulsePayload.STREAM_CODEC, (payload, context) -> {
            if (context.player() instanceof ServerPlayer player) ThunderingPulseItem.dash(player);
        });
        registrar.playToServer(WishCoreTransferPayload.TYPE, WishCoreTransferPayload.STREAM_CODEC, (payload, context) -> {
            if (context.player() instanceof ServerPlayer player) WishCoreTransfer.transfer(player, payload.containerId());
        });
        registrar.playToServer(ZiplineControlPayload.TYPE, ZiplineControlPayload.STREAM_CODEC, (payload, context) -> {
            if (context.player().getVehicle() instanceof ZiplineCarrierEntity carrier) {
                if (payload.targetId() == -1) carrier.release();
                else carrier.depart(context.player(), payload.targetId());
            }
        });
        registrar.playToClient(WishDropsPayload.TYPE, WishDropsPayload.STREAM_CODEC, (payload, context) -> WishLootItems.update(payload.drops()));
        registrar.playToClient(WishMaterialsPayload.TYPE, WishMaterialsPayload.STREAM_CODEC, (payload, context) -> WishMaterialValues.update(payload.values()));
        registrar.playToClient(ChoiceOpenPayload.TYPE, ChoiceOpenPayload.STREAM_CODEC, (payload, context) -> ChoiceClientHandler.open(payload.request()));
        registrar.playToClient(ChoiceResultPayload.TYPE, ChoiceResultPayload.STREAM_CODEC, (payload, context) -> ChoiceClientHandler.result(payload));
        registrar.playToClient(ParticleBurstPayload.TYPE, ParticleBurstPayload.STREAM_CODEC, (payload, context) -> ChoiceParticles.handle(payload));
        registrar.playToClient(ItemActivationPayload.TYPE, ItemActivationPayload.STREAM_CODEC, (payload, context) -> ItemActivationFx.show(payload.stack()));
        registrar.playToServer(ChoiceSelectPayload.TYPE, ChoiceSelectPayload.STREAM_CODEC, (payload, context) -> {
            if (context.player() instanceof ServerPlayer player)
                ChoiceRegistry.respond(player, payload.requestId(), payload.index());
        });
        registrar.playToServer(AnemoEffectPayload.TYPE, AnemoEffectPayload.STREAM_CODEC, (payload, context) -> {
            if (context.player() instanceof ServerPlayer player) AnemoEffectMode.cycle(player);
        });
        registrar.playToServer(LuckySpecialTicketCashOutPayload.TYPE, LuckySpecialTicketCashOutPayload.STREAM_CODEC, (payload, context) -> {
            if (context.player() instanceof ServerPlayer player) LuckySpecialTicketItem.cashOut(player);
        });
    }
}
