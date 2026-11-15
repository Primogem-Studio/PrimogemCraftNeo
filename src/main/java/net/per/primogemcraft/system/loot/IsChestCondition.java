package net.per.primogemcraft.system.loot;

import com.mojang.serialization.MapCodec;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryKey;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.LootConditionType;
import net.per.primogemcraft.registry.PGCLootConditions;

public class IsChestCondition implements LootCondition {
    public static final net.minecraft.util.JsonSerializer<IsChestCondition> SERIALIZER =
            new net.minecraft.util.JsonSerializer<>() {
                @Override
                public void toJson(com.google.gson.JsonObject json, IsChestCondition value, com.google.gson.JsonSerializationContext context) {
                }

                @Override
                public IsChestCondition fromJson(com.google.gson.JsonObject json, com.google.gson.JsonDeserializationContext context) {
                    return new IsChestCondition();
                }
            };

    @Override
    public LootConditionType getType() {
        return PGCLootConditions.IS_CHEST.get();
    }

    @Override
    public boolean test(LootContext context) {
        var state = context.get(net.minecraft.loot.context.LootContextParameters.BLOCK_STATE);
        return state != null && state.getBlock() instanceof net.minecraft.block.ChestBlock;
    }
}
