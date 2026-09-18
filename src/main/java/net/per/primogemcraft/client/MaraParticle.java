package net.per.primogemcraft.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

public class MaraParticle extends TextureSheetParticle {
    private static final float SIZE = 0.2F;
    private static final float SPEED_FACTOR = 0.2F;
    private static final float GRAVITY = 0.01F;
    private static final int BASE_LIFETIME = 8;
    private static final int LIFETIME_VARIANCE = 3;
    private static final int FRAME_TICKS = 5;
    private static final int FRAME_COUNT = 10;
    private static final int ROLL_START = 1;
    private static final float ANGULAR_VELOCITY = 0.05F;
    private static final float ANGULAR_ACCELERATION = 0.01F;
    private static final int FULL_BRIGHT = 15728880;

    private final SpriteSet sprites;
    private float angularVelocity = ANGULAR_VELOCITY;

    private MaraParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, SpriteSet sprites) {
        super(level, x, y, z);
        this.sprites = sprites;
        setSize(SIZE, SIZE);
        lifetime = Math.max(1, BASE_LIFETIME + random.nextInt(LIFETIME_VARIANCE * 2) - LIFETIME_VARIANCE);
        gravity = GRAVITY;
        hasPhysics = true;
        xd = xSpeed * SPEED_FACTOR;
        yd = ySpeed * SPEED_FACTOR;
        zd = zSpeed * SPEED_FACTOR;
        setSpriteFromAge(sprites);
    }

    public static ParticleProvider<SimpleParticleType> provider(SpriteSet sprites) {
        return (type, level, x, y, z, xSpeed, ySpeed, zSpeed) -> new MaraParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, sprites);
    }

    @Override
    public int getLightColor(float partialTick) {
        return FULL_BRIGHT;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    @Override
    public void tick() {
        super.tick();
        oRoll = roll;
        roll += angularVelocity;
        angularVelocity += ANGULAR_ACCELERATION;
        if (!removed) setSprite(sprites.get(age / FRAME_TICKS % FRAME_COUNT + ROLL_START, FRAME_COUNT));
    }
}
