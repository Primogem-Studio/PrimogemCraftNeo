package net.per.primogemcraft.registry;

import com.mojang.serialization.Codec;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.per.primogemcraft.system.loot.AddTableLootModifier;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

/**
 * 全局战利品修改器序列化器注册。
 *
 * <p>这是 Forge 自有注册表（{@code forge:global_loot_modifier_serializers}），
 * 不属于原版 {@link net.minecraft.core.registries.Registries}，因此直接用 Forge 的
 * {@link DeferredRegister}，不走 compat 层。
 *
 * <p>注册名必须是 {@code add_table}，配合 {@code forge} 命名空间才能匹配数据包里
 * 写死的 {@code "type": "forge:add_table"}。故此处 modid 传 "forge"。
 */
public class PGCLootModifiers {
    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> REGISTRY =
            DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, "forge");

    public static final RegistryObject<Codec<? extends IGlobalLootModifier>> ADD_TABLE =
            REGISTRY.register("add_table", () -> AddTableLootModifier.CODEC);

    private PGCLootModifiers() {
    }
}