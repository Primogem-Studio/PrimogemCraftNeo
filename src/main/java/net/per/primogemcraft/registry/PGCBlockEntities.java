package net.per.primogemcraft.registry;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.per.primogemcraft.block.entity.ContainerWindowBlockEntity;
import net.per.primogemcraft.block.entity.GorgeousSmithingTableBlockEntity;
import net.per.primogemcraft.block.entity.MoraPileBlockEntity;
import net.per.primogemcraft.block.entity.StellarConverterBlockEntity;
import net.per.primogemcraft.block.entity.UnidentifiedDollBlockEntity;
import net.per.primogemcraft.block.entity.XiaoLanternLauncherBlockEntity;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public class PGCBlockEntities {
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, STELLAR_CONVERTER.get(),
                (converter, side) -> converter.itemHandler());
    }

    public static final String UNIDENTIFIED_DOLL_NAME = "unidentified_doll";
    public static final String GORGEOUS_SMITHING_TABLE_NAME = "gorgeous_smithing_table";
    public static final String STELLAR_CONVERTER_NAME = "stellar_converter";
    public static final String MORA_PILE_NAME = "mora_pile";
    public static final String XIAO_LANTERN_LAUNCHER_NAME = "xiao_lantern_launcher";
    public static final String CONTAINER_WINDOW_NAME = "container_window";

    public static final DeferredRegister<BlockEntityType<?>> REGISTRY = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, MOD_ID);

    @SuppressWarnings("DataFlowIssue")
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<UnidentifiedDollBlockEntity>> UNIDENTIFIED_DOLL =
            REGISTRY.register(UNIDENTIFIED_DOLL_NAME, () -> BlockEntityType.Builder.of(UnidentifiedDollBlockEntity::new, PGCBlocks.UNIDENTIFIED_DOLL.get()).build(null));

    @SuppressWarnings("DataFlowIssue")
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GorgeousSmithingTableBlockEntity>> GORGEOUS_SMITHING_TABLE =
            REGISTRY.register(GORGEOUS_SMITHING_TABLE_NAME, () -> BlockEntityType.Builder.of(GorgeousSmithingTableBlockEntity::new, PGCBlocks.GORGEOUS_SMITHING_TABLE.get()).build(null));

    @SuppressWarnings("DataFlowIssue")
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<StellarConverterBlockEntity>> STELLAR_CONVERTER =
            REGISTRY.register(STELLAR_CONVERTER_NAME, () -> BlockEntityType.Builder.of(StellarConverterBlockEntity::new, PGCBlocks.STELLAR_CONVERTER.get()).build(null));

    @SuppressWarnings("DataFlowIssue")
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MoraPileBlockEntity>> MORA_PILE =
            REGISTRY.register(MORA_PILE_NAME, () -> BlockEntityType.Builder.of(MoraPileBlockEntity::new, PGCBlocks.MORA_PILE.get()).build(null));

    @SuppressWarnings("DataFlowIssue")
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ContainerWindowBlockEntity>> CONTAINER_WINDOW =
            REGISTRY.register(CONTAINER_WINDOW_NAME, () -> BlockEntityType.Builder.of(ContainerWindowBlockEntity::new,
                    PGCBlocks.TRASH_CAN.get(), PGCBlocks.CLEAN_TRASH_CAN.get(), PGCBlocks.MORA_TRASH_CAN.get()).build(null));

    @SuppressWarnings("DataFlowIssue")
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<XiaoLanternLauncherBlockEntity>> XIAO_LANTERN_LAUNCHER =
            REGISTRY.register(XIAO_LANTERN_LAUNCHER_NAME, () -> BlockEntityType.Builder.of(XiaoLanternLauncherBlockEntity::new,
                    PGCBlocks.XIAO_LANTERN_LAUNCHER.get(), PGCBlocks.CREATIVE_XIAO_LANTERN_LAUNCHER.get()).build(null));
}
