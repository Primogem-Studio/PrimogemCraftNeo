package net.per.primogemcraft.system.shop;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.per.primogemcraft.enchantment.EnchantChoice;
import net.per.primogemcraft.enchantment.EnchantCost;
import net.per.primogemcraft.enchantment.EnchantGrade;
import net.per.primogemcraft.enchantment.EnchantOption;
import net.per.primogemcraft.item.misc.OtherworldBankbook;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.system.curio.CurioChoice;
import net.per.primogemcraft.system.curio.CurioEnchanting;
import net.per.primogemcraft.system.curio.CurioItem;
import net.per.primogemcraft.system.curio.Curios;
import net.per.primogemcraft.util.PlayerItems;

import java.util.ArrayList;
import java.util.List;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public final class HertaShop {
    private static final String CURIO_KEY = "gui.primogemcraft.herta_shop.curio";
    private static final String CURIO_HINT_KEY = "gui.primogemcraft.herta_shop.curio.hint";
    private static final String CODE_KEY = "gui.primogemcraft.herta_shop.code";
    private static final String CODE_HINT_KEY = "gui.primogemcraft.herta_shop.code.hint";
    private static final String FRAGMENT_PRICE_KEY = "gui.primogemcraft.herta_shop.price.fragments";
    private static final String FRAGMENT_RANGE_KEY = "gui.primogemcraft.herta_shop.price.fragments_range";
    private static final String ITEM_PRICE_KEY = "gui.primogemcraft.herta_shop.price.item";
    private static final String SHORT_KEY = "message.primogemcraft.herta_shop.not_enough";
    private static final String TARGET_KEY = "message.primogemcraft.enchant.invalid_target";

    private static final int CURIO_PRICE = 16;
    private static final int CODE_PRICE = 1;
    private static final int ENCHANT_STEP = 10;
    private static final int DISCOUNT_DIVISOR = 5;
    private static final int OPTION_COUNT = 3;
    private static final List<ResourceLocation> POOLS = List.of(
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "herta_shop/curio_01"),
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "herta_shop/curio_02"),
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "herta_shop/curio_03"));

    private static List<List<Item>> codePairs;

    private HertaShop() {
    }

    public static void open(ServerPlayer player) {
        var discounted = discount(player);
        HertaShopOptions.open(player, fragmentPrice(fragments(CURIO_PRICE, discounted)), itemPrice(PGCItems.RANDOM_EVENT_ERROR_CODE.get()), fragmentRange(discounted), index -> {
            if (index == HertaShopOptions.CURIO) buyCurio(player);
            else if (index == HertaShopOptions.CODE) chooseCode(player);
            else if (index == HertaShopOptions.ENCHANT) enchant(player);
        });
    }

    private static void buyCurio(ServerPlayer player) {
        var price = fragments(CURIO_PRICE, discount(player));
        if (unaffordableFragments(player, price)) return;
        var options = rollCurios(player);
        CurioChoice.open(player, CURIO_KEY, CURIO_HINT_KEY, options, chosen -> {
            if (!chargeFragments(player, price)) return;
            Curios.give(player, chosen);
        });
    }

    private static void chooseCode(ServerPlayer player) {
        var device = PGCItems.RANDOM_EVENT_ERROR_CODE.get();
        if (unaffordable(player, device, CODE_PRICE)) return;
        var options = rollCodes(player);
        CurioChoice.open(player, CODE_KEY, CODE_HINT_KEY, options, chosen -> {
            if (!charge(player, device)) return;
            Curios.give(player, chosen);
        });
    }

    private static void enchant(ServerPlayer player) {
        var target = player.getMainHandItem();
        if (!target.isEnchantable()) {
            player.displayClientMessage(Component.translatable(TARGET_KEY), true);
            return;
        }
        var discounted = discount(player);
        var options = new ArrayList<EnchantOption>();
        for (var grade : grades(player)) {
            var level = grade.rollLevel(player.getRandom());
            var price = fragments(enchantPrice(grade), discounted);
            var cost = EnchantCost.of(fragmentPrice(price), payer -> OtherworldBankbook.canPayFragments(payer, price),
                    payer -> OtherworldBankbook.payFragments(payer, price));
            options.add(EnchantOption.of(target, CurioEnchanting.tablePoolResult(player, target, level), grade, level, cost));
        }
        EnchantChoice.open(player, options, 1);
    }

    private static List<ItemStack> rollCurios(ServerPlayer player) {
        var level = player.serverLevel();
        var params = new LootParams.Builder(level).withParameter(LootContextParams.ORIGIN, player.position()).create(LootContextParamSets.CHEST);
        var options = new ArrayList<ItemStack>();
        for (var pool : POOLS) {
            var table = level.getServer().reloadableRegistries().getLootTable(ResourceKey.create(Registries.LOOT_TABLE, pool));
            for (var stack : table.getRandomItems(params)) {
                if (stack.isEmpty()) continue;
                var option = stack.copy();
                option.setCount(1);
                options.add(option);
                break;
            }
        }
        return options;
    }

    private static List<ItemStack> rollCodes(ServerPlayer player) {
        var random = player.getRandom();
        var options = new ArrayList<ItemStack>();
        for (var pair : codePairs()) options.add(new ItemStack(pair.get(random.nextInt(pair.size()))));
        return options;
    }

    private static List<List<Item>> codePairs() {
        if (codePairs == null) {
            codePairs = List.of(
                    List.of(PGCItems.PRECISE_ELEGANT_CODE.get(), PGCItems.MESSY_CODE.get()),
                    List.of(PGCItems.SLIGHTLY_ODD_CODE.get(), PGCItems.INFINITELY_RECURSIVE_CODE.get()),
                    List.of(PGCItems.UNCOMMENTED_CODE.get(), PGCItems.CONVENTIONAL_CODE.get()));
        }
        return codePairs;
    }

    private static List<EnchantGrade> grades(ServerPlayer player) {
        var pool = new ArrayList<>(List.of(EnchantGrade.values()));
        var picked = new ArrayList<EnchantGrade>();
        for (var index = 0; index < OPTION_COUNT && !pool.isEmpty(); index++) picked.add(pool.remove(player.getRandom().nextInt(pool.size())));
        return picked;
    }

    private static boolean discount(ServerPlayer player) {
        for (var context : Curios.held(player)) {
            if (context.stack().getItem() instanceof CurioItem curio && curio.discountsHertaShop()) return true;
        }
        return false;
    }

    private static boolean unaffordable(ServerPlayer player, Item item, int amount) {
        if (PlayerItems.count(player, item) >= amount) return false;
        player.displayClientMessage(Component.translatable(SHORT_KEY), true);
        return true;
    }

    private static boolean unaffordableFragments(ServerPlayer player, int amount) {
        if (OtherworldBankbook.canPayFragments(player, amount)) return false;
        player.displayClientMessage(Component.translatable(SHORT_KEY), true);
        return true;
    }

    private static boolean chargeFragments(ServerPlayer player, int amount) {
        if (unaffordableFragments(player, amount)) return false;
        return OtherworldBankbook.payFragments(player, amount);
    }

    private static boolean charge(ServerPlayer player, Item item) {
        if (unaffordable(player, item, HertaShop.CODE_PRICE)) return false;
        PlayerItems.take(player, item, HertaShop.CODE_PRICE);
        return true;
    }

    private static int enchantPrice(EnchantGrade grade) {
        return ENCHANT_STEP * (grade.ordinal() + 1);
    }

    private static int fragments(int price, boolean discounted) {
        return discounted ? price - price / DISCOUNT_DIVISOR : price;
    }

    private static Component fragmentPrice(int amount) {
        return Component.translatable(FRAGMENT_PRICE_KEY, amount);
    }

    private static Component fragmentRange(boolean discounted) {
        return Component.translatable(FRAGMENT_RANGE_KEY, fragments(ENCHANT_STEP, discounted), fragments(ENCHANT_STEP * EnchantGrade.values().length, discounted));
    }

    private static Component itemPrice(Item item) {
        return Component.translatable(ITEM_PRICE_KEY, HertaShop.CODE_PRICE, new ItemStack(item).getHoverName());
    }
}
