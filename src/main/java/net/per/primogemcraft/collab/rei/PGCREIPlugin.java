package net.per.primogemcraft.collab.rei;

import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.DisplaySerializerRegistry;
import me.shedaniel.rei.api.common.plugins.REIServerPlugin;
import me.shedaniel.rei.forge.REIPluginCommon;
import net.per.primogemcraft.system.wish.WishRarity;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

@REIPluginCommon
public class PGCREIPlugin implements REIServerPlugin {
    public static final CategoryIdentifier<WishDisplay> WISH = CategoryIdentifier.of(MOD_ID, "wish");
    public static final CategoryIdentifier<ConversionDisplay> CONVERSION = CategoryIdentifier.of(MOD_ID, "conversion");

    @Override
    public void registerDisplaySerializer(DisplaySerializerRegistry registry) {
        registry.register(WISH, WishDisplay.serializer());
    }

    static WishRarity rarityOf(String name) {
        for (var rarity : WishRarity.values()) {
            if (rarity.name().equals(name)) return rarity;
        }
        return WishRarity.BLUE;
    }
}
