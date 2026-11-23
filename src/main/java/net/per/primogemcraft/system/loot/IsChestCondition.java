package net.per.primogemcraft.system.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.per.primogemcraft.registry.PGCLootConditions;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public record IsChestCondition(boolean externalOnly) implements LootItemCondition {
    public static final MapCodec<IsChestCondition> CODEC = Codec.BOOL.optionalFieldOf("external_only", false)
            .xmap(IsChestCondition::new, IsChestCondition::externalOnly);

    public IsChestCondition() {
        this(false);
    }

    @Override
    public LootItemConditionType getType() {
        return PGCLootConditions.IS_CHEST.get();
    }

    @Override
    public boolean test(LootContext context) {
        var id = context.getQueriedLootTableId();
        if (externalOnly && id.getNamespace().equals(MOD_ID)) return false;
        var table = context.getResolver().lookupOrThrow(Registries.LOOT_TABLE).get(ResourceKey.create(Registries.LOOT_TABLE, id));
        return table.filter(holder -> holder.value().getParamSet() == LootContextParamSets.CHEST).isPresent();
    }
}
