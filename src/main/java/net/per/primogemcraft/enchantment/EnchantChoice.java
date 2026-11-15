package net.per.primogemcraft.enchantment;

import net.minecraft.text.Text;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.sound.SoundCategory;
import net.minecraft.item.ItemStack;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
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

    public static boolean open(ServerPlayerEntity player, EnchantGrade grade) {
        return open(player, grade, DEFAULT_ITEMS);
    }

    public static boolean open(ServerPlayerEntity player, EnchantGrade grade, int items) {
        return open(player, grade, items, DEFAULT_TIMES, DEFAULT_PICKS);
    }

    public static boolean open(ServerPlayerEntity player, EnchantGrade grade, int items, int times, int picks) {
        var level = grade.rollLevel(player.getRandom());
        var options = new ArrayList<EnchantOption>();
        for (var target : targets(player, items)) {
            for (var application = 0; application < Math.max(1, times); application++) {
                options.add(EnchantOption.of(target, CurioEnchanting.tablePoolResult(player, target, level), grade, level, EnchantCost.free()));
            }
        }
        return open(player, options, picks);
    }

    public static boolean open(ServerPlayerEntity player, List<EnchantOption> options, int picks) {
        if (round(player, new ArrayList<>(options), Math.max(1, picks))) return true;
        player.sendMessage(Text.translatable(NO_RESULT_KEY), true);
        return false;
    }

    public static boolean hasTargets(ServerPlayerEntity player) {
        return !pool(player).isEmpty();
    }

    private static boolean round(ServerPlayerEntity player, List<EnchantOption> options, int picks) {
        var shown = new ArrayList<EnchantOption>();
        for (var option : options) if (applicable(option)) shown.add(option);
        if (shown.isEmpty()) return false;
        var cards = new ArrayList<ChoiceCard>();
        for (var option : shown) cards.add(presentation(option));
        ChoiceRegistry.open(player, Text.translatable(TITLE_KEY), Text.translatable(HINT_KEY), ChoiceVisual.ITEM_MODEL, ChoiceSupport.BACKGROUND, ChoiceSupport.ENCHANT_CARDS, ChoiceSupport.spin(LADDER.get(0)), cards, index -> {
            if (index < 0 || index >= shown.size()) return true;
            var option = shown.get(index);
            if (!option.cost().payable().test(player)) {
                player.sendMessage(Text.translatable(UNAVAILABLE_KEY), true);
                return true;
            }
            option.cost().pay().accept(player);
            CurioEnchanting.applyResult(option.target(), option.preview());
            playEnchantSound(player);
            options.remove(option);
            if (picks <= 1) return true;
            var remaining = refill(player, options);
            if (remaining.isEmpty()) return true;
            if (!round(player, remaining, picks - 1)) player.sendMessage(Text.translatable(NO_RESULT_KEY), true);
            return true;
        });
        return true;
    }

    private static List<EnchantOption> refill(ServerPlayerEntity player, List<EnchantOption> options) {
        var refilled = new ArrayList<EnchantOption>();
        for (var option : options) refilled.add(EnchantOption.of(option.target(), CurioEnchanting.tablePoolResult(player, option.target(), option.level()), option.grade(), option.level(), option.cost()));
        return refilled;
    }

    private static List<ItemStack> pool(ServerPlayerEntity player) {
        var pool = new ArrayList<ItemStack>();
        for (var stack : Curios.inventory(player)) if (enchantable(stack)) pool.add(stack);
        for (var stack : player.getInventory().armor) if (enchantable(stack)) pool.add(stack);
        return pool;
    }

    private static boolean enchantable(ItemStack stack) {
        return stack.isEnchantable() && stack.getItem().getEnchantability() > 0;
    }

    private static List<ItemStack> targets(ServerPlayerEntity player, int count) {
        var pool = pool(player);
        var picked = new ArrayList<ItemStack>();
        var random = player.getRandom();
        for (var index = 0; index < count && !pool.isEmpty(); index++) picked.add(pool.remove(random.nextInt(pool.size())));
        return picked;
    }

    private static boolean applicable(EnchantOption option) {
        var preview = option.preview();
        return !preview.isEmpty() && !EnchantmentHelper.get(preview).isEmpty();
    }

    private static void playEnchantSound(ServerPlayerEntity player) {
        player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.PLAYERS, ENCHANT_VOLUME, ENCHANT_PITCH);
    }

    static ChoiceCard presentation(EnchantOption option) {
        var grade = option.grade();
        var preview = option.preview();
        return ChoiceSupport.card(Text.translatable(grade.translationKey()), preview, description(option, preview), option.cost().description())
                .withSpin(LADDER.get(Math.min(grade.ordinal(), LADDER.size() - 1)))
                .withOverlay(ChoiceSupport.level(grade.column()))
                .withQuality(grade.column())
                .withTextTooltip()
                .withItemTooltip();
    }

    private static Text description(EnchantOption option, ItemStack preview) {
        var content = Text.empty();
        for (var enchantment : EnchantmentHelper.get(preview).entrySet()) {
            content.append(enchantment.getKey().getName(enchantment.getValue())).append(LINE_BREAK);
        }
        return content.append(Text.translatable(LEVEL_KEY, option.level()));
    }
}
