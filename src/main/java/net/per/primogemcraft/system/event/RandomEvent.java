package net.per.primogemcraft.system.event;

import net.minecraft.network.chat.Component;

import java.util.List;

public record RandomEvent(int number, Component title, Component description, EventAction action, EventCondition condition,
                          boolean forced) {
    public static RandomEvent of(EventAction action) {
        return new RandomEvent(0, Component.empty(), Component.empty(), action, null, false);
    }

    public static RandomEvent of(Component title, Component description, EventAction action) {
        return new RandomEvent(0, title, description, action, null, false);
    }

    public static RandomEvent of(Component title, Component description, EventAction action, EventCondition condition) {
        return new RandomEvent(0, title, description, action, condition, false);
    }

    public static RandomEvent of(Component title, Component description, EventAction action, EventCondition condition, boolean forced) {
        return new RandomEvent(0, title, description, action, condition, forced);
    }

    public boolean conditional() {
        return condition != null;
    }

    public boolean available(EventContext context) {
        return condition == null || condition.met(context);
    }

    public List<Component> unmet(EventContext context) {
        return condition == null ? List.of() : condition.unmet(context);
    }
}
