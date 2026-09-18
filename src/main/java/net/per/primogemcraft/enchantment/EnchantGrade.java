package net.per.primogemcraft.enchantment;

import net.minecraft.util.RandomSource;

import java.util.Locale;

public enum EnchantGrade {
    LOW(1, 10, 0),
    MEDIUM(11, 20, 1),
    HIGH(21, 30, 2),
    SPECIAL(31, 40, 3);

    private static final String KEY_PREFIX = "gui.primogemcraft.enchant_choice.grade.";

    private final int minLevel;
    private final int maxLevel;
    private final int column;

    EnchantGrade(int minLevel, int maxLevel, int column) {
        this.minLevel = minLevel;
        this.maxLevel = maxLevel;
        this.column = column;
    }

    public static EnchantGrade of(int level) {
        for (var grade : values()) if (level <= grade.maxLevel) return grade;
        return SPECIAL;
    }

    public int minLevel() {
        return minLevel;
    }

    public int column() {
        return column;
    }

    public int rollLevel(RandomSource random) {
        return minLevel + random.nextInt(maxLevel - minLevel + 1);
    }

    public String translationKey() {
        return KEY_PREFIX + name().toLowerCase(Locale.ROOT);
    }
}
