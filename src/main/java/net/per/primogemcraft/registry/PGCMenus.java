package net.per.primogemcraft.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.per.primogemcraft.system.menu.ContainerWindowMenu;
import net.per.primogemcraft.system.menu.GorgeousSmithingTableMenu;
import net.per.primogemcraft.system.menu.StellarConverterMenu;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public class PGCMenus {
    public static final DeferredRegister<MenuType<?>> REGISTRY = DeferredRegister.create(Registries.MENU, MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<GorgeousSmithingTableMenu>> GORGEOUS_SMITHING_TABLE =
            REGISTRY.register("gorgeous_smithing_table", () -> IMenuTypeExtension.create(GorgeousSmithingTableMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<StellarConverterMenu>> STELLAR_CONVERTER =
            REGISTRY.register("stellar_converter", () -> IMenuTypeExtension.create(StellarConverterMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<ContainerWindowMenu>> CONTAINER_WINDOW =
            REGISTRY.register("container_window", () -> IMenuTypeExtension.create(ContainerWindowMenu::new));
}
