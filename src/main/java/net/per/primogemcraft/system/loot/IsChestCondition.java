package net.per.primogemcraft.system.loot;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.per.primogemcraft.registry.PGCLootConditions;

public class IsChestCondition implements LootItemCondition {
    public static final net.minecraft.world.level.storage.loot.Serializer<IsChestCondition> SERIALIZER =
            new net.minecraft.world.level.storage.loot.Serializer<>() {
                @Override
                public void serialize(com.google.gson.JsonObject json, IsChestCondition value, com.google.gson.JsonSerializationContext context) {
                }

                @Override
                public IsChestCondition deserialize(com.google.gson.JsonObject json, com.google.gson.JsonDeserializationContext context) {
                    return new IsChestCondition();
                }
            };

    @Override
    public LootItemConditionType getType() {
        return PGCLootConditions.IS_CHEST.get();
    }

    @Override
    public boolean test(LootContext context) {
        var id = context.getQueriedLootTableId();
        if (!"minecraft".equals(id.getNamespace())) return false;
        var path = id.getPath();
        return path.startsWith("chests/") || path.equals("gameplay/fishing/treasure");
    }
}
