package net.per.primogemcraft.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.per.primogemcraft.registry.PGCAttachments;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class PlayerFlags {
    public static final Codec<PlayerFlags> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(ResourceLocation.CODEC, Codec.INT).fieldOf("counters").forGetter(flags -> Map.copyOf(flags.counters)),
            ResourceLocation.CODEC.listOf().fieldOf("flags").forGetter(flags -> List.copyOf(flags.flags))
    ).apply(instance, PlayerFlags::new));

    private final Map<ResourceLocation, Integer> counters = new LinkedHashMap<>();
    private final Set<ResourceLocation> flags = new LinkedHashSet<>();

    public PlayerFlags() {
    }

    private PlayerFlags(Map<ResourceLocation, Integer> counters, List<ResourceLocation> flags) {
        this.counters.putAll(counters);
        this.flags.addAll(flags);
    }

    public static PlayerFlags of(Player player) {
        return player.getData(PGCAttachments.PLAYER_FLAGS);
    }

    public int counter(ResourceLocation key) {
        return counters.getOrDefault(key, 0);
    }

    public int advance(ResourceLocation key) {
        var value = counter(key) + 1;
        counters.put(key, value);
        return value;
    }

    public void set(ResourceLocation key, int value) {
        counters.put(key, value);
    }

    public boolean flag(ResourceLocation key) {
        return flags.contains(key);
    }

    public void setFlag(ResourceLocation key, boolean value) {
        if (value) flags.add(key);
        else flags.remove(key);
    }
}
