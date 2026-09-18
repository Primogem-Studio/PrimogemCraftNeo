package net.per.primogemcraft.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.per.primogemcraft.registry.PGCDataComponents;

public record PastDelay(int seconds, boolean descending) {
    public static final Codec<PastDelay> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("seconds").forGetter(PastDelay::seconds),
            Codec.BOOL.fieldOf("descending").forGetter(PastDelay::descending)
    ).apply(instance, PastDelay::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, PastDelay> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, PastDelay::seconds,
            ByteBufCodecs.BOOL, PastDelay::descending,
            PastDelay::new);

    public static final int MIN_SECONDS = 10;
    public static final int MAX_SECONDS = 40;
    public static final int STEP_SECONDS = 10;
    public static final PastDelay DEFAULT = new PastDelay(MIN_SECONDS, false);

    private static final String DECREASED_KEY = "message.primogemcraft.weapon.past_delay_decreased";
    private static final String INCREASED_KEY = "message.primogemcraft.weapon.past_delay_increased";
    private static final float SOUND_VOLUME = 1.0F;
    private static final float SOUND_PITCH = 1.0F;

    public static int secondsOf(ItemStack stack) {
        return stack.getOrDefault(PGCDataComponents.PAST_DELAY.get(), DEFAULT).seconds();
    }

    public static void adjust(LivingEntity entity, ItemStack stack) {
        var current = stack.getOrDefault(PGCDataComponents.PAST_DELAY.get(), DEFAULT);
        var descending = current.descending();
        var seconds = descending ? current.seconds() - STEP_SECONDS : current.seconds() + STEP_SECONDS;
        var nextDescending = seconds >= MAX_SECONDS || (seconds > MIN_SECONDS && descending);
        stack.set(PGCDataComponents.PAST_DELAY.get(), new PastDelay(seconds, nextDescending));
        var level = entity.level();
        level.playSound(null, entity.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, SOUND_VOLUME, SOUND_PITCH);
        if (entity instanceof ServerPlayer player)
            player.displayClientMessage(Component.translatable(descending ? DECREASED_KEY : INCREASED_KEY, seconds), true);
    }
}
