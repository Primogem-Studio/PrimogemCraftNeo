package net.per.primogemcraft.enchantment;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.per.primogemcraft.system.choice.*;
import net.per.primogemcraft.system.curio.CurioEnchanting;
import net.per.primogemcraft.system.curio.Curios;

import java.util.ArrayList;
import java.util.List;

public final class EnchantChoice {
    public static final int DEFAULT_ITEMS = 4;
    public static final int DEFAULT_TIMES = 3;
    public static final int DEFAULT_PICKS = 1;

    private static final String TITLE_KEY = "gui.primogemcraft.enchant_choice.title";
    private static final String HINT_KEY = "gui.primogemcraft.enchant_choice.hint";
    private static final String LEVEL_KEY = "gui.primogemcraft.enchant_choice.level";
    private static final String UNAVAILABLE_KEY = "message.primogemcraft.enchant_choice.unavailable";
    private static final String NO_RESULT_KEY = "message.primogemcraft.enchant_choice.no_result";
    private static final String LINE_BREAK = "\n";
    private static final float ENCHANT_VOLUME = 1.0F;
    private static final float ENCHANT_PITCH = 1.0F;
    private static final List<ChoiceCardSpin> LADDER = List.of(
            ChoiceCardSpin.of(1, ChoiceSpinSpeed.SLOW),
            ChoiceCardSpin.of(2, ChoiceSpinSpeed.MEDIUM),
            ChoiceCardSpin.of(3, ChoiceSpinSpeed.FAST),
            ChoiceCardSpin.of(4, ChoiceSpinSpeed.FASTEST));

    private EnchantChoice() {
    }

    public static boolean open(ServerPlayer player, EnchantGrade grade) {
        return open(player, grade, DEFAULT_ITEMS);
    }

    public static boolean open(ServerPlayer player, EnchantGrade grade, int items) {
        return open(player, grade, items, DEFAULT_TIMES, DEFAULT_PICKS);
    }

    public static boolean open(ServerPlayer player, EnchantGrade grade, int items, int times, int picks) {
        var level = grade.rollLevel(player.getRandom());
        var options = new ArrayList<EnchantOption>();
        for (var target : targets(player, items)) {
            for (var application = 0; application < Math.max(1, times); application++) {
                options.add(EnchantOption.of(target, CurioEnchanting.tablePoolResult(player, target, level), grade, level, EnchantCost.free()));
            }
        }
        return open(player, options, picks);
    }

    public static boolean open(ServerPlayer player, List<EnchantOption> options, int picks) {
        if (round(player, new ArrayList<>(options), Math.max(1, picks))) return true;
        player.displayClientMessage(Component.translatable(NO_RESULT_KEY), true);
        return false;
    }

    public static boolean hasTargets(ServerPlayer player) {
        return !pool(player).isEmpty();
    }

    private static boolean round(ServerPlayer player, List<EnchantOption> options, int picks) {
        var shown = new ArrayList<EnchantOption>();
        for (var option : options) if (applicable(option)) shown.add(option);
        if (shown.isEmpty()) return false;
        var cards = new ArrayList<ChoiceCard>();
        for (var option : shown) cards.add(presentation(option));
        ChoiceRegistry.open(player, Component.translatable(TITLE_KEY), Component.translatable(HINT_KEY), ChoiceVisual.ITEM_MODEL, ChoiceSupport.BACKGROUND, ChoiceSupport.ENCHANT_CARDS, ChoiceSupport.spin(LADDER.getFirst()), cards, index -> {
            if (index < 0 || index >= shown.size()) return true;
            var option = shown.get(index);
            if (!option.cost().payable().test(player)) {
                player.displayClientMessage(Component.translatable(UNAVAILABLE_KEY), true);
                return true;
            }
            option.cost().pay().accept(player);
            CurioEnchanting.applyResult(option.target(), option.preview());
            playEnchantSound(player);
            options.remove(option);
            if (picks <= 1) return true;
            var remaining = refill(player, options);
            if (remaining.isEmpty()) return true;
            if (!round(player, remaining, picks - 1)) player.displayClientMessage(Component.translatable(NO_RESULT_KEY), true);
            return true;
        });
        return true;
    }

    private static List<EnchantOption> refill(ServerPlayer player, List<EnchantOption> options) {
        var refilled = new ArrayList<EnchantOption>();
        for (var option : options) refilled.add(EnchantOption.of(option.target(), CurioEnchanting.tablePoolResult(player, option.target(), option.level()), option.grade(), option.level(), option.cost()));
        return refilled;
    }

    private static List<ItemStack> pool(ServerPlayer player) {
        var pool = new ArrayList<ItemStack>();
        for (var stack : Curios.inventory(player)) if (enchantable(stack)) pool.add(stack);
        for (var stack : player.getInventory().armor) if (enchantable(stack)) pool.add(stack);
        return pool;
    }

    private static boolean enchantable(ItemStack stack) {
        return stack.isEnchantable() && stack.getEnchantmentValue() > 0;
    }

    private static List<ItemStack> targets(ServerPlayer player, int count) {
        var pool = pool(player);
        var picked = new ArrayList<ItemStack>();
        var random = player.getRandom();
        for (var index = 0; index < count && !pool.isEmpty(); index++) picked.add(pool.remove(random.nextInt(pool.size())));
        return picked;
    }

    private static boolean applicable(EnchantOption option) {
        var preview = option.preview();
        return !preview.isEmpty() && !EnchantmentHelper.getEnchantmentsForCrafting(preview).isEmpty();
    }

    private static void playEnchantSound(ServerPlayer player) {
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, ENCHANT_VOLUME, ENCHANT_PITCH);
    }

    static ChoiceCard presentation(EnchantOption option) {
        var grade = option.grade();
        var preview = option.preview();
        return ChoiceSupport.card(Component.translatable(grade.translationKey()), preview, description(option, preview), option.cost().description())
                .withSpin(LADDER.get(Math.min(grade.ordinal(), LADDER.size() - 1)))
                .withOverlay(ChoiceSupport.level(grade.column()))
                .withQuality(grade.column())
                .withTextTooltip()
                .withItemTooltip();
    }

    private static Component description(EnchantOption option, ItemStack preview) {
        var content = Component.empty();
        for (var enchantment : EnchantmentHelper.getEnchantmentsForCrafting(preview).entrySet()) {
            content.append(Enchantment.getFullname(enchantment.getKey(), enchantment.getIntValue())).append(LINE_BREAK);
        }
        return content.append(Component.translatable(LEVEL_KEY, option.level()));
    }
}
