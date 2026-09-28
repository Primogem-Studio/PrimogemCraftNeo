package net.per.primogemcraft.system.weapon;

import net.minecraft.network.chat.Component;
import net.per.primogemcraft.system.wish.WishTooltips;

import java.util.ArrayList;
import java.util.List;

public final class WishWeaponTooltips {
    private static final String DETAILS_KEY = "weapon.primogemcraft.hint.details";
    private static final String TUTORIAL_KEY = "weapon.primogemcraft.hint.tutorial";

    private WishWeaponTooltips() {
    }

    public static List<Component> lines(String descriptionPrefix, List<WeaponDescription> descriptions) {
        return lines(descriptionPrefix, descriptions, List.of());
    }

    public static List<Component> lines(String descriptionPrefix, List<WeaponDescription> descriptions, List<Component> passiveLines) {
        var tooltip = new ArrayList<Component>();
        if (WishTooltips.showsDetails()) {
            for (var effect : descriptions) tooltip.addAll(effect.lines(descriptionPrefix));
            tooltip.addAll(passiveLines);
        } else {
            tooltip.add(Component.translatable(DETAILS_KEY));
        }
        if (!WishTooltips.showsTutorial()) tooltip.add(Component.translatable(TUTORIAL_KEY));
        return tooltip;
    }
}
