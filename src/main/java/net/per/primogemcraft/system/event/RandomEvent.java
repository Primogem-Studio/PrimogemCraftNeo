package net.per.primogemcraft.system.event;

import net.minecraft.text.Text;

import java.util.List;

public record RandomEvent(int number, Text title, Text description, EventAction action, EventCondition condition,
                          boolean forced) {
    public static RandomEvent of(EventAction action) {
        return new RandomEvent(0, Text.empty(), Text.empty(), action, null, false);
    }

    public static RandomEvent of(Text title, Text description, EventAction action) {
        return new RandomEvent(0, title, description, action, null, false);
    }

    public static RandomEvent of(Text title, Text description, EventAction action, EventCondition condition) {
        return new RandomEvent(0, title, description, action, condition, false);
    }

    public static RandomEvent of(Text title, Text description, EventAction action, EventCondition condition, boolean forced) {
        return new RandomEvent(0, title, description, action, condition, forced);
    }

    public boolean conditional() {
        return condition != null;
    }

    public boolean available(EventContext context) {
        return condition == null || condition.met(context);
    }

    public List<Text> unmet(EventContext context) {
        return condition == null ? List.of() : condition.unmet(context);
    }
}
