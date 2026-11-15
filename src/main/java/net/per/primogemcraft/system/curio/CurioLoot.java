package net.per.primogemcraft.system.curio;

import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.block.BlockState;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.util.math.Vec3d;
import net.per.primogemcraft.registry.PGCBlocks;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public final class CurioLoot {
    private static final Identifier BIG_LOOT = Identifier.of(MOD_ID, "blocks/big_jar");
    private static final Identifier SMALL_LOOT = Identifier.of(MOD_ID, "blocks/small_jar");

    private CurioLoot() {
    }

    public static boolean isJar(BlockState state) {
        return state.isOf(PGCBlocks.SMALL_JAR.get()) || state.isOf(PGCBlocks.BIG_JAR.get());
    }

    public static Identifier jarLoot(BlockState state) {
        return state.isOf(PGCBlocks.SMALL_JAR.get()) ? SMALL_LOOT : BIG_LOOT;
    }

    public static void dropTable(Identifier table, ServerWorld level, Vec3d position, BlockState state, Entity agent) {
        var loot = level.getServer().getLootManager().getLootTable(table);
        var params = new LootContextParameterSet.Builder(level)
                .add(LootContextParameters.ORIGIN, position)
                .add(LootContextParameters.BLOCK_STATE, state)
                .add(LootContextParameters.TOOL, ItemStack.EMPTY)
                .addOptional(LootContextParameters.THIS_ENTITY, agent)
                .build(LootContextTypes.BLOCK);
        for (var stack : loot.generateLoot(params)) spawn(level, position, stack);
    }

    public static void spawn(ServerWorld level, Vec3d position, ItemStack stack) {
        if (stack.isEmpty()) return;
        var entity = new ItemEntity(level, position.getX(), position.getY(), position.getZ(), stack);
        entity.setPickupDelay(0);
        level.spawnEntity(entity);
    }
}
