package net.per.primogemcraft.client;

import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.per.primogemcraft.compat.network.PayloadRegistrar;
import net.per.primogemcraft.network.AnemoEffectPayload;
import org.lwjgl.glfw.GLFW;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

@Mod.EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT)
public final class PGCKeyMappings {
    private static final KeyMapping ANEMO_EFFECT = new KeyMapping("key." + MOD_ID + ".anemo_effect", GLFW.GLFW_KEY_UNKNOWN, "key.categories.gameplay");

    private PGCKeyMappings() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        while (ANEMO_EFFECT.consumeClick()) PayloadRegistrar.sendToServer(new AnemoEffectPayload());
    }

    @Mod.EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class Registration {
        private Registration() {
        }

        @SubscribeEvent
        public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
            event.register(ANEMO_EFFECT);
        }
    }
}
