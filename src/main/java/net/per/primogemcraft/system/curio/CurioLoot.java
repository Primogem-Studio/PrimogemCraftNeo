package net.per.primogemcraft.system.curio;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.per.primogemcraft.registry.PGCBlocks;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public final class CurioLoot {
    private static final ResourceLocation BIG_LOOT = ResourceLocation.fromNamespaceAndPath(MOD_ID, "blocks/big_jar");
    private static final ResourceLocation SMALL_LOOT = ResourceLocation.fromNamespaceAndPath(MOD_ID, "blocks/small_jar");

    private CurioLoot() {
    }

    public static boolean isJar(BlockState state) {
        return state.is(PGCBlocks.SMALL_JAR.get()) || state.is(PGCBlocks.BIG_JAR.get());
    }

    public static ResourceLocation jarLoot(BlockState state) {
        return state.is(PGCBlocks.SMALL_JAR.get()) ? SMALL_LOOT : BIG_LOOT;
    }

    public static void dropTable(ResourceLocation table, ServerLevel level, Vec3 position, BlockState state, Entity agent) {
        var loot = level.getServer().reloadableRegistries().getLootTable(ResourceKey.create(Registries.LOOT_TABLE, table));
        var params = new LootParams.Builder(level)
                .withParameter(LootContextParams.ORIGIN, position)
                .withParameter(LootContextParams.BLOCK_STATE, state)
                .withParameter(LootContextParams.TOOL, ItemStack.EMPTY)
                .withOptionalParameter(LootContextParams.THIS_ENTITY, agent)
                .create(LootContextParamSets.BLOCK);
        for (var stack : loot.getRandomItems(params)) spawn(level, position, stack);
    }

    public static void spawn(ServerLevel level, Vec3 position, ItemStack stack) {
        if (stack.isEmpty()) return;
        var entity = new ItemEntity(level, position.x, position.y, position.z, stack);
        entity.setDefaultPickUpDelay();
        level.addFreshEntity(entity);
    }
}
