package net.per.primogemcraft.system.curio;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.per.primogemcraft.system.choice.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class CurioChoice {
    private static final String TITLE_KEY = "gui.primogemcraft.curio_choice.title";
    private static final String HINT_KEY = "gui.primogemcraft.curio_choice.hint";
    private static final ResourceLocation STAR = ChoiceSupport.texture("textures/gui/curio_star.png");
    private static final ResourceLocation FUSION_STAR = ChoiceSupport.texture("textures/gui/curio_star_fusion.png");
    private static final int NEGATIVE_COLUMN = 4;
    private static final int BASE_TIER = 0;
    private static final List<ChoiceCardSpin> LADDER = List.of(
            ChoiceCardSpin.of(1, ChoiceSpinSpeed.SLOW),
            ChoiceCardSpin.of(2, ChoiceSpinSpeed.MEDIUM),
            ChoiceCardSpin.of(3, ChoiceSpinSpeed.FAST),
            ChoiceCardSpin.of(4, ChoiceSpinSpeed.FASTEST));

    private CurioChoice() {
    }

    public static void open(ServerPlayer player, List<ItemStack> options, Consumer<ItemStack> callback) {
        open(player, TITLE_KEY, HINT_KEY, options, callback);
    }

    public static void open(ServerPlayer player, String titleKey, String hintKey, List<ItemStack> options, Consumer<ItemStack> callback) {
        open(player, Component.translatable(titleKey), Component.translatable(hintKey), options, callback);
    }

    public static void open(ServerPlayer player, Component title, Component hint, List<ItemStack> options, Consumer<ItemStack> callback) {
        var cards = new ArrayList<ChoiceCard>();
        for (var option : options) cards.add(card(option));
        ChoiceRegistry.open(player, title, hint, ChoiceVisual.ITEM_MODEL, ChoiceSupport.BACKGROUND, ChoiceSupport.CURIO_CARDS, ChoiceSupport.spin(LADDER.get(BASE_TIER)), cards, callback == null ? null : index -> {
            if (index >= 0 && index < options.size()) callback.accept(options.get(index));
            return true;
        });
    }

    private static ChoiceCard card(ItemStack option) {
        var form = Curios.formOf(option);
        var grade = CurioGrade.of(option);
        var tier = tierOf(form, grade);
        return presentation(option, form, grade, tier).withSpin(ladder(tier));
    }

    static ChoiceCard presentation(ItemStack option) {
        var form = Curios.formOf(option);
        var grade = CurioGrade.of(option);
        return presentation(option, form, grade, tierOf(form, grade));
    }

    private static ChoiceCard presentation(ItemStack option, CurioForm form, CurioGrade grade, int tier) {
        return ChoiceSupport.card(option).withIcons(stars(form, grade)).withOverlay(level(form, grade, tier)).withQuality(tier);
    }

    private static ChoiceCardSpin ladder(int tier) {
        return LADDER.get(Math.min(tier, LADDER.size() - 1));
    }

    private static ChoiceCardIcons stars(CurioForm form, CurioGrade grade) {
        if (form == null || grade == null) return null;
        return ChoiceCardIcons.of(form == CurioForm.FUSION ? FUSION_STAR : STAR, grade.stars());
    }

    private static ChoiceCardOverlay level(CurioForm form, CurioGrade grade, int tier) {
        if (form == CurioForm.NEGATIVE) return ChoiceSupport.level(NEGATIVE_COLUMN);
        if (form == null || grade == null) return null;
        return ChoiceSupport.level(tier);
    }

    private static int tierOf(CurioForm form, CurioGrade grade) {
        if (form == null || grade == null) return BASE_TIER;
        return grade.tier(form);
    }
}
