package net.per.primogemcraft.system.loot;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;

/**
 * {@code forge:add_table} 全局战利品修改器。
 *
 * <p>上游 NeoForge 1.21 内置了 {@code neoforge:add_table}；Forge 1.20.1 只内置
 * {@code forge:add_item} / {@code forge:remove_loot} / {@code forge:loot_table_id} 等，
 * 没有 add_table。数据包里的 chest_injection / fishing_injection 仍写着
 * {@code forge:add_table}，缺少该序列化器会导致两条注入全部被丢弃
 * （日志：Unknown registry key ... forge:add_table），箱子与钓鱼拿不到模组物品。
 *
 * <p>行为与 Fabric 端的 PGCLootInjection 一致：条件通过后，把 {@code table}
 * 指向的战利品表整体摇一遍，产物追加到原掉落列表。
 */
public class AddTableLootModifier extends LootModifier {
    public static final Codec<AddTableLootModifier> CODEC = RecordCodecBuilder.create(instance ->
            codecStart(instance)
                    .and(ResourceLocation.CODEC.fieldOf("table").forGetter(m -> m.table))
                    .apply(instance, AddTableLootModifier::new));

    private final ResourceLocation table;

    protected AddTableLootModifier(LootItemCondition[] conditions, ResourceLocation table) {
        super(conditions);
        this.table = table;
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        LootTable extra = context.getResolver().getLootTable(table);
        if (extra == LootTable.EMPTY) return generatedLoot;
        // 用同一上下文摇附加表；附加表自身的 pool 条件（如 random_chance）继续生效。
        extra.getRandomItemsRaw(context, LootTable.createStackSplitter(context.getLevel(), generatedLoot::add));
        return generatedLoot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}