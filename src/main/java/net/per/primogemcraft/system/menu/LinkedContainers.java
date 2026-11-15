package net.per.primogemcraft.system.menu;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.inventory.Inventory;
import net.minecraft.world.World;
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

    public static List<Link> scan(World level, BlockPos origin) {
        var candidates = new ArrayList<Link>();
        for (var pos : BlockPos.iterate(origin.add(-RADIUS, -RADIUS, -RADIUS), origin.add(RADIUS, RADIUS, RADIUS))) {
            var anchor = pos.toImmutable();
            if (anchor.equals(origin)) continue;
            var size = sizeAt(level, anchor);
            if (size > 0) candidates.add(new Link(anchor, size));
        }
        candidates.sort(Comparator.comparingDouble((Link link) -> link.pos().getSquaredDistance(origin)).thenComparing(Link::pos));
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

    public static void write(PacketByteBuf buffer, List<Link> links) {
        buffer.writeVarInt(links.size());
        for (var link : links) {
            buffer.writeBlockPos(link.pos());
            buffer.writeVarInt(link.size());
        }
    }

    public static List<Link> read(PacketByteBuf buffer) {
        var count = buffer.readVarInt();
        var links = new ArrayList<Link>(count);
        for (var index = 0; index < count; index++) links.add(new Link(buffer.readBlockPos(), buffer.readVarInt()));
        return List.copyOf(links);
    }

    public static Inventory resolve(World level, BlockPos pos) {
        if (!level.isChunkLoaded(pos)) return null;
        return level.getBlockEntity(pos) instanceof Inventory container ? container : null;
    }

    private static int sizeAt(World level, BlockPos pos) {
        var container = resolve(level, pos);
        if (container == null || container instanceof GorgeousSmithingTableBlockEntity) return 0;
        return container.size();
    }
}
