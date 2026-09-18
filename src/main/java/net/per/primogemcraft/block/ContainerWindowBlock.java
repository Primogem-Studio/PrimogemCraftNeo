package net.per.primogemcraft.block;

/**
 * A block that opens the shared container window on right-click. The window reads the slot count from the block, so a
 * single block entity type serves every container size.
 */
public interface ContainerWindowBlock {
    int containerSize();
}
