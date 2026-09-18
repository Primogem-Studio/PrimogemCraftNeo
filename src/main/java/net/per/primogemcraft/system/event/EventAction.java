package net.per.primogemcraft.system.event;

@FunctionalInterface
public interface EventAction {
    boolean run(EventContext context);
}
