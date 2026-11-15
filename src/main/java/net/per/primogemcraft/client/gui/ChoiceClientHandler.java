package net.per.primogemcraft.client.gui;

import net.minecraft.client.MinecraftClient;
import net.per.primogemcraft.network.ChoiceResultPayload;
import net.per.primogemcraft.system.choice.ChoiceRequest;

public final class ChoiceClientHandler {
    private ChoiceClientHandler() {
    }

    public static void open(ChoiceRequest request) {
        var client = MinecraftClient.getInstance();
        if (client.currentScreen instanceof ChoiceScreen screen) {
            screen.update(request);
            return;
        }
        client.setScreen(new ChoiceScreen(request));
    }

    public static void result(ChoiceResultPayload payload) {
        if (MinecraftClient.getInstance().currentScreen instanceof ChoiceScreen screen)
            screen.result(payload.requestId(), payload.index(), payload.accepted());
    }
}
