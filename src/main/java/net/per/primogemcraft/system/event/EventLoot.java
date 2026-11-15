package net.per.primogemcraft.system.event;

import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextTypes;

import java.util.List;

public final class EventLoot {
    private EventLoot() {
    }

    public static List<ItemStack> roll(ServerWorld level, Identifier table) {
        var key = table;
        var params = new LootContextParameterSet.Builder(level).build(LootContextTypes.EMPTY);
        return level.getServer().getLootManager().getLootTable(key).generateLoot(params);
    }
}
