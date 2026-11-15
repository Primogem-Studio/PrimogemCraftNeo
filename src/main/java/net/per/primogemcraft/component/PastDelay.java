package net.per.primogemcraft.component;

import net.per.primogemcraft.compat.component.StackComponents;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.per.primogemcraft.compat.network.RegistryFriendlyByteBuf;
import net.per.primogemcraft.compat.network.ByteBufCodecs;
import net.per.primogemcraft.compat.network.StreamCodec;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.sound.SoundEvents;
import net.minecraft.sound.SoundCategory;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
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
        return StackComponents.getOrDefault(stack, PGCDataComponents.PAST_DELAY, DEFAULT).seconds();
    }

    public static void adjust(LivingEntity entity, ItemStack stack) {
        var current = StackComponents.getOrDefault(stack, PGCDataComponents.PAST_DELAY, DEFAULT);
        var descending = current.descending();
        var seconds = descending ? current.seconds() - STEP_SECONDS : current.seconds() + STEP_SECONDS;
        var nextDescending = seconds >= MAX_SECONDS || (seconds > MIN_SECONDS && descending);
        StackComponents.set(stack, PGCDataComponents.PAST_DELAY, new PastDelay(seconds, nextDescending));
        var level = entity.getWorld();
        level.playSound(null, entity.getBlockPos(), SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, SOUND_VOLUME, SOUND_PITCH);
        if (entity instanceof ServerPlayerEntity player)
            player.sendMessage(Text.translatable(descending ? DECREASED_KEY : INCREASED_KEY, seconds), true);
    }
}
