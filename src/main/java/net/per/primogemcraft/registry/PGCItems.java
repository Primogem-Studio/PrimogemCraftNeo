package net.per.primogemcraft.registry;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.per.primogemcraft.enchantment.EnchantChoice;
import net.per.primogemcraft.enchantment.EnchantGrade;
import net.per.primogemcraft.enchantment.PGCEnchantments;
import net.per.primogemcraft.item.armor.MoraArmorItem;
import net.per.primogemcraft.item.curio.*;
import net.per.primogemcraft.item.misc.*;
import net.per.primogemcraft.item.tool.*;
import net.per.primogemcraft.item.weapon.*;
import net.per.primogemcraft.item.weapon.element.*;
import net.per.primogemcraft.system.choice.ChoiceSupport;
import net.per.primogemcraft.system.curio.*;
import net.per.primogemcraft.system.curio.effect.LottoPunishmentEffect;
import net.per.primogemcraft.system.curio.effect.PrescriptionEffect;
import net.per.primogemcraft.system.element.Element;
import net.per.primogemcraft.system.wish.WishBanner;
import net.per.primogemcraft.util.EffectSpecs;
import net.per.primogemcraft.util.PlayerItems;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public class PGCItems {
    public static final DeferredRegister.Items REGISTRY = DeferredRegister.createItems(MOD_ID);

    private static final int FROSTED_SLIME_JUMP_TICKS = 100;
    private static final int PRAISE_OF_HIGH_MORALS_TICKS = 1200;

    public static final DeferredItem<PrimogemItem> PRIMOGEM = REGISTRY.registerItem("primogem",
            properties -> new PrimogemItem(properties.fireResistant().rarity(Rarity.RARE).food(food(9, 0.4F))));
    public static final DeferredItem<MoraItem> MORA = REGISTRY.registerItem("mora", MoraItem::new);
    public static final DeferredItem<Item> PRIMOGEM_SHARD = REGISTRY.registerSimpleItem("primogem_shard");
    public static final DeferredItem<DescribedItem> PRIMOGEM_BILLET = REGISTRY.registerItem("primogem_billet", properties -> new DescribedItem(properties.fireResistant()));
    public static final DeferredItem<GenesisCrystalItem> GENESIS_CRYSTAL = REGISTRY.registerItem("genesis_crystal",
            properties -> new GenesisCrystalItem(properties.food(new FoodProperties.Builder().nutrition(5).saturationModifier(3.0F).alwaysEdible().build())));
    public static final DeferredItem<Item> LUCENT_AFTERGLOW = REGISTRY.registerSimpleItem("lucent_afterglow");

    public static final DeferredItem<DescribedItem> PRITHIVA_TOPAZ_GEMSTONE = described("prithiva_topaz_gemstone");
    public static final DeferredItem<DescribedItem> PRITHIVA_TOPAZ_FRAGMENT = described("prithiva_topaz_fragment");
    public static final DeferredItem<DescribedItem> PRITHIVA_TOPAZ_CHUNK = described("prithiva_topaz_chunk");
    public static final DeferredItem<DescribedItem> PRITHIVA_TOPAZ_SLIVER = described("prithiva_topaz_sliver");
    public static final DeferredItem<DescribedItem> VARUNADA_LAZURITE_GEMSTONE = described("varunada_lazurite_gemstone");
    public static final DeferredItem<DescribedItem> VARUNADA_LAZURITE_FRAGMENT = described("varunada_lazurite_fragment");
    public static final DeferredItem<DescribedItem> VARUNADA_LAZURITE_CHUNK = described("varunada_lazurite_chunk");
    public static final DeferredItem<DescribedItem> VARUNADA_LAZURITE_SLIVER = described("varunada_lazurite_sliver");
    public static final DeferredItem<DescribedItem> NAGADUS_EMERALD_GEMSTONE = described("nagadus_emerald_gemstone");
    public static final DeferredItem<DescribedItem> NAGADUS_EMERALD_FRAGMENT = described("nagadus_emerald_fragment");
    public static final DeferredItem<DescribedItem> NAGADUS_EMERALD_CHUNK = described("nagadus_emerald_chunk");
    public static final DeferredItem<DescribedItem> NAGADUS_EMERALD_SLIVER = described("nagadus_emerald_sliver");
    public static final DeferredItem<DescribedItem> VAYUDA_TURQUOISE_GEMSTONE = described("vayuda_turquoise_gemstone");
    public static final DeferredItem<DescribedItem> VAYUDA_TURQUOISE_FRAGMENT = described("vayuda_turquoise_fragment");
    public static final DeferredItem<DescribedItem> VAYUDA_TURQUOISE_CHUNK = described("vayuda_turquoise_chunk");
    public static final DeferredItem<DescribedItem> VAYUDA_TURQUOISE_SLIVER = described("vayuda_turquoise_sliver");
    public static final DeferredItem<DescribedItem> SHIVADA_JADE_GEMSTONE = described("shivada_jade_gemstone");
    public static final DeferredItem<DescribedItem> SHIVADA_JADE_FRAGMENT = described("shivada_jade_fragment");
    public static final DeferredItem<DescribedItem> SHIVADA_JADE_CHUNK = described("shivada_jade_chunk");
    public static final DeferredItem<DescribedItem> SHIVADA_JADE_SLIVER = described("shivada_jade_sliver");
    public static final DeferredItem<DescribedItem> AGNIDUS_AGATE_GEMSTONE = described("agnidus_agate_gemstone");
    public static final DeferredItem<DescribedItem> AGNIDUS_AGATE_FRAGMENT = described("agnidus_agate_fragment");
    public static final DeferredItem<DescribedItem> AGNIDUS_AGATE_CHUNK = described("agnidus_agate_chunk");
    public static final DeferredItem<DescribedItem> AGNIDUS_AGATE_SLIVER = described("agnidus_agate_sliver");
    public static final DeferredItem<DescribedItem> VAJRADA_AMETHYST_GEMSTONE = described("vajrada_amethyst_gemstone");
    public static final DeferredItem<DescribedItem> VAJRADA_AMETHYST_FRAGMENT = described("vajrada_amethyst_fragment");
    public static final DeferredItem<DescribedItem> VAJRADA_AMETHYST_CHUNK = described("vajrada_amethyst_chunk");
    public static final DeferredItem<DescribedItem> VAJRADA_AMETHYST_SLIVER = described("vajrada_amethyst_sliver");

    public static final DeferredItem<DescribedItem> JUSTICE_METAL = described("justice_metal");
    public static final DeferredItem<DescribedItem> JUSTICE_METAL_GRAIN = described("justice_metal_grain");
    public static final DeferredItem<DescribedItem> WISDOM_METAL = described("wisdom_metal");
    public static final DeferredItem<DescribedItem> WISDOM_METAL_GRAIN = described("wisdom_metal_grain");
    public static final DeferredItem<DescribedItem> UNFETTERED_METAL = described("unfettered_metal");
    public static final DeferredItem<DescribedItem> UNFETTERED_METAL_GRAIN = described("unfettered_metal_grain");
    public static final DeferredItem<DescribedItem> ETERNAL_METAL = described("eternal_metal");
    public static final DeferredItem<DescribedItem> ETERNAL_METAL_GRAIN = described("eternal_metal_grain");
    public static final DeferredItem<DescribedItem> STURDY_METAL = described("sturdy_metal");
    public static final DeferredItem<DescribedItem> STURDY_METAL_GRAIN = described("sturdy_metal_grain");
    public static final DeferredItem<DescribedItem> BURNING_WISH_METAL = described("burning_wish_metal");
    public static final DeferredItem<DescribedItem> BURNING_WISH_METAL_GRAIN = described("burning_wish_metal_grain");
    public static final DeferredItem<DescribedItem> COMPASSION_METAL = described("compassion_metal");
    public static final DeferredItem<DescribedItem> COMPASSION_METAL_GRAIN = described("compassion_metal_grain");

    public static final DeferredItem<EnhancementOreItem> FINE_ENHANCEMENT_ORE = REGISTRY.registerItem("fine_enhancement_ore", EnhancementOreItem::new);
    public static final DeferredItem<EnhancementOreItem> MYSTIC_ENHANCEMENT_ORE = REGISTRY.registerItem("mystic_enhancement_ore", EnhancementOreItem::new);
    public static final DeferredItem<EnhancementOreItem> ENHANCEMENT_ORE = REGISTRY.registerItem("enhancement_ore", EnhancementOreItem::new);
    public static final DeferredItem<TreatedFineOreItem> SPECIALLY_TREATED_FINE_ORE = REGISTRY.registerItem("specially_treated_fine_ore", TreatedFineOreItem::new);
    public static final DeferredItem<DescribedItem> CUSTOM_SUPERIMPOSER = REGISTRY.registerItem("custom_superimposer", properties -> new DescribedItem(properties.stacksTo(1)));
    public static final DeferredItem<UpgradeSmithingTemplateItem> MORA_UPGRADE_SMITHING_TEMPLATE = REGISTRY.registerItem("mora_upgrade_smithing_template", UpgradeSmithingTemplateItem::new);
    public static final DeferredItem<UpgradeSmithingTemplateItem> ELEMENTAL_CRYSTAL_UPGRADE_SMITHING_TEMPLATE = REGISTRY.registerItem("elemental_crystal_upgrade_smithing_template",
            properties -> new UpgradeSmithingTemplateItem(properties.fireResistant()));
    public static final DeferredItem<UpgradeSmithingTemplateItem> ELEMENTAL_CRYSTAL_ENHANCEMENT_SMITHING_TEMPLATE = REGISTRY.registerItem("elemental_crystal_enhancement_smithing_template",
            properties -> new UpgradeSmithingTemplateItem(properties.fireResistant()));
    public static final DeferredItem<DescribedItem> MASTERLESS_STARDUST = described("masterless_stardust");
    public static final DeferredItem<DescribedItem> MASTERLESS_STARGLITTER = described("masterless_starglitter");
    public static final DeferredItem<DescribedItem> DUST_OF_AZOTH = described("dust_of_azoth");
    public static final DeferredItem<DescribedItem> ELEMENTAL_CRYSTAL = described("elemental_crystal");
    public static final DeferredItem<DescribedItem> OTHERWORLD_CRYSTAL = described("otherworld_crystal");
    public static final DeferredItem<ElementalMoltenBeadItem> ELEMENTAL_MOLTEN_BEAD = REGISTRY.registerItem("elemental_molten_bead",
            properties -> new ElementalMoltenBeadItem(properties.stacksTo(1).fireResistant()));
    public static final DeferredItem<DescribedItem> ELEMENTAL_MOLTEN_BEAD_FRAGMENT = described("elemental_molten_bead_fragment", Rarity.EPIC);
    public static final DeferredItem<DescribedItem> ELEMENTAL_DISSOLVING_BEAD_DUST = described("elemental_dissolving_bead_dust", Rarity.RARE);
    public static final DeferredItem<DescribedItem> ELEMENTAL_CRYSTAL_DUST = described("elemental_crystal_dust");
    public static final DeferredItem<DescribedItem> PRIMOGEM_DUST = described("primogem_dust");
    public static final DeferredItem<DescribedItem> DIAMOND_SHARD = described("diamond_shard");
    public static final DeferredItem<DescribedItem> FIREARM_FRAGMENT = described("firearm_fragment");

    public static final DeferredItem<DescribedItem> ARCHAIC_STONE = described("archaic_stone");
    public static final DeferredItem<DescribedItem> GEOMARROW = described("geomarrow");
    public static final DeferredItem<DescribedItem> WHITE_IRON_ORE = described("white_iron_ore");
    public static final DeferredItem<DreamSakuraItem> DREAM_SAKURA = REGISTRY.registerItem("dream_sakura", DreamSakuraItem::new);
    public static final DeferredItem<DescribedItem> BROKEN_DREAM_SAKURA = described("broken_dream_sakura");
    public static final DeferredItem<EnigmataPetalItem> ENIGMATA_PETAL = REGISTRY.registerItem("enigmata_petal", EnigmataPetalItem::new);
    public static final DeferredItem<DescribedItem> UNKNOWN_LEAF = described("unknown_leaf");
    public static final DeferredItem<DescribedItem> DENDRO_CORE = described("dendro_core");
    public static final DeferredItem<DescribedItem> OTHERWORLD_WOOD_STICK = described("otherworld_wood_stick");
    public static final DeferredItem<DescribedItem> OTHERWORLDLY_MOON_GOLD = described("otherworldly_moon_gold");
    public static final DeferredItem<DescribedItem> HEAVY_FLAVORED_IRON_INGOT = described("heavy_flavored_iron_ingot");
    public static final DeferredItem<DescribedItem> CUCKOO_CLOCK_PART = described("cuckoo_clock_part");
    public static final DeferredItem<CuckooClockSandwichItem> CUCKOO_CLOCK_SANDWICH = REGISTRY.registerItem("cuckoo_clock_sandwich",
            properties -> new CuckooClockSandwichItem(properties.fireResistant().rarity(Rarity.UNCOMMON)
                    .food(new FoodProperties.Builder().nutrition(1).saturationModifier(-0.4F).alwaysEdible().build())));
    public static final DeferredItem<EffectFoodItem> FROSTED_SLIME = REGISTRY.registerItem("frosted_slime",
            properties -> new EffectFoodItem(properties.food(food(10, 0.8F)), PGCItems::frostedSlimeBonus,
                    EffectSpecs.hidden(MobEffects.JUMP, FROSTED_SLIME_JUMP_TICKS, 0)));
    public static final DeferredItem<EffectFoodItem> ADEPTUS_TEMPTATION = REGISTRY.registerItem("adeptus_temptation",
            properties -> new EffectFoodItem(properties.stacksTo(8).fireResistant().rarity(Rarity.EPIC).food(
                    new FoodProperties.Builder().nutrition(12).saturationModifier(0.8F).alwaysEdible().build()),
                    null, EffectSpecs.of(PGCEffects.ATTACK_BOOST, 6000, 19)));
    public static final DeferredItem<OrdinaryCoffeeItem> ORDINARY_COFFEE = REGISTRY.registerItem("ordinary_coffee",
            properties -> new OrdinaryCoffeeItem(properties.stacksTo(1)));
    public static final DeferredItem<DescribedItem> THREE_MOONS_BLESSING = REGISTRY.registerItem("three_moons_blessing",
            properties -> new DescribedItem(properties.stacksTo(1).fireResistant()));
    public static final DeferredItem<DescribedItem> HAKUSHINS_LULLABY = disc("hakushins_lullaby");
    public static final DeferredItem<DescribedItem> BALLAD_OF_MANY_WATERS = disc("ballad_of_many_waters");
    public static final DeferredItem<ChasingStarlightWithYouItem> CHASING_STARLIGHT_WITH_YOU = REGISTRY.registerItem("chasing_starlight_with_you", ChasingStarlightWithYouItem::new);
    public static final DeferredItem<TheBeginningOfEverythingItem> THE_BEGINNING_OF_EVERYTHING = REGISTRY.registerItem("the_beginning_of_everything", TheBeginningOfEverythingItem::new);
    public static final DeferredItem<TakeTheJourneyItem> TAKE_THE_JOURNEY = REGISTRY.registerItem("take_the_journey", TakeTheJourneyItem::new);
    public static final DeferredItem<DescribedItem> SPACE_WALK = disc("space_walk");
    public static final DeferredItem<DescribedItem> SALTY_MOON = disc("salty_moon");
    public static final DeferredItem<DescribedItem> A_DAY_OF_HOPE = disc("a_day_of_hope");
    public static final DeferredItem<DescribedItem> VILLAGE_SURROUNDED_BY_GREEN = disc("village_surrounded_by_green");
    public static final DeferredItem<DescribedItem> THE_VILLAGE_NO_LONGER_YOUNG = disc("the_village_no_longer_young");
    public static final DeferredItem<EffectFoodItem> PRAISE_OF_HIGH_MORALS = REGISTRY.registerItem("praise_of_high_morals",
            properties -> new EffectFoodItem(properties.fireResistant().rarity(Rarity.UNCOMMON).food(food(0, 0.0F)), null,
                    EffectSpecs.of(MobEffects.HERO_OF_THE_VILLAGE, PRAISE_OF_HIGH_MORALS_TICKS, 0)));
    public static final DeferredItem<XiaoLanternItem> XIAO_LANTERN = REGISTRY.registerItem("xiao_lantern", XiaoLanternItem::new);
    public static final DeferredItem<EquilibriumTabletItem> EQUILIBRIUM_TABLET = REGISTRY.registerItem("equilibrium_tablet", properties -> new EquilibriumTabletItem(1, properties));
    public static final DeferredItem<EquilibriumTabletItem> EQUILIBRIUM_TABLET_1 = REGISTRY.registerItem("equilibrium_tablet_1", properties -> new EquilibriumTabletItem(2, properties));
    public static final DeferredItem<EquilibriumTabletItem> EQUILIBRIUM_TABLET_2 = REGISTRY.registerItem("equilibrium_tablet_2", properties -> new EquilibriumTabletItem(3, properties));

    public static final DeferredItem<DescribedItem> MORA_COMMEMORATIVE_COIN = described("mora_commemorative_coin");
    public static final DeferredItem<DescribedItem> REFINED_MORA = described("refined_mora");
    public static final DeferredItem<DescribedItem> LARGE_REFINED_MORA = described("large_refined_mora");
    public static final DeferredItem<DescribedItem> SMALL_REFINED_MORA = described("small_refined_mora");
    public static final DeferredItem<DescribedItem> MORA_PROTOTYPE = described("mora_prototype");

    public static final DeferredItem<MoraArmorItem> MORA_HELMET = moraArmor("mora_helmet", ArmorItem.Type.HELMET);
    public static final DeferredItem<MoraArmorItem> MORA_CHESTPLATE = moraArmor("mora_chestplate", ArmorItem.Type.CHESTPLATE);
    public static final DeferredItem<MoraArmorItem> MORA_LEGGINGS = moraArmor("mora_leggings", ArmorItem.Type.LEGGINGS);
    public static final DeferredItem<MoraArmorItem> MORA_BOOTS = moraArmor("mora_boots", ArmorItem.Type.BOOTS);

    private static final int WANDERERS_ADVICE_CAPACITY = 550;
    private static final int ADVENTURERS_EXPERIENCE_CAPACITY = 1089;
    private static final int HEROS_WIT_CAPACITY = 1395;

    public static final DeferredItem<ExperienceBookItem> HEROS_WIT = experienceBook("heros_wit", HEROS_WIT_CAPACITY, Rarity.EPIC);
    public static final DeferredItem<RewardExperienceBookItem> HEROS_WIT_REWARD = reward("heros_wit_reward", Rarity.EPIC, 500, 1398, 200, 800);
    public static final DeferredItem<ExperienceBookItem> WANDERERS_ADVICE = experienceBook("wanderers_advice", WANDERERS_ADVICE_CAPACITY, Rarity.COMMON);
    public static final DeferredItem<RewardExperienceBookItem> WANDERERS_ADVICE_REWARD = reward("wanderers_advice_reward", Rarity.COMMON, 100, 550, 70, 200);
    public static final DeferredItem<ExperienceBookItem> ADVENTURERS_EXPERIENCE = experienceBook("adventurers_experience", ADVENTURERS_EXPERIENCE_CAPACITY, Rarity.RARE);
    public static final DeferredItem<RewardExperienceBookItem> ADVENTURERS_EXPERIENCE_REWARD = reward("adventurers_experience_reward", Rarity.RARE, 300, 1090, 100, 600);

    private static final TagKey<Block> DETECTABLE_ORES = blockTag("detectable_ores");
    private static final TagKey<Block> VANILLA_ORES = blockTag("vanilla_ores");

    public static final DeferredItem<MoraCompassItem> MORA_COMPASS = compass("mora_compass", 1, Rarity.UNCOMMON, DETECTABLE_ORES);
    public static final DeferredItem<MoraCompassItem> COMPLETE_MORA_COMPASS = compass("complete_mora_compass", 4, Rarity.EPIC, null);
    public static final DeferredItem<MoraCompassItem> NETHERITE_MORA_COMPASS = compass("netherite_mora_compass", 3, Rarity.UNCOMMON, Tags.Blocks.ORES);
    public static final DeferredItem<MoraCompassItem> DIAMOND_MORA_COMPASS = compass("diamond_mora_compass", 2, Rarity.RARE, VANILLA_ORES);
    private static final int WANDERERS_POUCH_SIZE = 18;
    private static final int ADVENTURERS_POUCH_SIZE = 27;
    private static final int FOREIGNERS_POUCH_SIZE = 66;
    private static final int EMERGENCY_STORAGE_POUCH_SIZE = 143;

    public static final DeferredItem<PouchItem> EMERGENCY_STORAGE_POUCH = pouch("emergency_storage_pouch", EMERGENCY_STORAGE_POUCH_SIZE);
    public static final DeferredItem<OtherworldBankbookItem> OTHERWORLD_BANKBOOK = REGISTRY.registerItem("otherworld_bankbook",
            properties -> new OtherworldBankbookItem(properties.stacksTo(1)));
    public static final DeferredItem<PouchItem> ADVENTURERS_POUCH = pouch("adventurers_pouch", ADVENTURERS_POUCH_SIZE);
    public static final DeferredItem<PouchItem> FOREIGNERS_POUCH = pouch("foreigners_pouch", FOREIGNERS_POUCH_SIZE);
    public static final DeferredItem<PouchItem> WANDERERS_POUCH = pouch("wanderers_pouch", WANDERERS_POUCH_SIZE);
    public static final DeferredItem<OtherworldWindCatcherItem> OTHERWORLD_WIND_CATCHER = REGISTRY.registerItem("otherworld_wind_catcher",
            properties -> new OtherworldWindCatcherItem(properties.stacksTo(1), true));
    public static final DeferredItem<OtherworldWindCatcherItem> OTHERWORLD_WIND_CATCHER_EMPTY = REGISTRY.registerItem("otherworld_wind_catcher_empty",
            properties -> new OtherworldWindCatcherItem(properties.stacksTo(1), false));
    public static final DeferredItem<BlessingOfTheWelkinMoonItem> BLESSING_OF_THE_WELKIN_MOON = REGISTRY.registerItem("blessing_of_the_welkin_moon",
            properties -> new BlessingOfTheWelkinMoonItem(properties.fireResistant().rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<DescribedItem> UNLUCKY_VOUCHER = described("unlucky_voucher");
    public static final DeferredItem<FootprintsOfFateItem> FOOTPRINTS_OF_FATE = REGISTRY.registerItem("footprints_of_fate", FootprintsOfFateItem::new);
    public static final DeferredItem<LuckySpecialTicketItem> LUCKY_SPECIAL_TICKET = REGISTRY.registerItem("lucky_special_ticket",
            properties -> new LuckySpecialTicketItem(properties.rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<StackOfEnchantedBooksItem> STACK_OF_ENCHANTED_BOOKS = REGISTRY.registerItem("stack_of_enchanted_books", StackOfEnchantedBooksItem::new);
    public static final DeferredItem<StackOfCosmicBigLottoItem> STACK_OF_COSMIC_BIG_LOTTO = REGISTRY.registerItem("stack_of_cosmic_big_lotto", StackOfCosmicBigLottoItem::new);
    public static final DeferredItem<WondrousEncounterItem> WONDROUS_ENCOUNTER = REGISTRY.registerItem("wondrous_encounter", WondrousEncounterItem::new);
    public static final DeferredItem<DescribedItem> GORGEOUS_SILK_THREAD = described("gorgeous_silk_thread");
    public static final DeferredItem<DescribedItem> ENIGMATA_FACTION_BOND = described("enigmata_faction_bond");
    public static final DeferredItem<EnigmataGiftBoxItem> ENIGMATA_GIFT_BOX = REGISTRY.registerItem("enigmata_gift_box", EnigmataGiftBoxItem::new);
    public static final DeferredItem<DescribedItem> STELLAR_JADE = described("stellar_jade");
    public static final DeferredItem<CosmicFragmentItem> COSMIC_FRAGMENT = REGISTRY.registerItem("cosmic_fragment", CosmicFragmentItem::new);
    public static final DeferredItem<DescribedItem> CURIO_FUSION_AGENT = REGISTRY.registerItem("curio_fusion_agent", properties -> new DescribedItem(properties.rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<DescribedItem> POTENT_CURIO_FUSION_AGENT = REGISTRY.registerItem("potent_curio_fusion_agent", properties -> new DescribedItem(properties.rarity(Rarity.EPIC)));
    public static final DeferredItem<DescribedItem> CURIO_SYNTHESIS_REAGENT = REGISTRY.registerItem("curio_synthesis_reagent", properties -> new DescribedItem(properties.rarity(Rarity.RARE)));

    public static final DeferredItem<EffectFoodItem> TRASH = REGISTRY.registerItem("trash",
            properties -> new EffectFoodItem(properties.rarity(Rarity.EPIC).food(food(2, -10.0F)), null,
                    EffectSpecs.hidden(MobEffects.CONFUSION, 400, 127),
                    EffectSpecs.hidden(MobEffects.POISON, 60, 20),
                    EffectSpecs.hidden(MobEffects.WEAKNESS, 600, 10)));
    public static final DeferredItem<EffectFoodItem> SANCTITY_OF_THE_TRASHCAN = REGISTRY.registerItem("sanctity_of_the_trashcan",
            properties -> new EffectFoodItem(properties.fireResistant().rarity(Rarity.UNCOMMON).food(food(0, 0.0F)), null,
                    EffectSpecs.of(MobEffects.DAMAGE_RESISTANCE, 3600, 1)));
    public static final DeferredItem<DescribedItem> TRASH_CAN_FRAGMENT = described("trash_can_fragment");
    public static final DeferredItem<StinkingSolutionItem> STINKING_SOLUTION = REGISTRY.registerItem("stinking_solution",
            properties -> new StinkingSolutionItem(properties.food(food(0, 0.0F)).craftRemainder(Items.GLASS_BOTTLE),
                    EffectSpecs.of(MobEffects.POISON, 1480, 3),
                    EffectSpecs.of(MobEffects.CONFUSION, 6280, 3)));
    public static final DeferredItem<EffectFoodItem> PLEASANT_LOOKING_TRASH = REGISTRY.registerItem("pleasant_looking_trash",
            properties -> new EffectFoodItem(properties.rarity(Rarity.UNCOMMON).food(food(4, 0.3F)),
                    PGCItems::cureLottoPunishment,
                    EffectSpecs.of(MobEffects.HEALTH_BOOST, 1200, 1),
                    EffectSpecs.of(MobEffects.HEAL, 10, 1)));
    public static final DeferredItem<ShieldOfRadiantWillItem> SHIELD_OF_RADIANT_WILL = REGISTRY.registerItem("shield_of_radiant_will", ShieldOfRadiantWillItem::new);
    public static final DeferredItem<IronTrashCanLidItem> IRON_TRASH_CAN_LID = REGISTRY.registerItem("iron_trash_can_lid", IronTrashCanLidItem::new);
    public static final DeferredItem<ShieldOfWealthAndHopeItem> SHIELD_OF_WEALTH_AND_HOPE = REGISTRY.registerItem("shield_of_wealth_and_hope", ShieldOfWealthAndHopeItem::new);

    public static final DeferredItem<TheBlackSwordItem> THE_BLACK_SWORD = REGISTRY.registerItem("the_black_sword", TheBlackSwordItem::new);
    public static final DeferredItem<MistsplitterReforgedItem> MISTSPLITTER_REFORGED = fiveStarWeapon("mistsplitter_reforged", MistsplitterReforgedItem::new);
    public static final DeferredItem<SkywardBladeItem> SKYWARD_BLADE = fiveStarWeapon("skyward_blade", SkywardBladeItem::new);
    public static final DeferredItem<HewnEdgeBladeItem> HEWN_EDGE_BLADE = fiveStarWeapon("hewn_edge_blade", HewnEdgeBladeItem::new);
    public static final DeferredItem<PrimordialJadeCutterItem> PRIMORDIAL_JADE_CUTTER = fiveStarWeapon("primordial_jade_cutter", PrimordialJadeCutterItem::new);
    public static final DeferredItem<DullBladeItem> DULL_BLADE = REGISTRY.registerItem("dull_blade", DullBladeItem::new);
    public static final DeferredItem<TheFluteItem> THE_FLUTE = REGISTRY.registerItem("the_flute", TheFluteItem::new);
    public static final DeferredItem<LionsRoarItem> LIONS_ROAR = REGISTRY.registerItem("lions_roar", LionsRoarItem::new);
    public static final DeferredItem<HarbingerOfDawnItem> HARBINGER_OF_DAWN = REGISTRY.registerItem("harbinger_of_dawn", HarbingerOfDawnItem::new);
    public static final DeferredItem<SplendorOfTranquilWatersItem> SPLENDOR_OF_TRANQUIL_WATERS = fiveStarWeapon("splendor_of_tranquil_waters", SplendorOfTranquilWatersItem::new);
    public static final DeferredItem<SkyriderSwordItem> SKYRIDER_SWORD = REGISTRY.registerItem("skyrider_sword", SkyriderSwordItem::new);
    public static final DeferredItem<WasterGreatswordItem> WASTER_GREATSWORD = REGISTRY.registerItem("waster_greatsword", WasterGreatswordItem::new);
    public static final DeferredItem<EngulfingLightningItem> ENGULFING_LIGHTNING = fiveStarWeapon("engulfing_lightning", EngulfingLightningItem::new);
    public static final DeferredItem<BlackTasselItem> BLACK_TASSEL = REGISTRY.registerItem("black_tassel", BlackTasselItem::new);
    public static final DeferredItem<FavoniusLanceItem> FAVONIUS_LANCE = REGISTRY.registerItem("favonius_lance", FavoniusLanceItem::new);
    public static final DeferredItem<BeginnersProtectorItem> BEGINNERS_PROTECTOR = REGISTRY.registerItem("beginners_protector", BeginnersProtectorItem::new);
    public static final DeferredItem<StaffOfHomaItem> STAFF_OF_HOMA = fiveStarWeapon("staff_of_homa", StaffOfHomaItem::new);
    public static final DeferredItem<DeathmatchItem> DEATHMATCH = fiveStarWeapon("deathmatch", DeathmatchItem::new);
    public static final DeferredItem<PrimordialJadeWingedSpearItem> PRIMORDIAL_JADE_WINGED_SPEAR = fiveStarWeapon("primordial_jade_winged_spear", PrimordialJadeWingedSpearItem::new);
    public static final DeferredItem<MoraSwordItem> MORA_SWORD = REGISTRY.registerItem("mora_sword", MoraSwordItem::new);
    public static final DeferredItem<MoraHoeItem> MORA_HOE = REGISTRY.registerItem("mora_hoe", MoraHoeItem::new);
    public static final DeferredItem<MoraPickaxeItem> MORA_PICKAXE = REGISTRY.registerItem("mora_pickaxe", MoraPickaxeItem::new);
    public static final DeferredItem<MoraAxeItem> MORA_AXE = REGISTRY.registerItem("mora_axe", MoraAxeItem::new);
    public static final DeferredItem<MoraShovelItem> MORA_SHOVEL = REGISTRY.registerItem("mora_shovel", MoraShovelItem::new);
    public static final DeferredItem<VarunadaLazuriteHoeItem> VARUNADA_LAZURITE_HOE = REGISTRY.registerItem("varunada_lazurite_hoe", VarunadaLazuriteHoeItem::new);
    public static final DeferredItem<IntertwinedFatePickaxeItem> INTERTWINED_FATE_PICKAXE = fiveStarWeapon("intertwined_fate_pickaxe", IntertwinedFatePickaxeItem::new);
    public static final DeferredItem<PhilosophiesOfDiligenceItem> PHILOSOPHIES_OF_DILIGENCE = REGISTRY.registerItem("philosophies_of_diligence", PhilosophiesOfDiligenceItem::new);
    public static final DeferredItem<PhilosophiesOfGoldItem> PHILOSOPHIES_OF_GOLD = REGISTRY.registerItem("philosophies_of_gold", PhilosophiesOfGoldItem::new);
    public static final DeferredItem<PhilosophiesOfProsperityItem> PHILOSOPHIES_OF_PROSPERITY = REGISTRY.registerItem("philosophies_of_prosperity", PhilosophiesOfProsperityItem::new);

    public static final DeferredItem<PrimogemSwordItem> PRIMOGEM_SWORD = REGISTRY.registerItem("primogem_sword", PrimogemSwordItem::new);
    public static final DeferredItem<PrimogemPickaxeItem> PRIMOGEM_PICKAXE = REGISTRY.registerItem("primogem_pickaxe", PrimogemPickaxeItem::new);
    public static final DeferredItem<PrimogemAxeItem> PRIMOGEM_AXE = REGISTRY.registerItem("primogem_axe", PrimogemAxeItem::new);
    public static final DeferredItem<PrimogemShovelItem> PRIMOGEM_SHOVEL = REGISTRY.registerItem("primogem_shovel", PrimogemShovelItem::new);
    public static final DeferredItem<PrimogemHoeItem> PRIMOGEM_HOE = REGISTRY.registerItem("primogem_hoe", PrimogemHoeItem::new);
    public static final DeferredItem<StrangePrimogemSwordItem> STRANGE_PRIMOGEM_SWORD = REGISTRY.registerItem("strange_primogem_sword", StrangePrimogemSwordItem::new);

    public static final DeferredItem<VayudaTurquoisePickaxeItem> VAYUDA_TURQUOISE_PICKAXE = REGISTRY.registerItem("vayuda_turquoise_pickaxe", VayudaTurquoisePickaxeItem::new);
    public static final DeferredItem<VayudaTurquoiseAxeItem> VAYUDA_TURQUOISE_AXE = REGISTRY.registerItem("vayuda_turquoise_axe", VayudaTurquoiseAxeItem::new);
    public static final DeferredItem<VayudaTurquoiseHoeItem> VAYUDA_TURQUOISE_HOE = REGISTRY.registerItem("vayuda_turquoise_hoe", VayudaTurquoiseHoeItem::new);
    public static final DeferredItem<VayudaTurquoiseShovelItem> VAYUDA_TURQUOISE_SHOVEL = REGISTRY.registerItem("vayuda_turquoise_shovel", VayudaTurquoiseShovelItem::new);

    public static final DeferredItem<PrithivaTopazPickaxeItem> PRITHIVA_TOPAZ_PICKAXE = REGISTRY.registerItem("prithiva_topaz_pickaxe", PrithivaTopazPickaxeItem::new);
    public static final DeferredItem<PrithivaTopazAxeItem> PRITHIVA_TOPAZ_AXE = REGISTRY.registerItem("prithiva_topaz_axe", PrithivaTopazAxeItem::new);
    public static final DeferredItem<PrithivaTopazHoeItem> PRITHIVA_TOPAZ_HOE = REGISTRY.registerItem("prithiva_topaz_hoe", PrithivaTopazHoeItem::new);
    public static final DeferredItem<PrithivaTopazShovelItem> PRITHIVA_TOPAZ_SHOVEL = REGISTRY.registerItem("prithiva_topaz_shovel", PrithivaTopazShovelItem::new);

    public static final DeferredItem<VajradaAmethystPickaxeItem> VAJRADA_AMETHYST_PICKAXE = REGISTRY.registerItem("vajrada_amethyst_pickaxe", VajradaAmethystPickaxeItem::new);
    public static final DeferredItem<VajradaAmethystAxeItem> VAJRADA_AMETHYST_AXE = REGISTRY.registerItem("vajrada_amethyst_axe", VajradaAmethystAxeItem::new);
    public static final DeferredItem<VajradaAmethystHoeItem> VAJRADA_AMETHYST_HOE = REGISTRY.registerItem("vajrada_amethyst_hoe", VajradaAmethystHoeItem::new);
    public static final DeferredItem<VajradaAmethystShovelItem> VAJRADA_AMETHYST_SHOVEL = REGISTRY.registerItem("vajrada_amethyst_shovel", VajradaAmethystShovelItem::new);

    public static final DeferredItem<NagadusEmeraldPickaxeItem> NAGADUS_EMERALD_PICKAXE = REGISTRY.registerItem("nagadus_emerald_pickaxe", NagadusEmeraldPickaxeItem::new);
    public static final DeferredItem<NagadusEmeraldAxeItem> NAGADUS_EMERALD_AXE = REGISTRY.registerItem("nagadus_emerald_axe", NagadusEmeraldAxeItem::new);
    public static final DeferredItem<NagadusEmeraldHoeItem> NAGADUS_EMERALD_HOE = REGISTRY.registerItem("nagadus_emerald_hoe", NagadusEmeraldHoeItem::new);
    public static final DeferredItem<NagadusEmeraldShovelItem> NAGADUS_EMERALD_SHOVEL = REGISTRY.registerItem("nagadus_emerald_shovel", NagadusEmeraldShovelItem::new);

    public static final DeferredItem<VarunadaLazuritePickaxeItem> VARUNADA_LAZURITE_PICKAXE = REGISTRY.registerItem("varunada_lazurite_pickaxe", VarunadaLazuritePickaxeItem::new);
    public static final DeferredItem<VarunadaLazuriteAxeItem> VARUNADA_LAZURITE_AXE = REGISTRY.registerItem("varunada_lazurite_axe", VarunadaLazuriteAxeItem::new);
    public static final DeferredItem<VarunadaLazuriteShovelItem> VARUNADA_LAZURITE_SHOVEL = REGISTRY.registerItem("varunada_lazurite_shovel", VarunadaLazuriteShovelItem::new);

    public static final DeferredItem<AgnidusAgatePickaxeItem> AGNIDUS_AGATE_PICKAXE = REGISTRY.registerItem("agnidus_agate_pickaxe", AgnidusAgatePickaxeItem::new);
    public static final DeferredItem<AgnidusAgateAxeItem> AGNIDUS_AGATE_AXE = REGISTRY.registerItem("agnidus_agate_axe", AgnidusAgateAxeItem::new);
    public static final DeferredItem<AgnidusAgateHoeItem> AGNIDUS_AGATE_HOE = REGISTRY.registerItem("agnidus_agate_hoe", AgnidusAgateHoeItem::new);
    public static final DeferredItem<AgnidusAgateShovelItem> AGNIDUS_AGATE_SHOVEL = REGISTRY.registerItem("agnidus_agate_shovel", AgnidusAgateShovelItem::new);

    public static final DeferredItem<ShivadaJadePickaxeItem> SHIVADA_JADE_PICKAXE = REGISTRY.registerItem("shivada_jade_pickaxe", ShivadaJadePickaxeItem::new);
    public static final DeferredItem<ShivadaJadeAxeItem> SHIVADA_JADE_AXE = REGISTRY.registerItem("shivada_jade_axe", ShivadaJadeAxeItem::new);
    public static final DeferredItem<ShivadaJadeHoeItem> SHIVADA_JADE_HOE = REGISTRY.registerItem("shivada_jade_hoe", ShivadaJadeHoeItem::new);
    public static final DeferredItem<ShivadaJadeShovelItem> SHIVADA_JADE_SHOVEL = REGISTRY.registerItem("shivada_jade_shovel", ShivadaJadeShovelItem::new);
    public static final DeferredItem<AnomalyShivadaJadePickaxeItem> ANOMALY_SHIVADA_JADE_PICKAXE = REGISTRY.registerItem("anomaly_shivada_jade_pickaxe", AnomalyShivadaJadePickaxeItem::new);
    public static final DeferredItem<VarunadaLazuriteBlossomPickaxeItem> VARUNADA_LAZURITE_BLOSSOM_PICKAXE = REGISTRY.registerItem("varunada_lazurite_blossom_pickaxe", VarunadaLazuriteBlossomPickaxeItem::new);

    public static final DeferredItem<ShiningTrapezohedronDieItem> SHINING_TRAPEZOHEDRON_DIE = REGISTRY.registerItem("shining_trapezohedron_die", properties -> new ShiningTrapezohedronDieItem(properties.fireResistant()));
    public static final DeferredItem<FictionalThreeEightDieItem> FICTIONAL_THREE_EIGHT_DIE = REGISTRY.registerItem("fictional_three_eight_die", FictionalThreeEightDieItem::new);
    public static final DeferredItem<CasketOfInaccuracyItem> CASKET_OF_INACCURACY = REGISTRY.registerItem("casket_of_inaccuracy", properties -> new CasketOfInaccuracyItem(properties.fireResistant().rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<DescribedItem> DAMAGED_CASKET_OF_INACCURACY = damaged("damaged_casket_of_inaccuracy");

    public static final DeferredItem<SplittingCurioItem> GOLD_COIN_OF_DISCORD = splitting("gold_coin_of_discord", CurioForm.NORMAL, 12, 1200, context -> context.give(new ItemStack(COSMIC_FRAGMENT.get())));
    public static final DeferredItem<SplittingCurioItem> SPLIT_SPLIT_CRACK_CRACK_COIN_COIN = splitting("split_split_crack_crack_coin_coin", CurioForm.FUSION, 24, 5280,
            context -> context.give(new ItemStack(COSMIC_FRAGMENT.get(), context.chance(0.5D) ? Mth.nextInt(context.random(), 1, 10) : 1)));
    public static final DeferredItem<SplittingCurioItem> SPLINTERED_GALACTIC_MEGA_COIN = splitting("splintered_galactic_mega_coin", CurioForm.FUSION, 12, 600,
            context -> CurioReward.open(context.player(), ChoiceSupport.CURIO_CARDS, List.of(context.reward(context.chance(0.5D) ? Curios.randomCurio(context.random()) : Curios.randomCurio(context.random(), CurioForm.NEGATIVE.tag())))));
    public static final DeferredItem<SplittingCurioItem> FICTIONAL_GOLD_COIN = splitting("fictional_gold_coin", CurioForm.FUSION, 0, 1200, context -> context.heal(0.1F));
    public static final DeferredItem<EffectCurioItem> SILVER_COIN_OF_DISCORD = effect("silver_coin_of_discord", CurioForm.NORMAL, context -> {
        context.give(new ItemStack(COSMIC_FRAGMENT.get(), Mth.nextInt(context.random(), 4, 12)));
        context.give(new ItemStack(COSMIC_FRAGMENT.get(), Mth.nextInt(context.random(), 4, 12)));
        context.damage(1);
    });
    public static final DeferredItem<EnchantingCurioItem> CASKET_OF_MYRIAD_IMPERMANENCE = enchanting("casket_of_myriad_impermanence", CurioForm.NORMAL, 2,
            new EnchantingCurioItem.Spec(20, EnchantingCurioItem.Target.ENCHANTABLE, 2, 0.0F));
    public static final DeferredItem<EnchantingCurioItem> ENTROPIC_DIE = enchanting("entropic_die", CurioForm.NORMAL, 1,
            new EnchantingCurioItem.Spec(30, EnchantingCurioItem.Target.ENCHANTED, 1, 0.0F));
    public static final DeferredItem<VoidCandleDieItem> VOID_CANDLE_DIE = REGISTRY.registerItem("void_candle_die",
            properties -> new VoidCandleDieItem(CurioForm.FUSION, 1, new EnchantingCurioItem.Spec(30, EnchantingCurioItem.Target.ENCHANTED, 1, 0.6F), properties));

    public static final DeferredItem<ContainerCurioItem> OMNISCIENT_CAPSULE = container("omniscient_capsule", CurioForm.NORMAL, 32, 1, null);
    public static final DeferredItem<ContainerCurioItem> FLAME_OF_OMNISCIENCE = container("flame_of_omniscience", CurioForm.FUSION, 5, 10, null);
    public static final DeferredItem<ContainerCurioItem> OMNISCIENT_CHEESE = REGISTRY.registerItem("omniscient_cheese",
            properties -> new ContainerCurioItem(CurioForm.FUSION, 20, 1, PGCItems::omniscientCheeseBonus, cheese(properties)));
    public static final DeferredItem<ContainerCurioItem> ENIGMATA_MAGNET_BAG = container("enigmata_magnet_bag", CurioForm.FUSION, 20, 1,
            (context, impact) -> CurioLoot.spawn(impact.level(), impact.position(), new ItemStack(COSMIC_FRAGMENT.get(), Mth.nextInt(context.random(), 1, 5))));
    public static final DeferredItem<BugNetItem> BUG_NET = REGISTRY.registerItem("bug_net",
            properties -> new BugNetItem(properties.fireResistant()));

    public static final DeferredItem<PunklordeMentalityItem> PUNKLORDE_MENTALITY = REGISTRY.registerItem("punklorde_mentality", PunklordeMentalityItem::new);
    public static final DeferredItem<WhimsicalFanciesMachineryCrewItem> WHIMSICAL_FANCIES_MACHINERY_CREW = REGISTRY.registerItem("whimsical_fancies_machinery_crew", WhimsicalFanciesMachineryCrewItem::new);

    public static final DeferredItem<PrescriptionCurioItem> ABSOLUTE_FAILURE_PRESCRIPTION_ZERO = prescription(PGCEffects.ABSOLUTE_FAILURE_PRESCRIPTION_ZERO);
    public static final DeferredItem<PrescriptionCurioItem> ABSOLUTE_FAILURE_PRESCRIPTION_ONE = prescription(PGCEffects.ABSOLUTE_FAILURE_PRESCRIPTION_ONE);
    public static final DeferredItem<PrescriptionCurioItem> ABSOLUTE_FAILURE_PRESCRIPTION_TWO = prescription(PGCEffects.ABSOLUTE_FAILURE_PRESCRIPTION_TWO);

    public static final DeferredItem<DescribedItem> DAMAGED_COSMIC_BIG_LOTTO = damaged("damaged_cosmic_big_lotto");
    public static final DeferredItem<CosmicBigLottoItem> COSMIC_BIG_LOTTO = REGISTRY.registerItem("cosmic_big_lotto", CosmicBigLottoItem::new);
    public static final DeferredItem<InterastralBigLottoItem> INTERASTRAL_BIG_LOTTO = REGISTRY.registerItem("interastral_big_lotto", InterastralBigLottoItem::new);
    public static final DeferredItem<QuantumBigLottoItem> QUANTUM_BIG_LOTTO = REGISTRY.registerItem("quantum_big_lotto", QuantumBigLottoItem::new);
    public static final DeferredItem<FictionalMegaLottoItem> FICTIONAL_MEGA_LOTTO = REGISTRY.registerItem("fictional_mega_lotto", FictionalMegaLottoItem::new);
    public static final DeferredItem<DiamondLotteryItem> DIAMOND_LOTTERY = REGISTRY.registerItem("diamond_lottery", DiamondLotteryItem::new);
    public static final DeferredItem<MysteryLotteryItem> MYSTERY_LOTTERY = REGISTRY.registerItem("mystery_lottery", MysteryLotteryItem::new);
    public static final DeferredItem<DescribedItem> TRUE_GALAXY_LOTTO = REGISTRY.registerItem("true_galaxy_lotto", properties -> new DescribedItem(properties.durability(1).fireResistant().rarity(Rarity.EPIC)));
    public static final DeferredItem<DescribedItem> NETHERITE_INGOT = disc("netherite_ingot");

    public static final DeferredItem<DescribedItem> DAMAGED_FORTUNE_GLUE = damaged("damaged_fortune_glue");
    public static final DeferredItem<DescribedItem> DAMAGED_INTERASTRAL_BIG_LOTTO = damaged("damaged_interastral_big_lotto");
    public static final DeferredItem<FortuneGlueItem> FORTUNE_GLUE = REGISTRY.registerItem("fortune_glue", FortuneGlueItem::new);
    public static final DeferredItem<CelesticometAlloyItem> CELESTICOMET_ALLOY = REGISTRY.registerItem("celesticomet_alloy", CelesticometAlloyItem::new);
    public static final DeferredItem<CelesticometAlloyItem> CELESTICOMET_ALLOY_TYPE_III = REGISTRY.registerItem("celesticomet_alloy_type_iii", CelesticometAlloyItem::new);
    public static final DeferredItem<WrittenInWaterItem> WRITTEN_IN_WATER = REGISTRY.registerItem("written_in_water", WrittenInWaterItem::new);

    public static final DeferredItem<EnigmataMagnetismItem> ENIGMATA_MAGNETISM = REGISTRY.registerItem("enigmata_magnetism", EnigmataMagnetismItem::new);
    public static final DeferredItem<MagneticFlameItem> MAGNETIC_FLAME = REGISTRY.registerItem("magnetic_flame", MagneticFlameItem::new);
    public static final DeferredItem<EmptyCandleFlameItem> EMPTY_CANDLE_FLAME = REGISTRY.registerItem("empty_candle_flame", EmptyCandleFlameItem::new);
    public static final DeferredItem<SplittingCurioItem> RADIANT_GOLD_COIN_OF_DISCORD = splitting("radiant_gold_coin_of_discord", CurioForm.FUSION, 3, 360, context -> {
        var rewards = new ArrayList<ItemStack>();
        rewards.add(context.reward(Curios.randomCurio(context.random(), CurioForm.NEGATIVE.tag())));
        if (context.chance(0.35D)) rewards.addAll(Curios.convert(context, 0.0D));
        CurioReward.open(context.player(), ChoiceSupport.CURIO_CARDS, List.copyOf(rewards));
    });

    public static final DeferredItem<ThalanToxiFlameItem> THALAN_TOXI_FLAME = REGISTRY.registerItem("thalan_toxi_flame", ThalanToxiFlameItem::new);
    public static final DeferredItem<TypicalGeniusSocietyGossipItem> TYPICAL_GENIUS_SOCIETY_GOSSIP = REGISTRY.registerItem("typical_genius_society_gossip", TypicalGeniusSocietyGossipItem::new);
    public static final DeferredItem<RupertEmpireMechanicalGearItem> RUPERT_EMPIRE_MECHANICAL_GEAR = REGISTRY.registerItem("rupert_empire_mechanical_gear", RupertEmpireMechanicalGearItem::new);
    public static final DeferredItem<RobeOfTheBeautyItem> ROBE_OF_THE_BEAUTY = REGISTRY.registerItem("robe_of_the_beauty", RobeOfTheBeautyItem::new);
    public static final DeferredItem<CavitySystemModelItem> CAVITY_SYSTEM_MODEL = REGISTRY.registerItem("cavity_system_model", CavitySystemModelItem::new);
    public static final DeferredItem<InvalidThoughtCodeMachineItem> INVALID_THOUGHT_CODE_MACHINE = REGISTRY.registerItem("invalid_thought_code_machine", InvalidThoughtCodeMachineItem::new);
    public static final DeferredItem<ComicBookItem> COMIC_BOOK = REGISTRY.registerItem("comic_book", ComicBookItem::new);

    private static final List<Holder<Attribute>> CODE_ARMOR_TOUGHNESS = List.of(Attributes.ARMOR_TOUGHNESS);
    private static final List<Holder<Attribute>> CODE_MAX_HEALTH = List.of(Attributes.MAX_HEALTH);
    private static final List<Holder<Attribute>> CODE_ARMOR = List.of(Attributes.ARMOR);
    private static final List<Holder<Attribute>> CODE_MOVEMENT_SPEED = List.of(Attributes.MOVEMENT_SPEED);
    private static final List<Holder<Attribute>> CODE_ATTACK_SPEED = List.of(Attributes.ATTACK_SPEED);
    private static final List<Holder<Attribute>> CODE_ATTACK_DAMAGE = List.of(Attributes.ATTACK_DAMAGE);
    private static final List<Holder<Attribute>> CODE_ALL = List.of(Attributes.ARMOR_TOUGHNESS, Attributes.MAX_HEALTH, Attributes.ARMOR, Attributes.MOVEMENT_SPEED, Attributes.ATTACK_SPEED, Attributes.ATTACK_DAMAGE);

    private static final int DAMAGED_STACK_SIZE = 8;
    private static final float FROSTED_SLIME_HEAL = 4.0F;
    private static final int FROSTED_SLIME_FREEZE_TICKS = 100;

    private static final int INSECT_PARASITE_LEVEL = 5;
    private static final int INSECT_PARASITE_TICKS = 400;
    private static final double INSECT_PARASITE_RADIUS = 8.0D;
    private static final double INSECT_PARASITE_HEALTH_RATIO = 10.0D;

    private static final double COCOON_NET_CHANCE = 0.05D;
    private static final int COCOON_PARASITE_LEVEL = 2;
    private static final int COCOON_PARASITE_TICKS = 200;

    private static final double OMNISCIENT_BOX_EXTRA_CHANCE = 0.5D;
    private static final double OMNISCIENT_BOX_ENCHANT_CHANCE = 0.15D;
    private static final double OMNISCIENT_BOX_MEDIUM_CHANCE = 0.3D;
    private static final double FRUIT_OF_INACCURACY_MEDIUM_CHANCE = 0.5D;
    private static final int GALAXY_GEL_MIN_ITEMS = 1;
    private static final int GALAXY_GEL_MAX_ITEMS = 2;
    private static final double GALAXY_GEL_DAMAGE_CHANCE = 0.7D;
    private static final float GALAXY_GEL_DAMAGE_RATIO = 0.95F;

    private static final int SPACE_TIME_COIN_INTERVAL = 1200;
    private static final int SPACE_TIME_COIN_TRIGGERS = 3;
    private static final double SPACE_TIME_COIN_BOND_CHANCE = 0.01D;
    private static final int SPACE_TIME_COIN_MIN_FRAGMENTS = 1;
    private static final int SPACE_TIME_COIN_MAX_FRAGMENTS = 10;

    public static final DeferredItem<CodeCurioItem> MESSY_CODE = code("messy_code", CODE_ARMOR_TOUGHNESS, false);
    public static final DeferredItem<CodeCurioItem> INFINITELY_RECURSIVE_CODE = code("infinitely_recursive_code", CODE_MAX_HEALTH, false);
    public static final DeferredItem<CodeCurioItem> CONVENTIONAL_CODE = code("conventional_code", CODE_ARMOR, false);
    public static final DeferredItem<CodeCurioItem> PRECISE_ELEGANT_CODE = code("precise_elegant_code", CODE_MOVEMENT_SPEED, false);
    public static final DeferredItem<CodeCurioItem> UNCOMMENTED_CODE = code("uncommented_code", CODE_ATTACK_SPEED, false);
    public static final DeferredItem<CodeCurioItem> SLIGHTLY_ODD_CODE = code("slightly_odd_code", CODE_ATTACK_DAMAGE, false);
    public static final DeferredItem<CodeCurioItem> INTEGRATED_CODE = code("integrated_code", CODE_ALL, true);

    public static final DeferredItem<DeathSaveCurioItem> FRUIT_OF_THE_ALIEN_TREE = fruit("fruit_of_the_alien_tree", CurioForm.NORMAL, 3, new DeathSaveCurioItem.Spec(true, 5, 1200, 2, false), null, null);
    public static final DeferredItem<DeathSaveCurioItem> VOID_CANDLE_FRUIT = fruit("void_candle_fruit", CurioForm.FUSION, 0, new DeathSaveCurioItem.Spec(false, 5, 1200, 2, false), null, null);
    public static final DeferredItem<DeathSaveCurioItem> OMNISCIENT_FRUIT = fruit("omniscient_fruit", CurioForm.NORMAL, 4, new DeathSaveCurioItem.Spec(true, 10, 2400, 2, false), null, null);
    public static final DeferredItem<DeathSaveCurioItem> EVIL_FRUIT_OF_THE_ALIEN_TREE = fruit("evil_fruit_of_the_alien_tree", CurioForm.FUSION, 1, new DeathSaveCurioItem.Spec(true, 5, 1200, 2, false),
            context -> DeathSaveCurioItem.armRevenge(context.player()), (context, impact) -> {
                if (!(impact.subject() instanceof LivingEntity attacker)) return;
                if (!DeathSaveCurioItem.consumeRevenge(context.player())) return;
                if (!context.chance(0.2D)) return;
                DeathSaveCurioItem.strike(attacker, 0.2F);
            });
    public static final DeferredItem<DeathSaveCurioItem> SCENTED_DRIED_FRUIT = fruit("scented_dried_fruit", CurioForm.FUSION, 2, new DeathSaveCurioItem.Spec(true, 5, 1200, 2, true), null, null);
    public static final DeferredItem<DeathSaveCurioItem> THALAN_TOXI_FRUIT = fruit("thalan_toxi_fruit", CurioForm.FUSION, 3, new DeathSaveCurioItem.Spec(true, 5, 1200, 2, false),
            context -> DeathSaveCurioItem.exemptNextPunishment(context.player()), null);
    public static final DeferredItem<DeathSaveCurioItem> MAGNETIC_FRUIT = fruit("magnetic_fruit", CurioForm.FUSION, 3, new DeathSaveCurioItem.Spec(true, 5, 600, 2, false), context -> {
        context.give(new ItemStack(COSMIC_FRAGMENT.get(), Mth.nextInt(context.random(), 2, 20)));
        context.player().removeEffect(MobEffects.WEAKNESS);
        context.player().addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 600, 3, false, false));
    }, null);
    public static final DeferredItem<DeathSaveCurioItem> INSECT_FRUIT = fruit("insect_fruit", CurioForm.FUSION, 4, new DeathSaveCurioItem.Spec(true, 5, 1200, 2, false),
            context -> Parasitism.infectNearby(context.player(), INSECT_PARASITE_LEVEL, INSECT_PARASITE_TICKS, INSECT_PARASITE_RADIUS, INSECT_PARASITE_HEALTH_RATIO), null);
    public static final DeferredItem<WormInfestedCheeseItem> WORM_INFESTED_CHEESE = REGISTRY.registerItem("worm_infested_cheese",
            properties -> new WormInfestedCheeseItem(cheese(properties)));
    public static final DeferredItem<TheParchmentThatAlwaysEatsItem> THE_PARCHMENT_THAT_ALWAYS_EATS = REGISTRY.registerItem("the_parchment_that_always_eats", TheParchmentThatAlwaysEatsItem::new);
    public static final DeferredItem<FictionalMechItem> FICTIONAL_MECH = REGISTRY.registerItem("fictional_mech", FictionalMechItem::new);
    public static final DeferredItem<PerpetualCuckooClockItem> PERPETUAL_CUCKOO_CLOCK = REGISTRY.registerItem("perpetual_cuckoo_clock", PerpetualCuckooClockItem::new);
    public static final DeferredItem<AmbergrisCheeseItem> AMBERGRIS_CHEESE = REGISTRY.registerItem("ambergris_cheese",
            properties -> new AmbergrisCheeseItem(cheese(properties.fireResistant())));
    public static final DeferredItem<CorporateCuckooClockItem> CORPORATE_CUCKOO_CLOCK = REGISTRY.registerItem("corporate_cuckoo_clock",
            properties -> new CorporateCuckooClockItem(properties.fireResistant()));
    public static final DeferredItem<BlackForestCuckooClockItem> BLACK_FOREST_CUCKOO_CLOCK = REGISTRY.registerItem("black_forest_cuckoo_clock",
            properties -> new BlackForestCuckooClockItem(properties.fireResistant()));
    public static final DeferredItem<SocietyTicketItem> SOCIETY_TICKET = REGISTRY.registerItem("society_ticket",
            properties -> new SocietyTicketItem(properties.fireResistant()));
    public static final DeferredItem<BeaconColoringPasteItem> BEACON_COLORING_PASTE = REGISTRY.registerItem("beacon_coloring_paste",
            properties -> new BeaconColoringPasteItem(properties.fireResistant()));

    public static final DeferredItem<SpaceTimePrismItem> SPACE_TIME_PRISM = REGISTRY.registerItem("space_time_prism", SpaceTimePrismItem::new);
    public static final DeferredItem<SpaceTimeTriangleItem> SPACE_TIME_TRIANGLE = REGISTRY.registerItem("space_time_triangle", SpaceTimeTriangleItem::new);
    public static final DeferredItem<SplittingCurioItem> SPACE_TIME_COIN = splitting("space_time_coin", CurioForm.FUSION, SPACE_TIME_COIN_TRIGGERS, SPACE_TIME_COIN_INTERVAL,
            PGCItems::spaceTimeCoinBurst);
    public static final DeferredItem<FaithBondItem> FAITH_BOND = REGISTRY.registerItem("faith_bond", FaithBondItem::new);
    public static final DeferredItem<ClubFaithVoucherItem> CLUB_FAITH_VOUCHER = REGISTRY.registerItem("club_faith_voucher", ClubFaithVoucherItem::new);
    public static final DeferredItem<RandomEventErrorCodeItem> RANDOM_EVENT_ERROR_CODE = REGISTRY.registerItem("random_event_error_code", properties -> new RandomEventErrorCodeItem(properties.rarity(Rarity.EPIC)));
    public static final DeferredItem<ClubFlameItem> CLUB_FLAME = REGISTRY.registerItem("club_flame", ClubFlameItem::new);
    public static final DeferredItem<FoolsMaskItem> FOOLS_MASK = REGISTRY.registerItem("fools_mask",
            properties -> new FoolsMaskItem(properties.fireResistant()));
    public static final DeferredItem<FoolsGelItem> FOOLS_GEL = REGISTRY.registerItem("fools_gel", FoolsGelItem::new);

    public static final DeferredItem<RepairCurioItem> VOID_WICK_TRIMMER = REGISTRY.registerItem("void_wick_trimmer",
            properties -> new RepairCurioItem(CurioTrigger.ACTIVE, CurioForm.NORMAL, 2, properties.fireResistant()));
    public static final DeferredItem<ObliterationWickTrimmerItem> OBLITERATION_WICK_TRIMMER = REGISTRY.registerItem("obliteration_wick_trimmer",
            properties -> new ObliterationWickTrimmerItem(CurioForm.NORMAL, properties.fireResistant()));
    public static final DeferredItem<RepairCurioItem> OMNISCIENT_WICK_TRIMMER = REGISTRY.registerItem("omniscient_wick_trimmer",
            properties -> new RepairCurioItem(CurioTrigger.ACTIVE, CurioForm.FUSION, 6, properties.fireResistant()));
    public static final DeferredItem<WickTrimmerSystemModelItem> WICK_TRIMMER_SYSTEM_MODEL = REGISTRY.registerItem("wick_trimmer_system_model",
            WickTrimmerSystemModelItem::new);
    public static final DeferredItem<ContainerCurioItem> OMNISCIENT_BOX = REGISTRY.registerItem("omniscient_box",
            properties -> new ContainerCurioItem(CurioForm.FUSION, 20, 0, PGCItems::omniscientBoxBonus, properties.fireResistant()));
    public static final DeferredItem<EnchantingCurioItem> OMNISCIENT_ENTROPIC_DIE = enchanting("omniscient_entropic_die", CurioForm.FUSION, 1,
            new EnchantingCurioItem.Spec(30, EnchantingCurioItem.Target.ENCHANTED, 1, 0.0F, new EnchantingCurioItem.Offhand(PGCEnchantments.CAN_OPENER, 1, 4)));
    public static final DeferredItem<EnchantingCurioItem> ABUNDANCE_FRUIT_DIE = enchanting("abundance_fruit_die", CurioForm.FUSION, 1,
            new EnchantingCurioItem.Spec(30, EnchantingCurioItem.Target.ENCHANTED, 1, 0.0F, new EnchantingCurioItem.Offhand(PGCEnchantments.AMBROSIAL_ARBOR_ATTACHMENT, 5, 5)));
    public static final DeferredItem<ContainerCurioItem> COCOON_OF_TEN_THOUSAND_WORMS = container("cocoon_of_ten_thousand_worms", CurioForm.FUSION, 20, 1, PGCItems::cocoonBonus);

    public static final DeferredItem<DeathSaveCurioItem> FRUIT_OF_INACCURACY = fruit("fruit_of_inaccuracy", CurioForm.FUSION, 3, new DeathSaveCurioItem.Spec(true, 5, 1200, 2, false),
            PGCItems::fruitOfInaccuracySave, null);
    public static final DeferredItem<JoyBigLottoItem> JOY_BIG_LOTTO = REGISTRY.registerItem("joy_big_lotto", JoyBigLottoItem::new);
    public static final DeferredItem<CharmonyBigLottoItem> CHARMONY_BIG_LOTTO = REGISTRY.registerItem("charmony_big_lotto", CharmonyBigLottoItem::new);
    public static final DeferredItem<EffectCurioItem> GALAXY_GEL = effect("galaxy_gel", CurioForm.FUSION, PGCItems::galaxyGelBurst);
    public static final DeferredItem<VoidCheeseItem> VOID_CHEESE = REGISTRY.registerItem("void_cheese",
            properties -> new VoidCheeseItem(cheese(properties)));
    public static final DeferredItem<UnknownLostPropertyItem> UNKNOWN_LOST_PROPERTY = REGISTRY.registerItem("unknown_lost_property",
            properties -> new UnknownLostPropertyItem(properties.rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<CharmonyLostPropertyItem> CHARMONY_LOST_PROPERTY = REGISTRY.registerItem("charmony_lost_property",
            properties -> new CharmonyLostPropertyItem(properties.rarity(Rarity.RARE)));
    public static final DeferredItem<FamilyTiesItem> FAMILY_TIES = REGISTRY.registerItem("family_ties",
            properties -> new FamilyTiesItem(properties.fireResistant()));

    public static final DeferredItem<FissionCuckooClockItem> FISSION_CUCKOO_CLOCK = REGISTRY.registerItem("fission_cuckoo_clock", FissionCuckooClockItem::new);
    public static final DeferredItem<FissionCuckooClockReplicaItem> FISSION_CUCKOO_CLOCK_I = REGISTRY.registerItem("fission_cuckoo_clock_i",
            properties -> new FissionCuckooClockReplicaItem(properties.fireResistant()));
    public static final DeferredItem<JubilantDrumRollDeviceItem> JUBILANT_DRUM_ROLL_DEVICE = REGISTRY.registerItem("jubilant_drum_roll_device", JubilantDrumRollDeviceItem::new);
    public static final DeferredItem<TriangularDrumRollDeviceItem> TRIANGULAR_DRUM_ROLL_DEVICE = REGISTRY.registerItem("triangular_drum_roll_device", TriangularDrumRollDeviceItem::new);
    public static final DeferredItem<ChaosReishiItem> CHAOS_REISHI = REGISTRY.registerItem("chaos_reishi",
            properties -> new ChaosReishiItem(properties.fireResistant()));
    public static final DeferredItem<VoidWickNetItem> VOID_WICK_NET = REGISTRY.registerItem("void_wick_net", VoidWickNetItem::new);
    public static final DeferredItem<CharmonyFestivalItem> CHARMONY_FESTIVAL = REGISTRY.registerItem("charmony_festival", CharmonyFestivalItem::new);
    public static final DeferredItem<RobeOfHarmonyItem> ROBE_OF_HARMONY = REGISTRY.registerItem("robe_of_harmony", RobeOfHarmonyItem::new);
    public static final DeferredItem<WaxSealItem> WAR_WAX_SEAL = waxSeal("war_wax_seal", Element.PYRO);
    public static final DeferredItem<WaxSealItem> FREEDOM_WAX_SEAL = waxSeal("freedom_wax_seal", Element.ANEMO);
    public static final DeferredItem<WaxSealItem> CONTRACT_WAX_SEAL = waxSeal("contract_wax_seal", Element.GEO);
    public static final DeferredItem<WaxSealItem> JUSTICE_WAX_SEAL = waxSeal("justice_wax_seal", Element.HYDRO);
    public static final DeferredItem<WaxSealItem> COMPASSION_WAX_SEAL = waxSeal("compassion_wax_seal", Element.CRYO);
    public static final DeferredItem<WaxSealItem> ETERNAL_WAX_SEAL = waxSeal("eternal_wax_seal", Element.ELECTRO);
    public static final DeferredItem<WaxSealItem> WISDOM_WAX_SEAL = waxSeal("wisdom_wax_seal", Element.DENDRO);

    public static final DeferredItem<WishFateItem> ACQUAINT_FATE = REGISTRY.registerItem("acquaint_fate", properties -> new WishFateItem(properties.rarity(Rarity.UNCOMMON), WishBanner.ACQUAINT));
    public static final DeferredItem<WishFateItem> INTERTWINED_FATE = REGISTRY.registerItem("intertwined_fate", properties -> new WishFateItem(properties.rarity(Rarity.UNCOMMON), WishBanner.INTERTWINED));
    public static final DeferredItem<WishCoreItem> WISH_CORE = REGISTRY.registerItem("wish_core", properties -> new WishCoreItem(properties.stacksTo(1).rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<DescribedItem> UNBUFFED_WISH_CORE = described("unbuffed_wish_core");
    public static final DeferredItem<WishingStaffItem> WISHING_STAFF = REGISTRY.registerItem("wishing_staff", WishingStaffItem::new);
    public static final DeferredItem<WishDataExporterItem> WISH_DATA_EXPORTER = REGISTRY.registerItem("wish_data_exporter", WishDataExporterItem::new);
    public static final DeferredItem<StarRailSpecialPassItem> STAR_RAIL_SPECIAL_PASS = REGISTRY.registerItem("star_rail_special_pass", StarRailSpecialPassItem::new);
    public static final DeferredItem<ColorfulSunglassesItem> COLORFUL_SUNGLASSES = REGISTRY.registerItem("colorful_sunglasses", properties -> new ColorfulSunglassesItem(PGCArmorMaterials.COLORFUL_SUNGLASSES, properties.durability(ArmorItem.Type.HELMET.getDurability(15))));
    public static final DeferredItem<ArmorItem> SPECIAL_01_HELMET = REGISTRY.registerItem("special_01_helmet",
            properties -> new ArmorItem(PGCArmorMaterials.SPECIAL_01, ArmorItem.Type.HELMET, properties.durability(ArmorItem.Type.HELMET.getDurability(225))));

    public static final DeferredItem<DendroCoreSpawnEggItem> DENDRO_CORE_SPAWN_EGG = REGISTRY.registerItem("dendro_core_spawn_egg", DendroCoreSpawnEggItem::new);
    public static final DeferredItem<DeferredSpawnEggItem> ABUNDANCE_BLIGHT_ZOMBIE_SPAWN_EGG = REGISTRY.registerItem("abundance_blight_zombie_spawn_egg", properties -> new DeferredSpawnEggItem(PGCEntities.ABUNDANCE_BLIGHT_ZOMBIE, -1, -1, properties));
    public static final DeferredItem<XiaoLanternSpawnEggItem> XIAO_LANTERN_SPAWN_EGG = REGISTRY.registerItem("xiao_lantern_spawn_egg", XiaoLanternSpawnEggItem::new);
    public static final DeferredItem<HertaOtherworldBranchTowerSpawnEggItem> HERTA_OTHERWORLD_BRANCH_TOWER_SPAWN_EGG = REGISTRY.registerItem("herta_otherworld_branch_tower_spawn_egg", HertaOtherworldBranchTowerSpawnEggItem::new);

    public static final DeferredItem<BlockItem> PRIMOGEM_ORE = REGISTRY.registerSimpleBlockItem("primogem_ore", PGCBlocks.PRIMOGEM_ORE);
    public static final DeferredItem<BlockItem> OTHERWORLD_LOG_PLANKS = REGISTRY.registerSimpleBlockItem("otherworld_log_planks", PGCBlocks.OTHERWORLD_LOG_PLANKS);
    public static final DeferredItem<BlockItem> INTERTWINED_FATE_BLOCK = REGISTRY.registerSimpleBlockItem("intertwined_fate_block", PGCBlocks.INTERTWINED_FATE_BLOCK);
    public static final DeferredItem<BlockItem> MORA_PILE = REGISTRY.registerSimpleBlockItem("mora_pile", PGCBlocks.MORA_PILE);
    public static final DeferredItem<BlockItem> MORA_BLOCK = REGISTRY.registerSimpleBlockItem("mora_block", PGCBlocks.MORA_BLOCK);
    public static final DeferredItem<BlockItem> OTHERWORLD_WOOD_STAIRS = REGISTRY.registerSimpleBlockItem("otherworld_wood_stairs", PGCBlocks.OTHERWORLD_WOOD_STAIRS);
    public static final DeferredItem<BlockItem> OTHERWORLD_LOG_SLAB = REGISTRY.registerSimpleBlockItem("otherworld_log_slab", PGCBlocks.OTHERWORLD_LOG_SLAB);
    public static final DeferredItem<BlockItem> OTHERWORLD_WOOD_FENCE_GATE = REGISTRY.registerSimpleBlockItem("otherworld_wood_fence_gate", PGCBlocks.OTHERWORLD_WOOD_FENCE_GATE);
    public static final DeferredItem<BlockItem> OTHERWORLD_WOOD_FENCE = REGISTRY.registerSimpleBlockItem("otherworld_wood_fence", PGCBlocks.OTHERWORLD_WOOD_FENCE);
    public static final DeferredItem<DescribedBlockItem> MARCH_7TH_STATUE = REGISTRY.registerItem("march_7th_statue", properties -> new DescribedBlockItem(PGCBlocks.MARCH_7TH_STATUE.get(), properties));
    public static final DeferredItem<BlockItem> REFINED_MORA_BLOCK = REGISTRY.registerSimpleBlockItem("refined_mora_block", PGCBlocks.REFINED_MORA_BLOCK);
    public static final DeferredItem<BlockItem> CHEAP_MORA_BLOCK = REGISTRY.registerSimpleBlockItem("cheap_mora_block", PGCBlocks.CHEAP_MORA_BLOCK);
    public static final DeferredItem<BlockItem> CHEAP_MORA_BLOCK_SLAB = REGISTRY.registerSimpleBlockItem("cheap_mora_block_slab", PGCBlocks.CHEAP_MORA_BLOCK_SLAB);
    public static final DeferredItem<BlockItem> CHEAP_MORA_BLOCK_STAIRS = REGISTRY.registerSimpleBlockItem("cheap_mora_block_stairs", PGCBlocks.CHEAP_MORA_BLOCK_STAIRS);
    public static final DeferredItem<BlockItem> CHEAP_MORA_BLOCK_WALL = REGISTRY.registerSimpleBlockItem("cheap_mora_block_wall", PGCBlocks.CHEAP_MORA_BLOCK_WALL);
    public static final DeferredItem<BlockItem> SMALL_JAR = REGISTRY.registerSimpleBlockItem("small_jar", PGCBlocks.SMALL_JAR);
    public static final DeferredItem<BlockItem> BIG_JAR = REGISTRY.registerSimpleBlockItem("big_jar", PGCBlocks.BIG_JAR);
    public static final DeferredItem<BlockItem> TRASH_CAN = REGISTRY.registerSimpleBlockItem("trash_can", PGCBlocks.TRASH_CAN);
    public static final DeferredItem<DescribedBlockItem> CLEAN_TRASH_CAN = REGISTRY.registerItem("clean_trash_can", properties -> new DescribedBlockItem(PGCBlocks.CLEAN_TRASH_CAN.get(), properties));
    public static final DeferredItem<DescribedBlockItem> MORA_TRASH_CAN = REGISTRY.registerItem("mora_trash_can", properties -> new DescribedBlockItem(PGCBlocks.MORA_TRASH_CAN.get(), properties));
    public static final DeferredItem<BlockItem> VAYUDA_TURQUOISE_ORE = REGISTRY.registerSimpleBlockItem("vayuda_turquoise_ore", PGCBlocks.VAYUDA_TURQUOISE_ORE);
    public static final DeferredItem<BlockItem> VAYUDA_TURQUOISE_BLOCK = REGISTRY.registerSimpleBlockItem("vayuda_turquoise_block", PGCBlocks.VAYUDA_TURQUOISE_BLOCK);
    public static final DeferredItem<BlockItem> PRITHIVA_TOPAZ_ORE = REGISTRY.registerSimpleBlockItem("prithiva_topaz_ore", PGCBlocks.PRITHIVA_TOPAZ_ORE);
    public static final DeferredItem<BlockItem> PRITHIVA_TOPAZ_BLOCK = REGISTRY.registerSimpleBlockItem("prithiva_topaz_block", PGCBlocks.PRITHIVA_TOPAZ_BLOCK);
    public static final DeferredItem<BlockItem> VAJRADA_AMETHYST_ORE = REGISTRY.registerSimpleBlockItem("vajrada_amethyst_ore", PGCBlocks.VAJRADA_AMETHYST_ORE);
    public static final DeferredItem<BlockItem> VAJRADA_AMETHYST_BLOCK = REGISTRY.registerSimpleBlockItem("vajrada_amethyst_block", PGCBlocks.VAJRADA_AMETHYST_BLOCK);
    public static final DeferredItem<DescribedBlockItem> NAGADUS_EMERALD_ORE = REGISTRY.registerItem("nagadus_emerald_ore", properties -> new DescribedBlockItem(PGCBlocks.NAGADUS_EMERALD_ORE.get(), properties));
    public static final DeferredItem<BlockItem> NAGADUS_EMERALD_BLOCK = REGISTRY.registerSimpleBlockItem("nagadus_emerald_block", PGCBlocks.NAGADUS_EMERALD_BLOCK);
    public static final DeferredItem<BlockItem> DENDRO_CORE_BLOCK = REGISTRY.registerSimpleBlockItem("dendro_core_block", PGCBlocks.DENDRO_CORE_BLOCK);
    public static final DeferredItem<BlockItem> DENDRO_CORE_PLANKS = REGISTRY.registerSimpleBlockItem("dendro_core_planks", PGCBlocks.DENDRO_CORE_PLANKS);
    public static final DeferredItem<BlockItem> DENDRO_CORE_PLANKS_STAIRS = REGISTRY.registerSimpleBlockItem("dendro_core_planks_stairs", PGCBlocks.DENDRO_CORE_PLANKS_STAIRS);
    public static final DeferredItem<BlockItem> DENDRO_CORE_PLANKS_SLAB = REGISTRY.registerSimpleBlockItem("dendro_core_planks_slab", PGCBlocks.DENDRO_CORE_PLANKS_SLAB);
    public static final DeferredItem<BlockItem> DENDRO_CORE_PLANKS_FENCE = REGISTRY.registerSimpleBlockItem("dendro_core_planks_fence", PGCBlocks.DENDRO_CORE_PLANKS_FENCE);
    public static final DeferredItem<BlockItem> DENDRO_CORE_PLANKS_FENCE_GATE = REGISTRY.registerSimpleBlockItem("dendro_core_planks_fence_gate", PGCBlocks.DENDRO_CORE_PLANKS_FENCE_GATE);
    public static final DeferredItem<BlockItem> DENDRO_CORE_PLANKS_PRESSURE_PLATE = REGISTRY.registerSimpleBlockItem("dendro_core_planks_pressure_plate", PGCBlocks.DENDRO_CORE_PLANKS_PRESSURE_PLATE);
    public static final DeferredItem<BlockItem> DENDRO_CORE_PLANKS_BUTTON = REGISTRY.registerSimpleBlockItem("dendro_core_planks_button", PGCBlocks.DENDRO_CORE_PLANKS_BUTTON);
    public static final DeferredItem<DescribedBlockItem> VARUNADA_LAZURITE_ORE = REGISTRY.registerItem("varunada_lazurite_ore", properties -> new DescribedBlockItem(PGCBlocks.VARUNADA_LAZURITE_ORE.get(), properties));
    public static final DeferredItem<BlockItem> VARUNADA_LAZURITE_BLOCK = REGISTRY.registerSimpleBlockItem("varunada_lazurite_block", PGCBlocks.VARUNADA_LAZURITE_BLOCK);
    public static final DeferredItem<BlockItem> CHEAP_NETHERITE_BLOCK = REGISTRY.registerSimpleBlockItem("cheap_netherite_block", PGCBlocks.CHEAP_NETHERITE_BLOCK);
    public static final DeferredItem<BlockItem> AGNIDUS_AGATE_ORE = REGISTRY.registerSimpleBlockItem("agnidus_agate_ore", PGCBlocks.AGNIDUS_AGATE_ORE);
    public static final DeferredItem<BlockItem> AGNIDUS_AGATE_BLOCK = REGISTRY.registerSimpleBlockItem("agnidus_agate_block", PGCBlocks.AGNIDUS_AGATE_BLOCK);
    public static final DeferredItem<BlockItem> CHARCOAL_BLOCK = REGISTRY.registerSimpleBlockItem("charcoal_block", PGCBlocks.CHARCOAL_BLOCK);
    public static final DeferredItem<BlockItem> NETHERRACK_FARMLAND = REGISTRY.registerSimpleBlockItem("netherrack_farmland", PGCBlocks.NETHERRACK_FARMLAND);
    public static final DeferredItem<BlockItem> SHIVADA_JADE_ORE = REGISTRY.registerSimpleBlockItem("shivada_jade_ore", PGCBlocks.SHIVADA_JADE_ORE);
    public static final DeferredItem<BlockItem> SHIVADA_JADE_BLOCK = REGISTRY.registerSimpleBlockItem("shivada_jade_block", PGCBlocks.SHIVADA_JADE_BLOCK);
    public static final DeferredItem<BlockItem> ELEMENTAL_CRYSTAL_BLOCK = REGISTRY.registerSimpleBlockItem("elemental_crystal_block", PGCBlocks.ELEMENTAL_CRYSTAL_BLOCK);
    public static final DeferredItem<DescribedBlockItem> XIAO_LANTERN_LAUNCHER = REGISTRY.registerItem("xiao_lantern_launcher",
            properties -> new DescribedBlockItem(PGCBlocks.XIAO_LANTERN_LAUNCHER.get(), properties));
    public static final DeferredItem<DescribedBlockItem> CREATIVE_XIAO_LANTERN_LAUNCHER = REGISTRY.registerItem("creative_xiao_lantern_launcher",
            properties -> new DescribedBlockItem(PGCBlocks.CREATIVE_XIAO_LANTERN_LAUNCHER.get(), properties));
    public static final DeferredItem<BlockItem> MONOCHROME_OTHERWORLD_PLANKS = REGISTRY.registerSimpleBlockItem("monochrome_otherworld_planks", PGCBlocks.MONOCHROME_OTHERWORLD_PLANKS);
    public static final DeferredItem<BlockItem> MONOCHROME_OTHERWORLD_PLANKS_STAIRS = REGISTRY.registerSimpleBlockItem("monochrome_otherworld_planks_stairs", PGCBlocks.MONOCHROME_OTHERWORLD_PLANKS_STAIRS);
    public static final DeferredItem<BlockItem> MONOCHROME_OTHERWORLD_PLANKS_SLAB = REGISTRY.registerSimpleBlockItem("monochrome_otherworld_planks_slab", PGCBlocks.MONOCHROME_OTHERWORLD_PLANKS_SLAB);
    public static final DeferredItem<BlockItem> MONOCHROME_OTHERWORLD_PLANKS_FENCE = REGISTRY.registerSimpleBlockItem("monochrome_otherworld_planks_fence", PGCBlocks.MONOCHROME_OTHERWORLD_PLANKS_FENCE);
    public static final DeferredItem<BlockItem> MONOCHROME_OTHERWORLD_PLANKS_FENCE_GATE = REGISTRY.registerSimpleBlockItem("monochrome_otherworld_planks_fence_gate", PGCBlocks.MONOCHROME_OTHERWORLD_PLANKS_FENCE_GATE);
    public static final DeferredItem<BlockItem> MONOCHROME_OTHERWORLD_PLANKS_PRESSURE_PLATE = REGISTRY.registerSimpleBlockItem("monochrome_otherworld_planks_pressure_plate", PGCBlocks.MONOCHROME_OTHERWORLD_PLANKS_PRESSURE_PLATE);
    public static final DeferredItem<BlockItem> MONOCHROME_OTHERWORLD_PLANKS_BUTTON = REGISTRY.registerSimpleBlockItem("monochrome_otherworld_planks_button", PGCBlocks.MONOCHROME_OTHERWORLD_PLANKS_BUTTON);
    public static final DeferredItem<BlockItem> GEOMARROW_CRYSTAL_BLOCK = REGISTRY.registerSimpleBlockItem("geomarrow_crystal_block", PGCBlocks.GEOMARROW_CRYSTAL_BLOCK);
    public static final DeferredItem<BlockItem> GEOMARROW_CRYSTAL_CLUSTER = REGISTRY.registerSimpleBlockItem("geomarrow_crystal_cluster", PGCBlocks.GEOMARROW_CRYSTAL_CLUSTER);
    public static final DeferredItem<BlockItem> WEATHERED_STONE_BRICKS = REGISTRY.registerSimpleBlockItem("weathered_stone_bricks", PGCBlocks.WEATHERED_STONE_BRICKS);
    public static final DeferredItem<BlockItem> RUSTY_IRON_BLOCK = REGISTRY.registerSimpleBlockItem("rusty_iron_block", PGCBlocks.RUSTY_IRON_BLOCK);
    public static final DeferredItem<BlockItem> RUSTY_IRON_FENCE = REGISTRY.registerSimpleBlockItem("rusty_iron_fence", PGCBlocks.RUSTY_IRON_FENCE);
    public static final DeferredItem<BlockItem> ROTTEN_WOOD = REGISTRY.registerSimpleBlockItem("rotten_wood", PGCBlocks.ROTTEN_WOOD);
    public static final DeferredItem<BlockItem> ROTTEN_WOOD_STAIRS = REGISTRY.registerSimpleBlockItem("rotten_wood_stairs", PGCBlocks.ROTTEN_WOOD_STAIRS);
    public static final DeferredItem<BlockItem> ROTTEN_WOOD_FENCE = REGISTRY.registerSimpleBlockItem("rotten_wood_fence", PGCBlocks.ROTTEN_WOOD_FENCE);
    public static final DeferredItem<BlockItem> WEATHERED_STONE_BRICKS_STAIRS = REGISTRY.registerSimpleBlockItem("weathered_stone_bricks_stairs", PGCBlocks.WEATHERED_STONE_BRICKS_STAIRS);
    public static final DeferredItem<BlockItem> WEATHERED_STONE_BRICKS_SLAB = REGISTRY.registerSimpleBlockItem("weathered_stone_bricks_slab", PGCBlocks.WEATHERED_STONE_BRICKS_SLAB);
    public static final DeferredItem<BlockItem> WEATHERED_STONE_BRICKS_WALL = REGISTRY.registerSimpleBlockItem("weathered_stone_bricks_wall", PGCBlocks.WEATHERED_STONE_BRICKS_WALL);
    public static final DeferredItem<BlockItem> PRIMOGEM_BLOCK = REGISTRY.registerSimpleBlockItem("primogem_block", PGCBlocks.PRIMOGEM_BLOCK);
    public static final DeferredItem<BlockItem> DEEPSLATE_PRIMOGEM_ORE = REGISTRY.registerSimpleBlockItem("deepslate_primogem_ore", PGCBlocks.DEEPSLATE_PRIMOGEM_ORE);
    public static final DeferredItem<BlockItem> DEEPSLATE_PRITHIVA_TOPAZ_ORE = REGISTRY.registerSimpleBlockItem("deepslate_prithiva_topaz_ore", PGCBlocks.DEEPSLATE_PRITHIVA_TOPAZ_ORE);
    public static final DeferredItem<BlockItem> DENDRO_BLESSING = REGISTRY.registerSimpleBlockItem("dendro_blessing", PGCBlocks.DENDRO_BLESSING);
    public static final DeferredItem<BlockItem> PACKED_MORA_PILE = REGISTRY.registerSimpleBlockItem("packed_mora_pile", PGCBlocks.PACKED_MORA_PILE);
    public static final DeferredItem<BlockItem> UNFETTERED_METAL_BLOCK = REGISTRY.registerSimpleBlockItem("unfettered_metal_block", PGCBlocks.UNFETTERED_METAL_BLOCK);
    public static final DeferredItem<BlockItem> STURDY_METAL_BLOCK = REGISTRY.registerSimpleBlockItem("sturdy_metal_block", PGCBlocks.STURDY_METAL_BLOCK);
    public static final DeferredItem<BlockItem> ETERNAL_METAL_BLOCK = REGISTRY.registerSimpleBlockItem("eternal_metal_block", PGCBlocks.ETERNAL_METAL_BLOCK);
    public static final DeferredItem<BlockItem> WISDOM_METAL_BLOCK = REGISTRY.registerSimpleBlockItem("wisdom_metal_block", PGCBlocks.WISDOM_METAL_BLOCK);
    public static final DeferredItem<BlockItem> JUSTICE_METAL_BLOCK = REGISTRY.registerSimpleBlockItem("justice_metal_block", PGCBlocks.JUSTICE_METAL_BLOCK);
    public static final DeferredItem<BlockItem> BURNING_WISH_METAL_BLOCK = REGISTRY.registerSimpleBlockItem("burning_wish_metal_block", PGCBlocks.BURNING_WISH_METAL_BLOCK);
    public static final DeferredItem<BlockItem> COMPASSION_METAL_BLOCK = REGISTRY.registerSimpleBlockItem("compassion_metal_block", PGCBlocks.COMPASSION_METAL_BLOCK);
    public static final DeferredItem<DescribedBlockItem> A_CAKE_FOR_YOU = REGISTRY.registerItem("a_cake_for_you", properties -> new DescribedBlockItem(PGCBlocks.A_CAKE_FOR_YOU.get(), properties));
    public static final DeferredItem<BlockItem> FINE_FORGED_ORE_FUSION_BLOCK = REGISTRY.registerSimpleBlockItem("fine_forged_ore_fusion_block", PGCBlocks.FINE_FORGED_ORE_FUSION_BLOCK);
    public static final DeferredItem<BlockItem> SOLID_CRYSTAL_PLATE = REGISTRY.registerSimpleBlockItem("solid_crystal_plate", PGCBlocks.SOLID_CRYSTAL_PLATE);
    public static final DeferredItem<BlockItem> GORGEOUS_SMITHING_TABLE = REGISTRY.registerSimpleBlockItem("gorgeous_smithing_table", PGCBlocks.GORGEOUS_SMITHING_TABLE);
    public static final DeferredItem<BlockItem> UNIDENTIFIED_DOLL = REGISTRY.registerSimpleBlockItem("unidentified_doll", PGCBlocks.UNIDENTIFIED_DOLL);
    public static final DeferredItem<BlockItem> DETONATOR = REGISTRY.registerSimpleBlockItem("detonator", PGCBlocks.DETONATOR);
    public static final DeferredItem<BlockItem> ELEMENTAL_CRYSTAL_ORE = REGISTRY.registerSimpleBlockItem("elemental_crystal_ore", PGCBlocks.ELEMENTAL_CRYSTAL_ORE);
    public static final DeferredItem<BlockItem> DEEPSLATE_ELEMENTAL_CRYSTAL_ORE = REGISTRY.registerSimpleBlockItem("deepslate_elemental_crystal_ore", PGCBlocks.DEEPSLATE_ELEMENTAL_CRYSTAL_ORE);
    public static final DeferredItem<BlockItem> WHITE_IRON_ORE_BLOCK = REGISTRY.registerSimpleBlockItem("white_iron_ore_block", PGCBlocks.WHITE_IRON_ORE_BLOCK);
    public static final DeferredItem<BlockItem> DEEPSLATE_WHITE_IRON_ORE_BLOCK = REGISTRY.registerSimpleBlockItem("deepslate_white_iron_ore_block", PGCBlocks.DEEPSLATE_WHITE_IRON_ORE_BLOCK);
    public static final DeferredItem<BlockItem> OTHERWORLD_CRYSTAL_ORE = REGISTRY.registerSimpleBlockItem("otherworld_crystal_ore", PGCBlocks.OTHERWORLD_CRYSTAL_ORE);
    public static final DeferredItem<BlockItem> DEEPSLATE_OTHERWORLD_CRYSTAL_ORE = REGISTRY.registerSimpleBlockItem("deepslate_otherworld_crystal_ore", PGCBlocks.DEEPSLATE_OTHERWORLD_CRYSTAL_ORE);
    public static final DeferredItem<BlockItem> OTHERWORLD_CRYSTAL_CLUSTER = REGISTRY.registerSimpleBlockItem("otherworld_crystal_cluster", PGCBlocks.OTHERWORLD_CRYSTAL_CLUSTER);
    public static final DeferredItem<BlockItem> CRUDE_WHITE_IRON_BLOCK = REGISTRY.registerSimpleBlockItem("crude_white_iron_block", PGCBlocks.CRUDE_WHITE_IRON_BLOCK);
    public static final DeferredItem<BlockItem> WONDROUS_ENCOUNTER_BLOCK = REGISTRY.registerSimpleBlockItem("wondrous_encounter_block", PGCBlocks.WONDROUS_ENCOUNTER_BLOCK);
    public static final DeferredItem<DescribedBlockItem> LUCKY_STATUE = REGISTRY.registerItem("lucky_statue", properties -> new DescribedBlockItem(PGCBlocks.LUCKY_STATUE.get(), properties));
    public static final DeferredItem<BlockItem> MORA_CASKET = REGISTRY.registerSimpleBlockItem("mora_casket", PGCBlocks.MORA_CASKET);
    public static final DeferredItem<BlockItem> PRIMOGEM_CASKET = REGISTRY.registerSimpleBlockItem("primogem_casket", PGCBlocks.PRIMOGEM_CASKET);
    public static final DeferredItem<BlockItem> COSMIC_CASKET = REGISTRY.registerSimpleBlockItem("cosmic_casket", PGCBlocks.COSMIC_CASKET);
    public static final DeferredItem<DescribedBlockItem> STELLAR_CONVERTER =
            REGISTRY.registerItem("stellar_converter", properties -> new DescribedBlockItem(PGCBlocks.STELLAR_CONVERTER.get(), properties));

    private static DeferredItem<DescribedItem> described(String name) {
        return REGISTRY.registerItem(name, DescribedItem::new);
    }

    private static DeferredItem<DescribedItem> described(String name, Rarity rarity) {
        return REGISTRY.registerItem(name, properties -> new DescribedItem(properties.rarity(rarity)));
    }

    private static <T extends Item> DeferredItem<T> fiveStarWeapon(String name, Function<Item.Properties, ? extends T> factory) {
        return REGISTRY.registerItem(name, properties -> factory.apply(properties.rarity(Rarity.EPIC)));
    }

    private static DeferredItem<PouchItem> pouch(String name, int capacity) {
        return REGISTRY.registerItem(name, properties -> new PouchItem(properties.stacksTo(1).fireResistant(), capacity));
    }

    private static DeferredItem<ExperienceBookItem> experienceBook(String name, int capacity, Rarity rarity) {
        return REGISTRY.registerItem(name, properties -> new ExperienceBookItem(properties.stacksTo(1).fireResistant().rarity(rarity), capacity));
    }

    private static DeferredItem<DescribedItem> disc(String name) {
        return REGISTRY.registerItem(name, properties -> new DescribedItem(properties.stacksTo(1).rarity(Rarity.RARE)
                .jukeboxPlayable(ResourceKey.create(Registries.JUKEBOX_SONG, ResourceLocation.fromNamespaceAndPath(MOD_ID, name)))));
    }

    private static TagKey<Block> blockTag(String name) {
        return TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(MOD_ID, name));
    }

    private static DeferredItem<MoraCompassItem> compass(String name, int radius, Rarity rarity, TagKey<Block> targets) {
        return REGISTRY.registerItem(name, properties -> new MoraCompassItem(
                properties.stacksTo(1).fireResistant().rarity(rarity), radius, targets));
    }

    private static DeferredItem<RewardExperienceBookItem> reward(String name, Rarity rarity, int highMin, int highMax, int lowMin, int lowMax) {
        return REGISTRY.registerItem(name, properties -> new RewardExperienceBookItem(
                properties.stacksTo(1).fireResistant().rarity(rarity), highMin, highMax, lowMin, lowMax));
    }

    private static FoodProperties food(int nutrition, float saturation) {
        return new FoodProperties.Builder().nutrition(nutrition).saturationModifier(saturation).alwaysEdible().build();
    }

    private static void cureLottoPunishment(ServerPlayer player) {
        if (player.hasEffect(PGCEffects.LOTTO_PUNISHMENT)) LottoPunishmentEffect.cure(player);
    }

    private static void frostedSlimeBonus(ServerPlayer player) {
        player.heal(FROSTED_SLIME_HEAL);
        player.setTicksFrozen(FROSTED_SLIME_FREEZE_TICKS);
    }

    private static DeferredItem<MoraArmorItem> moraArmor(String name, ArmorItem.Type type) {
        return REGISTRY.registerItem(name, properties -> new MoraArmorItem(type, PGCArmorMaterials.MORA, properties));
    }

    private static DeferredItem<DescribedItem> damaged(String name) {
        return REGISTRY.registerItem(name, properties -> new DescribedItem(properties.stacksTo(DAMAGED_STACK_SIZE)));
    }

    private static Item.Properties cheese(Item.Properties properties) {
        return properties.food(new FoodProperties.Builder().nutrition(0).saturationModifier(0.0F).alwaysEdible().build());
    }

    private static void omniscientCheeseBonus(CurioContext context, CurioImpact impact) {
        for (var index = 0; index < Mth.nextInt(context.random(), 1, 3); index++) {
            var amount = (int) Mth.nextDouble(context.random(), 2.0D, Math.max(2.0D, context.player().getXpNeededForNextLevel() * 0.05D));
            var position = impact.position();
            impact.level().addFreshEntity(new ExperienceOrb(impact.level(), position.x, position.y, position.z, amount));
        }
    }

    private static void cocoonBonus(CurioContext context, CurioImpact impact) {
        if (context.chance(COCOON_NET_CHANCE))
            CurioLoot.spawn(impact.level(), impact.position(), new ItemStack(BUG_NET.get()));
        if (Parasitism.level(context.player()) > 0) return;
        Parasitism.apply(context.player(), COCOON_PARASITE_LEVEL, COCOON_PARASITE_TICKS);
    }

    private static void omniscientBoxBonus(CurioContext context, CurioImpact impact) {
        if (!context.chance(OMNISCIENT_BOX_EXTRA_CHANCE)) return;
        CurioLoot.dropTable(CurioLoot.jarLoot(impact.state()), impact.level(), impact.position(), impact.state(), context.player());
        if (!context.chance(OMNISCIENT_BOX_ENCHANT_CHANCE)) return;
        EnchantChoice.open(context.player(), context.chance(OMNISCIENT_BOX_MEDIUM_CHANCE) ? EnchantGrade.MEDIUM : EnchantGrade.LOW);
    }

    private static void fruitOfInaccuracySave(CurioContext context) {
        EnchantChoice.open(context.player(), context.chance(FRUIT_OF_INACCURACY_MEDIUM_CHANCE) ? EnchantGrade.MEDIUM : EnchantGrade.LOW);
    }

    private static void galaxyGelBurst(CurioContext context) {
        var player = context.player();
        var items = Mth.nextInt(player.getRandom(), GALAXY_GEL_MIN_ITEMS, GALAXY_GEL_MAX_ITEMS);
        if (!EnchantChoice.open(player, EnchantGrade.HIGH, items)) return;
        context.damage(1);
        if (context.chance(GALAXY_GEL_DAMAGE_CHANCE))
            player.hurt(player.damageSources().magic(), player.getMaxHealth() * GALAXY_GEL_DAMAGE_RATIO);
    }

    private static void spaceTimeCoinBurst(CurioContext context) {
        var bonds = PlayerItems.count(context.player(), ENIGMATA_FACTION_BOND.get());
        if (context.chance(bonds * SPACE_TIME_COIN_BOND_CHANCE)) {
            context.give(new ItemStack(ENIGMATA_FACTION_BOND.get()));
            return;
        }
        context.give(new ItemStack(COSMIC_FRAGMENT.get(), Mth.nextInt(context.random(), SPACE_TIME_COIN_MIN_FRAGMENTS, SPACE_TIME_COIN_MAX_FRAGMENTS)));
    }

    private static DeferredItem<SplittingCurioItem> splitting(String name, CurioForm form, int integrity, int interval, Consumer<CurioContext> burst) {
        return REGISTRY.registerItem(name, properties -> new SplittingCurioItem(form, integrity, interval, burst, properties));
    }

    private static DeferredItem<EnchantingCurioItem> enchanting(String name, CurioForm form, int integrity, EnchantingCurioItem.Spec spec) {
        return REGISTRY.registerItem(name, properties -> new EnchantingCurioItem(form, integrity, spec, properties));
    }

    private static DeferredItem<EffectCurioItem> effect(String name, CurioForm form, Consumer<CurioContext> burst) {
        return REGISTRY.registerItem(name, properties -> new EffectCurioItem(form, 1, burst, properties));
    }

    private static DeferredItem<ContainerCurioItem> container(String name, CurioForm form, int integrity, int rolls, BiConsumer<CurioContext, CurioImpact> bonus) {
        return REGISTRY.registerItem(name, properties -> new ContainerCurioItem(form, integrity, rolls, bonus, properties));
    }

    private static DeferredItem<PrescriptionCurioItem> prescription(DeferredHolder<MobEffect, PrescriptionEffect> effect) {
        return REGISTRY.registerItem(effect.getId().getPath(), properties -> new PrescriptionCurioItem(effect, properties));
    }

    private static DeferredItem<CodeCurioItem> code(String name, List<Holder<Attribute>> attributes, boolean integrated) {
        return REGISTRY.registerItem(name, properties -> new CodeCurioItem(attributes, integrated, properties));
    }

    private static DeferredItem<WaxSealItem> waxSeal(String name, Element element) {
        return REGISTRY.registerItem(name, properties -> new WaxSealItem(element, properties));
    }

    private static DeferredItem<DeathSaveCurioItem> fruit(String name, CurioForm form, int integrity, DeathSaveCurioItem.Spec spec, Consumer<CurioContext> onSave, BiConsumer<CurioContext, CurioImpact> onAbsorb) {
        return REGISTRY.registerItem(name, properties -> new DeathSaveCurioItem(form, integrity, spec, onSave, onAbsorb, properties));
    }
}
