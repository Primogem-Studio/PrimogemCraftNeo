package net.per.primogemcraft.registry;

import net.minecraft.block.*;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockSetType;
import net.minecraft.block.enums.Instrument;
import net.minecraft.block.WoodType;
import net.minecraft.block.MapColor;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.util.shape.VoxelShape;
import net.per.primogemcraft.compat.registries.DeferredBlock;
import net.per.primogemcraft.compat.registries.DeferredRegister;
import net.per.primogemcraft.block.*;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public class PGCBlocks {
    public static final DeferredRegister.Blocks REGISTRY = DeferredRegister.createBlocks(MOD_ID);

    public static final DeferredBlock<ZiplineBaseBlock> ZIPLINE_BASE = REGISTRY.register("zipline_base", () -> new ZiplineBaseBlock(
            AbstractBlock.Settings.create().mapColor(MapColor.IRON_GRAY).sounds(net.minecraft.sound.BlockSoundGroup.METAL).strength(5f, 30f)
                    .requiresTool().nonOpaque().pistonBehavior(PistonBehavior.BLOCK).dropsNothing()));

    public static final DeferredBlock<Block> PRIMOGEM_ORE = REGISTRY.registerSimpleBlock("primogem_ore",
            AbstractBlock.Settings.create().mapColor(MapColor.PURPLE).sounds(net.minecraft.sound.BlockSoundGroup.ANCIENT_DEBRIS).strength(3f, 5f)
                    .requiresTool().instrument(Instrument.BASEDRUM));

    public static final DeferredBlock<Block> OTHERWORLD_LOG_PLANKS = REGISTRY.registerSimpleBlock("otherworld_log_planks",
            AbstractBlock.Settings.create().mapColor(MapColor.PINK).sounds(net.minecraft.sound.BlockSoundGroup.WOOD).strength(1f).burnable()
                    .instrument(Instrument.BASS));

    public static final DeferredBlock<Block> INTERTWINED_FATE_BLOCK = REGISTRY.registerSimpleBlock("intertwined_fate_block",
            AbstractBlock.Settings.create().mapColor(MapColor.PINK).sounds(net.minecraft.sound.BlockSoundGroup.METAL).strength(30f)
                    .requiresTool().instrument(Instrument.BASEDRUM));

    public static final DeferredBlock<MoraPileBlock> MORA_PILE = REGISTRY.register("mora_pile", () -> new MoraPileBlock(
            AbstractBlock.Settings.create().mapColor(MapColor.GOLD).sounds(net.minecraft.sound.BlockSoundGroup.CHAIN).strength(1f, 6f).nonOpaque()
                    .solidBlock((bs, br, bp) -> false)));

    public static final DeferredBlock<Block> MORA_BLOCK = REGISTRY.registerSimpleBlock("mora_block",
            AbstractBlock.Settings.create().mapColor(MapColor.GOLD).sounds(net.minecraft.sound.BlockSoundGroup.METAL).strength(3f, 100f)
                    .requiresTool());

    public static final DeferredBlock<StairsBlock> OTHERWORLD_WOOD_STAIRS =
            REGISTRY.register("otherworld_wood_stairs", () -> new StairsBlock(OTHERWORLD_LOG_PLANKS.get().getDefaultState(),
                    AbstractBlock.Settings.create().mapColor(MapColor.PINK).sounds(net.minecraft.sound.BlockSoundGroup.WOOD).strength(2f, 10f).burnable()
                            .instrument(Instrument.BASS)));

    public static final DeferredBlock<SlabBlock> OTHERWORLD_LOG_SLAB =
            REGISTRY.register("otherworld_log_slab", () -> new SlabBlock(
                    AbstractBlock.Settings.create().mapColor(MapColor.PINK).sounds(net.minecraft.sound.BlockSoundGroup.WOOD).strength(2f, 10f).burnable()
                            .instrument(Instrument.BASS)));

    public static final DeferredBlock<FenceGateBlock> OTHERWORLD_WOOD_FENCE_GATE =
            REGISTRY.register("otherworld_wood_fence_gate", () -> new FenceGateBlock(
                    AbstractBlock.Settings.create().mapColor(MapColor.PINK).sounds(net.minecraft.sound.BlockSoundGroup.WOOD).strength(3f, 30f).nonOpaque()
                            .solidBlock((bs, br, bp) -> false).burnable().instrument(Instrument.BASS)
                            .solid(), WoodType.OAK));

    public static final DeferredBlock<FenceBlock> OTHERWORLD_WOOD_FENCE =
            REGISTRY.register("otherworld_wood_fence", () -> new FenceBlock(
                    AbstractBlock.Settings.create().mapColor(MapColor.PINK).sounds(net.minecraft.sound.BlockSoundGroup.WOOD).strength(3f, 30f).nonOpaque()
                            .solidBlock((bs, br, bp) -> false).burnable().instrument(Instrument.BASS)
                            .solid()));

    public static final DeferredBlock<March7thStatueBlock> MARCH_7TH_STATUE = REGISTRY.register("march_7th_statue", () -> new March7thStatueBlock(
            AbstractBlock.Settings.create().mapColor(MapColor.PINK).strength(3f, 100f).requiresTool()
                    .slipperiness(0.1f).nonOpaque().solidBlock((bs, br, bp) -> false)
                    .instrument(Instrument.BASEDRUM),
            Block.createCuboidShape(4, 0, 4, 12, 13, 12)));

    public static final DeferredBlock<Block> REFINED_MORA_BLOCK = REGISTRY.registerSimpleBlock("refined_mora_block",
            AbstractBlock.Settings.create().mapColor(MapColor.GOLD).sounds(net.minecraft.sound.BlockSoundGroup.METAL).strength(4f, 100f)
                    .requiresTool());

    public static final DeferredBlock<Block> CHEAP_MORA_BLOCK = REGISTRY.registerSimpleBlock("cheap_mora_block",
            AbstractBlock.Settings.create().mapColor(MapColor.GOLD).strength(2f, 4f).requiresTool()
                    .instrument(Instrument.BASEDRUM));

    public static final DeferredBlock<SlabBlock> CHEAP_MORA_BLOCK_SLAB =
            REGISTRY.register("cheap_mora_block_slab", () -> new SlabBlock(
                    AbstractBlock.Settings.create().mapColor(MapColor.GOLD).strength(2f, 4f).instrument(Instrument.BASEDRUM)));

    public static final DeferredBlock<StairsBlock> CHEAP_MORA_BLOCK_STAIRS =
            REGISTRY.register("cheap_mora_block_stairs", () -> new StairsBlock(CHEAP_MORA_BLOCK.get().getDefaultState(),
                    AbstractBlock.Settings.create().mapColor(MapColor.GOLD).strength(2f, 4f).requiresTool()
                            .instrument(Instrument.BASEDRUM)));

    public static final DeferredBlock<WallBlock> CHEAP_MORA_BLOCK_WALL =
            REGISTRY.register("cheap_mora_block_wall", () -> new WallBlock(
                    AbstractBlock.Settings.create().mapColor(MapColor.GOLD).strength(3f, 30f).nonOpaque()
                            .solidBlock((bs, br, bp) -> false).instrument(Instrument.BASEDRUM).solid()));

    public static final DeferredBlock<PGCHorizontalFallingBlock> SMALL_JAR = REGISTRY.register("small_jar", () -> new PGCHorizontalFallingBlock(
            AbstractBlock.Settings.create().mapColor(MapColor.DIRT_BROWN).sounds(net.minecraft.sound.BlockSoundGroup.DECORATED_POT).strength(0f, 1f).noCollision()
                    .nonOpaque().solidBlock((bs, br, bp) -> false).offset(AbstractBlock.OffsetType.XZ),
            Block.createCuboidShape(5, 0, 5, 11, 7, 11)));

    public static final DeferredBlock<PGCHorizontalFallingBlock> BIG_JAR = REGISTRY.register("big_jar", () -> new PGCHorizontalFallingBlock(
            AbstractBlock.Settings.create().mapColor(MapColor.DIRT_BROWN).sounds(net.minecraft.sound.BlockSoundGroup.DECORATED_POT).strength(0f, 1f).nonOpaque()
                    .solidBlock((bs, br, bp) -> false).dynamicBounds().offset(AbstractBlock.OffsetType.XZ),
            Block.createCuboidShape(4, 0, 4, 12, 10, 12)));

    private static final int CLEAN_TRASH_CAN_SIZE = 45;
    private static final int MORA_TRASH_CAN_SIZE = 54;

    public static final DeferredBlock<TrashCanBlock> TRASH_CAN = REGISTRY.register("trash_can", () -> new TrashCanBlock(
            AbstractBlock.Settings.create().mapColor(MapColor.BLACK).sounds(net.minecraft.sound.BlockSoundGroup.ANVIL).strength(3f, 15f).nonOpaque()
                    .solidBlock((bs, br, bp) -> false),
            Block.createCuboidShape(2, 0, 2, 14, 16, 14)));

    public static final DeferredBlock<FaceAttachedContainerBlock> CLEAN_TRASH_CAN = REGISTRY.register("clean_trash_can", () -> new FaceAttachedContainerBlock(
            AbstractBlock.Settings.create().mapColor(MapColor.IRON_GRAY).sounds(net.minecraft.sound.BlockSoundGroup.ANVIL).strength(3f, 15f)
                    .requiresTool().nonOpaque().solidBlock((bs, br, bp) -> false),
            PGCBlocks::trashCanShape, CLEAN_TRASH_CAN_SIZE));

    public static final DeferredBlock<FaceAttachedContainerBlock> MORA_TRASH_CAN = REGISTRY.register("mora_trash_can", () -> new FaceAttachedContainerBlock(
            AbstractBlock.Settings.create().mapColor(MapColor.GOLD).sounds(net.minecraft.sound.BlockSoundGroup.ANVIL).strength(4f, 10f).nonOpaque()
                    .solidBlock((bs, br, bp) -> false),
            PGCBlocks::trashCanShape, MORA_TRASH_CAN_SIZE));

    public static final DeferredBlock<Block> VAYUDA_TURQUOISE_ORE = REGISTRY.registerSimpleBlock("vayuda_turquoise_ore",
            AbstractBlock.Settings.create().mapColor(MapColor.CYAN).strength(3f, 5f).requiresTool()
                    .instrument(Instrument.BASEDRUM));

    public static final DeferredBlock<Block> VAYUDA_TURQUOISE_BLOCK = REGISTRY.registerSimpleBlock("vayuda_turquoise_block",
            AbstractBlock.Settings.create().mapColor(MapColor.CYAN).sounds(net.minecraft.sound.BlockSoundGroup.GLASS).strength(3f, 10f)
                    .requiresTool().instrument(Instrument.BASEDRUM));

    public static final DeferredBlock<Block> PRITHIVA_TOPAZ_ORE = REGISTRY.registerSimpleBlock("prithiva_topaz_ore",
            AbstractBlock.Settings.create().mapColor(MapColor.GOLD).strength(4f, 5f).requiresTool()
                    .instrument(Instrument.BASEDRUM));

    public static final DeferredBlock<Block> PRITHIVA_TOPAZ_BLOCK = REGISTRY.registerSimpleBlock("prithiva_topaz_block",
            AbstractBlock.Settings.create().mapColor(MapColor.GOLD).sounds(net.minecraft.sound.BlockSoundGroup.GLASS).strength(3f, 100f)
                    .requiresTool().instrument(Instrument.BASEDRUM));

    public static final DeferredBlock<Block> VAJRADA_AMETHYST_ORE = REGISTRY.registerSimpleBlock("vajrada_amethyst_ore",
            AbstractBlock.Settings.create().mapColor(MapColor.PURPLE).strength(3f, 5f).requiresTool()
                    .instrument(Instrument.BASEDRUM));

    public static final DeferredBlock<Block> VAJRADA_AMETHYST_BLOCK = REGISTRY.registerSimpleBlock("vajrada_amethyst_block",
            AbstractBlock.Settings.create().mapColor(MapColor.PURPLE).sounds(net.minecraft.sound.BlockSoundGroup.GLASS).strength(3f, 11f));

    public static final DeferredBlock<CrystalOreBlock> NAGADUS_EMERALD_ORE = REGISTRY.register("nagadus_emerald_ore", () -> new CrystalOreBlock(
            AbstractBlock.Settings.create().mapColor(MapColor.DARK_GREEN).sounds(net.minecraft.sound.BlockSoundGroup.GRAVEL).strength(2f)
                    .requiresTool()));

    public static final DeferredBlock<Block> NAGADUS_EMERALD_BLOCK = REGISTRY.registerSimpleBlock("nagadus_emerald_block",
            AbstractBlock.Settings.create().mapColor(MapColor.GREEN).sounds(net.minecraft.sound.BlockSoundGroup.GLASS).strength(3f, 15f));

    public static final DeferredBlock<Block> DENDRO_CORE_BLOCK = REGISTRY.registerSimpleBlock("dendro_core_block",
            AbstractBlock.Settings.create().mapColor(MapColor.GREEN).strength(10f, 20f).requiresTool()
                    .instrument(Instrument.BASEDRUM));

    public static final DeferredBlock<Block> DENDRO_CORE_PLANKS = REGISTRY.registerSimpleBlock("dendro_core_planks",
            AbstractBlock.Settings.create().mapColor(MapColor.DARK_GREEN).sounds(net.minecraft.sound.BlockSoundGroup.WOOD).strength(2f, 10f).burnable()
                    .instrument(Instrument.BASS));

    public static final DeferredBlock<StairsBlock> DENDRO_CORE_PLANKS_STAIRS =
            REGISTRY.register("dendro_core_planks_stairs", () -> new StairsBlock(DENDRO_CORE_PLANKS.get().getDefaultState(),
                    AbstractBlock.Settings.create().mapColor(MapColor.DARK_GREEN).sounds(net.minecraft.sound.BlockSoundGroup.WOOD).strength(3f, 2f).burnable()
                            .instrument(Instrument.BASS)));

    public static final DeferredBlock<SlabBlock> DENDRO_CORE_PLANKS_SLAB =
            REGISTRY.register("dendro_core_planks_slab", () -> new SlabBlock(
                    AbstractBlock.Settings.create().mapColor(MapColor.DARK_GREEN).sounds(net.minecraft.sound.BlockSoundGroup.WOOD).strength(2f, 3f).burnable()
                            .instrument(Instrument.BASS)));

    public static final DeferredBlock<FenceBlock> DENDRO_CORE_PLANKS_FENCE =
            REGISTRY.register("dendro_core_planks_fence", () -> new FenceBlock(
                    AbstractBlock.Settings.create().mapColor(MapColor.DARK_GREEN).sounds(net.minecraft.sound.BlockSoundGroup.WOOD).strength(2f, 3f).burnable()
                            .instrument(Instrument.BASS).solid()));

    public static final DeferredBlock<FenceGateBlock> DENDRO_CORE_PLANKS_FENCE_GATE =
            REGISTRY.register("dendro_core_planks_fence_gate", () -> new FenceGateBlock(
                    AbstractBlock.Settings.create().mapColor(MapColor.DARK_GREEN).sounds(net.minecraft.sound.BlockSoundGroup.WOOD).strength(2f, 3f).burnable()
                            .instrument(Instrument.BASS).solid(), WoodType.OAK));

    public static final DeferredBlock<PressurePlateBlock> DENDRO_CORE_PLANKS_PRESSURE_PLATE =
            REGISTRY.register("dendro_core_planks_pressure_plate", () -> new PressurePlateBlock(PressurePlateBlock.ActivationRule.EVERYTHING, 
                    AbstractBlock.Settings.create().mapColor(MapColor.DARK_GREEN).sounds(net.minecraft.sound.BlockSoundGroup.WOOD).strength(2f, 3f).burnable()
                            .instrument(Instrument.BASS).solid(), BlockSetType.OAK));

    public static final DeferredBlock<ButtonBlock> DENDRO_CORE_PLANKS_BUTTON =
            REGISTRY.register("dendro_core_planks_button", () -> new ButtonBlock(
                    AbstractBlock.Settings.create().mapColor(MapColor.DARK_GREEN).sounds(net.minecraft.sound.BlockSoundGroup.WOOD).strength(2f, 3f).burnable()
                            .instrument(Instrument.BASS), BlockSetType.OAK, 30, false));

    public static final DeferredBlock<CrystalOreFallingBlock> VARUNADA_LAZURITE_ORE =
            REGISTRY.register("varunada_lazurite_ore", () -> new CrystalOreFallingBlock(
                    AbstractBlock.Settings.create().mapColor(MapColor.WATER_BLUE).sounds(net.minecraft.sound.BlockSoundGroup.GRAVEL).strength(2f)
                            .requiresTool().nonOpaque().solidBlock((bs, br, bp) -> false)));

    public static final DeferredBlock<Block> VARUNADA_LAZURITE_BLOCK = REGISTRY.registerSimpleBlock("varunada_lazurite_block",
            AbstractBlock.Settings.create().mapColor(MapColor.WATER_BLUE).sounds(net.minecraft.sound.BlockSoundGroup.GLASS).strength(3f, 20f)
                    .requiresTool());

    public static final DeferredBlock<Block> CHEAP_NETHERITE_BLOCK = REGISTRY.registerSimpleBlock("cheap_netherite_block",
            AbstractBlock.Settings.create().mapColor(MapColor.TERRACOTTA_BLACK).sounds(net.minecraft.sound.BlockSoundGroup.NETHERITE).strength(3f, 10f)
                    .requiresTool());

    public static final DeferredBlock<Block> AGNIDUS_AGATE_ORE = REGISTRY.registerSimpleBlock("agnidus_agate_ore",
            AbstractBlock.Settings.create().mapColor(MapColor.BRIGHT_RED).sounds(net.minecraft.sound.BlockSoundGroup.NETHER_ORE).strength(1f, 2f)
                    .requiresTool().instrument(Instrument.BASEDRUM));

    public static final DeferredBlock<AgnidusAgateBlock> AGNIDUS_AGATE_BLOCK = REGISTRY.register("agnidus_agate_block", () -> new AgnidusAgateBlock(
            AbstractBlock.Settings.create().mapColor(MapColor.BRIGHT_RED).sounds(net.minecraft.sound.BlockSoundGroup.GLASS).strength(3f, 18f)
                    .requiresTool().nonOpaque().solidBlock((bs, br, bp) -> false)));

    public static final DeferredBlock<Block> CHARCOAL_BLOCK = REGISTRY.registerSimpleBlock("charcoal_block",
            AbstractBlock.Settings.create().mapColor(MapColor.BLACK).strength(2f, 20f).requiresTool()
                    .instrument(Instrument.BASEDRUM));

    public static final DeferredBlock<NetherrackFarmlandBlock> NETHERRACK_FARMLAND = REGISTRY.register("netherrack_farmland", () -> new NetherrackFarmlandBlock(
            AbstractBlock.Settings.create().mapColor(MapColor.DARK_RED).sounds(net.minecraft.sound.BlockSoundGroup.NETHERRACK).strength(1f, 10f)
                    .luminance(s -> 6).requiresTool().nonOpaque().pistonBehavior(PistonBehavior.PUSH_ONLY)
                    .solidBlock((bs, br, bp) -> false).instrument(Instrument.BASEDRUM),
            Block.createCuboidShape(0, 0, 0, 16, 15, 16)));

    public static final DeferredBlock<Block> SHIVADA_JADE_ORE = REGISTRY.registerSimpleBlock("shivada_jade_ore",
            AbstractBlock.Settings.create().mapColor(MapColor.LIGHT_BLUE).strength(3f, 5f).requiresTool().slipperiness(1f)
                    .instrument(Instrument.BASEDRUM));

    public static final DeferredBlock<Block> SHIVADA_JADE_BLOCK = REGISTRY.registerSimpleBlock("shivada_jade_block",
            AbstractBlock.Settings.create().mapColor(MapColor.LIGHT_BLUE).sounds(net.minecraft.sound.BlockSoundGroup.GLASS).strength(3f, 20f)
                    .requiresTool().slipperiness(1.2f));

    public static final DeferredBlock<Block> ELEMENTAL_CRYSTAL_BLOCK = REGISTRY.registerSimpleBlock("elemental_crystal_block",
            AbstractBlock.Settings.create().mapColor(MapColor.TERRACOTTA_WHITE).sounds(net.minecraft.sound.BlockSoundGroup.GLASS).strength(1000f, 10000f)
                    .requiresTool());

    public static final DeferredBlock<XiaoLanternLauncherBlock> XIAO_LANTERN_LAUNCHER = REGISTRY.register("xiao_lantern_launcher", () -> new XiaoLanternLauncherBlock(
            AbstractBlock.Settings.create().mapColor(MapColor.YELLOW).sounds(net.minecraft.sound.BlockSoundGroup.NETHERITE).strength(3f, 10f)
                    .pistonBehavior(PistonBehavior.BLOCK).instrument(Instrument.BASEDRUM)));

    public static final DeferredBlock<XiaoLanternLauncherBlock> CREATIVE_XIAO_LANTERN_LAUNCHER =
            REGISTRY.register("creative_xiao_lantern_launcher", () -> new XiaoLanternLauncherBlock(
                    AbstractBlock.Settings.create().mapColor(MapColor.YELLOW).sounds(net.minecraft.sound.BlockSoundGroup.NETHERITE).strength(3f, 10f)
                            .pistonBehavior(PistonBehavior.BLOCK).instrument(Instrument.BASEDRUM)));

    public static final DeferredBlock<Block> MONOCHROME_OTHERWORLD_PLANKS =
            REGISTRY.registerSimpleBlock("monochrome_otherworld_planks",
                    AbstractBlock.Settings.create().mapColor(MapColor.PINK).sounds(net.minecraft.sound.BlockSoundGroup.WOOD).strength(2f, 3f).burnable()
                            .instrument(Instrument.BASS));

    public static final DeferredBlock<StairsBlock> MONOCHROME_OTHERWORLD_PLANKS_STAIRS =
            REGISTRY.register("monochrome_otherworld_planks_stairs", () -> new StairsBlock(MONOCHROME_OTHERWORLD_PLANKS.get().getDefaultState(),
                    AbstractBlock.Settings.create().mapColor(MapColor.PINK).sounds(net.minecraft.sound.BlockSoundGroup.WOOD).strength(3f, 2f).burnable()
                            .instrument(Instrument.BASS)));

    public static final DeferredBlock<SlabBlock> MONOCHROME_OTHERWORLD_PLANKS_SLAB =
            REGISTRY.register("monochrome_otherworld_planks_slab", () -> new SlabBlock(
                    AbstractBlock.Settings.create().mapColor(MapColor.PINK).sounds(net.minecraft.sound.BlockSoundGroup.WOOD).strength(2f, 3f).burnable()
                            .instrument(Instrument.BASS)));

    public static final DeferredBlock<FenceBlock> MONOCHROME_OTHERWORLD_PLANKS_FENCE =
            REGISTRY.register("monochrome_otherworld_planks_fence", () -> new FenceBlock(
                    AbstractBlock.Settings.create().mapColor(MapColor.PINK).sounds(net.minecraft.sound.BlockSoundGroup.WOOD).strength(2f, 3f).burnable()
                            .instrument(Instrument.BASS).solid()));

    public static final DeferredBlock<FenceGateBlock> MONOCHROME_OTHERWORLD_PLANKS_FENCE_GATE =
            REGISTRY.register("monochrome_otherworld_planks_fence_gate", () -> new FenceGateBlock(
                    AbstractBlock.Settings.create().mapColor(MapColor.PINK).sounds(net.minecraft.sound.BlockSoundGroup.WOOD).strength(2f, 3f).burnable()
                            .instrument(Instrument.BASS).solid(), WoodType.OAK));

    public static final DeferredBlock<PressurePlateBlock> MONOCHROME_OTHERWORLD_PLANKS_PRESSURE_PLATE =
            REGISTRY.register("monochrome_otherworld_planks_pressure_plate", () -> new PressurePlateBlock(PressurePlateBlock.ActivationRule.EVERYTHING, 
                    AbstractBlock.Settings.create().mapColor(MapColor.PINK).sounds(net.minecraft.sound.BlockSoundGroup.WOOD).strength(2f, 3f).burnable()
                            .instrument(Instrument.BASS).solid(), BlockSetType.OAK));

    public static final DeferredBlock<ButtonBlock> MONOCHROME_OTHERWORLD_PLANKS_BUTTON =
            REGISTRY.register("monochrome_otherworld_planks_button", () -> new ButtonBlock(
                    AbstractBlock.Settings.create().mapColor(MapColor.PINK).sounds(net.minecraft.sound.BlockSoundGroup.WOOD).strength(2f, 3f).burnable()
                            .instrument(Instrument.BASS), BlockSetType.OAK, 30, false));

    public static final DeferredBlock<Block> BLUE_MONOCHROME_OTHERWORLD_PLANKS =
            REGISTRY.registerSimpleBlock("blue_monochrome_otherworld_planks",
                    AbstractBlock.Settings.create().mapColor(MapColor.LIGHT_BLUE).sounds(net.minecraft.sound.BlockSoundGroup.WOOD).strength(2f, 3f).burnable()
                            .instrument(Instrument.BASS));

    public static final DeferredBlock<StairsBlock> BLUE_MONOCHROME_OTHERWORLD_PLANKS_STAIRS =
            REGISTRY.register("blue_monochrome_otherworld_planks_stairs", () -> new StairsBlock(BLUE_MONOCHROME_OTHERWORLD_PLANKS.get().getDefaultState(),
                    AbstractBlock.Settings.create().mapColor(MapColor.LIGHT_BLUE).sounds(net.minecraft.sound.BlockSoundGroup.WOOD).strength(3f, 2f).burnable()
                            .instrument(Instrument.BASS)));

    public static final DeferredBlock<SlabBlock> BLUE_MONOCHROME_OTHERWORLD_PLANKS_SLAB =
            REGISTRY.register("blue_monochrome_otherworld_planks_slab", () -> new SlabBlock(
                    AbstractBlock.Settings.create().mapColor(MapColor.LIGHT_BLUE).sounds(net.minecraft.sound.BlockSoundGroup.WOOD).strength(2f, 3f).burnable()
                            .instrument(Instrument.BASS)));

    public static final DeferredBlock<FenceBlock> BLUE_MONOCHROME_OTHERWORLD_PLANKS_FENCE =
            REGISTRY.register("blue_monochrome_otherworld_planks_fence", () -> new FenceBlock(
                    AbstractBlock.Settings.create().mapColor(MapColor.LIGHT_BLUE).sounds(net.minecraft.sound.BlockSoundGroup.WOOD).strength(2f, 3f).burnable()
                            .instrument(Instrument.BASS).solid()));

    public static final DeferredBlock<FenceGateBlock> BLUE_MONOCHROME_OTHERWORLD_PLANKS_FENCE_GATE =
            REGISTRY.register("blue_monochrome_otherworld_planks_fence_gate", () -> new FenceGateBlock(
                    AbstractBlock.Settings.create().mapColor(MapColor.LIGHT_BLUE).sounds(net.minecraft.sound.BlockSoundGroup.WOOD).strength(2f, 3f).burnable()
                            .instrument(Instrument.BASS).solid(), WoodType.OAK));

    public static final DeferredBlock<PressurePlateBlock> BLUE_MONOCHROME_OTHERWORLD_PLANKS_PRESSURE_PLATE =
            REGISTRY.register("blue_monochrome_otherworld_planks_pressure_plate", () -> new PressurePlateBlock(PressurePlateBlock.ActivationRule.EVERYTHING, 
                    AbstractBlock.Settings.create().mapColor(MapColor.LIGHT_BLUE).sounds(net.minecraft.sound.BlockSoundGroup.WOOD).strength(2f, 3f).burnable()
                            .instrument(Instrument.BASS).solid(), BlockSetType.OAK));

    public static final DeferredBlock<ButtonBlock> BLUE_MONOCHROME_OTHERWORLD_PLANKS_BUTTON =
            REGISTRY.register("blue_monochrome_otherworld_planks_button", () -> new ButtonBlock(
                    AbstractBlock.Settings.create().mapColor(MapColor.LIGHT_BLUE).sounds(net.minecraft.sound.BlockSoundGroup.WOOD).strength(2f, 3f).burnable()
                            .instrument(Instrument.BASS), BlockSetType.OAK, 30, false));

    public static final DeferredBlock<Block> GEOMARROW_CRYSTAL_BLOCK = REGISTRY.registerSimpleBlock("geomarrow_crystal_block",
            AbstractBlock.Settings.create().mapColor(MapColor.YELLOW).sounds(net.minecraft.sound.BlockSoundGroup.GLASS).strength(5f, 20f)
                    .luminance(s -> 10).requiresTool().slipperiness(0.7f).velocityMultiplier(0.9f).jumpVelocityMultiplier(0.2f)
                    .postProcess((bs, br, bp) -> true).emissiveLighting((bs, br, bp) -> true)
                    .instrument(Instrument.BASEDRUM));

    public static final DeferredBlock<PGCFaceAttachedBlock> GEOMARROW_CRYSTAL_CLUSTER = REGISTRY.register("geomarrow_crystal_cluster", () -> new PGCFaceAttachedBlock(
            AbstractBlock.Settings.create().mapColor(MapColor.YELLOW).sounds(net.minecraft.sound.BlockSoundGroup.GLASS).strength(5f, 20f)
                    .luminance(s -> 15).requiresTool().slipperiness(0.7f).velocityMultiplier(0.9f).jumpVelocityMultiplier(1.2f)
                    .nonOpaque().postProcess((bs, br, bp) -> true).emissiveLighting((bs, br, bp) -> true)
                    .solidBlock((bs, br, bp) -> false).instrument(Instrument.BASEDRUM),
            PGCBlocks::crystalClusterShape));

    public static final DeferredBlock<Block> WEATHERED_STONE_BRICKS = REGISTRY.registerSimpleBlock("weathered_stone_bricks",
            AbstractBlock.Settings.create().mapColor(MapColor.BLACK).strength(2f, 15f).requiresTool()
                    .instrument(Instrument.BASEDRUM));

    public static final DeferredBlock<Block> RUSTY_IRON_BLOCK = REGISTRY.registerSimpleBlock("rusty_iron_block",
            AbstractBlock.Settings.create().mapColor(MapColor.BLACK).sounds(net.minecraft.sound.BlockSoundGroup.LANTERN).strength(2f, 10f)
                    .requiresTool());

    public static final DeferredBlock<PaneBlock> RUSTY_IRON_FENCE =
            REGISTRY.register("rusty_iron_fence", () -> new PaneBlock(
                    AbstractBlock.Settings.create().mapColor(MapColor.BLACK).sounds(net.minecraft.sound.BlockSoundGroup.LANTERN).strength(2f, 10f)
                            .requiresTool().nonOpaque().solidBlock((bs, br, bp) -> false).burnable()));

    public static final DeferredBlock<Block> ROTTEN_WOOD = REGISTRY.registerSimpleBlock("rotten_wood",
            AbstractBlock.Settings.create().mapColor(MapColor.OAK_TAN).sounds(net.minecraft.sound.BlockSoundGroup.WOOD).strength(0.5f, 1f).burnable()
                    .instrument(Instrument.BASS));

    public static final DeferredBlock<StairsBlock> ROTTEN_WOOD_STAIRS =
            REGISTRY.register("rotten_wood_stairs", () -> new StairsBlock(ROTTEN_WOOD.get().getDefaultState(),
                    AbstractBlock.Settings.create().mapColor(MapColor.OAK_TAN).sounds(net.minecraft.sound.BlockSoundGroup.WOOD).strength(1f).nonOpaque()
                            .solidBlock((bs, br, bp) -> false).burnable().instrument(Instrument.BASS)));

    public static final DeferredBlock<FenceBlock> ROTTEN_WOOD_FENCE = REGISTRY.register("rotten_wood_fence", () -> new FenceBlock(
            AbstractBlock.Settings.create().mapColor(MapColor.OAK_TAN).sounds(net.minecraft.sound.BlockSoundGroup.WOOD).strength(1f).nonOpaque()
                    .solidBlock((bs, br, bp) -> false).burnable().instrument(Instrument.BASS)
                    .solid()));

    public static final DeferredBlock<StairsBlock> WEATHERED_STONE_BRICKS_STAIRS =
            REGISTRY.register("weathered_stone_bricks_stairs", () -> new StairsBlock(WEATHERED_STONE_BRICKS.get().getDefaultState(),
                    AbstractBlock.Settings.create().mapColor(MapColor.BLACK).strength(2f, 15f).requiresTool()
                            .instrument(Instrument.BASEDRUM)));

    public static final DeferredBlock<SlabBlock> WEATHERED_STONE_BRICKS_SLAB =
            REGISTRY.register("weathered_stone_bricks_slab", () -> new SlabBlock(
                    AbstractBlock.Settings.create().mapColor(MapColor.BLACK).strength(2f, 15f).requiresTool()
                            .instrument(Instrument.BASEDRUM)));

    public static final DeferredBlock<WallBlock> WEATHERED_STONE_BRICKS_WALL =
            REGISTRY.register("weathered_stone_bricks_wall", () -> new WallBlock(
                    AbstractBlock.Settings.create().mapColor(MapColor.BLACK).strength(2f, 15f).requiresTool()
                            .nonOpaque().solidBlock((bs, br, bp) -> false).instrument(Instrument.BASEDRUM)
                            .solid()));

    public static final DeferredBlock<PGCFaceAttachedBlock> PRIMOGEM_BLOCK = REGISTRY.register("primogem_block", () -> new PGCFaceAttachedBlock(
            AbstractBlock.Settings.create().mapColor(MapColor.PINK).sounds(net.minecraft.sound.BlockSoundGroup.GLASS).strength(6f, 14f)
                    .luminance(s -> 8).requiresTool().slipperiness(0.7f).postProcess((bs, br, bp) -> true)
                    .emissiveLighting((bs, br, bp) -> true).instrument(Instrument.HAT),
            state -> VoxelShapes.fullCube()));

    public static final DeferredBlock<Block> DEEPSLATE_PRIMOGEM_ORE = REGISTRY.registerSimpleBlock("deepslate_primogem_ore",
            AbstractBlock.Settings.create().mapColor(MapColor.PURPLE).sounds(net.minecraft.sound.BlockSoundGroup.POLISHED_DEEPSLATE).strength(5f)
                    .luminance(s -> 1).requiresTool().instrument(Instrument.BASEDRUM));

    public static final DeferredBlock<Block> DEEPSLATE_PRITHIVA_TOPAZ_ORE =
            REGISTRY.registerSimpleBlock("deepslate_prithiva_topaz_ore",
                    AbstractBlock.Settings.create().mapColor(MapColor.GOLD).sounds(net.minecraft.sound.BlockSoundGroup.DEEPSLATE).strength(30f, 5f)
                            .requiresTool().instrument(Instrument.BASEDRUM));

    public static final DeferredBlock<PGCPillarBlock> DENDRO_BLESSING = REGISTRY.register("dendro_blessing", () -> new PGCPillarBlock(
            AbstractBlock.Settings.create().mapColor(MapColor.DARK_GREEN).sounds(net.minecraft.sound.BlockSoundGroup.GLASS).strength(1f, 20f).nonOpaque()
                    .solidBlock((bs, br, bp) -> false).instrument(Instrument.HAT),
            state -> switch (state.get(PGCPillarBlock.AXIS)) {
                case X -> Block.createCuboidShape(0, 1, 1, 14, 14, 14);
                case Z -> Block.createCuboidShape(1, 1, 2, 14, 14, 16);
                default -> Block.createCuboidShape(1, 0, 1, 14, 14, 14);
            }));

    public static final DeferredBlock<PackedMoraPileBlock> PACKED_MORA_PILE =
            REGISTRY.register("packed_mora_pile", () -> new PackedMoraPileBlock(
                    AbstractBlock.Settings.create().mapColor(MapColor.GOLD).sounds(net.minecraft.sound.BlockSoundGroup.ANVIL).strength(10f, 20f)
                            .requiresTool()));

    public static final DeferredBlock<Block> UNFETTERED_METAL_BLOCK = REGISTRY.registerSimpleBlock("unfettered_metal_block",
            AbstractBlock.Settings.create().mapColor(MapColor.CYAN).sounds(net.minecraft.sound.BlockSoundGroup.NETHERITE).strength(3f, 15f)
                    .requiresTool());

    public static final DeferredBlock<Block> STURDY_METAL_BLOCK = REGISTRY.registerSimpleBlock("sturdy_metal_block",
            AbstractBlock.Settings.create().mapColor(MapColor.GOLD).sounds(net.minecraft.sound.BlockSoundGroup.NETHERITE).strength(3f, 15f)
                    .requiresTool());

    public static final DeferredBlock<Block> ETERNAL_METAL_BLOCK = REGISTRY.registerSimpleBlock("eternal_metal_block",
            AbstractBlock.Settings.create().mapColor(MapColor.PURPLE).sounds(net.minecraft.sound.BlockSoundGroup.NETHERITE).strength(3f, 15f)
                    .requiresTool());

    public static final DeferredBlock<Block> WISDOM_METAL_BLOCK = REGISTRY.registerSimpleBlock("wisdom_metal_block",
            AbstractBlock.Settings.create().mapColor(MapColor.DARK_GREEN).sounds(net.minecraft.sound.BlockSoundGroup.NETHERITE).strength(3f, 15f)
                    .requiresTool());

    public static final DeferredBlock<Block> JUSTICE_METAL_BLOCK = REGISTRY.registerSimpleBlock("justice_metal_block",
            AbstractBlock.Settings.create().mapColor(MapColor.WATER_BLUE).sounds(net.minecraft.sound.BlockSoundGroup.NETHERITE).strength(3f, 15f)
                    .requiresTool());

    public static final DeferredBlock<Block> BURNING_WISH_METAL_BLOCK = REGISTRY.registerSimpleBlock("burning_wish_metal_block",
            AbstractBlock.Settings.create().mapColor(MapColor.BRIGHT_RED).sounds(net.minecraft.sound.BlockSoundGroup.NETHERITE).strength(3f, 15f)
                    .requiresTool());

    public static final DeferredBlock<Block> COMPASSION_METAL_BLOCK = REGISTRY.registerSimpleBlock("compassion_metal_block",
            AbstractBlock.Settings.create().mapColor(MapColor.LIGHT_BLUE).sounds(net.minecraft.sound.BlockSoundGroup.NETHERITE).strength(3f, 15f)
                    .requiresTool());

    public static final DeferredBlock<ACakeForYouBlock> A_CAKE_FOR_YOU = REGISTRY.register("a_cake_for_you", () -> new ACakeForYouBlock(
            AbstractBlock.Settings.create().mapColor(MapColor.ORANGE).sounds(net.minecraft.sound.BlockSoundGroup.WOOL).strength(2f, 5f).nonOpaque()
                    .solidBlock((bs, br, bp) -> false),
            Block.createCuboidShape(1, 0, 1, 15, 8, 15)));

    public static final DeferredBlock<Block> FINE_FORGED_ORE_FUSION_BLOCK =
            REGISTRY.registerSimpleBlock("fine_forged_ore_fusion_block",
                    AbstractBlock.Settings.create().mapColor(MapColor.PINK).sounds(net.minecraft.sound.BlockSoundGroup.AMETHYST_BLOCK).strength(5f, 15f)
                            .requiresTool().slipperiness(0.7f));

    public static final DeferredBlock<PaneBlock> SOLID_CRYSTAL_PLATE =
            REGISTRY.register("solid_crystal_plate", () -> new PaneBlock(
                    AbstractBlock.Settings.create().mapColor(MapColor.LIGHT_BLUE).sounds(net.minecraft.sound.BlockSoundGroup.GLASS).strength(300f, 1000f)
                            .requiresTool().instrument(Instrument.HAT)));

    public static final DeferredBlock<GorgeousSmithingTableBlock> GORGEOUS_SMITHING_TABLE = REGISTRY.register("gorgeous_smithing_table", () -> new GorgeousSmithingTableBlock(
            AbstractBlock.Settings.create().mapColor(MapColor.GOLD).sounds(net.minecraft.sound.BlockSoundGroup.WOOD).strength(3f, 10f).burnable()
                    .instrument(Instrument.BASS)));

    public static final DeferredBlock<UnidentifiedDollBlock> UNIDENTIFIED_DOLL = REGISTRY.register("unidentified_doll", () -> new UnidentifiedDollBlock(
            AbstractBlock.Settings.create().mapColor(MapColor.BRIGHT_RED).sounds(net.minecraft.sound.BlockSoundGroup.GRASS).strength(10f, 20f).nonOpaque()
                    .luminance(s -> s.get(UnidentifiedDollBlock.LIT) ? UnidentifiedDollBlock.LIT_LIGHT : 0)
                    .solidBlock((bs, br, bp) -> false).instrument(Instrument.BASEDRUM),
            Block.createCuboidShape(4, 0, 4, 12, 7, 12)));

    public static final DeferredBlock<DetonatorBlock> DETONATOR = REGISTRY.register("detonator", () -> new DetonatorBlock(
            AbstractBlock.Settings.create().sounds(net.minecraft.sound.BlockSoundGroup.WOOD).strength(1f, 10f).nonOpaque()
                    .solidBlock((bs, br, bp) -> false).instrument(Instrument.BASEDRUM),
            Block.createCuboidShape(1, 0, 1, 15, 8, 15)));

    public static final DeferredBlock<Block> ELEMENTAL_CRYSTAL_ORE = REGISTRY.registerSimpleBlock("elemental_crystal_ore",
            AbstractBlock.Settings.create().mapColor(MapColor.PURPLE).strength(3f, 5f).luminance(s -> 1)
                    .requiresTool().instrument(Instrument.BASEDRUM));

    public static final DeferredBlock<Block> DEEPSLATE_ELEMENTAL_CRYSTAL_ORE =
            REGISTRY.registerSimpleBlock("deepslate_elemental_crystal_ore",
                    AbstractBlock.Settings.create().mapColor(MapColor.PURPLE).strength(4f, 5f).luminance(s -> 1)
                            .requiresTool().instrument(Instrument.BASEDRUM));

    public static final DeferredBlock<Block> WHITE_IRON_ORE_BLOCK = REGISTRY.registerSimpleBlock("white_iron_ore_block",
            AbstractBlock.Settings.create().mapColor(MapColor.IRON_GRAY).strength(3f, 5f).requiresTool());

    public static final DeferredBlock<Block> DEEPSLATE_WHITE_IRON_ORE_BLOCK =
            REGISTRY.registerSimpleBlock("deepslate_white_iron_ore_block",
                    AbstractBlock.Settings.create().mapColor(MapColor.IRON_GRAY).sounds(net.minecraft.sound.BlockSoundGroup.DEEPSLATE).strength(4f, 5f)
                            .requiresTool());

    public static final DeferredBlock<Block> OTHERWORLD_CRYSTAL_ORE = REGISTRY.registerSimpleBlock("otherworld_crystal_ore",
            AbstractBlock.Settings.create().mapColor(MapColor.PURPLE).sounds(net.minecraft.sound.BlockSoundGroup.AMETHYST_BLOCK).strength(5f)
                    .requiresTool());

    public static final DeferredBlock<Block> DEEPSLATE_OTHERWORLD_CRYSTAL_ORE =
            REGISTRY.registerSimpleBlock("deepslate_otherworld_crystal_ore",
                    AbstractBlock.Settings.create().mapColor(MapColor.PURPLE).sounds(net.minecraft.sound.BlockSoundGroup.AMETHYST_BLOCK).strength(7f, 5f)
                            .requiresTool());

    public static final DeferredBlock<OtherworldCrystalClusterBlock> OTHERWORLD_CRYSTAL_CLUSTER =
            REGISTRY.register("otherworld_crystal_cluster", () -> new OtherworldCrystalClusterBlock(
                    AbstractBlock.Settings.create().mapColor(MapColor.PURPLE).sounds(net.minecraft.sound.BlockSoundGroup.AMETHYST_CLUSTER).strength(5f)
                            .luminance(s -> 3).requiresTool().nonOpaque().postProcess((bs, br, bp) -> true)
                            .emissiveLighting((bs, br, bp) -> true).solidBlock((bs, br, bp) -> false),
                    PGCBlocks::crystalClusterShape));

    public static final DeferredBlock<Block> CRUDE_WHITE_IRON_BLOCK = REGISTRY.registerSimpleBlock("crude_white_iron_block",
            AbstractBlock.Settings.create().mapColor(MapColor.IRON_GRAY).strength(3f, 10f).requiresTool());

    public static final DeferredBlock<Block> WONDROUS_ENCOUNTER_BLOCK = REGISTRY.registerSimpleBlock("wondrous_encounter_block",
            AbstractBlock.Settings.create().mapColor(MapColor.PINK).sounds(net.minecraft.sound.BlockSoundGroup.AMETHYST_CLUSTER).strength(1f, 10f));

    public static final DeferredBlock<LuckyStatueBlock> LUCKY_STATUE = REGISTRY.register("lucky_statue", () -> new LuckyStatueBlock(
            AbstractBlock.Settings.create().mapColor(MapColor.GOLD).sounds(net.minecraft.sound.BlockSoundGroup.METAL).strength(10f, 1000f).luminance(s -> 1)
                    .nonOpaque().postProcess((bs, br, bp) -> true).emissiveLighting((bs, br, bp) -> true)
                    .solidBlock((bs, br, bp) -> false),
            Block.createCuboidShape(2, 0, 2, 14, 4, 14)));

    public static final DeferredBlock<RewardCasketBlock> MORA_CASKET = REGISTRY.register("mora_casket", () -> new RewardCasketBlock(
            AbstractBlock.Settings.create().mapColor(MapColor.GOLD).sounds(net.minecraft.sound.BlockSoundGroup.WOOD).strength(1f, 10f)));

    public static final DeferredBlock<RewardCasketBlock> PRIMOGEM_CASKET = REGISTRY.register("primogem_casket", () -> new RewardCasketBlock(
            AbstractBlock.Settings.create().mapColor(MapColor.PURPLE).sounds(net.minecraft.sound.BlockSoundGroup.AMETHYST_CLUSTER).strength(1f, 10f)));

    public static final DeferredBlock<CurioCasketBlock> COSMIC_CASKET = REGISTRY.register("cosmic_casket", () -> new CurioCasketBlock(
            AbstractBlock.Settings.create().mapColor(MapColor.OAK_TAN).sounds(net.minecraft.sound.BlockSoundGroup.ANCIENT_DEBRIS).strength(1f, 10f)));

    public static final DeferredBlock<StellarConverterBlock> STELLAR_CONVERTER = REGISTRY.register("stellar_converter", () -> new StellarConverterBlock(
            AbstractBlock.Settings.create().mapColor(MapColor.TERRACOTTA_WHITE).sounds(net.minecraft.sound.BlockSoundGroup.METAL).strength(10f, 20f)
                    .requiresTool()));

    private static VoxelShape trashCanShape(BlockState state) {
        return switch (state.get(PGCFaceAttachedBlock.FACE)) {
            case FLOOR, CEILING -> Block.createCuboidShape(2, 0, 2, 14, 16, 14);
            case WALL -> switch (state.get(PGCFaceAttachedBlock.FACING)) {
                case EAST, WEST -> Block.createCuboidShape(0, 2, 2, 16, 14, 14);
                default -> Block.createCuboidShape(2, 2, 0, 14, 14, 16);
            };
        };
    }

    private static VoxelShape crystalClusterShape(BlockState state) {
        return switch (state.get(PGCFaceAttachedBlock.FACE)) {
            case FLOOR -> Block.createCuboidShape(3, 0, 3, 13, 13, 13);
            case CEILING -> Block.createCuboidShape(3, 3, 3, 13, 16, 13);
            case WALL -> switch (state.get(PGCFaceAttachedBlock.FACING)) {
                case NORTH -> Block.createCuboidShape(3, 3, 3, 13, 13, 16);
                case EAST -> Block.createCuboidShape(0, 3, 3, 13, 13, 13);
                case WEST -> Block.createCuboidShape(3, 3, 3, 16, 13, 13);
                default -> Block.createCuboidShape(3, 3, 0, 13, 13, 13);
            };
        };
    }
}
