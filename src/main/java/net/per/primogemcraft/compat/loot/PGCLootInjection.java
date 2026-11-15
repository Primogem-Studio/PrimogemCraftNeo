package net.per.primogemcraft.compat.loot;

import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.minecraft.item.Item;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.condition.RandomChanceLootCondition;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.function.SetCountLootFunction;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.loot.provider.number.UniformLootNumberProvider;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

/**
 * Fabric 等价实现 Forge 的 {@code forge:add_table} loot modifier。
 * 原 JSON 位于 data/primogemcraft/loot_modifiers/，在 Fabric 下不生效，
 * 这里用 LootTableEvents.MODIFY 在代码中注入相同的战利品池。
 *
 * <p>chest_injection：对所有箱子类战利品表以 20% 概率注入 primogemcraft:injection/chest 池；
 * fishing_injection：对原版钓鱼表以 3% 概率注入 primogemcraft:injection/fishing 池。
 */
public final class PGCLootInjection {
    private static final Identifier FISHING = Identifier.of("minecraft", "gameplay/fishing");

    private PGCLootInjection() {
    }

    public static void register() {
        LootTableEvents.MODIFY.register((resourceManager, lootManager, id, tableBuilder, source) -> {
            if (FISHING.equals(id)) {
                tableBuilder.pool(fishingPool());
            } else if (isChestTable(id)) {
                tableBuilder.pool(chestPool());
            }
        });
    }

    private static boolean isChestTable(Identifier id) {
        if (!id.getNamespace().equals("minecraft")) return false;
        var path = id.getPath();
        return path.startsWith("chests/") || path.equals("gameplay/fishing/treasure");
    }

    private static LootPool.Builder chestPool() {
        var pool = LootPool.builder()
                .rolls(ConstantLootNumberProvider.create(1.0F))
                .conditionally(RandomChanceLootCondition.builder(0.2F));
        addItem(pool, "primogem", 10, 1, 3);
        addItem(pool, "enigmata_gift_box", 3, 1, 1);
        addItem(pool, "enhancement_ore", 10, 1, 9);
        addItem(pool, "fine_enhancement_ore", 10, 1, 3);
        addItem(pool, "adventurers_experience_reward", 5, 1, 1);
        addItem(pool, "wanderers_advice_reward", 5, 1, 1);
        addItem(pool, "acquaint_fate", 2, 1, 2);
        addItem(pool, "intertwined_fate", 2, 1, 1);
        addItem(pool, "wish_core", 2, 1, 1);
        addItem(pool, "unidentified_doll", 4, 1, 1);
        addItem(pool, "footprints_of_fate", 3, 1, 1);
        addItem(pool, "lucky_special_ticket", 1, 1, 1);
        addItem(pool, "colorful_sunglasses", 3, 1, 1);
        return pool;
    }

    private static LootPool.Builder fishingPool() {
        var pool = LootPool.builder()
                .rolls(ConstantLootNumberProvider.create(1.0F))
                .conditionally(RandomChanceLootCondition.builder(0.03F));
        addItem(pool, "mora_casket", 200, 1, 1);
        addItem(pool, "primogem_casket", 50, 1, 1);
        addItem(pool, "cosmic_casket", 50, 1, 1);
        return pool;
    }

    private static void addItem(LootPool.Builder pool, String name, int weight, int min, int max) {
        Item item = Registries.ITEM.get(Identifier.of(MOD_ID, name));
        pool.with(ItemEntry.builder(item)
                .weight(weight)
                .apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(min, max))));
    }
}