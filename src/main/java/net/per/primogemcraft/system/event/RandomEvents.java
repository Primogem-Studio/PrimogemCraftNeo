package net.per.primogemcraft.system.event;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.per.primogemcraft.enchantment.EnchantGrade;
import net.per.primogemcraft.registry.PGCEntities;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.system.curio.CurioForm;
import net.per.primogemcraft.system.curio.CurioGrade;
import net.per.primogemcraft.system.curio.LotteryCurioItem;
import net.per.primogemcraft.system.weapon.WishWeapon;
import net.per.primogemcraft.util.PGCTimer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class RandomEvents {
    private static final TagKey<Item> CLOCK = common("curio/clock");
    private static final TagKey<Item> NEGATIVE_CF = common("curio/negative/cf");
    private static final TagKey<Item> CODE = common("curio/code");

    private static final String LEAVE = "leave";

    private static final String ENCHANT_FRAGMENTS_LOW = "enchant/fragments_low";
    private static final String ENCHANT_FRAGMENTS_MEDIUM = "enchant/fragments_medium";
    private static final String ENCHANT_FRAGMENTS_SPECIAL = "enchant/fragments_special";
    private static final String ENCHANT_HEALTH_LOW = "enchant/health_low";
    private static final String ENCHANT_HEALTH_MEDIUM = "enchant/health_medium";
    private static final String ENCHANT_HEALTH_SPECIAL = "enchant/health_special";
    private static final String ENCHANT_FREE_LOW = "enchant/free_low";
    private static final String ENCHANT_FREE_MEDIUM = "enchant/free_medium";
    private static final String ENCHANT_FREE_HIGH = "enchant/free_high";
    private static final String ENCHANT_FREE_SPECIAL = "enchant/free_special";

    private static final String CURIO_NORMAL_B = "curio/normal_b";
    private static final String CURIO_NORMAL_A = "curio/normal_a";
    private static final String CURIO_NORMAL_S = "curio/normal_s";
    private static final String CURIO_FUSION_B = "curio/fusion_b";
    private static final String CURIO_FUSION_A = "curio/fusion_a";
    private static final String CURIO_FUSION_S = "curio/fusion_s";
    private static final String CURIO_NEGATIVE = "curio/negative";
    private static final String CURIO_CLOCK = "curio/clock";
    private static final String CURIO_NEGATIVE_CF = "curio/negative_cf";

    private static final String COMBAT_ABUNDANCE_BLIGHT = "combat/abundance_blight";
    private static final String COMBAT_ABUNDANCE_BLIGHT_WEAK = "combat/abundance_blight_weak";
    private static final String COMBAT_ZOMBIES = "combat/zombies";
    private static final String COMBAT_ZOMBIES_REWARD = "combat/zombies_reward";
    private static final String COMBAT_CREEPERS = "combat/creepers";
    private static final String COMBAT_RAVAGER = "combat/ravager";
    private static final String COMBAT_SKIRMISH = "combat/skirmish";
    private static final String COMBAT_SILVERFISH = "combat/silverfish";

    private static final String TRASH_SHIELD = "trash/shield";
    private static final String TRASH_FOOLS_MASK = "trash/fools_mask";
    private static final String TRASH_RADIANT_SHIELD = "trash/radiant_shield";

    private static final String FRAGMENTS_LOW = "fragments/low";
    private static final String FRAGMENTS_MEDIUM = "fragments/medium";
    private static final String FRAGMENTS_HIGH = "fragments/high";
    private static final String FRAGMENTS_SPECIAL = "fragments/special";

    private static final String QUOTA_WORLD_UP = "quota/world_up";
    private static final String QUOTA_WORLD_DOWN = "quota/world_down";
    private static final String QUOTA_PLAYER_UP = "quota/player_up";
    private static final String QUOTA_PLAYER_DOWN = "quota/player_down";
    private static final String QUOTA_LIMIT_UP = "quota/limit_up";
    private static final String QUOTA_LIMIT_DOWN = "quota/limit_down";

    private static final String SUPPLY_REDSTONE = "supply/redstone";
    private static final String SUPPLY_SILK_TOUCH_CHEST = "supply/silk_touch_chest";

    private static final String CHAIN_RANDOM_TEN = "chain/random_ten";
    private static final String CODE_SELECT = "code/select";

    private static final String LOTTERY_BAN = "lottery/ban";
    private static final String LOTTERY_PAY_HEALTH = "lottery/pay_health";
    private static final String LOTTERY_EXTORTION = "lottery/extortion";
    private static final String LOTTERY_PAY_SHORT = "lottery/pay_short";
    private static final String LOTTERY_PAY_FULL = "lottery/pay_full";

    private static final String WEAPON_LOW = "weapon/low";
    private static final String WEAPON_FIVE_STAR = "weapon/five_star";
    private static final String WEAPON_IRON = "weapon/iron";

    private static final String LIVING_DIAMOND = "living/diamond";
    private static final String LIVING_PRIMOGEM = "living/primogem";

    private static final String GROUP_ENCHANT_BASIC = "group/enchant/basic";
    private static final String GROUP_ENCHANT_VITAL = "group/enchant/vital";
    private static final String GROUP_CHOICE_FIRST = "group/choice/first";
    private static final String GROUP_CHOICE_SECOND = "group/choice/second";
    private static final String GROUP_CHOICE_THIRD = "group/choice/third";
    private static final String GROUP_CHOICE_FOURTH = "group/choice/fourth";
    private static final String GROUP_CHOICE_FIFTH = "group/choice/fifth";
    private static final String GROUP_CHOICE_SIXTH = "group/choice/sixth";
    private static final String GROUP_REWARD_CURIO = "group/reward/curio";
    private static final String GROUP_REWARD_RICHER = "group/reward/richer";
    private static final String GROUP_REWARD_FRAGMENTS = "group/reward/fragments";
    private static final String GROUP_REWARD_FRAGMENTS_BIG = "group/reward/fragments_big";
    private static final String GROUP_CURIO_CLOCK = "group/curio/clock";
    private static final String GROUP_CURIO_NEGATIVE = "group/curio/negative";
    private static final String GROUP_CURIO_NEGATIVE_CF = "group/curio/negative_cf";
    private static final String GROUP_RANDOM_TRIPLE = "group/random/triple";
    private static final String GROUP_COMBAT_MIXED = "group/combat/mixed";
    private static final String GROUP_COMBAT_LIGHT = "group/combat/light";
    private static final String GROUP_COMBAT_CREEPER = "group/combat/creeper";
    private static final String GROUP_COMBAT_RAVAGER = "group/combat/ravager";
    private static final String GROUP_COMBAT_LOOP = "group/combat/loop";
    private static final String GROUP_TRASH_GOODS = "group/trash/goods";
    private static final String GROUP_QUOTA_MORE = "group/quota/more";
    private static final String GROUP_QUOTA_LESS = "group/quota/less";
    private static final String GROUP_SUPPLY_DELICACIES = "group/supply/delicacies";
    private static final String GROUP_THRILLING = "group/thrilling";
    private static final String GROUP_CODE = "group/code";
    private static final String GROUP_LOTTERY_LEAGUE = "group/lottery/league";
    private static final String GROUP_WEAPON_CHOICE = "group/weapon_choice";
    private static final String GROUP_LIVING_CHOICE = "group/living_choice";

    private static final TagKey<Item> LOW_WEAPONS = TagKey.create(Registries.ITEM, EventRegistry.id("weapon/low"));

    private static final int FIVE_STAR_AFTERGLOW = 3;

    private static final ResourceLocation ENCOUNTER_REWARD_LOOT = EventRegistry.id("events/abundance_blight_reward");

    private static final ResourceLocation EVENT_3_TEXTURE = EventRegistry.entityTexture("event3");
    private static final ResourceLocation EVENT_4_TEXTURE = EventRegistry.entityTexture("event4");
    private static final ResourceLocation EVENT_5_TEXTURE = EventRegistry.entityTexture("event5");
    private static final ResourceLocation EVENT_6_TEXTURE = EventRegistry.entityTexture("event6");
    private static final ResourceLocation EVENT_7_TEXTURE = EventRegistry.entityTexture("event7");

    private static final int HOUR = 72000;
    private static final int FIVE_MINUTES = 6000;

    private static final Consumer<LivingEntity> WEAK_BLIGHT = living -> {
        baseValue(living, Attributes.MAX_HEALTH, 2.0D);
        baseValue(living, Attributes.ATTACK_DAMAGE, 1.0D);
    };
    private static final Consumer<LivingEntity> WEAK_RAVAGER = living -> {
        baseValue(living, Attributes.MAX_HEALTH, 20.0D);
        baseValue(living, Attributes.ATTACK_DAMAGE, 1.0D);
    };

    private static RandomEvent leave;
    public static EventGroup trashGoods;
    private static RandomEvent enchantFragmentsLow;
    private static RandomEvent enchantFragmentsMedium;
    private static RandomEvent enchantFragmentsSpecial;
    private static RandomEvent enchantHealthLow;
    private static RandomEvent enchantHealthMedium;
    private static RandomEvent enchantHealthSpecial;
    private static RandomEvent enchantFreeLow;
    private static RandomEvent enchantFreeMedium;
    private static RandomEvent curioNormalB;
    private static RandomEvent curioNormalA;
    private static RandomEvent curioFusionA;
    private static RandomEvent curioFusionS;
    private static RandomEvent curioNegative;
    private static RandomEvent curioClock;
    private static RandomEvent curioNegativeCf;
    private static RandomEvent combatAbundanceBlight;
    private static RandomEvent combatAbundanceBlightWeak;
    private static RandomEvent combatZombies;
    private static RandomEvent combatZombiesReward;
    private static RandomEvent combatCreepers;
    private static RandomEvent combatRavager;
    private static RandomEvent combatSkirmish;
    private static RandomEvent combatSilverfish;
    private static RandomEvent trashShield;
    private static RandomEvent trashFoolsMask;
    private static RandomEvent trashRadiantShield;
    private static RandomEvent fragmentsLow;
    private static RandomEvent fragmentsMedium;
    private static RandomEvent fragmentsHigh;
    private static RandomEvent fragmentsSpecial;
    private static RandomEvent quotaWorldUp;
    private static RandomEvent quotaWorldDown;
    private static RandomEvent quotaPlayerUp;
    private static RandomEvent quotaPlayerDown;
    private static RandomEvent quotaLimitUp;
    private static RandomEvent quotaLimitDown;
    private static RandomEvent supplyRedstone;
    private static RandomEvent supplySilkTouchChest;
    private static RandomEvent chainRandomTen;
    private static RandomEvent codeSelect;
    private static RandomEvent lotteryBan;
    private static RandomEvent lotteryPayHealth;
    private static RandomEvent lotteryExtortion;
    private static RandomEvent lotteryPayShort;
    private static RandomEvent lotteryPayFull;
    private static RandomEvent weaponLow;
    private static RandomEvent weaponFiveStar;
    private static RandomEvent weaponIron;
    private static RandomEvent livingDiamond;
    private static RandomEvent livingPrimogem;

    private RandomEvents() {
    }

    public static void registerAll() {
        leave = register(LEAVE, context -> true);

        enchantFragmentsLow = register(ENCHANT_FRAGMENTS_LOW, context -> context.enchant(EnchantGrade.LOW, 10), EventCondition.all(EventCondition.fragments(10), EventCondition.enchantTargets()));
        enchantFragmentsMedium = register(ENCHANT_FRAGMENTS_MEDIUM, context -> context.enchant(grade(context.range(1, 2)), 20), EventCondition.all(EventCondition.fragments(20), EventCondition.enchantTargets()));
        enchantFragmentsSpecial = register(ENCHANT_FRAGMENTS_SPECIAL, context -> context.enchant(grade(context.range(2, 4)), 40), EventCondition.all(EventCondition.fragments(40), EventCondition.enchantTargets()));
        enchantHealthLow = register(ENCHANT_HEALTH_LOW, context -> context.health(0.2D) && context.enchant(EnchantGrade.LOW), EventCondition.all(EventCondition.health(0.2D), EventCondition.enchantTargets()));
        enchantHealthMedium = register(ENCHANT_HEALTH_MEDIUM, context -> context.health(0.7D) && context.enchant(EnchantGrade.MEDIUM), EventCondition.all(EventCondition.health(0.7D), EventCondition.enchantTargets()));
        enchantHealthSpecial = register(ENCHANT_HEALTH_SPECIAL, context -> context.health(0.95D) && context.fragments(20) && context.enchant(EnchantGrade.SPECIAL), EventCondition.all(EventCondition.health(0.95D), EventCondition.fragments(20), EventCondition.enchantTargets()));
        enchantFreeLow = register(ENCHANT_FREE_LOW, context -> context.enchant(EnchantGrade.LOW), EventCondition.enchantTargets());
        enchantFreeMedium = register(ENCHANT_FREE_MEDIUM, context -> context.enchant(EnchantGrade.MEDIUM), EventCondition.enchantTargets());
        RandomEvent enchantFreeHigh = register(ENCHANT_FREE_HIGH, context -> context.enchant(EnchantGrade.HIGH), EventCondition.enchantTargets());
        RandomEvent enchantFreeSpecial = register(ENCHANT_FREE_SPECIAL, context -> context.enchant(EnchantGrade.SPECIAL), EventCondition.enchantTargets());

        curioNormalB = register(CURIO_NORMAL_B, context -> context.curios(CurioGrade.B.tag(CurioForm.NORMAL), 3), EventCondition.curios(CurioGrade.B.tag(CurioForm.NORMAL)));
        curioNormalA = register(CURIO_NORMAL_A, context -> context.curios(CurioGrade.A.tag(CurioForm.NORMAL), 3), EventCondition.curios(CurioGrade.A.tag(CurioForm.NORMAL)));
        RandomEvent curioNormalS = register(CURIO_NORMAL_S, context -> context.curios(CurioGrade.S.tag(CurioForm.NORMAL), 3), EventCondition.curios(CurioGrade.S.tag(CurioForm.NORMAL)));
        RandomEvent curioFusionB = register(CURIO_FUSION_B, context -> context.curios(CurioGrade.B.tag(CurioForm.FUSION), 3), EventCondition.curios(CurioGrade.B.tag(CurioForm.FUSION)));
        curioFusionA = register(CURIO_FUSION_A, context -> context.curios(CurioGrade.A.tag(CurioForm.FUSION), 3), EventCondition.curios(CurioGrade.A.tag(CurioForm.FUSION)));
        curioFusionS = register(CURIO_FUSION_S, context -> context.curios(CurioGrade.S.tag(CurioForm.FUSION), 3), EventCondition.curios(CurioGrade.S.tag(CurioForm.FUSION)));
        curioNegative = registerForced(CURIO_NEGATIVE, context -> context.curios(CurioForm.NEGATIVE.tag(), 3), EventCondition.curios(CurioForm.NEGATIVE.tag()));
        curioClock = registerForced(CURIO_CLOCK, context -> context.curios(CLOCK, 3), EventCondition.curios(CLOCK));
        curioNegativeCf = registerForced(CURIO_NEGATIVE_CF, context -> context.curios(NEGATIVE_CF, 3), EventCondition.curios(NEGATIVE_CF));

        combatAbundanceBlight = register(COMBAT_ABUNDANCE_BLIGHT, context -> EventCombat.loot(context, PGCEntities.ABUNDANCE_BLIGHT_ZOMBIE.get(), 1, ENCOUNTER_REWARD_LOOT));
        combatAbundanceBlightWeak = register(COMBAT_ABUNDANCE_BLIGHT_WEAK, context -> EventCombat.guard(context, PGCEntities.ABUNDANCE_BLIGHT_ZOMBIE.get(), 2, WEAK_BLIGHT));
        combatZombies = register(COMBAT_ZOMBIES, context -> EventCombat.summon(context, EntityType.ZOMBIE, 5, null));
        combatZombiesReward = register(COMBAT_ZOMBIES_REWARD, context -> {
            var extra = EventRegistry.randomEvent(context.random());
            return EventCombat.challenge(context, EntityType.ZOMBIE, 6, 5, null, finished -> finished.group(
                    EventGroup.dynamic(text(COMBAT_ZOMBIES_REWARD, "group"), 1,
                            () -> List.of(curioNormalB.number(), enchantFreeMedium.number(), extra))));
        });
        combatCreepers = register(COMBAT_CREEPERS, context -> EventCombat.guard(context, EntityType.CREEPER, 2, null));
        combatRavager = register(COMBAT_RAVAGER, context -> EventCombat.guard(context, EntityType.RAVAGER, 1, WEAK_RAVAGER));
        combatSkirmish = register(COMBAT_SKIRMISH, context -> {
            var first = context.chance(0.5D) ? combatSkirmish.number() : EventRegistry.randomEvent(context.random());
            var second = EventRegistry.randomEvent(context.random());
            return EventCombat.challenge(context, EntityType.ZOMBIE, 5, 3, null, finished -> finished.group(
                    EventGroup.dynamic(text(COMBAT_SKIRMISH, "group"), 1,
                            () -> List.of(first, second, enchantFreeLow.number()))));
        });
        combatSilverfish = register(COMBAT_SILVERFISH, context -> EventCombat.challenge(context, EntityType.SILVERFISH, 1, 1, null, finished -> finished.group(
                EventGroup.dynamic(text(COMBAT_SILVERFISH, "group"), 1,
                        () -> List.of(supplyRedstone.number(), supplySilkTouchChest.number(), leave.number())))));

        trashShield = register(TRASH_SHIELD, context -> context.give(new ItemStack(Items.SHIELD)) && context.prompt(text(TRASH_SHIELD, "line")));
        trashFoolsMask = register(TRASH_FOOLS_MASK, context -> context.give(new ItemStack(PGCItems.FOOLS_MASK.get())) && context.prompt(text(TRASH_FOOLS_MASK, "line")));
        trashRadiantShield = register(TRASH_RADIANT_SHIELD, context -> context.give(new ItemStack(PGCItems.SHIELD_OF_RADIANT_WILL.get())) && context.prompt(text(TRASH_RADIANT_SHIELD, "line")));

        fragmentsLow = register(FRAGMENTS_LOW, context -> fragments(context, 1, 10));
        fragmentsMedium = register(FRAGMENTS_MEDIUM, context -> fragments(context, 5, 20));
        fragmentsHigh = register(FRAGMENTS_HIGH, context -> fragments(context, 10, 40));
        fragmentsSpecial = register(FRAGMENTS_SPECIAL, context -> fragments(context, 15, 64));

        quotaWorldUp = register(QUOTA_WORLD_UP, context -> context.worldQuota(1));
        quotaWorldDown = register(QUOTA_WORLD_DOWN, context -> context.worldQuota(-1));
        quotaPlayerUp = register(QUOTA_PLAYER_UP, context -> context.playerQuota(1));
        quotaPlayerDown = register(QUOTA_PLAYER_DOWN, context -> context.playerQuota(-1));
        quotaLimitUp = register(QUOTA_LIMIT_UP, context -> context.worldLimit(1));
        quotaLimitDown = register(QUOTA_LIMIT_DOWN, context -> context.worldLimit(-1));

        supplyRedstone = register(SUPPLY_REDSTONE, context -> {
            var redstone = new ItemStack(Items.REDSTONE, 8);
            redstone.set(DataComponents.CUSTOM_NAME, text(SUPPLY_REDSTONE, "name"));
            return context.give(redstone);
        });
        supplySilkTouchChest = register(SUPPLY_SILK_TOUCH_CHEST, context -> {
            var chest = new ItemStack(Items.CHEST);
            chest.enchant(context.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SILK_TOUCH), 1);
            return context.give(chest);
        });

        chainRandomTen = register(CHAIN_RANDOM_TEN, context -> context.randomEvents(10), EventCondition.chain());
        codeSelect = register(CODE_SELECT, context -> context.curios(CODE, 3), EventCondition.curios(CODE));

        lotteryBan = register(LOTTERY_BAN, context -> {
            ban(context, HOUR);
            return context.prompt(text(LOTTERY_BAN, "line"));
        });
        lotteryPayHealth = register(LOTTERY_PAY_HEALTH, context -> {
            var paid = context.hasHealth(0.5D);
            if (paid) context.health(0.5D);
            ban(context, paid ? FIVE_MINUTES : HOUR);
            return context.prompt(text(LOTTERY_PAY_HEALTH, paid ? "line" : "line_alt"));
        });
        lotteryExtortion = register(LOTTERY_EXTORTION, context -> {
            ban(context, HOUR);
            context.prompt(text(LOTTERY_EXTORTION, "line"));
            return context.group(EventGroup.dynamic(text(LOTTERY_EXTORTION, "group"), 1,
                    () -> List.of(lotteryPayShort.number(), lotteryPayFull.number(), leave.number())));
        });
        lotteryPayShort = register(LOTTERY_PAY_SHORT, context -> pay(context, 20, FIVE_MINUTES, LOTTERY_PAY_SHORT), EventCondition.fragments(20));
        lotteryPayFull = register(LOTTERY_PAY_FULL, context -> pay(context, 40, 0, LOTTERY_PAY_FULL), EventCondition.fragments(40));

        weaponLow = register(WEAPON_LOW, context -> context.choose(weaponOptions(LOW_WEAPONS), text(WEAPON_LOW, "title")));
        weaponFiveStar = register(WEAPON_FIVE_STAR, context -> context.afterglow(FIVE_STAR_AFTERGLOW) && context.choose(weaponOptions(WishWeapon.FIVE_STAR), text(WEAPON_FIVE_STAR, "title")),
                EventCondition.afterglow(FIVE_STAR_AFTERGLOW));
        weaponIron = register(WEAPON_IRON, context -> context.choose(ironWeapons(), text(WEAPON_IRON, "title")));

        livingDiamond = register(LIVING_DIAMOND, context -> context.livingItems(3, new ItemStack(Items.DIAMOND)));
        livingPrimogem = register(LIVING_PRIMOGEM, context -> context.livingItems(1, new ItemStack(PGCItems.PRIMOGEM.get())));

        registerGroups();
    }

    private static void registerGroups() {
        EventRegistry.registerGroup(EventGroup.of(text(GROUP_ENCHANT_BASIC, "title"), EVENT_6_TEXTURE, enchantFragmentsLow, enchantFragmentsMedium, enchantFragmentsSpecial));
        EventRegistry.registerGroup(EventGroup.of(text(GROUP_ENCHANT_VITAL, "title"), EVENT_6_TEXTURE, enchantHealthLow, enchantHealthMedium, enchantHealthSpecial));
        EventRegistry.registerGroup(EventGroup.of(text(GROUP_CHOICE_FIRST, "title"), enchantFragmentsLow, enchantHealthLow, leave));
        EventRegistry.registerRichGroup(EventGroup.of(text(GROUP_REWARD_CURIO, "title"), EVENT_6_TEXTURE, 1, curioNormalB, enchantFreeLow, leave));
        EventRegistry.registerGroup(EventGroup.of(text(GROUP_CHOICE_SECOND, "title"), enchantFragmentsMedium, enchantHealthMedium, leave));
        EventRegistry.registerGroup(EventGroup.of(text(GROUP_CHOICE_THIRD, "title"), enchantFragmentsSpecial, enchantHealthSpecial, leave));
        EventRegistry.registerRichGroup(EventGroup.of(text(GROUP_REWARD_RICHER, "title"), EVENT_6_TEXTURE, 1, curioFusionA, enchantFreeMedium, leave));
        EventRegistry.registerGroup(EventGroup.of(text(GROUP_CURIO_CLOCK, "title"), EVENT_3_TEXTURE, curioClock, curioClock, curioClock));
        EventRegistry.registerGroup(EventGroup.of(text(GROUP_CURIO_NEGATIVE, "title"), curioNegative, curioNegative, curioNegative));
        EventRegistry.registerGroup(EventGroup.of(text(GROUP_CURIO_NEGATIVE_CF, "title"), curioNegativeCf, curioNegativeCf, curioNegativeCf));
        EventRegistry.registerRichGroup(EventGroup.dynamic(text(GROUP_RANDOM_TRIPLE, "title"), EVENT_7_TEXTURE, 5, () -> List.of(randomEvent(), randomEvent(), randomEvent())));
        EventRegistry.registerRichGroup(EventGroup.of(text(GROUP_COMBAT_MIXED, "title"), EVENT_4_TEXTURE, 1, combatAbundanceBlight, combatZombiesReward, curioNegative));
        EventRegistry.registerGroup(EventGroup.of(text(GROUP_COMBAT_LIGHT, "title"), EVENT_4_TEXTURE, 1, combatAbundanceBlightWeak, combatZombies, curioNegative));
        trashGoods = EventRegistry.registerGroup(EventGroup.of(text(GROUP_TRASH_GOODS, "title"), trashShield, trashFoolsMask, trashRadiantShield));
        EventRegistry.registerGroup(EventGroup.of(text(GROUP_COMBAT_CREEPER, "title"), EVENT_4_TEXTURE, 1, combatCreepers, combatCreepers, combatCreepers));
        EventRegistry.registerGroup(EventGroup.of(text(GROUP_COMBAT_RAVAGER, "title"), EVENT_4_TEXTURE, 1, combatRavager, combatRavager, combatRavager));
        EventRegistry.registerGroup(EventGroup.of(text(GROUP_COMBAT_LOOP, "title"), EVENT_4_TEXTURE, 1, combatSkirmish, combatSkirmish, combatSkirmish));
        EventRegistry.registerRichGroup(EventGroup.of(text(GROUP_REWARD_FRAGMENTS, "title"), EVENT_6_TEXTURE, 1, fragmentsLow, fragmentsMedium, fragmentsLow));
        EventRegistry.registerGroup(EventGroup.of(text(GROUP_QUOTA_MORE, "title"), EVENT_5_TEXTURE, 1, quotaLimitUp, quotaWorldUp, quotaPlayerUp));
        EventRegistry.registerGroup(EventGroup.of(text(GROUP_QUOTA_LESS, "title"), EVENT_5_TEXTURE, 1, quotaLimitDown, quotaWorldDown, quotaPlayerDown));
        EventRegistry.registerRichGroup(EventGroup.of(text(GROUP_CHOICE_FOURTH, "title"), EVENT_7_TEXTURE, fragmentsSpecial, quotaLimitUp, curioFusionS));
        EventRegistry.registerGroup(EventGroup.of(text(GROUP_CHOICE_FIFTH, "title"), EVENT_7_TEXTURE, quotaWorldDown, quotaLimitDown, curioNegative));
        EventRegistry.registerGroup(EventGroup.of(text(GROUP_CHOICE_SIXTH, "title"), EVENT_7_TEXTURE, curioNegativeCf, quotaPlayerUp, quotaLimitUp));
        EventRegistry.registerRichGroup(EventGroup.of(text(GROUP_REWARD_FRAGMENTS_BIG, "title"), EVENT_6_TEXTURE, 1, fragmentsHigh, fragmentsHigh, fragmentsHigh));
        EventRegistry.registerGroup(EventGroup.of(text(GROUP_SUPPLY_DELICACIES, "title"), supplyRedstone, supplySilkTouchChest, combatSilverfish));
        EventRegistry.registerRichGroup(EventGroup.of(text(GROUP_THRILLING, "title"), EVENT_7_TEXTURE, chainRandomTen, fragmentsMedium, curioNormalA));
        EventRegistry.registerGroup(EventGroup.of(text(GROUP_CODE, "title"), codeSelect, fragmentsLow, leave));
        EventRegistry.registerGroup(EventGroup.of(text(GROUP_LOTTERY_LEAGUE, "title"), EVENT_3_TEXTURE, lotteryBan, lotteryPayHealth, lotteryExtortion));
        EventRegistry.registerGroup(EventGroup.of(text(GROUP_WEAPON_CHOICE, "title"), EVENT_7_TEXTURE, weaponLow, weaponFiveStar, weaponIron, leave));
        EventRegistry.registerGroup(EventGroup.of(text(GROUP_LIVING_CHOICE, "title"), EVENT_6_TEXTURE, 1, livingDiamond, livingPrimogem, leave));
    }

    private static List<ItemStack> weaponOptions(TagKey<Item> tag) {
        var tagged = BuiltInRegistries.ITEM.getTag(tag);
        if (tagged.isEmpty()) return List.of();
        var options = new ArrayList<ItemStack>();
        for (var holder : tagged.get()) {
            var item = holder.value();
            if (item instanceof WishWeapon) options.add(new ItemStack(item));
        }
        return List.copyOf(options);
    }

    private static List<ItemStack> ironWeapons() {
        return List.of(new ItemStack(Items.IRON_SWORD), new ItemStack(Items.IRON_AXE));
    }

    private static boolean fragments(EventContext context, int min, int max) {
        return context.choose(List.of(fragment(context, min, max), fragment(context, min, max), fragment(context, min, max)), context.event().title());
    }

    private static ItemStack fragment(EventContext context, int min, int max) {
        return new ItemStack(PGCItems.COSMIC_FRAGMENT.get(), context.range(min, max));
    }

    private static boolean pay(EventContext context, int amount, int ticks, String path) {
        if (!context.fragments(amount)) return false;
        ban(context, ticks);
        return context.prompt(text(path, "line"));
    }

    private static void ban(EventContext context, int ticks) {
        PGCTimer.set(context.player(), LotteryCurioItem.BAN_TIMER, ticks);
    }

    private static EnchantGrade grade(int value) {
        var grades = EnchantGrade.values();
        return grades[Mth.clamp(value, 1, grades.length) - 1];
    }

    private static RandomEvent register(String path, EventAction action) {
        return EventRegistry.register(text(path, "title"), text(path, "description"), action);
    }

    private static RandomEvent register(String path, EventAction action, EventCondition condition) {
        return EventRegistry.register(text(path, "title"), text(path, "description"), action, condition);
    }

    private static RandomEvent registerForced(String path, EventAction action, EventCondition condition) {
        return EventRegistry.register(text(path, "title"), text(path, "description"), action, condition, true);
    }

    public static RandomEvent leaveEvent() {
        return leave;
    }

    private static int randomEvent() {
        return EventRegistry.randomEvent(RandomSource.create());
    }

    private static void baseValue(LivingEntity living, Holder<Attribute> attribute, double value) {
        var instance = living.getAttribute(attribute);
        if (instance != null) instance.setBaseValue(value);
    }

    private static Component text(String path, String suffix) {
        return Component.translatable("event.primogemcraft." + path.replace('/', '.') + "." + suffix);
    }

    private static TagKey<Item> common(String path) {
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", path));
    }
}
