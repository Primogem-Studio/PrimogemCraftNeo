package net.per.primogemcraft.client.gui;

import net.minecraft.client.Minecraft;
import net.per.primogemcraft.network.ChoiceResultPayload;
import net.per.primogemcraft.system.choice.ChoiceRequest;

public final class ChoiceClientHandler {
    private ChoiceClientHandler() {
    }

    public static void open(ChoiceRequest request) {
        var minecraft = Minecraft.getInstance();
        if (minecraft.screen instanceof ChoiceScreen screen) {
            screen.update(request);
            return;
        }
        minecraft.setScreen(new ChoiceScreen(request));
    }

    public static void result(ChoiceResultPayload payload) {
        if (Minecraft.getInstance().screen instanceof ChoiceScreen screen)
            screen.result(payload.requestId(), payload.index(), payload.accepted());
    }

}
