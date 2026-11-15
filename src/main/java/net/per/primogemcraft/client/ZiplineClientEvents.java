package net.per.primogemcraft.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.common.Mod;
import net.per.primogemcraft.compat.EnumProxy;
import net.minecraftforge.client.IArmPoseTransformer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderBlockScreenEffectEvent;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.per.primogemcraft.compat.network.PayloadRegistrar;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.per.primogemcraft.entity.misc.ZiplineCarrierEntity;
import net.per.primogemcraft.entity.misc.ZiplineAnchorEntity;
import net.per.primogemcraft.network.ZiplineControlPayload;
import net.per.primogemcraft.registry.PGCSounds;
import net.per.primogemcraft.system.zipline.ZiplineGrip;
import org.joml.Vector4f;

import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.util.List;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

@Mod.EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT)
public final class ZiplineClientEvents {
    private static ZiplineCarrierEntity currentCarrier;
    private static boolean releaseReady;
    private static boolean showTarget;
    private static float targetX;
    private static float targetY;
    private static float renderAge;
    private static int selectedId = -1;
    private static final Map<ZiplineCarrierEntity, ZiplineTravelSound> TRAVEL_SOUNDS = new HashMap<>();
    private static final Set<ZiplineCarrierEntity> CARRIERS = new HashSet<>();
    private static List<ZiplineAnchorEntity> candidates = List.of();

