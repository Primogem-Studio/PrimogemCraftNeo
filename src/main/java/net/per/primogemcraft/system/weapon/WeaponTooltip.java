package net.per.primogemcraft.system.weapon;

import net.minecraft.client.item.TooltipData;

public record WeaponTooltip(int level, int refinement, int extra, int stacks, int stackCapacity) implements TooltipData {
}
