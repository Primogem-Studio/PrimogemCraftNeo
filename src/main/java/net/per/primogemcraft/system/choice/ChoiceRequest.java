package net.per.primogemcraft.system.choice;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public record ChoiceRequest(int id, Component title, Component subtitle, ChoiceVisual visual, ResourceLocation texture,
                            ChoiceCardTextures textures, ChoiceSpin spin, int settleTicks, List<ChoiceCard> options, long expireTime,
                            ChoiceMode mode, int leaveIndex) {
    public static final int NO_LEAVE = -1;

    public int size() {
        return options.size();
    }

    public ChoiceCard option(int index) {
        return options.get(index);
    }
}
