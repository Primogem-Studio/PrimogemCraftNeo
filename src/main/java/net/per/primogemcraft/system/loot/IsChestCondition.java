package net.per.primogemcraft.system.loot;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.per.primogemcraft.registry.PGCLootConditions;

public class IsChestCondition implements LootItemCondition {
    public static final MapCodec<IsChestCondition> CODEC = MapCodec.unit(new IsChestCondition());

    @Override
    public LootItemConditionType getType() {
        return PGCLootConditions.IS_CHEST.get();
    }

    @Override
    public boolean test(LootContext context) {
        var table = context.getResolver().lookupOrThrow(Registries.LOOT_TABLE).get(ResourceKey.create(Registries.LOOT_TABLE, context.getQueriedLootTableId()));
        return table.filter(holder -> holder.value().getParamSet() == LootContextParamSets.CHEST).isPresent();
    }
}
