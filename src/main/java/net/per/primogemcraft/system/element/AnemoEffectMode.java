package net.per.primogemcraft.system.element;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.per.primogemcraft.util.PlayerFlags;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public enum AnemoEffectMode {
    DISABLE_SLOW_FALLING("disable_slow_falling"),
    DISABLE_JUMP("disable_jump"),
    FLIGHT_ONLY("flight_only"),
    ALL("all");

    private static final ResourceLocation KEY = ResourceLocation.fromNamespaceAndPath(MOD_ID, "anemo_effect_mode");
    private static final String MESSAGE_KEY = "message." + MOD_ID + ".element.anemo_mode";
    private static final String LABEL_PREFIX = "element." + MOD_ID + ".anemo.mode.";

    private final String id;

    AnemoEffectMode(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public boolean allowsSlowFalling() {
        return this != DISABLE_SLOW_FALLING && this != FLIGHT_ONLY;
    }

    public boolean allowsJump() {
        return this != DISABLE_JUMP && this != FLIGHT_ONLY;
    }

    public static AnemoEffectMode of(Player player) {
        var stored = PlayerFlags.of(player).counter(KEY);
        return stored >= 1 && stored <= values().length ? values()[stored - 1] : ALL;
    }

    public static void cycle(ServerPlayer player) {
        var next = values()[(of(player).ordinal() + 1) % values().length];
        PlayerFlags.of(player).set(KEY, next.ordinal() + 1);
        player.displayClientMessage(Component.translatable(MESSAGE_KEY, Component.translatable(LABEL_PREFIX + next.id())), true);
    }
}
