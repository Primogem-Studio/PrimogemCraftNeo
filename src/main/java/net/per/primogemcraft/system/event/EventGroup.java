package net.per.primogemcraft.system.event;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public record EventGroup(int number, Component title, ResourceLocation texture, int weight, Supplier<List<Integer>> provider) {
    public EventGroup {
        weight = Math.max(1, weight);
    }

    public static EventGroup of(RandomEvent... events) {
        return new EventGroup(0, Component.empty(), null, 1, numbers(events));
    }

    public static EventGroup of(Component title, RandomEvent... events) {
        return new EventGroup(0, title, null, 1, numbers(events));
    }

    public static EventGroup of(Component title, int weight, RandomEvent... events) {
        return new EventGroup(0, title, null, weight, numbers(events));
    }

    public static EventGroup of(Component title, ResourceLocation texture, RandomEvent... events) {
        return new EventGroup(0, title, texture, 1, numbers(events));
    }

    public static EventGroup of(Component title, ResourceLocation texture, int weight, RandomEvent... events) {
        return new EventGroup(0, title, texture, weight, numbers(events));
    }

    public static EventGroup dynamic(Component title, int weight, Supplier<List<Integer>> provider) {
        return new EventGroup(0, title, null, weight, provider);
    }

    public static EventGroup dynamic(Component title, ResourceLocation texture, int weight, Supplier<List<Integer>> provider) {
        return new EventGroup(0, title, texture, weight, provider);
    }

    public List<Integer> events() {
        return List.copyOf(provider.get());
    }

    public ResourceLocation textureOrDefault() {
        return texture == null ? EventRegistry.DEFAULT_TEXTURE : texture;
    }

    public boolean isEmpty() {
        return provider.get().isEmpty();
    }

    private static Supplier<List<Integer>> numbers(RandomEvent... events) {
        return () -> Arrays.stream(events).mapToInt(RandomEvent::number).boxed().toList();
    }
}
