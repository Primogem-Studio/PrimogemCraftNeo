package net.per.primogemcraft.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record TemporalRecord(double x, double y, double z, float health) {
    public static final TemporalRecord EMPTY = new TemporalRecord(0.0D, 0.0D, 0.0D, 0.0F);

    public static final Codec<TemporalRecord> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.DOUBLE.fieldOf("x").forGetter(TemporalRecord::x),
            Codec.DOUBLE.fieldOf("y").forGetter(TemporalRecord::y),
            Codec.DOUBLE.fieldOf("z").forGetter(TemporalRecord::z),
            Codec.FLOAT.fieldOf("health").forGetter(TemporalRecord::health)
    ).apply(instance, TemporalRecord::new));
}
