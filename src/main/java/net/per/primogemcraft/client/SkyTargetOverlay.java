package net.per.primogemcraft.client;

import com.mojang.math.Axis;
import com.tacz.guns.entity.EntityKineticBullet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.per.primogemcraft.collab.tacz.SkyTargeting;
import net.per.primogemcraft.config.PGCClientConfig;
import net.per.primogemcraft.network.SkyTargetPayload;
import net.per.primogemcraft.network.SkyBulletPayload;
import org.joml.Vector4f;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

@EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT)
public final class SkyTargetOverlay {
    private static final int CYAN = 0xFF78E5EF;
    private static final int GOLD = 0xFFF5D9A0;
    private static final int WHITE = 0xFFE9FFFF;
    private static int targetId = -1;
    private static int lockTicks;
    private static boolean projected;
    private static float targetX;
    private static float targetY;

    private SkyTargetOverlay() {
    }

    public static void update(SkyTargetPayload payload) {
        var player = Minecraft.getInstance().player;
        if (player != null && PGCClientConfig.SKY_TARGET_OVERLAY.get()
                && payload.ticks() == SkyTargeting.LOCK_TICKS && lockTicks < SkyTargeting.LOCK_TICKS)
            player.playSound(SoundEvents.AMETHYST_BLOCK_CHIME, 0.45F, 1.6F);
        targetId = payload.targetId();
        lockTicks = payload.ticks();
        projected = false;
    }

    public static void updateBullet(SkyBulletPayload payload) {
        var level = Minecraft.getInstance().level;
        if (level == null || !(level.getEntity(payload.entityId()) instanceof EntityKineticBullet bullet)) return;
        bullet.setPos(payload.position());
        bullet.setDeltaMovement(payload.velocity());
    }

    @SubscribeEvent
    public static void disconnected(ClientPlayerNetworkEvent.LoggingOut event) {
        targetId = -1;
        lockTicks = 0;
        projected = false;
    }

    @SubscribeEvent
    public static void registerLayers(RegisterGuiLayersEvent event) {
        if (ModList.get().isLoaded("tacz"))
            event.registerAboveAll(ResourceLocation.fromNamespaceAndPath(MOD_ID, "sky_target"),
                    (graphics, deltaTracker) -> render(graphics, deltaTracker.getGameTimeDeltaPartialTick(false)));
    }

    private static boolean active() {
        var minecraft = Minecraft.getInstance();
        return PGCClientConfig.SKY_TARGET_OVERLAY.get() && ModList.get().isLoaded("tacz")
                && minecraft.player != null && minecraft.level != null
                && minecraft.screen == null && !minecraft.options.hideGui && minecraft.options.getCameraType().isFirstPerson()
                && SkyTargeting.isAiming(minecraft.player);
    }

    @SubscribeEvent
    public static void projectTarget(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;
        projected = false;
        if (!active() || targetId < 0) return;
        var minecraft = Minecraft.getInstance();
        if (!(minecraft.level.getEntity(targetId) instanceof LivingEntity target) || !target.isAlive()) return;
        var partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        var center = target.getPosition(partialTick).add(0, target.getBbHeight() * 0.5, 0);
        var offset = center.subtract(event.getCamera().getPosition());
        var point = new Vector4f((float) offset.x, (float) offset.y, (float) offset.z, 1)
                .mul(event.getModelViewMatrix()).mul(event.getProjectionMatrix());
        if (point.w <= 0.05F) return;
        targetX = point.x / point.w;
        targetY = point.y / point.w;
        projected = Math.abs(targetX) < 0.95F && Math.abs(targetY) < 0.9F;
    }

