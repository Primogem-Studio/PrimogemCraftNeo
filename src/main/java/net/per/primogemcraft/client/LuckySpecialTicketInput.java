package net.per.primogemcraft.client;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.per.primogemcraft.network.LuckySpecialTicketCashOutPayload;
import net.per.primogemcraft.registry.PGCItems;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

@EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT)
public final class LuckySpecialTicketInput {
    @SubscribeEvent
    public static void input(InputEvent.InteractionKeyMappingTriggered event) {
        if (!event.isAttack()) return;
        var minecraft = Minecraft.getInstance();
        var player = minecraft.player;
        if (minecraft.screen != null || player == null || player.isSpectator()
                || !player.getMainHandItem().is(PGCItems.LUCKY_SPECIAL_TICKET.get())) return;
        PacketDistributor.sendToServer(new LuckySpecialTicketCashOutPayload());
    }
}
