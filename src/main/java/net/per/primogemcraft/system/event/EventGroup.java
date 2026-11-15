package net.per.primogemcraft.system.event;

import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public record EventGroup(int number, Text title, Identifier texture, int weight, Supplier<List<Integer>> provider) {
    public EventGroup {
        weight = Math.max(1, weight);
    }

    public static EventGroup of(RandomEvent... events) {
        return new EventGroup(0, Text.empty(), null, 1, numbers(events));
    }

    public static EventGroup of(Text title, RandomEvent... events) {
        return new EventGroup(0, title, null, 1, numbers(events));
    }

    public static EventGroup of(Text title, int weight, RandomEvent... events) {
        return new EventGroup(0, title, null, weight, numbers(events));
    }

    public static EventGroup of(Text title, Identifier texture, RandomEvent... events) {
        return new EventGroup(0, title, texture, 1, numbers(events));
    }

    public static EventGroup of(Text title, Identifier texture, int weight, RandomEvent... events) {
        return new EventGroup(0, title, texture, weight, numbers(events));
    }

    public static EventGroup dynamic(Text title, int weight, Supplier<List<Integer>> provider) {
        return new EventGroup(0, title, null, weight, provider);
    }

    public static EventGroup dynamic(Text title, Identifier texture, int weight, Supplier<List<Integer>> provider) {
        return new EventGroup(0, title, texture, weight, provider);
    }

    public List<Integer> events() {
        return List.copyOf(provider.get());
    }

    public Identifier textureOrDefault() {
        return texture == null ? EventRegistry.DEFAULT_TEXTURE : texture;
    }

    public boolean isEmpty() {
        return provider.get().isEmpty();
    }

    private static Supplier<List<Integer>> numbers(RandomEvent... events) {
        return () -> Arrays.stream(events).mapToInt(RandomEvent::number).boxed().toList();
    }
}
