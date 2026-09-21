package net.per.primogemcraft.command.debug;

import com.google.gson.JsonParser;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.per.primogemcraft.block.ZiplineBaseBlock;
import net.per.primogemcraft.entity.misc.ZiplineAnchorEntity;
import net.per.primogemcraft.registry.PGCBlocks;
import net.per.primogemcraft.registry.PGCEntities;
import net.per.primogemcraft.system.zipline.ZiplineGrip;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public final class Zipline {
    private static final int MAX_RADIUS = 256;
    private static final int MAX_COUNT = 256;
    private static final int MAX_SUPPORT = 8;

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        var root = Commands.literal("zipline").requires(source -> source.hasPermission(2))
                .executes(context -> preset(context.getSource(), "square", 32, 16, MAX_COUNT));
        root.then(parameters(Commands.literal("grid"), "square"));
        for (var shape : List.of("square", "circle", "ring", "line"))
            root.then(parameters(Commands.literal(shape), shape));
        root.then(Commands.literal("path")
                .then(Commands.argument("spacing", IntegerArgumentType.integer(3, 128))
                        .then(Commands.argument("count", IntegerArgumentType.integer(1, MAX_COUNT))
                                .then(Commands.argument("points", StringArgumentType.greedyString())
                                        .executes(context -> path(context.getSource(),
                                                IntegerArgumentType.getInteger(context, "spacing"),
                                                IntegerArgumentType.getInteger(context, "count"),
                                                StringArgumentType.getString(context, "points")))))));
        dispatcher.register(Commands.literal(MOD_ID).then(root));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> parameters(LiteralArgumentBuilder<CommandSourceStack> command, String shape) {
        return command.executes(context -> preset(context.getSource(), shape, 32, 16, MAX_COUNT))
                .then(Commands.argument("radius", IntegerArgumentType.integer(1, MAX_RADIUS))
                        .executes(context -> preset(context.getSource(), shape, IntegerArgumentType.getInteger(context, "radius"), 16, MAX_COUNT))
                        .then(Commands.argument("spacing", IntegerArgumentType.integer(3, 128))
                                .executes(context -> preset(context.getSource(), shape, IntegerArgumentType.getInteger(context, "radius"),
                                        IntegerArgumentType.getInteger(context, "spacing"), MAX_COUNT))
                                .then(Commands.argument("count", IntegerArgumentType.integer(1, MAX_COUNT))
                                        .executes(context -> preset(context.getSource(), shape, IntegerArgumentType.getInteger(context, "radius"),
                                                IntegerArgumentType.getInteger(context, "spacing"), IntegerArgumentType.getInteger(context, "count"))))));
    }

    private static int preset(CommandSourceStack source, String shape, int radius, int spacing, int count) throws CommandSyntaxException {
        var points = new LinkedHashSet<BlockPos>();
        if (shape.equals("ring")) {
            var samples = Math.max(1, (int) Math.floor(Math.PI * 2 * radius / spacing));
            for (var index = 0; index < samples; index++) {
                var angle = Math.PI * 2 * index / samples;
                points.add(new BlockPos((int) Math.round(Math.cos(angle) * radius), 0, (int) Math.round(Math.sin(angle) * radius)));
            }
        } else {
            var extent = radius / spacing * spacing;
            for (var x = -extent; x <= extent; x += spacing) {
                for (var z = -extent; z <= extent; z += spacing) {
                    if (shape.equals("line") && z != 0) continue;
                    if (shape.equals("circle") && x * x + z * z > radius * radius) continue;
                    points.add(new BlockPos(x, 0, z));
                }
            }
        }
        var ordered = new ArrayList<>(points);
        ordered.sort(Comparator.comparingDouble(position -> position.distSqr(BlockPos.ZERO)));
        return place(source, ordered, count);
    }

    private static int path(CommandSourceStack source, int spacing, int count, String input) throws CommandSyntaxException {
        var vertices = new ArrayList<BlockPos>();
        try {
            var array = JsonParser.parseString(input).getAsJsonArray();
            if (array.size() < 2 || array.size() > 64) throw new IllegalArgumentException();
            for (var element : array) {
                var pair = element.getAsJsonArray();
                if (pair.size() != 2) throw new IllegalArgumentException();
                var coordinates = new int[2];
                for (var index = 0; index < 2; index++) {
                    if (!pair.get(index).isJsonPrimitive() || !pair.get(index).getAsJsonPrimitive().isNumber()) throw new IllegalArgumentException();
                    var value = pair.get(index).getAsDouble();
                    if (!Double.isFinite(value) || value != Math.rint(value) || Math.abs(value) > MAX_RADIUS) throw new IllegalArgumentException();
                    coordinates[index] = (int) value;
                }
                vertices.add(new BlockPos(coordinates[0], 0, coordinates[1]));
            }
        } catch (RuntimeException exception) {
            source.sendFailure(Component.translatable("command.primogemcraft.zipline.invalid_path"));
            return 0;
        }
        var points = new LinkedHashSet<BlockPos>();
        for (var index = 1; index < vertices.size(); index++) {
            var start = vertices.get(index - 1);
            var end = vertices.get(index);
            var distance = Math.sqrt(start.distSqr(end));
            for (var offset = 0.0; offset < distance; offset += spacing) {
                var fraction = offset / distance;
                points.add(new BlockPos((int) Math.round(start.getX() + (end.getX() - start.getX()) * fraction), 0,
                        (int) Math.round(start.getZ() + (end.getZ() - start.getZ()) * fraction)));
            }
            points.add(end);
        }
        return place(source, new ArrayList<>(points), count);
    }

    private static int place(CommandSourceStack source, List<BlockPos> offsets, int count) throws CommandSyntaxException {
        var player = source.getPlayerOrException();
        var origin = BlockPos.containing(source.getPosition());
        var level = source.getLevel();
        var placed = 0;
        var skipped = 0;
        for (var offset : offsets) {
            if (placed >= count) break;
            if (placeAnchor(level, origin.offset(offset.getX(), 0, offset.getZ()), ZiplineGrip.modelYaw(player.getDirection().toYRot()))) placed++;
            else skipped++;
        }
        var successful = placed;
        var rejected = skipped;
        source.sendSuccess(() -> Component.translatable("command.primogemcraft.zipline.placed", successful, rejected, count), true);
        return placed;
    }

    private static boolean placeAnchor(ServerLevel level, BlockPos horizontal, float yaw) {
        var surfaces = new ArrayList<BlockPos>();
        var highest = level.getMinBuildHeight();
        for (var x = -1; x <= 1; x++) {
            for (var z = -1; z <= 1; z++) {
                var column = horizontal.offset(x, 0, z);
                if (!level.hasChunkAt(column) || !level.getWorldBorder().isWithinBounds(column)) return false;
                var surface = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, column).below();
                var state = level.getBlockState(surface);
                if (level.isOutsideBuildHeight(surface) || !state.getFluidState().isEmpty()
                        || !state.isFaceSturdy(level, surface, Direction.UP) || state.is(PGCBlocks.ZIPLINE_BASE)) return false;
                surfaces.add(surface);
                highest = Math.max(highest, surface.getY());
            }
        }
        var center = new BlockPos(horizontal.getX(), highest + 1, horizontal.getZ());
        var original = new LinkedHashMap<BlockPos, BlockState>();
        for (var surface : surfaces) {
            if (highest - surface.getY() > MAX_SUPPORT) return false;
            for (var y = surface.getY() + 1; y <= center.getY() + 6; y++) {
                var position = new BlockPos(surface.getX(), y, surface.getZ());
                var state = level.getBlockState(position);
                if (level.isOutsideBuildHeight(position) || !state.canBeReplaced() || !state.getFluidState().isEmpty()
                        || level.getBlockEntity(position) != null) return false;
                if (y <= center.getY()) original.put(position, state);
            }
        }
        var anchor = new ZiplineAnchorEntity(PGCEntities.ZIPLINE_ANCHOR.get(), level);
        anchor.setPos(center.getX() + 0.5, center.getY(), center.getZ() + 0.5);
        anchor.setYRot(yaw);
        for (var position : original.keySet()) {
            var part = (position.getZ() - center.getZ() + 1) * 3 + position.getX() - center.getX() + 1;
            var state = position.getY() == center.getY()
                    ? PGCBlocks.ZIPLINE_BASE.get().defaultBlockState().setValue(ZiplineBaseBlock.PART, part)
                    : Blocks.STONE.defaultBlockState();
            if (!level.setBlock(position, state, 3)) {
                restore(level, original);
                return false;
            }
        }
        if (!level.addFreshEntity(anchor)) {
            restore(level, original);
            return false;
        }
        return true;
    }

    private static void restore(ServerLevel level, LinkedHashMap<BlockPos, BlockState> original) {
        for (var entry : original.entrySet()) level.setBlock(entry.getKey(), entry.getValue(), 3);
    }
}
