package net.per.primogemcraft.client;

import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.per.primogemcraft.network.AnemoEffectPayload;
import org.lwjgl.glfw.GLFW;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

@EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT)
public final class PGCKeyMappings {
    private static final KeyMapping ANEMO_EFFECT = new KeyMapping("key." + MOD_ID + ".anemo_effect", GLFW.GLFW_KEY_UNKNOWN, "key.categories.gameplay");

    private PGCKeyMappings() {
    }

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(ANEMO_EFFECT);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        while (ANEMO_EFFECT.consumeClick()) PacketDistributor.sendToServer(new AnemoEffectPayload());
    }
}
