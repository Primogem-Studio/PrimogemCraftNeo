package net.per.primogemcraft.system.menu;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.level.Level;
import net.per.primogemcraft.block.entity.GorgeousSmithingTableBlockEntity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class LinkedContainers {
    private static final int RADIUS = 4;
    private static final int MAX_CONTAINER_COUNT = 8;
    private static final int MAX_SLOT_COUNT = 128;

    private LinkedContainers() {
    }

    public record Link(BlockPos pos, int size) {
    }

    public static List<Link> scan(Level level, BlockPos origin) {
        var candidates = new ArrayList<Link>();
        for (var pos : BlockPos.betweenClosed(origin.offset(-RADIUS, -RADIUS, -RADIUS), origin.offset(RADIUS, RADIUS, RADIUS))) {
            var anchor = pos.immutable();
            if (anchor.equals(origin)) continue;
            var size = sizeAt(level, anchor);
            if (size > 0) candidates.add(new Link(anchor, size));
        }
        candidates.sort(Comparator.comparingDouble((Link link) -> link.pos().distSqr(origin)).thenComparing(Link::pos));
        var links = new ArrayList<Link>();
        var room = MAX_SLOT_COUNT;
        for (var link : candidates) {
            if (links.size() >= MAX_CONTAINER_COUNT) break;
            if (link.size() > room) continue;
            room -= link.size();
            links.add(link);
        }
        return List.copyOf(links);
    }

    public static void write(FriendlyByteBuf buffer, List<Link> links) {
        buffer.writeVarInt(links.size());
        for (var link : links) {
            buffer.writeBlockPos(link.pos());
            buffer.writeVarInt(link.size());
        }
    }

    public static List<Link> read(FriendlyByteBuf buffer) {
        var count = buffer.readVarInt();
        var links = new ArrayList<Link>(count);
        for (var index = 0; index < count; index++) links.add(new Link(buffer.readBlockPos(), buffer.readVarInt()));
        return List.copyOf(links);
    }

    public static Container resolve(Level level, BlockPos pos) {
        var chunk = level.getChunkSource().getChunkNow(SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getZ()));
        if (chunk == null) return null;
        return chunk.getBlockEntity(pos) instanceof Container container ? container : null;
    }

    private static int sizeAt(Level level, BlockPos pos) {
        var container = resolve(level, pos);
        if (container == null || container instanceof GorgeousSmithingTableBlockEntity) return 0;
        return container.getContainerSize();
    }
}
