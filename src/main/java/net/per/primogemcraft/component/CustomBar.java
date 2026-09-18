package net.per.primogemcraft.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record CustomBar(int numerator, int denominator, boolean visible) {
    public static final Codec<CustomBar> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("numerator").forGetter(CustomBar::numerator),
            Codec.INT.fieldOf("denominator").forGetter(CustomBar::denominator),
            Codec.BOOL.fieldOf("visible").forGetter(CustomBar::visible)
    ).apply(instance, CustomBar::new));

    public static final StreamCodec<ByteBuf, CustomBar> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, CustomBar::numerator,
            ByteBufCodecs.VAR_INT, CustomBar::denominator,
            ByteBufCodecs.BOOL, CustomBar::visible,
            CustomBar::new);

    public int remaining() {
        return Math.max(0, denominator - numerator);
    }

    public CustomBar advancedBy(int amount) {
        return new CustomBar(Math.min(denominator, numerator + amount), denominator, visible);
    }

    public CustomBar reducedBy(int amount) {
        return new CustomBar(Math.max(0, numerator - amount), denominator, visible);
    }
}
