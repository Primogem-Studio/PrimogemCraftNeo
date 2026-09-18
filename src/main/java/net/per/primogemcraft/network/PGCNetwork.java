package net.per.primogemcraft.network;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.per.primogemcraft.client.ChoiceParticles;
import net.per.primogemcraft.client.ItemActivationFx;
import net.per.primogemcraft.client.gui.ChoiceClientHandler;
import net.per.primogemcraft.system.choice.ChoiceRegistry;
import net.per.primogemcraft.system.element.AnemoEffectMode;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

@EventBusSubscriber(modid = MOD_ID)
public final class PGCNetwork {
    private static final String PROTOCOL_VERSION = "1.1";

    @SubscribeEvent
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar(PROTOCOL_VERSION);
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
    }
}
