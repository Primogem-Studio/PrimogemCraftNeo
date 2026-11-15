package net.per.primogemcraft.compat.attribute;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

/**
 * 创造式飞行的兼容实现。
 *
 * <p>1.21 通过 {@code NeoForgeMod.CREATIVE_FLIGHT} 属性授予飞行；1.20.1 没有该属性，
 * 飞行能力由 {@code Player.abilities.mayfly} 控制。本类用「按来源计数的限时飞行」
 * 复现同等行为：多个来源（武器技能、元素飞行效果）可叠加，全部到期后才收回飞行。
 *
 * <p>与 1.21 的属性方案一致，创造模式玩家本就拥有飞行，不受影响；
 * 收回时只在玩家非创造/旁观且无其他来源时关闭 mayfly。
 */
@Mod.EventBusSubscriber(modid = MOD_ID)
public final class FlightCompat {

    /** 每个玩家 -> (来源 id -> 剩余 tick)。 */
    private static final Map<UUID, Map<ResourceLocation, Integer>> ACTIVE = new HashMap<>();

    private FlightCompat() {
    }

    /** 授予限时飞行。同一来源重复调用会刷新时长（与属性修饰器 addOrUpdate 语义一致）。 */
    public static void grant(Player player, ResourceLocation source, int ticks) {
        if (player.level().isClientSide() || ticks <= 0) return;
        ACTIVE.computeIfAbsent(player.getUUID(), key -> new HashMap<>()).put(source, ticks);
        enable(player);
    }

    /** 立即收回某个来源的飞行。 */
    public static void revoke(Player player, ResourceLocation source) {
        var sources = ACTIVE.get(player.getUUID());
        if (sources == null) return;
        sources.remove(source);
        if (sources.isEmpty()) {
            ACTIVE.remove(player.getUUID());
            disable(player);
        }
    }

    public static boolean isFlying(Player player) {
        var sources = ACTIVE.get(player.getUUID());
        return sources != null && !sources.isEmpty();
    }

    private static void enable(Player player) {
        if (player.getAbilities().mayfly) return;
        player.getAbilities().mayfly = true;
        player.onUpdateAbilities();
    }

    private static void disable(Player player) {
        if (player.isCreative() || player.isSpectator()) return;
        if (!player.getAbilities().mayfly) return;
        player.getAbilities().mayfly = false;
        player.getAbilities().flying = false;
        player.onUpdateAbilities();
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (ACTIVE.isEmpty()) return;
        var server = event.getServer();
        ACTIVE.entrySet().removeIf(entry -> {
            var player = server.getPlayerList().getPlayer(entry.getKey());
            if (player == null) return true;
            entry.getValue().entrySet().removeIf(source -> {
                var remaining = source.getValue() - 1;
                if (remaining > 0) {
                    source.setValue(remaining);
                    return false;
                }
                return true;
            });
            if (entry.getValue().isEmpty()) {
                disable(player);
                return true;
            }
            return false;
        });
    }

    /** 玩家重生/切换维度后重新应用飞行能力（abilities 会被重置）。 */
    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (!isFlying(event.getOriginal())) return;
        var sources = ACTIVE.remove(event.getOriginal().getUUID());
        if (sources != null && !sources.isEmpty()) {
            ACTIVE.put(event.getEntity().getUUID(), sources);
            enable(event.getEntity());
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        var sources = ACTIVE.remove(event.getEntity().getUUID());
        if (sources != null && !sources.isEmpty()) disable(event.getEntity());
    }
}
