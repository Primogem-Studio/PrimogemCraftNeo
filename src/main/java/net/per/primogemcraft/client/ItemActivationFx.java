package net.per.primogemcraft.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;

/**
 * 客户端物品激活特效。服务端发送 ItemActivationPayload 后，
 * 在玩家位置播放一圈附魔粒子作为反馈。
 */
public final class ItemActivationFx {
    private ItemActivationFx() {
    }

    public static void show(ItemStack stack) {
        var client = MinecraftClient.getInstance();
        if (client.world == null || client.player == null) return;
        var player = client.player;
        for (var i = 0; i < 12; i++) {
            var angle = player.getRandom().nextDouble() * Math.PI * 2.0;
            var vx = Math.cos(angle) * 0.15;
            var vz = Math.sin(angle) * 0.15;
            client.world.addParticle(ParticleTypes.ENCHANT, player.getX(), player.getY() + 1.0, player.getZ(), vx, 0.1, vz);
        }
    }
}