    private static void render(GuiGraphics graphics, float partialTick) {
        if (!active()) return;
        var minecraft = Minecraft.getInstance();
        var x = graphics.guiWidth() / 2;
        var y = graphics.guiHeight() / 2;
        var age = minecraft.player.tickCount + partialTick;
        var progress = Mth.clamp(lockTicks / (float) SkyTargeting.LOCK_TICKS, 0, 1);
        var locked = progress >= 1;
        var color = locked ? GOLD : CYAN;
        var breath = 0.7F + 0.1F * Mth.sin(age * 0.06F);
        var scale = Math.min(1, Math.min(graphics.guiWidth() / 340.0F, graphics.guiHeight() / 230.0F));
        var pose = graphics.pose();
        pose.pushPose();
        pose.translate(x, y, 0);
        pose.scale(scale, scale, 1);
        for (var side = -1; side <= 1; side += 2) {
            wing(graphics, side, 52, 4, 94, -18, 80, 4, breath);
            wing(graphics, side, 52, 12, 82, 2, 72, 12, breath * 0.5F);
            line(graphics, side * 48, -26, side * 40, -18, alpha(GOLD, breath));
            line(graphics, side * 40, -18, side * 40, -8, alpha(GOLD, breath));
            line(graphics, side * 48, 26, side * 40, 18, alpha(GOLD, breath));
            line(graphics, side * 40, 18, side * 40, 8, alpha(GOLD, breath));
            line(graphics, side * 12, -49, side * 29, -49, alpha(GOLD, 0.6F));
            line(graphics, side * 29, -49, side * 35, -43, alpha(GOLD, 0.6F));
            line(graphics, side * 37, 48, side * 54, 48, alpha(CYAN, 0.3F));
            line(graphics, side * 54, 48, side * 60, 42, alpha(CYAN, 0.3F));
        }
        diamond(graphics, 0, -49, 3, alpha(GOLD, 0.7F));
        for (var index = 0; index < 7; index++) {
            var lit = targetId >= 0 && progress >= (Math.abs(index - 3) + 1) / 4.0F;
            diamond(graphics, (index - 3) * 9, 48, index == 3 ? 3 : 2,
                    alpha(locked && index == 3 ? GOLD : CYAN, lit ? 0.8F : 0.2F));
        }
        pose.popPose();
        for (var side = -1; side <= 1; side += 2) {
            line(graphics, x + side * 7, y, x + side * 4, y, alpha(CYAN, 0.8F));
            line(graphics, x, y + side * 7, x, y + side * 4, alpha(CYAN, 0.8F));
        }
        diamond(graphics, x, y, 1, WHITE);
        var status = Component.translatable(locked ? "gui.primogemcraft.sky.locked"
                : targetId >= 0 ? "gui.primogemcraft.sky.locking" : "gui.primogemcraft.sky.searching");
        graphics.drawCenteredString(minecraft.font, status, x, y + Mth.ceil(64 * scale), alpha(color, 0.85F));
        if (!projected || targetId < 0) return;
        var markerX = (targetX + 1) * graphics.guiWidth() * 0.5F;
        var markerY = (1 - targetY) * graphics.guiHeight() * 0.5F;
        var size = 14 + (1 - progress) * 14;
        for (var side = -1; side <= 1; side += 2) {
            line(graphics, markerX + side * size, markerY, markerX + side * (size - 6), markerY - 6, color);
            line(graphics, markerX + side * size, markerY, markerX + side * (size - 6), markerY + 6, color);
            line(graphics, markerX, markerY + side * size, markerX - 6, markerY + side * (size - 6), color);
            line(graphics, markerX, markerY + side * size, markerX + 6, markerY + side * (size - 6), color);
        }
        if (minecraft.level.getEntity(targetId) instanceof LivingEntity target) {
            var distance = Component.translatable("gui.primogemcraft.sky.distance", Mth.ceil(minecraft.player.distanceTo(target)));
            graphics.drawCenteredString(minecraft.font, distance, (int) markerX, (int) (markerY + size + 10), WHITE);
        }
    }

    private static int alpha(int color, float opacity) {
        return (int) (255 * opacity) << 24 | color & 0xFFFFFF;
    }

    private static void wing(GuiGraphics graphics, int side, float rootX, float rootY, float tipX, float tipY,
                             float heelX, float heelY, float opacity) {
        line(graphics, side * rootX, rootY, side * tipX, tipY, alpha(CYAN, opacity));
        line(graphics, side * tipX, tipY, side * heelX, heelY, alpha(CYAN, opacity * 0.5F));
        line(graphics, side * heelX, heelY, side * rootX, rootY, alpha(CYAN, opacity * 0.5F));
    }

    private static void diamond(GuiGraphics graphics, float x, float y, float size, int color) {
        line(graphics, x, y - size, x + size, y, color);
        line(graphics, x + size, y, x, y + size, color);
        line(graphics, x, y + size, x - size, y, color);
        line(graphics, x - size, y, x, y - size, color);
    }

    private static void line(GuiGraphics graphics, float x, float y, float endX, float endY, int color) {
        var pose = graphics.pose();
        pose.pushPose();
        pose.translate(x, y, 0);
        pose.mulPose(Axis.ZP.rotation((float) Math.atan2(endY - y, endX - x)));
        pose.scale((float) Math.hypot(endX - x, endY - y), 0.5F, 1);
        graphics.fill(0, -1, 1, 1, color);
        pose.popPose();
    }
}
