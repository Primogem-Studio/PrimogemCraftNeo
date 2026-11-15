package net.per.primogemcraft.system.zipline;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.block.BlockState;
import net.fabricmc.loader.api.FabricLoader;
import net.per.primogemcraft.block.ZiplineBaseBlock;
import net.per.primogemcraft.registry.PGCBlocks;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class ZiplineAssembly {
    public static void setup() {
        if (FabricLoader.getInstance().isModLoaded("create")) registerAttachedCheck();
        if (FabricLoader.getInstance().isModLoaded("simulated")) registerAdditionalBlocks();
    }

    private static void registerAttachedCheck() {
        var name = "com.simibubi.create.api.contraption.BlockMovementChecks";
        try {
            var result = Class.forName(name + "$CheckResult");
            var success = result.getField("SUCCESS").get(null);
            var pass = result.getField("PASS").get(null);
            register(name, "AttachedCheck", "registerAttachedCheck", (proxy, method, arguments) -> {
                var state = (BlockState) arguments[0];
                if (!state.isOf(PGCBlocks.ZIPLINE_BASE.get())) return pass;
                var level = (World) arguments[1];
                var position = (BlockPos) arguments[2];
                var neighbor = position.offset((Direction) arguments[3]);
                var other = level.getBlockState(neighbor);
                return other.isOf(PGCBlocks.ZIPLINE_BASE.get())
                        && ZiplineBaseBlock.center(state, position).equals(ZiplineBaseBlock.center(other, neighbor)) ? success : pass;
            });
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Cannot register Create zipline assembly checks", exception);
        }
    }

    private static void registerAdditionalBlocks() {
        try {
            register("dev.simulated_team.simulated.index.SimBlockMovementChecks", "AdditionalBlocks", "registerAdditionalBlocks",
                    (proxy, method, arguments) -> additionalBlocks((BlockState) arguments[0], (World) arguments[1],
                            (BlockPos) arguments[2], (Set<?>) arguments[3]));
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Cannot register Aeronautics zipline assembly checks", exception);
        }
    }

    private static List<BlockPos> additionalBlocks(BlockState state, World level, BlockPos position, Set<?> visited) {
        if (!state.isOf(PGCBlocks.ZIPLINE_BASE.get())) return List.of();
        var center = ZiplineBaseBlock.center(state, position);
        var blocks = new ArrayList<BlockPos>(9);
        for (var index = 0; index < 9; index++) {
            var part = center.add(index % 3 - 1, 0, index / 3 - 1);
            var other = level.getBlockState(part);
            if (!visited.contains(part) && other.isOf(PGCBlocks.ZIPLINE_BASE.get()) && other.get(ZiplineBaseBlock.PART) == index)
                blocks.add(part);
        }
        return blocks;
    }

    private static void register(String owner, String contract, String registration, InvocationHandler callback) throws ReflectiveOperationException {
        var type = Class.forName(owner + "$" + contract);
        var listener = Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, (proxy, method, arguments) -> {
            if (method.getDeclaringClass() == Object.class) {
                return switch (method.getName()) {
                    case "equals" -> proxy == arguments[0];
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "toString" -> "ZiplineAssembly." + contract;
                    default -> throw new UnsupportedOperationException(method.getName());
                };
            }
            return callback.invoke(proxy, method, arguments);
        });
        Class.forName(owner).getMethod(registration, type).invoke(null, listener);
    }
}
