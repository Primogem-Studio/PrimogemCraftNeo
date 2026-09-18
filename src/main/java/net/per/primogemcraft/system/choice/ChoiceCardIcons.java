package net.per.primogemcraft.system.choice;

import net.minecraft.resources.ResourceLocation;

public record ChoiceCardIcons(ResourceLocation texture, int count) {
    private static final int MAX_COUNT = 6;

    public ChoiceCardIcons {
        count = Math.clamp(count, 1, MAX_COUNT);
    }

    public static ChoiceCardIcons of(ResourceLocation texture, int count) {
        return new ChoiceCardIcons(texture, count);
    }
}
