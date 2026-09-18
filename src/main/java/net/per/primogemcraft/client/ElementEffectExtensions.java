package net.per.primogemcraft.client;

import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.extensions.common.IClientMobEffectExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.per.primogemcraft.registry.PGCEffects;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

@EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT)
public final class ElementEffectExtensions {
    private static final IClientMobEffectExtensions HIDDEN = new IClientMobEffectExtensions() {
        @Override
        public boolean isVisibleInGui(MobEffectInstance effect) {
            return false;
        }

        @Override
        public boolean isVisibleInInventory(MobEffectInstance effect) {
            return false;
        }
    };

    private ElementEffectExtensions() {
    }

    @SubscribeEvent
    public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerMobEffect(HIDDEN, PGCEffects.FLIGHT.get());
        event.registerMobEffect(HIDDEN, PGCEffects.DAMAGE_ABSORPTION_COOLDOWN.get());
        event.registerMobEffect(HIDDEN, PGCEffects.RETALIATION_COOLDOWN.get());
        event.registerMobEffect(HIDDEN, PGCEffects.PURIFIED_ARMOR_EFFECT_LIMIT.get());
        event.registerMobEffect(HIDDEN, PGCEffects.BURNING_RETALIATION.get());
        event.registerMobEffect(HIDDEN, PGCEffects.BURNING_RECOVERY.get());
        event.registerMobEffect(HIDDEN, PGCEffects.BUBBLE_LIMIT.get());
    }
}