    @SubscribeEvent
    public static void entityJoined(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() && event.getEntity() instanceof ZiplineCarrierEntity carrier) CARRIERS.add(carrier);
    }

    @SubscribeEvent
    public static void entityLeft(EntityLeaveLevelEvent event) {
        if (event.getLevel().isClientSide() && event.getEntity() instanceof ZiplineCarrierEntity carrier) {
            CARRIERS.remove(carrier);
            var sound = TRAVEL_SOUNDS.remove(carrier);
            if (sound != null) Minecraft.getInstance().getSoundManager().stop(sound);
        }
    }
    public static final EnumProxy<HumanoidModel.ArmPose> HANGING = new EnumProxy<>(HumanoidModel.ArmPose.class, true,
            (IArmPoseTransformer) (model, entity, arm) -> {
                model.attackTime = 0;
                model.swimAmount = 0;
                model.head.yRot = Mth.clamp(model.head.yRot, -1.309F, 1.309F);
                model.leftLeg.xRot = 0.12F;
                model.rightLeg.xRot = -0.08F;
                model.leftLeg.yRot = 0;
                model.rightLeg.yRot = 0;
                model.leftLeg.zRot = -0.03F;
                model.rightLeg.zRot = 0.03F;
                model.rightArm.x = -5;
                model.rightArm.z = 0;
                model.rightArm.xRot = -(float) Math.PI - Mth.sin(renderAge * 0.067F) * 0.05F;
                model.rightArm.yRot = 0;
                model.rightArm.zRot = ZiplineGrip.ARM_ROLL - (Mth.cos(renderAge * 0.09F) * 0.05F + 0.05F);
                model.leftArm.xRot = 0.12F;
                model.leftArm.yRot = 0;
                model.leftArm.zRot = -0.08F;
            });

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        var minecraft = Minecraft.getInstance();
        var carrier = minecraft.player != null && minecraft.player.getVehicle() instanceof ZiplineCarrierEntity hanging ? hanging : null;
        if (carrier != currentCarrier) {
            currentCarrier = carrier;
            releaseReady = false;
        }
        if (!minecraft.options.keyUse.isDown()) releaseReady = true;
        candidates = carrier != null && !carrier.moving() && !carrier.combat()
                ? carrier.level().getEntitiesOfClass(ZiplineAnchorEntity.class, carrier.getBoundingBox().inflate(ZiplineCarrierEntity.RANGE))
                : List.of();
        var selected = carrier != null ? carrier.select(minecraft.player, candidates) : null;
        var nextId = selected == null ? -1 : selected.getId();
        if (nextId != selectedId && nextId != -1)
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(PGCSounds.ZIPLINE_SELECT.get(), 1.0F));
        selectedId = nextId;
        TRAVEL_SOUNDS.entrySet().removeIf(entry -> {
            if (entry.getValue().isStopped() || entry.getKey().isRemoved() || !entry.getKey().moving() || entry.getKey().level() != minecraft.level) {
                minecraft.getSoundManager().stop(entry.getValue());
                return true;
            }
            return false;
        });
        CARRIERS.removeIf(entity -> entity.isRemoved() || entity.level() != minecraft.level);
        if (minecraft.level != null) {
            for (var moving : CARRIERS) {
                if (moving.moving() && !TRAVEL_SOUNDS.containsKey(moving)) {
                    var sound = new ZiplineTravelSound(moving);
                    TRAVEL_SOUNDS.put(moving, sound);
                    minecraft.getSoundManager().play(sound);
                }
            }
        }
    }

    @SubscribeEvent
    public static void blockOverlay(RenderBlockScreenEffectEvent event) {
        if (event.getPlayer().getVehicle() instanceof ZiplineCarrierEntity) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void input(InputEvent.InteractionKeyMappingTriggered event) {
        var minecraft = Minecraft.getInstance();
        if (minecraft.screen != null || minecraft.player == null
                || !(minecraft.player.getVehicle() instanceof ZiplineCarrierEntity carrier)) return;
        event.setCanceled(true);
        event.setSwingHand(false);
        if (event.getHand() != InteractionHand.MAIN_HAND) return;
        if (event.isUseItem()) {
            if (carrier == currentCarrier && releaseReady) {
                PayloadRegistrar.sendToServer(new ZiplineControlPayload(-1));
                releaseReady = false;
            }
        } else if (event.isAttack()) {
            var target = carrier.select(minecraft.player, candidates);
            if (target != null) PayloadRegistrar.sendToServer(new ZiplineControlPayload(target.getId()));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void pose(RenderPlayerEvent.Pre event) {
        if (!(event.getEntity().getVehicle() instanceof ZiplineCarrierEntity carrier)) return;
        var player = (AbstractClientPlayer) event.getEntity();
        var bodyYaw = carrier.bodyYaw(event.getPartialTick());
        player.yBodyRot = bodyYaw;
        player.yBodyRotO = bodyYaw;
        renderAge = player.tickCount + event.getPartialTick();
        var hand = ZiplineGrip.handOffset("slim".equals(((net.minecraft.client.player.AbstractClientPlayer) player).getModelName()), player.getScale(), bodyYaw);
        var correction = carrier.getPosition(event.getPartialTick()).subtract(player.getPosition(event.getPartialTick())).subtract(hand);
        event.getPoseStack().pushPose();
        event.getPoseStack().translate(correction.x, correction.y, correction.z);
        var model = event.getRenderer().getModel();
        model.rightArmPose = HANGING.getValue();
        model.leftArmPose = HANGING.getValue();
        model.crouching = false;
    }

    @SubscribeEvent
    public static void restorePose(RenderPlayerEvent.Post event) {
        if (event.getEntity().getVehicle() instanceof ZiplineCarrierEntity) event.getPoseStack().popPose();
    }

    @SubscribeEvent
    public static void projectTarget(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;
        showTarget = false;
        var minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.options.hideGui
                || !(minecraft.player.getVehicle() instanceof ZiplineCarrierEntity carrier)) return;
        var target = carrier.select(minecraft.player, candidates);
        if (target == null) return;
        var camera = event.getCamera();
        var offset = target.attachment().subtract(camera.getPosition());
        var projected = new Vector4f((float) offset.x, (float) offset.y, (float) offset.z, 1)
                .mul(net.per.primogemcraft.compat.client.Vertices.modelView(event.getPoseStack())).mul(event.getProjectionMatrix());
        if (projected.w <= 0.05F) return;
        targetX = projected.x / projected.w;
        targetY = projected.y / projected.w;
        showTarget = Math.abs(targetX) <= 1 && Math.abs(targetY) <= 1;
    }

    @SubscribeEvent
    public static void target(RenderGuiEvent.Post event) {
        var minecraft = Minecraft.getInstance();
        if (!showTarget || minecraft.options.hideGui || minecraft.player == null
                || !(minecraft.player.getVehicle() instanceof ZiplineCarrierEntity)) return;
        var width = event.getGuiGraphics().guiWidth();
        var height = event.getGuiGraphics().guiHeight();
        var x = (int) ((targetX + 1) * width * 0.5F);
        var y = (int) ((1 - targetY) * height * 0.5F);
        var graphics = event.getGuiGraphics();
        for (var row = -4; row <= 4; row++) {
            var halfWidth = (int) Math.sqrt(16 - row * row);
            graphics.fill(x - halfWidth, y + row, x + halfWidth + 1, y + row + 1, 0xFFFFDA24);
        }
        graphics.fill(x - 1, y - 1, x + 2, y + 2, 0xFFFFFFB0);
    }
}
