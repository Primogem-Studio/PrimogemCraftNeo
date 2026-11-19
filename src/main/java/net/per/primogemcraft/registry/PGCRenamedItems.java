package net.per.primogemcraft.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.MissingMappingsEvent;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

@Mod.EventBusSubscriber(modid = MOD_ID)
public final class PGCRenamedItems {
    @SubscribeEvent
    public static void onMissingItemMappings(MissingMappingsEvent event) {
        for (var mapping : event.<Item>getMappings(Registries.ITEM, MOD_ID)) {
            var key = mapping.getKey();
            if (key.getPath().equals("elemental_dissolving_bead_dust"))
                mapping.remap(PGCItems.ELEMENTAL_MOLTEN_BEAD_DUST.get());
        }
    }
}
