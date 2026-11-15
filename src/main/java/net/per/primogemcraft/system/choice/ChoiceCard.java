package net.per.primogemcraft.system.choice;

import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.minecraft.item.ItemStack;

import java.util.List;

public record ChoiceCard(Text title, ItemStack item, Text description, Text footnote,
                         ChoiceCardSpin spin, ChoiceCardIcons icons, ChoiceCardOverlay overlay, boolean textTooltip,
                         boolean itemTooltip, boolean enabled, int quality, Text badge, List<Text> unmet) {
    public static final int AUTO_QUALITY = -1;
    public static final int SILENT_QUALITY = 0;

    private static final int MAX_QUALITY = 3;
    private static final float PITCH_STEP = 0.15F;
    private static final float PITCH_NORMAL = 1.0F;

    public static ChoiceCard of(ItemStack item) {
        return new ChoiceCard(Text.empty(), item, Text.empty(), Text.empty(), null, null, null, false, false, true, AUTO_QUALITY, Text.empty(), List.of());
    }

    public ChoiceCard withSpin(ChoiceCardSpin spin) {
        return new ChoiceCard(title, item, description, footnote, spin, icons, overlay, textTooltip, itemTooltip, enabled, quality, badge, unmet);
    }

    public ChoiceCard withIcons(ChoiceCardIcons icons) {
        return new ChoiceCard(title, item, description, footnote, spin, icons, overlay, textTooltip, itemTooltip, enabled, quality, badge, unmet);
    }

    public ChoiceCard withOverlay(ChoiceCardOverlay overlay) {
        return new ChoiceCard(title, item, description, footnote, spin, icons, overlay, textTooltip, itemTooltip, enabled, quality, badge, unmet);
    }

    public ChoiceCard withTextTooltip() {
        return new ChoiceCard(title, item, description, footnote, spin, icons, overlay, true, itemTooltip, enabled, quality, badge, unmet);
    }

    public ChoiceCard withItemTooltip() {
        return new ChoiceCard(title, item, description, footnote, spin, icons, overlay, textTooltip, true, enabled, quality, badge, unmet);
    }

    public ChoiceCard withEnabled(boolean enabled) {
        return new ChoiceCard(title, item, description, footnote, spin, icons, overlay, textTooltip, itemTooltip, enabled, quality, badge, unmet);
    }

    public ChoiceCard withQuality(int quality) {
        return new ChoiceCard(title, item, description, footnote, spin, icons, overlay, textTooltip, itemTooltip, enabled, quality, badge, unmet);
    }

    public ChoiceCard withBadge(Text badge) {
        return new ChoiceCard(title, item, description, footnote, spin, icons, overlay, textTooltip, itemTooltip, enabled, quality, badge, unmet);
    }

    public ChoiceCard withUnmet(List<Text> unmet) {
        return new ChoiceCard(title, item, description, footnote, spin, icons, overlay, textTooltip, itemTooltip, enabled, quality, badge, unmet);
    }

    public Text displayTitle() {
        return title.getString().isEmpty() ? rarityName() : title;
    }

    private Text rarityName() {
        return Text.empty().append(item.getName()).formatted(item.getRarity().formatting);
    }

    public boolean hasDescription() {
        return !description.getString().isEmpty();
    }

    public boolean hasFootnote() {
        return !footnote.getString().isEmpty();
    }

    public boolean hasBadge() {
        return !badge.getString().isEmpty();
    }

    public int revealQuality() {
        var level = quality >= 0 ? quality : item.isEmpty() ? 0 : item.getRarity().ordinal();
        return MathHelper.clamp(level, 0, MAX_QUALITY);
    }

    public float revealPitch() {
        return PITCH_NORMAL + PITCH_STEP * (MAX_QUALITY - revealQuality());
    }
}
