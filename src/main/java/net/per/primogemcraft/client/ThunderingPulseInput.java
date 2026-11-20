package net.per.primogemcraft.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.ActionResult;
import net.per.primogemcraft.compat.network.PayloadRegistrar;
import net.per.primogemcraft.network.ThunderingPulsePayload;
import net.per.primogemcraft.registry.PGCItems;

/**
 * 手持飞雷之弦振时把左键攻击转为蓄力请求。
 *
 * <p>Forge 用 {@code InputEvent.InteractionKeyMappingTriggered} 统一拦截左键；
 * Fabric 无等价事件，改为分别拦截攻击方块与攻击实体两条路径。
 */
@Environment(EnvType.CLIENT)
public final class ThunderingPulseInput {
    private ThunderingPulseInput() {
    }

    public static void register() {
        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) ->
                holding() ? trigger() : ActionResult.PASS);
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) ->
                holding() ? trigger() : ActionResult.PASS);
    }

    private static boolean holding() {
        var minecraft = MinecraftClient.getInstance();
        var player = minecraft.player;
        return minecraft.currentScreen == null && player != null && player.isAlive() && !player.isSpectator()
                && player.getMainHandStack().isOf(PGCItems.THUNDERING_PULSE.get());
    }

    private static ActionResult trigger() {
        PayloadRegistrar.sendToServer(new ThunderingPulsePayload());
        // FAIL 同时阻止后续破坏/攻击与挥手动画，等价于原事件的 setSwingHand(false)
        return ActionResult.FAIL;
    }
}