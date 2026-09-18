package net.per.primogemcraft.system.weapon;

import net.minecraft.world.inventory.tooltip.TooltipComponent;

public record WeaponTooltip(int level, int refinement, int extra, int stacks, int stackCapacity) implements TooltipComponent {
}
