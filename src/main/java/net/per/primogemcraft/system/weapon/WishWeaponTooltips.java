package net.per.primogemcraft.system.weapon;

import net.minecraft.text.Text;
import net.per.primogemcraft.config.PGCConfig;
import net.per.primogemcraft.system.wish.WishTooltips;

import java.util.ArrayList;
import java.util.List;

public final class WishWeaponTooltips {
    private static final String DETAILS_KEY = "weapon.primogemcraft.hint.details";
    private static final String TUTORIAL_KEY = "weapon.primogemcraft.hint.tutorial";
    private static final String TUTORIAL_LINE_PREFIX = "weapon.primogemcraft.tutorial.";
    private static final int TUTORIAL_LINES = 8;
    private static final int ORE_LINE = 2;
    private static final int REFINEMENT_CAP_LINE = 5;

    private WishWeaponTooltips() {
    }

    public static List<Text> lines(String descriptionPrefix, List<WeaponDescription> descriptions) {
        var tooltip = new ArrayList<Text>();
        if (WishTooltips.showsDetails()) {
            for (var effect : descriptions) tooltip.addAll(effect.lines(descriptionPrefix));
        } else {
            tooltip.add(Text.translatable(DETAILS_KEY));
        }
        tooltip.addAll(hintLines());
        return tooltip;
    }

    private static List<Text> hintLines() {
        if (!WishTooltips.showsTutorial()) return List.of(Text.translatable(TUTORIAL_KEY));
        var lines = new ArrayList<Text>(TUTORIAL_LINES);
        for (var index = 0; index < TUTORIAL_LINES; index++) lines.add(tutorialLine(index));
        return lines;
    }

    private static Text tutorialLine(int index) {
        var key = TUTORIAL_LINE_PREFIX + index;
        return switch (index) {
            case ORE_LINE -> Text.translatable(key, PGCConfig.ENHANCEMENT_ORE_XP.get());
            case ORE_LINE + 1 -> Text.translatable(key, PGCConfig.FINE_ENHANCEMENT_ORE_XP.get());
            case ORE_LINE + 2 -> Text.translatable(key, PGCConfig.MYSTIC_ENHANCEMENT_ORE_XP.get());
            case REFINEMENT_CAP_LINE -> Text.translatable(key, PGCConfig.WEAPON_MAX_LEVEL_BONUS.get());
            default -> Text.translatable(key);
        };
    }
}
