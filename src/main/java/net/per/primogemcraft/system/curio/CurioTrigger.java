package net.per.primogemcraft.system.curio;

import net.minecraft.network.chat.Component;

public enum CurioTrigger {
    ACTIVE("curio.primogemcraft.trigger.active"),
    RIGHT_CLICK("curio.primogemcraft.trigger.right_click"),
    ENHANCE("curio.primogemcraft.trigger.enhance"),
    PRESENCE("curio.primogemcraft.trigger.presence");

    private final String key;

    CurioTrigger(String key) {
        this.key = key;
    }

    public Component label() {
        return Component.translatable(key);
    }
}
