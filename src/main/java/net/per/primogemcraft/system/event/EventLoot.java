package net.per.primogemcraft.system.event;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import java.util.List;

public final class EventLoot {
    private EventLoot() {
    }

    public static List<ItemStack> roll(ServerLevel level, ResourceLocation table) {
        var key = ResourceKey.create(Registries.LOOT_TABLE, table);
        var params = new LootParams.Builder(level).create(LootContextParamSets.EMPTY);
        return level.getServer().reloadableRegistries().getLootTable(key).getRandomItems(params);
    }
}
