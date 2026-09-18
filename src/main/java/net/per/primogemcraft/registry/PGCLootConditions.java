package net.per.primogemcraft.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.per.primogemcraft.system.loot.IsChestCondition;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public class PGCLootConditions {
    public static final DeferredRegister<LootItemConditionType> REGISTRY = DeferredRegister.create(Registries.LOOT_CONDITION_TYPE, MOD_ID);

    public static final DeferredHolder<LootItemConditionType, LootItemConditionType> IS_CHEST = REGISTRY.register("is_chest", () -> new LootItemConditionType(IsChestCondition.CODEC));
}
