package net.per.primogemcraft;

import com.mojang.logging.LogUtils;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.per.primogemcraft.collab.genshincraft.GenshinCraftIntegration;
import net.per.primogemcraft.collab.tacz.TaczIntegration;
import net.per.primogemcraft.collab.teyvatdelight.TeyvatDelightIntegration;
import net.per.primogemcraft.compat.network.RegisterPayloadHandlersEvent;
import net.per.primogemcraft.config.PGCConfig;
import net.per.primogemcraft.network.PGCNetwork;
import net.per.primogemcraft.system.zipline.ZiplineAssembly;
import net.per.primogemcraft.registry.*;
import org.slf4j.Logger;

@Mod(PrimogemCraft.MOD_ID)
public class PrimogemCraft {
    public static final String MOD_ID = "primogemcraft";
    public static final Logger LOGGER = LogUtils.getLogger();

    public PrimogemCraft() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        PGCBlocks.REGISTRY.register(modBus);
        PGCBlockEntities.REGISTRY.register(modBus);
        modBus.addListener(PGCBlockEntities::registerCapabilities);
        modBus.addListener(ZiplineAssembly::setup);
        PGCItems.REGISTRY.register(modBus);
        PGCSounds.REGISTRY.register(modBus);
        PGCDataComponents.REGISTRY.register(modBus);
        PGCEffects.REGISTRY.register(modBus);
        PGCParticles.REGISTRY.register(modBus);
        PGCPotions.REGISTRY.register(modBus);
        PGCAttachments.REGISTRY.register(modBus);
        PGCEntities.REGISTRY.register(modBus);
        PGCFeatures.REGISTRY.register(modBus);
        PGCPointsOfInterest.REGISTRY.register(modBus);
        PGCVillagerProfessions.REGISTRY.register(modBus);
        PGCMenus.REGISTRY.register(modBus);
        PGCRecipeSerializers.REGISTRY.register(modBus);
        PGCRecipeTypes.REGISTRY.register(modBus);
        PGCLootConditions.REGISTRY.register(modBus);
        PGCLootModifiers.REGISTRY.register(modBus);
        PGCCreativeTabs.REGISTRY.register(modBus);
        GenshinCraftIntegration.register(modBus);
        TaczIntegration.register();
        TeyvatDelightIntegration.register(modBus);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, PGCConfig.SPEC);
        PGCNetwork.registerPayloads(new RegisterPayloadHandlersEvent());
        modBus.addListener(this::onCommonSetup);
    }

    private void onCommonSetup(net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            net.per.primogemcraft.item.weapon.element.ElementTools.onModifyDefaultComponents();
            PGCItemBars.modifyDefaultComponents();
        });
    }
}
