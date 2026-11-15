package net.per.primogemcraft.registry;

import net.minecraft.registry.RegistryKeys;
import net.minecraft.loot.condition.LootConditionType;
import net.per.primogemcraft.compat.registries.DeferredHolder;
import net.per.primogemcraft.compat.registries.DeferredRegister;
import net.per.primogemcraft.system.loot.IsChestCondition;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public class PGCLootConditions {
    public static final DeferredRegister<LootConditionType> REGISTRY = DeferredRegister.create(RegistryKeys.LOOT_CONDITION_TYPE, MOD_ID);

    public static final DeferredHolder<LootConditionType, LootConditionType> IS_CHEST = REGISTRY.register("is_chest", () -> new LootConditionType(IsChestCondition.SERIALIZER));
}
