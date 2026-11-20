package net.per.primogemcraft.client;

import net.minecraft.client.MinecraftClient;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.per.primogemcraft.compat.network.PayloadRegistrar;
import net.per.primogemcraft.network.ThunderingPulsePayload;
import net.per.primogemcraft.registry.PGCItems;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

@Mod.EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT)
public final class ThunderingPulseInput {
    @SubscribeEvent
    public static void input(InputEvent.InteractionKeyMappingTriggered event) {
        if (event.isCanceled() || !event.isAttack()) return;
        var minecraft = Minecraft.getInstance();
        var player = minecraft.player;
        if (minecraft.screen != null || player == null || !player.isAlive() || player.isSpectator()
                || !player.getMainHandStack().is(PGCItems.THUNDERING_PULSE.get())) return;
        event.setCanceled(true);
        event.setSwingHand(false);
        PayloadRegistrar.sendToServer(new ThunderingPulsePayload());
    }
}
