package net.per.primogemcraft.registry;

import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.per.primogemcraft.block.*;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public class PGCBlocks {
    public static final DeferredRegister.Blocks REGISTRY = DeferredRegister.createBlocks(MOD_ID);

    public static final DeferredBlock<ZiplineBaseBlock> ZIPLINE_BASE = REGISTRY.register("zipline_base", () -> new ZiplineBaseBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.METAL).sound(SoundType.METAL).strength(5f, 30f)
                    .requiresCorrectToolForDrops().noOcclusion().pushReaction(PushReaction.BLOCK).noLootTable()));

    public static final DeferredBlock<Block> PRIMOGEM_ORE = REGISTRY.registerSimpleBlock("primogem_ore",
            BlockBehaviour.Properties.of().mapColor(MapColor.DIAMOND).sound(SoundType.ANCIENT_DEBRIS).strength(3f, 5f)
                    .requiresCorrectToolForDrops().instrument(NoteBlockInstrument.BASEDRUM));

    public static final DeferredBlock<Block> OTHERWORLD_LOG_PLANKS = REGISTRY.registerSimpleBlock("otherworld_log_planks",
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PINK).sound(SoundType.WOOD).strength(1f).ignitedByLava()
                    .instrument(NoteBlockInstrument.BASS));

    public static final DeferredBlock<Block> INTERTWINED_FATE_BLOCK = REGISTRY.registerSimpleBlock("intertwined_fate_block",
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PINK).sound(SoundType.METAL).strength(30f)
                    .requiresCorrectToolForDrops().instrument(NoteBlockInstrument.BASEDRUM));

    public static final DeferredBlock<MoraPileBlock> MORA_PILE = REGISTRY.register("mora_pile", () -> new MoraPileBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.GOLD).sound(SoundType.CHAIN).strength(1f, 6f).noOcclusion()
                    .isRedstoneConductor((bs, br, bp) -> false)));

    public static final DeferredBlock<Block> MORA_BLOCK = REGISTRY.registerSimpleBlock("mora_block",
            BlockBehaviour.Properties.of().mapColor(MapColor.GOLD).sound(SoundType.METAL).strength(3f, 100f)
                    .requiresCorrectToolForDrops());

    public static final DeferredBlock<StairBlock> OTHERWORLD_WOOD_STAIRS =
            REGISTRY.register("otherworld_wood_stairs", () -> new StairBlock(OTHERWORLD_LOG_PLANKS.get().defaultBlockState(),
                    BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PINK).sound(SoundType.WOOD).strength(2f, 10f).ignitedByLava()
                            .instrument(NoteBlockInstrument.BASS)));

    public static final DeferredBlock<SlabBlock> OTHERWORLD_LOG_SLAB =
            REGISTRY.register("otherworld_log_slab", () -> new SlabBlock(
                    BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PINK).sound(SoundType.WOOD).strength(2f, 10f).ignitedByLava()
                            .instrument(NoteBlockInstrument.BASS)));

    public static final DeferredBlock<FenceGateBlock> OTHERWORLD_WOOD_FENCE_GATE =
            REGISTRY.register("otherworld_wood_fence_gate", () -> new FenceGateBlock(WoodType.OAK,
                    BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PINK).sound(SoundType.WOOD).strength(3f, 30f).noOcclusion()
                            .isRedstoneConductor((bs, br, bp) -> false).ignitedByLava().instrument(NoteBlockInstrument.BASS)
                            .forceSolidOn()));

    public static final DeferredBlock<FenceBlock> OTHERWORLD_WOOD_FENCE =
            REGISTRY.register("otherworld_wood_fence", () -> new FenceBlock(
                    BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PINK).sound(SoundType.WOOD).strength(3f, 30f).noOcclusion()
                            .isRedstoneConductor((bs, br, bp) -> false).ignitedByLava().instrument(NoteBlockInstrument.BASS)
                            .forceSolidOn()));

    public static final DeferredBlock<March7thStatueBlock> MARCH_7TH_STATUE = REGISTRY.register("march_7th_statue", () -> new March7thStatueBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PINK).strength(3f, 100f).requiresCorrectToolForDrops()
                    .friction(0.1f).noOcclusion().isRedstoneConductor((bs, br, bp) -> false)
                    .instrument(NoteBlockInstrument.BASEDRUM),
            Block.box(4, 0, 4, 12, 13, 12)));

    public static final DeferredBlock<Block> REFINED_MORA_BLOCK = REGISTRY.registerSimpleBlock("refined_mora_block",
            BlockBehaviour.Properties.of().mapColor(MapColor.GOLD).sound(SoundType.METAL).strength(4f, 100f)
                    .requiresCorrectToolForDrops());

    public static final DeferredBlock<Block> CHEAP_MORA_BLOCK = REGISTRY.registerSimpleBlock("cheap_mora_block",
            BlockBehaviour.Properties.of().mapColor(MapColor.GOLD).strength(2f, 4f).requiresCorrectToolForDrops()
                    .instrument(NoteBlockInstrument.BASEDRUM));

    public static final DeferredBlock<SlabBlock> CHEAP_MORA_BLOCK_SLAB =
            REGISTRY.register("cheap_mora_block_slab", () -> new SlabBlock(
                    BlockBehaviour.Properties.of().mapColor(MapColor.GOLD).strength(2f, 4f).instrument(NoteBlockInstrument.BASEDRUM)));

    public static final DeferredBlock<StairBlock> CHEAP_MORA_BLOCK_STAIRS =
            REGISTRY.register("cheap_mora_block_stairs", () -> new StairBlock(CHEAP_MORA_BLOCK.get().defaultBlockState(),
                    BlockBehaviour.Properties.of().mapColor(MapColor.GOLD).strength(2f, 4f).requiresCorrectToolForDrops()
                            .instrument(NoteBlockInstrument.BASEDRUM)));

    public static final DeferredBlock<WallBlock> CHEAP_MORA_BLOCK_WALL =
            REGISTRY.register("cheap_mora_block_wall", () -> new WallBlock(
                    BlockBehaviour.Properties.of().mapColor(MapColor.GOLD).strength(3f, 30f).noOcclusion()
                            .isRedstoneConductor((bs, br, bp) -> false).instrument(NoteBlockInstrument.BASEDRUM).forceSolidOn()));

    public static final DeferredBlock<PGCHorizontalFallingBlock> SMALL_JAR = REGISTRY.register("small_jar", () -> new PGCHorizontalFallingBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.DIRT).sound(SoundType.DECORATED_POT).strength(0f, 1f).noCollission()
                    .noOcclusion().isRedstoneConductor((bs, br, bp) -> false).offsetType(BlockBehaviour.OffsetType.XZ),
            Block.box(5, 0, 5, 11, 7, 11)));

    public static final DeferredBlock<PGCHorizontalFallingBlock> BIG_JAR = REGISTRY.register("big_jar", () -> new PGCHorizontalFallingBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.DIRT).sound(SoundType.DECORATED_POT).strength(0f, 1f).noOcclusion()
                    .isRedstoneConductor((bs, br, bp) -> false).dynamicShape().offsetType(BlockBehaviour.OffsetType.XZ),
            Block.box(4, 0, 4, 12, 10, 12)));

    private static final int CLEAN_TRASH_CAN_SIZE = 45;
    private static final int MORA_TRASH_CAN_SIZE = 54;

    public static final DeferredBlock<TrashCanBlock> TRASH_CAN = REGISTRY.register("trash_can", () -> new TrashCanBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).sound(SoundType.ANVIL).strength(3f, 15f).noOcclusion()
                    .isRedstoneConductor((bs, br, bp) -> false),
            Block.box(2, 0, 2, 14, 16, 14)));

    public static final DeferredBlock<FaceAttachedContainerBlock> CLEAN_TRASH_CAN = REGISTRY.register("clean_trash_can", () -> new FaceAttachedContainerBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.METAL).sound(SoundType.ANVIL).strength(3f, 15f)
                    .requiresCorrectToolForDrops().noOcclusion().isRedstoneConductor((bs, br, bp) -> false),
            PGCBlocks::trashCanShape, CLEAN_TRASH_CAN_SIZE));

    public static final DeferredBlock<FaceAttachedContainerBlock> MORA_TRASH_CAN = REGISTRY.register("mora_trash_can", () -> new FaceAttachedContainerBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.GOLD).sound(SoundType.ANVIL).strength(4f, 10f).noOcclusion()
                    .isRedstoneConductor((bs, br, bp) -> false),
            PGCBlocks::trashCanShape, MORA_TRASH_CAN_SIZE));

    public static final DeferredBlock<Block> VAYUDA_TURQUOISE_ORE = REGISTRY.registerSimpleBlock("vayuda_turquoise_ore",
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_CYAN).strength(3f, 5f).requiresCorrectToolForDrops()
                    .instrument(NoteBlockInstrument.BASEDRUM));

    public static final DeferredBlock<Block> VAYUDA_TURQUOISE_BLOCK = REGISTRY.registerSimpleBlock("vayuda_turquoise_block",
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_CYAN).sound(SoundType.GLASS).strength(3f, 10f)
                    .requiresCorrectToolForDrops().instrument(NoteBlockInstrument.BASEDRUM));

    public static final DeferredBlock<Block> PRITHIVA_TOPAZ_ORE = REGISTRY.registerSimpleBlock("prithiva_topaz_ore",
            BlockBehaviour.Properties.of().mapColor(MapColor.GOLD).strength(4f, 5f).requiresCorrectToolForDrops()
                    .instrument(NoteBlockInstrument.BASEDRUM));

    public static final DeferredBlock<Block> PRITHIVA_TOPAZ_BLOCK = REGISTRY.registerSimpleBlock("prithiva_topaz_block",
            BlockBehaviour.Properties.of().mapColor(MapColor.GOLD).sound(SoundType.GLASS).strength(3f, 100f)
                    .requiresCorrectToolForDrops().instrument(NoteBlockInstrument.BASEDRUM));

    public static final DeferredBlock<Block> VAJRADA_AMETHYST_ORE = REGISTRY.registerSimpleBlock("vajrada_amethyst_ore",
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).strength(3f, 5f).requiresCorrectToolForDrops()
                    .instrument(NoteBlockInstrument.BASEDRUM));

    public static final DeferredBlock<Block> VAJRADA_AMETHYST_BLOCK = REGISTRY.registerSimpleBlock("vajrada_amethyst_block",
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).sound(SoundType.GLASS).strength(3f, 11f));

    public static final DeferredBlock<CrystalOreBlock> NAGADUS_EMERALD_ORE = REGISTRY.register("nagadus_emerald_ore", () -> new CrystalOreBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.GRASS).sound(SoundType.GRAVEL).strength(2f)
                    .requiresCorrectToolForDrops()));

    public static final DeferredBlock<Block> NAGADUS_EMERALD_BLOCK = REGISTRY.registerSimpleBlock("nagadus_emerald_block",
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GREEN).sound(SoundType.GLASS).strength(3f, 15f));

    public static final DeferredBlock<Block> DENDRO_CORE_BLOCK = REGISTRY.registerSimpleBlock("dendro_core_block",
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GREEN).strength(10f, 20f).requiresCorrectToolForDrops()
                    .instrument(NoteBlockInstrument.BASEDRUM));

    public static final DeferredBlock<Block> DENDRO_CORE_PLANKS = REGISTRY.registerSimpleBlock("dendro_core_planks",
            BlockBehaviour.Properties.of().mapColor(MapColor.GRASS).sound(SoundType.WOOD).strength(2f, 10f).ignitedByLava()
                    .instrument(NoteBlockInstrument.BASS));

    public static final DeferredBlock<StairBlock> DENDRO_CORE_PLANKS_STAIRS =
            REGISTRY.register("dendro_core_planks_stairs", () -> new StairBlock(DENDRO_CORE_PLANKS.get().defaultBlockState(),
                    BlockBehaviour.Properties.of().mapColor(MapColor.GRASS).sound(SoundType.WOOD).strength(3f, 2f).ignitedByLava()
                            .instrument(NoteBlockInstrument.BASS)));

    public static final DeferredBlock<SlabBlock> DENDRO_CORE_PLANKS_SLAB =
            REGISTRY.register("dendro_core_planks_slab", () -> new SlabBlock(
                    BlockBehaviour.Properties.of().mapColor(MapColor.GRASS).sound(SoundType.WOOD).strength(2f, 3f).ignitedByLava()
                            .instrument(NoteBlockInstrument.BASS)));

    public static final DeferredBlock<FenceBlock> DENDRO_CORE_PLANKS_FENCE =
            REGISTRY.register("dendro_core_planks_fence", () -> new FenceBlock(
                    BlockBehaviour.Properties.of().mapColor(MapColor.GRASS).sound(SoundType.WOOD).strength(2f, 3f).ignitedByLava()
                            .instrument(NoteBlockInstrument.BASS).forceSolidOn()));

    public static final DeferredBlock<FenceGateBlock> DENDRO_CORE_PLANKS_FENCE_GATE =
            REGISTRY.register("dendro_core_planks_fence_gate", () -> new FenceGateBlock(WoodType.OAK,
                    BlockBehaviour.Properties.of().mapColor(MapColor.GRASS).sound(SoundType.WOOD).strength(2f, 3f).ignitedByLava()
                            .instrument(NoteBlockInstrument.BASS).forceSolidOn()));

    public static final DeferredBlock<PressurePlateBlock> DENDRO_CORE_PLANKS_PRESSURE_PLATE =
            REGISTRY.register("dendro_core_planks_pressure_plate", () -> new PressurePlateBlock(BlockSetType.OAK,
                    BlockBehaviour.Properties.of().mapColor(MapColor.GRASS).sound(SoundType.WOOD).strength(2f, 3f).ignitedByLava()
                            .instrument(NoteBlockInstrument.BASS).forceSolidOn()));

    public static final DeferredBlock<ButtonBlock> DENDRO_CORE_PLANKS_BUTTON =
            REGISTRY.register("dendro_core_planks_button", () -> new ButtonBlock(BlockSetType.OAK, 30,
                    BlockBehaviour.Properties.of().mapColor(MapColor.GRASS).sound(SoundType.WOOD).strength(2f, 3f).ignitedByLava()
                            .instrument(NoteBlockInstrument.BASS)));

    public static final DeferredBlock<CrystalOreFallingBlock> VARUNADA_LAZURITE_ORE =
            REGISTRY.register("varunada_lazurite_ore", () -> new CrystalOreFallingBlock(
                    BlockBehaviour.Properties.of().mapColor(MapColor.WATER).sound(SoundType.GRAVEL).strength(2f)
                            .requiresCorrectToolForDrops().noOcclusion().isRedstoneConductor((bs, br, bp) -> false)));

    public static final DeferredBlock<Block> VARUNADA_LAZURITE_BLOCK = REGISTRY.registerSimpleBlock("varunada_lazurite_block",
            BlockBehaviour.Properties.of().mapColor(MapColor.WATER).sound(SoundType.GLASS).strength(3f, 20f)
                    .requiresCorrectToolForDrops());

    public static final DeferredBlock<Block> CHEAP_NETHERITE_BLOCK = REGISTRY.registerSimpleBlock("cheap_netherite_block",
            BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_BLACK).sound(SoundType.NETHERITE_BLOCK).strength(3f, 10f)
                    .requiresCorrectToolForDrops());

    public static final DeferredBlock<Block> AGNIDUS_AGATE_ORE = REGISTRY.registerSimpleBlock("agnidus_agate_ore",
            BlockBehaviour.Properties.of().mapColor(MapColor.FIRE).sound(SoundType.NETHER_ORE).strength(1f, 2f)
                    .requiresCorrectToolForDrops().instrument(NoteBlockInstrument.BASEDRUM));

    public static final DeferredBlock<AgnidusAgateBlock> AGNIDUS_AGATE_BLOCK = REGISTRY.register("agnidus_agate_block", () -> new AgnidusAgateBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.FIRE).sound(SoundType.GLASS).strength(3f, 18f)
                    .requiresCorrectToolForDrops().noOcclusion().isRedstoneConductor((bs, br, bp) -> false)));

    public static final DeferredBlock<Block> CHARCOAL_BLOCK = REGISTRY.registerSimpleBlock("charcoal_block",
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).strength(2f, 20f).requiresCorrectToolForDrops()
                    .instrument(NoteBlockInstrument.BASEDRUM));

    public static final DeferredBlock<NetherrackFarmlandBlock> NETHERRACK_FARMLAND = REGISTRY.register("netherrack_farmland", () -> new NetherrackFarmlandBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.NETHER).sound(SoundType.NETHERRACK).strength(1f, 10f)
                    .lightLevel(s -> 6).requiresCorrectToolForDrops().noOcclusion().pushReaction(PushReaction.PUSH_ONLY)
                    .isRedstoneConductor((bs, br, bp) -> false).instrument(NoteBlockInstrument.BASEDRUM),
            Block.box(0, 0, 0, 16, 15, 16)));

    public static final DeferredBlock<Block> SHIVADA_JADE_ORE = REGISTRY.registerSimpleBlock("shivada_jade_ore",
            BlockBehaviour.Properties.of().mapColor(MapColor.ICE).strength(3f, 5f).requiresCorrectToolForDrops().friction(1f)
                    .instrument(NoteBlockInstrument.BASEDRUM));

    public static final DeferredBlock<Block> SHIVADA_JADE_BLOCK = REGISTRY.registerSimpleBlock("shivada_jade_block",
            BlockBehaviour.Properties.of().mapColor(MapColor.ICE).sound(SoundType.GLASS).strength(3f, 20f)
                    .requiresCorrectToolForDrops().friction(1.2f));

    public static final DeferredBlock<Block> ELEMENTAL_CRYSTAL_BLOCK = REGISTRY.registerSimpleBlock("elemental_crystal_block",
            BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_WHITE).sound(SoundType.GLASS).strength(1000f, 10000f)
                    .requiresCorrectToolForDrops());

    public static final DeferredBlock<XiaoLanternLauncherBlock> XIAO_LANTERN_LAUNCHER = REGISTRY.register("xiao_lantern_launcher", () -> new XiaoLanternLauncherBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_YELLOW).sound(SoundType.NETHERITE_BLOCK).strength(3f, 10f)
                    .pushReaction(PushReaction.BLOCK).instrument(NoteBlockInstrument.BASEDRUM)));

    public static final DeferredBlock<XiaoLanternLauncherBlock> CREATIVE_XIAO_LANTERN_LAUNCHER =
            REGISTRY.register("creative_xiao_lantern_launcher", () -> new XiaoLanternLauncherBlock(
                    BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_YELLOW).sound(SoundType.NETHERITE_BLOCK).strength(3f, 10f)
                            .pushReaction(PushReaction.BLOCK).instrument(NoteBlockInstrument.BASEDRUM)));

    public static final DeferredBlock<Block> MONOCHROME_OTHERWORLD_PLANKS =
            REGISTRY.registerSimpleBlock("monochrome_otherworld_planks",
                    BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PINK).sound(SoundType.WOOD).strength(2f, 3f).ignitedByLava()
                            .instrument(NoteBlockInstrument.BASS));

    public static final DeferredBlock<StairBlock> MONOCHROME_OTHERWORLD_PLANKS_STAIRS =
            REGISTRY.register("monochrome_otherworld_planks_stairs", () -> new StairBlock(MONOCHROME_OTHERWORLD_PLANKS.get().defaultBlockState(),
                    BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PINK).sound(SoundType.WOOD).strength(3f, 2f).ignitedByLava()
                            .instrument(NoteBlockInstrument.BASS)));

    public static final DeferredBlock<SlabBlock> MONOCHROME_OTHERWORLD_PLANKS_SLAB =
            REGISTRY.register("monochrome_otherworld_planks_slab", () -> new SlabBlock(
                    BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PINK).sound(SoundType.WOOD).strength(2f, 3f).ignitedByLava()
                            .instrument(NoteBlockInstrument.BASS)));

    public static final DeferredBlock<FenceBlock> MONOCHROME_OTHERWORLD_PLANKS_FENCE =
            REGISTRY.register("monochrome_otherworld_planks_fence", () -> new FenceBlock(
                    BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PINK).sound(SoundType.WOOD).strength(2f, 3f).ignitedByLava()
                            .instrument(NoteBlockInstrument.BASS).forceSolidOn()));

    public static final DeferredBlock<FenceGateBlock> MONOCHROME_OTHERWORLD_PLANKS_FENCE_GATE =
            REGISTRY.register("monochrome_otherworld_planks_fence_gate", () -> new FenceGateBlock(WoodType.OAK,
                    BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PINK).sound(SoundType.WOOD).strength(2f, 3f).ignitedByLava()
                            .instrument(NoteBlockInstrument.BASS).forceSolidOn()));

    public static final DeferredBlock<PressurePlateBlock> MONOCHROME_OTHERWORLD_PLANKS_PRESSURE_PLATE =
            REGISTRY.register("monochrome_otherworld_planks_pressure_plate", () -> new PressurePlateBlock(BlockSetType.OAK,
                    BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PINK).sound(SoundType.WOOD).strength(2f, 3f).ignitedByLava()
                            .instrument(NoteBlockInstrument.BASS).forceSolidOn()));

    public static final DeferredBlock<ButtonBlock> MONOCHROME_OTHERWORLD_PLANKS_BUTTON =
            REGISTRY.register("monochrome_otherworld_planks_button", () -> new ButtonBlock(BlockSetType.OAK, 30,
                    BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PINK).sound(SoundType.WOOD).strength(2f, 3f).ignitedByLava()
                            .instrument(NoteBlockInstrument.BASS)));

    public static final DeferredBlock<Block> BLUE_MONOCHROME_OTHERWORLD_PLANKS =
            REGISTRY.registerSimpleBlock("blue_monochrome_otherworld_planks",
                    BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_BLUE).sound(SoundType.WOOD).strength(2f, 3f).ignitedByLava()
                            .instrument(NoteBlockInstrument.BASS));

    public static final DeferredBlock<StairBlock> BLUE_MONOCHROME_OTHERWORLD_PLANKS_STAIRS =
            REGISTRY.register("blue_monochrome_otherworld_planks_stairs", () -> new StairBlock(BLUE_MONOCHROME_OTHERWORLD_PLANKS.get().defaultBlockState(),
                    BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_BLUE).sound(SoundType.WOOD).strength(3f, 2f).ignitedByLava()
                            .instrument(NoteBlockInstrument.BASS)));

    public static final DeferredBlock<SlabBlock> BLUE_MONOCHROME_OTHERWORLD_PLANKS_SLAB =
            REGISTRY.register("blue_monochrome_otherworld_planks_slab", () -> new SlabBlock(
                    BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_BLUE).sound(SoundType.WOOD).strength(2f, 3f).ignitedByLava()
                            .instrument(NoteBlockInstrument.BASS)));

    public static final DeferredBlock<FenceBlock> BLUE_MONOCHROME_OTHERWORLD_PLANKS_FENCE =
            REGISTRY.register("blue_monochrome_otherworld_planks_fence", () -> new FenceBlock(
                    BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_BLUE).sound(SoundType.WOOD).strength(2f, 3f).ignitedByLava()
                            .instrument(NoteBlockInstrument.BASS).forceSolidOn()));

    public static final DeferredBlock<FenceGateBlock> BLUE_MONOCHROME_OTHERWORLD_PLANKS_FENCE_GATE =
            REGISTRY.register("blue_monochrome_otherworld_planks_fence_gate", () -> new FenceGateBlock(WoodType.OAK,
                    BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_BLUE).sound(SoundType.WOOD).strength(2f, 3f).ignitedByLava()
                            .instrument(NoteBlockInstrument.BASS).forceSolidOn()));

    public static final DeferredBlock<PressurePlateBlock> BLUE_MONOCHROME_OTHERWORLD_PLANKS_PRESSURE_PLATE =
            REGISTRY.register("blue_monochrome_otherworld_planks_pressure_plate", () -> new PressurePlateBlock(BlockSetType.OAK,
                    BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_BLUE).sound(SoundType.WOOD).strength(2f, 3f).ignitedByLava()
                            .instrument(NoteBlockInstrument.BASS).forceSolidOn()));

    public static final DeferredBlock<ButtonBlock> BLUE_MONOCHROME_OTHERWORLD_PLANKS_BUTTON =
            REGISTRY.register("blue_monochrome_otherworld_planks_button", () -> new ButtonBlock(BlockSetType.OAK, 30,
                    BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_BLUE).sound(SoundType.WOOD).strength(2f, 3f).ignitedByLava()
                            .instrument(NoteBlockInstrument.BASS)));

    public static final DeferredBlock<Block> GEOMARROW_CRYSTAL_BLOCK = REGISTRY.registerSimpleBlock("geomarrow_crystal_block",
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_YELLOW).sound(SoundType.GLASS).strength(5f, 20f)
                    .lightLevel(s -> 10).requiresCorrectToolForDrops().friction(0.7f).speedFactor(0.9f).jumpFactor(0.2f)
                    .hasPostProcess((bs, br, bp) -> true).emissiveRendering((bs, br, bp) -> true)
                    .instrument(NoteBlockInstrument.BASEDRUM));

    public static final DeferredBlock<PGCFaceAttachedBlock> GEOMARROW_CRYSTAL_CLUSTER = REGISTRY.register("geomarrow_crystal_cluster", () -> new PGCFaceAttachedBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_YELLOW).sound(SoundType.GLASS).strength(5f, 20f)
                    .lightLevel(s -> 15).requiresCorrectToolForDrops().friction(0.7f).speedFactor(0.9f).jumpFactor(1.2f)
                    .noOcclusion().hasPostProcess((bs, br, bp) -> true).emissiveRendering((bs, br, bp) -> true)
                    .isRedstoneConductor((bs, br, bp) -> false).instrument(NoteBlockInstrument.BASEDRUM),
            PGCBlocks::crystalClusterShape));

    public static final DeferredBlock<Block> WEATHERED_STONE_BRICKS = REGISTRY.registerSimpleBlock("weathered_stone_bricks",
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).strength(2f, 15f).requiresCorrectToolForDrops()
                    .instrument(NoteBlockInstrument.BASEDRUM));

    public static final DeferredBlock<Block> RUSTY_IRON_BLOCK = REGISTRY.registerSimpleBlock("rusty_iron_block",
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).sound(SoundType.LANTERN).strength(2f, 10f)
                    .requiresCorrectToolForDrops());

    public static final DeferredBlock<IronBarsBlock> RUSTY_IRON_FENCE =
            REGISTRY.register("rusty_iron_fence", () -> new IronBarsBlock(
                    BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).sound(SoundType.LANTERN).strength(2f, 10f)
                            .requiresCorrectToolForDrops().noOcclusion().isRedstoneConductor((bs, br, bp) -> false).ignitedByLava()));

    public static final DeferredBlock<Block> ROTTEN_WOOD = REGISTRY.registerSimpleBlock("rotten_wood",
            BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).sound(SoundType.WOOD).strength(0.5f, 1f).ignitedByLava()
                    .instrument(NoteBlockInstrument.BASS));

    public static final DeferredBlock<StairBlock> ROTTEN_WOOD_STAIRS =
            REGISTRY.register("rotten_wood_stairs", () -> new StairBlock(ROTTEN_WOOD.get().defaultBlockState(),
                    BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).sound(SoundType.WOOD).strength(1f).noOcclusion()
                            .isRedstoneConductor((bs, br, bp) -> false).ignitedByLava().instrument(NoteBlockInstrument.BASS)));

    public static final DeferredBlock<FenceBlock> ROTTEN_WOOD_FENCE = REGISTRY.register("rotten_wood_fence", () -> new FenceBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).sound(SoundType.WOOD).strength(1f).noOcclusion()
                    .isRedstoneConductor((bs, br, bp) -> false).ignitedByLava().instrument(NoteBlockInstrument.BASS)
                    .forceSolidOn()));

    public static final DeferredBlock<StairBlock> WEATHERED_STONE_BRICKS_STAIRS =
            REGISTRY.register("weathered_stone_bricks_stairs", () -> new StairBlock(WEATHERED_STONE_BRICKS.get().defaultBlockState(),
                    BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).strength(2f, 15f).requiresCorrectToolForDrops()
                            .instrument(NoteBlockInstrument.BASEDRUM)));

    public static final DeferredBlock<SlabBlock> WEATHERED_STONE_BRICKS_SLAB =
            REGISTRY.register("weathered_stone_bricks_slab", () -> new SlabBlock(
                    BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).strength(2f, 15f).requiresCorrectToolForDrops()
                            .instrument(NoteBlockInstrument.BASEDRUM)));

    public static final DeferredBlock<WallBlock> WEATHERED_STONE_BRICKS_WALL =
            REGISTRY.register("weathered_stone_bricks_wall", () -> new WallBlock(
                    BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).strength(2f, 15f).requiresCorrectToolForDrops()
                            .noOcclusion().isRedstoneConductor((bs, br, bp) -> false).instrument(NoteBlockInstrument.BASEDRUM)
                            .forceSolidOn()));

    public static final DeferredBlock<PGCFaceAttachedBlock> PRIMOGEM_BLOCK = REGISTRY.register("primogem_block", () -> new PGCFaceAttachedBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PINK).sound(SoundType.GLASS).strength(6f, 14f)
                    .lightLevel(s -> 8).requiresCorrectToolForDrops().friction(0.7f).hasPostProcess((bs, br, bp) -> true)
                    .emissiveRendering((bs, br, bp) -> true).instrument(NoteBlockInstrument.HAT),
            state -> Shapes.block()));

    public static final DeferredBlock<Block> DEEPSLATE_PRIMOGEM_ORE = REGISTRY.registerSimpleBlock("deepslate_primogem_ore",
            BlockBehaviour.Properties.of().mapColor(MapColor.DIAMOND).sound(SoundType.POLISHED_DEEPSLATE).strength(5f)
                    .lightLevel(s -> 1).requiresCorrectToolForDrops().instrument(NoteBlockInstrument.BASEDRUM));

    public static final DeferredBlock<Block> DEEPSLATE_PRITHIVA_TOPAZ_ORE =
            REGISTRY.registerSimpleBlock("deepslate_prithiva_topaz_ore",
                    BlockBehaviour.Properties.of().mapColor(MapColor.GOLD).sound(SoundType.DEEPSLATE).strength(30f, 5f)
                            .requiresCorrectToolForDrops().instrument(NoteBlockInstrument.BASEDRUM));

    public static final DeferredBlock<PGCPillarBlock> DENDRO_BLESSING = REGISTRY.register("dendro_blessing", () -> new PGCPillarBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.GRASS).sound(SoundType.GLASS).strength(1f, 20f).noOcclusion()
                    .isRedstoneConductor((bs, br, bp) -> false).instrument(NoteBlockInstrument.HAT),
            state -> switch (state.getValue(PGCPillarBlock.AXIS)) {
                case X -> Block.box(0, 1, 1, 14, 14, 14);
                case Z -> Block.box(1, 1, 2, 14, 14, 16);
                default -> Block.box(1, 0, 1, 14, 14, 14);
            }));

    public static final DeferredBlock<PackedMoraPileBlock> PACKED_MORA_PILE =
            REGISTRY.register("packed_mora_pile", () -> new PackedMoraPileBlock(
                    BlockBehaviour.Properties.of().mapColor(MapColor.GOLD).sound(SoundType.ANVIL).strength(10f, 20f)
                            .requiresCorrectToolForDrops()));

    public static final DeferredBlock<Block> UNFETTERED_METAL_BLOCK = REGISTRY.registerSimpleBlock("unfettered_metal_block",
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_CYAN).sound(SoundType.NETHERITE_BLOCK).strength(3f, 15f)
                    .requiresCorrectToolForDrops());

    public static final DeferredBlock<Block> STURDY_METAL_BLOCK = REGISTRY.registerSimpleBlock("sturdy_metal_block",
            BlockBehaviour.Properties.of().mapColor(MapColor.GOLD).sound(SoundType.NETHERITE_BLOCK).strength(3f, 15f)
                    .requiresCorrectToolForDrops());

    public static final DeferredBlock<Block> ETERNAL_METAL_BLOCK = REGISTRY.registerSimpleBlock("eternal_metal_block",
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).sound(SoundType.NETHERITE_BLOCK).strength(3f, 15f)
                    .requiresCorrectToolForDrops());

    public static final DeferredBlock<Block> WISDOM_METAL_BLOCK = REGISTRY.registerSimpleBlock("wisdom_metal_block",
            BlockBehaviour.Properties.of().mapColor(MapColor.GRASS).sound(SoundType.NETHERITE_BLOCK).strength(3f, 15f)
                    .requiresCorrectToolForDrops());

    public static final DeferredBlock<Block> JUSTICE_METAL_BLOCK = REGISTRY.registerSimpleBlock("justice_metal_block",
            BlockBehaviour.Properties.of().mapColor(MapColor.WATER).sound(SoundType.NETHERITE_BLOCK).strength(3f, 15f)
                    .requiresCorrectToolForDrops());

    public static final DeferredBlock<Block> BURNING_WISH_METAL_BLOCK = REGISTRY.registerSimpleBlock("burning_wish_metal_block",
            BlockBehaviour.Properties.of().mapColor(MapColor.FIRE).sound(SoundType.NETHERITE_BLOCK).strength(3f, 15f)
                    .requiresCorrectToolForDrops());

    public static final DeferredBlock<Block> COMPASSION_METAL_BLOCK = REGISTRY.registerSimpleBlock("compassion_metal_block",
            BlockBehaviour.Properties.of().mapColor(MapColor.ICE).sound(SoundType.NETHERITE_BLOCK).strength(3f, 15f)
                    .requiresCorrectToolForDrops());

    public static final DeferredBlock<ACakeForYouBlock> A_CAKE_FOR_YOU = REGISTRY.register("a_cake_for_you", () -> new ACakeForYouBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_ORANGE).sound(SoundType.WOOL).strength(2f, 5f).noOcclusion()
                    .isRedstoneConductor((bs, br, bp) -> false),
            Block.box(1, 0, 1, 15, 8, 15)));

    public static final DeferredBlock<Block> FINE_FORGED_ORE_FUSION_BLOCK =
            REGISTRY.registerSimpleBlock("fine_forged_ore_fusion_block",
                    BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PINK).sound(SoundType.AMETHYST).strength(5f, 15f)
                            .requiresCorrectToolForDrops().friction(0.7f));

    public static final DeferredBlock<IronBarsBlock> SOLID_CRYSTAL_PLATE =
            REGISTRY.register("solid_crystal_plate", () -> new IronBarsBlock(
                    BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_BLUE).sound(SoundType.GLASS).strength(300f, 1000f)
                            .requiresCorrectToolForDrops().instrument(NoteBlockInstrument.HAT)));

    public static final DeferredBlock<GorgeousSmithingTableBlock> GORGEOUS_SMITHING_TABLE = REGISTRY.register("gorgeous_smithing_table", () -> new GorgeousSmithingTableBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.GOLD).sound(SoundType.WOOD).strength(3f, 10f).ignitedByLava()
                    .instrument(NoteBlockInstrument.BASS)));

    public static final DeferredBlock<UnidentifiedDollBlock> UNIDENTIFIED_DOLL = REGISTRY.register("unidentified_doll", () -> new UnidentifiedDollBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.FIRE).sound(SoundType.GRASS).strength(10f, 20f).noOcclusion()
                    .lightLevel(s -> s.getValue(UnidentifiedDollBlock.LIT) ? UnidentifiedDollBlock.LIT_LIGHT : 0)
                    .isRedstoneConductor((bs, br, bp) -> false).instrument(NoteBlockInstrument.BASEDRUM),
            Block.box(4, 0, 4, 12, 7, 12)));

    public static final DeferredBlock<DetonatorBlock> DETONATOR = REGISTRY.register("detonator", () -> new DetonatorBlock(
            BlockBehaviour.Properties.of().sound(SoundType.WOOD).strength(1f, 10f).noOcclusion()
                    .isRedstoneConductor((bs, br, bp) -> false).instrument(NoteBlockInstrument.BASEDRUM),
            Block.box(1, 0, 1, 15, 8, 15)));

    public static final DeferredBlock<Block> ELEMENTAL_CRYSTAL_ORE = REGISTRY.registerSimpleBlock("elemental_crystal_ore",
            BlockBehaviour.Properties.of().mapColor(MapColor.DIAMOND).strength(3f, 5f).lightLevel(s -> 1)
                    .requiresCorrectToolForDrops().instrument(NoteBlockInstrument.BASEDRUM));

    public static final DeferredBlock<Block> DEEPSLATE_ELEMENTAL_CRYSTAL_ORE =
            REGISTRY.registerSimpleBlock("deepslate_elemental_crystal_ore",
                    BlockBehaviour.Properties.of().mapColor(MapColor.DIAMOND).strength(4f, 5f).lightLevel(s -> 1)
                            .requiresCorrectToolForDrops().instrument(NoteBlockInstrument.BASEDRUM));

    public static final DeferredBlock<Block> WHITE_IRON_ORE_BLOCK = REGISTRY.registerSimpleBlock("white_iron_ore_block",
            BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3f, 5f).requiresCorrectToolForDrops());

    public static final DeferredBlock<Block> DEEPSLATE_WHITE_IRON_ORE_BLOCK =
            REGISTRY.registerSimpleBlock("deepslate_white_iron_ore_block",
                    BlockBehaviour.Properties.of().mapColor(MapColor.METAL).sound(SoundType.DEEPSLATE).strength(4f, 5f)
                            .requiresCorrectToolForDrops());

    public static final DeferredBlock<Block> OTHERWORLD_CRYSTAL_ORE = REGISTRY.registerSimpleBlock("otherworld_crystal_ore",
            BlockBehaviour.Properties.of().mapColor(MapColor.DIAMOND).sound(SoundType.AMETHYST).strength(5f)
                    .requiresCorrectToolForDrops());

    public static final DeferredBlock<Block> DEEPSLATE_OTHERWORLD_CRYSTAL_ORE =
            REGISTRY.registerSimpleBlock("deepslate_otherworld_crystal_ore",
                    BlockBehaviour.Properties.of().mapColor(MapColor.DIAMOND).sound(SoundType.AMETHYST).strength(7f, 5f)
                            .requiresCorrectToolForDrops());

    public static final DeferredBlock<OtherworldCrystalClusterBlock> OTHERWORLD_CRYSTAL_CLUSTER =
            REGISTRY.register("otherworld_crystal_cluster", () -> new OtherworldCrystalClusterBlock(
                    BlockBehaviour.Properties.of().mapColor(MapColor.DIAMOND).sound(SoundType.AMETHYST_CLUSTER).strength(5f)
                            .lightLevel(s -> 3).requiresCorrectToolForDrops().noOcclusion().hasPostProcess((bs, br, bp) -> true)
                            .emissiveRendering((bs, br, bp) -> true).isRedstoneConductor((bs, br, bp) -> false),
                    PGCBlocks::crystalClusterShape));

    public static final DeferredBlock<Block> CRUDE_WHITE_IRON_BLOCK = REGISTRY.registerSimpleBlock("crude_white_iron_block",
            BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3f, 10f).requiresCorrectToolForDrops());

    public static final DeferredBlock<Block> WONDROUS_ENCOUNTER_BLOCK = REGISTRY.registerSimpleBlock("wondrous_encounter_block",
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PINK).sound(SoundType.AMETHYST_CLUSTER).strength(1f, 10f));

    public static final DeferredBlock<LuckyStatueBlock> LUCKY_STATUE = REGISTRY.register("lucky_statue", () -> new LuckyStatueBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.GOLD).sound(SoundType.METAL).strength(10f, 1000f).lightLevel(s -> 1)
                    .noOcclusion().hasPostProcess((bs, br, bp) -> true).emissiveRendering((bs, br, bp) -> true)
                    .isRedstoneConductor((bs, br, bp) -> false),
            Block.box(2, 0, 2, 14, 4, 14)));

    public static final DeferredBlock<RewardCasketBlock> MORA_CASKET = REGISTRY.register("mora_casket", () -> new RewardCasketBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.GOLD).sound(SoundType.WOOD).strength(1f, 10f)));

    public static final DeferredBlock<RewardCasketBlock> PRIMOGEM_CASKET = REGISTRY.register("primogem_casket", () -> new RewardCasketBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.DIAMOND).sound(SoundType.AMETHYST_CLUSTER).strength(1f, 10f)));

    public static final DeferredBlock<CurioCasketBlock> COSMIC_CASKET = REGISTRY.register("cosmic_casket", () -> new CurioCasketBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).sound(SoundType.ANCIENT_DEBRIS).strength(1f, 10f)));

    public static final DeferredBlock<StellarConverterBlock> STELLAR_CONVERTER = REGISTRY.register("stellar_converter", () -> new StellarConverterBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_WHITE).sound(SoundType.METAL).strength(10f, 20f)
                    .requiresCorrectToolForDrops()));

    private static VoxelShape trashCanShape(BlockState state) {
        return switch (state.getValue(PGCFaceAttachedBlock.FACE)) {
            case FLOOR, CEILING -> Block.box(2, 0, 2, 14, 16, 14);
            case WALL -> switch (state.getValue(PGCFaceAttachedBlock.FACING)) {
                case EAST, WEST -> Block.box(0, 2, 2, 16, 14, 14);
                default -> Block.box(2, 2, 0, 14, 14, 16);
            };
        };
    }

    private static VoxelShape crystalClusterShape(BlockState state) {
        return switch (state.getValue(PGCFaceAttachedBlock.FACE)) {
            case FLOOR -> Block.box(3, 0, 3, 13, 13, 13);
            case CEILING -> Block.box(3, 3, 3, 13, 16, 13);
            case WALL -> switch (state.getValue(PGCFaceAttachedBlock.FACING)) {
                case NORTH -> Block.box(3, 3, 3, 13, 13, 16);
                case EAST -> Block.box(0, 3, 3, 13, 13, 13);
                case WEST -> Block.box(3, 3, 3, 16, 13, 13);
                default -> Block.box(3, 3, 0, 13, 13, 13);
            };
        };
    }
}
