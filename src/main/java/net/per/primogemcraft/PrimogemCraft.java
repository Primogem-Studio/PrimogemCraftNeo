package net.per.primogemcraft;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.per.primogemcraft.collab.elixir.ElixirIntegration;
import net.per.primogemcraft.collab.genshincraft.GenshinCraftIntegration;
import net.per.primogemcraft.collab.tacz.TaczIntegration;
import net.per.primogemcraft.collab.teyvatdelight.TeyvatDelightIntegration;
import net.per.primogemcraft.config.PGCConfig;
import net.per.primogemcraft.system.zipline.ZiplineAssembly;
import net.per.primogemcraft.registry.*;
import org.slf4j.Logger;

@Mod(PrimogemCraft.MOD_ID)
public class PrimogemCraft {
    public static final String MOD_ID = "primogemcraft";
    public static final Logger LOGGER = LogUtils.getLogger();

    public PrimogemCraft(IEventBus modBus, ModContainer container) {
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
        PGCArmorMaterials.REGISTRY.register(modBus);
        PGCEntities.REGISTRY.register(modBus);
        PGCFeatures.REGISTRY.register(modBus);
        PGCPointsOfInterest.REGISTRY.register(modBus);
        PGCVillagerProfessions.REGISTRY.register(modBus);
        PGCMenus.REGISTRY.register(modBus);
        PGCRecipeSerializers.REGISTRY.register(modBus);
        PGCRecipeTypes.REGISTRY.register(modBus);
        PGCLootConditions.REGISTRY.register(modBus);
        PGCCreativeTabs.REGISTRY.register(modBus);
        GenshinCraftIntegration.register(modBus);
        ElixirIntegration.register(modBus);
        TaczIntegration.register();
        TeyvatDelightIntegration.register(modBus);
        container.registerConfig(ModConfig.Type.COMMON, PGCConfig.SPEC);
    }
}
