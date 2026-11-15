package net.per.primogemcraft.client;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.FireworksSparkParticle;
import net.per.primogemcraft.compat.component.FireworkExplosion;
import net.per.primogemcraft.compat.component.VanillaComponents;
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
        var client = MinecraftClient.getInstance();
        if (client.world == null) return;
        var camera = client.gameRenderer.getCamera();
        if (!camera.isReady()) return;

        var scaleX = (double) client.getWindow().getWidth() / Math.max(1, screenWidth);
        var scaleY = (double) client.getWindow().getHeight() / Math.max(1, screenHeight);
        var cam = camera.getPos();
        burst(cam.x + screenX * scaleX, cam.y + (screenHeight - screenY) * scaleY, cam.z, REVEAL_RADIUS, REVEAL_COUNT, REVEAL_COLORS, REVEAL_FADE_COLORS);
    }

    private static void burst(double x, double y, double z, double radius, int count, IntList colors, IntList fadeColors) {
        var level = MinecraftClient.getInstance().world;
        if (level == null) return;
        var tag = VanillaComponents.starterTag(List.of(FireworkExplosion.DEFAULT));
        for (var i = 0; i < count; i++) {
            var angle = level.random.nextDouble() * Math.PI * 2.0;
            var pitch = (level.random.nextDouble() - 0.5) * Math.PI;
            var speed = radius * (0.5 + level.random.nextDouble() * 0.5);
            var vx = Math.cos(angle) * Math.cos(pitch) * speed;
            var vy = Math.sin(pitch) * speed;
            var vz = Math.sin(angle) * Math.cos(pitch) * speed;
            MinecraftClient.getInstance().particleManager.addParticle(new FireworksSparkParticle.FireworkParticle(level, x, y, z, vx, vy, vz,
                    MinecraftClient.getInstance().particleManager, tag));
        }
    }
}
