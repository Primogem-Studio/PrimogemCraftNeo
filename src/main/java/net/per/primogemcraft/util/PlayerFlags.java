package net.per.primogemcraft.util;

import net.per.primogemcraft.compat.attachment.Attachments;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Identifier;
import net.minecraft.entity.player.PlayerEntity;
import net.per.primogemcraft.registry.PGCAttachments;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class PlayerFlags {
    public static final Codec<PlayerFlags> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(Identifier.CODEC, Codec.INT).fieldOf("counters").forGetter(flags -> Map.copyOf(flags.counters)),
            Identifier.CODEC.listOf().fieldOf("flags").forGetter(flags -> List.copyOf(flags.flags))
    ).apply(instance, PlayerFlags::new));

    private final Map<Identifier, Integer> counters = new LinkedHashMap<>();
    private final Set<Identifier> flags = new LinkedHashSet<>();

    public PlayerFlags() {
    }

    private PlayerFlags(Map<Identifier, Integer> counters, List<Identifier> flags) {
        this.counters.putAll(counters);
        this.flags.addAll(flags);
    }

    public static PlayerFlags of(PlayerEntity player) {
        return Attachments.get(player, PGCAttachments.PLAYER_FLAGS);
    }

    public int counter(Identifier key) {
        return counters.getOrDefault(key, 0);
    }

    public int advance(Identifier key) {
        var value = counter(key) + 1;
        counters.put(key, value);
        return value;
    }

    public void set(Identifier key, int value) {
        counters.put(key, value);
    }

    public boolean flag(Identifier key) {
        return flags.contains(key);
    }

    public void setFlag(Identifier key, boolean value) {
        if (value) flags.add(key);
        else flags.remove(key);
    }
}
