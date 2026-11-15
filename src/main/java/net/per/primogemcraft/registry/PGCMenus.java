package net.per.primogemcraft.registry;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenHandlerType;
import net.per.primogemcraft.compat.registries.DeferredHolder;
import net.per.primogemcraft.compat.registries.DeferredRegister;
import net.per.primogemcraft.system.menu.ContainerWindowMenu;
import net.per.primogemcraft.system.menu.GorgeousSmithingTableMenu;
import net.per.primogemcraft.system.menu.StellarConverterMenu;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public class PGCMenus {
    public static final DeferredRegister<ScreenHandlerType<?>> REGISTRY = DeferredRegister.create(Registries.SCREEN_HANDLER, MOD_ID);

    public static final DeferredHolder<ScreenHandlerType<?>, ScreenHandlerType<GorgeousSmithingTableMenu>> GORGEOUS_SMITHING_TABLE =
            REGISTRY.register("gorgeous_smithing_table", () -> new ExtendedScreenHandlerType<>(GorgeousSmithingTableMenu::new));

    public static final DeferredHolder<ScreenHandlerType<?>, ScreenHandlerType<StellarConverterMenu>> STELLAR_CONVERTER =
            REGISTRY.register("stellar_converter", () -> new ExtendedScreenHandlerType<>(StellarConverterMenu::new));

    public static final DeferredHolder<ScreenHandlerType<?>, ScreenHandlerType<ContainerWindowMenu>> CONTAINER_WINDOW =
            REGISTRY.register("container_window", () -> new ExtendedScreenHandlerType<>(ContainerWindowMenu::new));
}