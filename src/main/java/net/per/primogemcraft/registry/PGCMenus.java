package net.per.primogemcraft.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.per.primogemcraft.compat.registries.DeferredHolder;
import net.per.primogemcraft.compat.registries.DeferredRegister;
import net.per.primogemcraft.system.menu.ContainerWindowMenu;
import net.per.primogemcraft.system.menu.GorgeousSmithingTableMenu;
import net.per.primogemcraft.system.menu.StellarConverterMenu;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public class PGCMenus {
    public static final DeferredRegister<MenuType<?>> REGISTRY = DeferredRegister.create(Registries.MENU, MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<GorgeousSmithingTableMenu>> GORGEOUS_SMITHING_TABLE =
            REGISTRY.register("gorgeous_smithing_table", () -> IForgeMenuType.create(GorgeousSmithingTableMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<StellarConverterMenu>> STELLAR_CONVERTER =
            REGISTRY.register("stellar_converter", () -> IForgeMenuType.create(StellarConverterMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<ContainerWindowMenu>> CONTAINER_WINDOW =
            REGISTRY.register("container_window", () -> IForgeMenuType.create(ContainerWindowMenu::new));
}
