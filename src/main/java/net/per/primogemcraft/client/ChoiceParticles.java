package net.per.primogemcraft.client;

import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.FireworkParticles;
import net.minecraft.world.item.component.FireworkExplosion;
import net.per.primogemcraft.network.ParticleBurstPayload;

import java.util.List;

public final class ChoiceParticles {
    private static final IntList CAPTURE_COLORS = IntList.of(0xFF9FE0, 0xAEEDFF);
    private static final IntList CAPTURE_FADE_COLORS = IntList.of(0xF4C6FF);
    private static final IntList REVEAL_COLORS = IntList.of(0xFFE9B0, 0xFFF6DA);
    private static final IntList REVEAL_FADE_COLORS = IntList.of(0xF4C6FF);
    private static final double CAPTURE_RADIUS = 1.0D;
    private static final int CAPTURE_COUNT = 4;
    private static final double REVEAL_RADIUS = 0.5D;
    private static final int REVEAL_COUNT = 2;

    private ChoiceParticles() {
    }

    public static void handle(ParticleBurstPayload payload) {
        burst(payload.x(), payload.y(), payload.z(), CAPTURE_RADIUS, CAPTURE_COUNT, CAPTURE_COLORS, CAPTURE_FADE_COLORS);
    }

    public static void reveal(float screenX, float screenY, int screenWidth, int screenHeight) {
        var minecraft = Minecraft.getInstance();
        if (minecraft.level == null) return;
        var camera = minecraft.gameRenderer.getMainCamera();
        if (!camera.isInitialized()) return;

        var scaleX = (double) minecraft.getWindow().getWidth() / Math.max(1, screenWidth);
        var scaleY = (double) minecraft.getWindow().getHeight() / Math.max(1, screenHeight);
        var cam = camera.getPosition();
        burst(cam.x + screenX * scaleX, cam.y + (screenHeight - screenY) * scaleY, cam.z, REVEAL_RADIUS, REVEAL_COUNT, REVEAL_COLORS, REVEAL_FADE_COLORS);
    }

    private static void burst(double x, double y, double z, double radius, int count, IntList colors, IntList fadeColors) {
        var level = Minecraft.getInstance().level;
        if (level == null) return;
        var starter = new FireworkParticles.Starter(level, x, y, z, 0.0D, 0.0D, 0.0D, Minecraft.getInstance().particleEngine, List.of(FireworkExplosion.DEFAULT));
        starter.createParticleBall(radius, count, colors, fadeColors, true, false);
    }
}
