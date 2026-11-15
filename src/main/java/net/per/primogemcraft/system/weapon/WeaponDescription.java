package net.per.primogemcraft.system.weapon;

import net.minecraft.util.Formatting;
import net.minecraft.util.Language;
import net.minecraft.text.Text;

import java.util.List;

/**
 * One effect of a wish weapon, rendered as an action label line plus a text line.
 * The label comes from {@code weapon.primogemcraft.action.&lt;action&gt;} so weapons never repeat it;
 * the text comes from {@code item.primogemcraft.&lt;id&gt;.description.&lt;text&gt;} and fills its {@code %s}
 * placeholders with {@code values}. An empty text renders the label alone, and {@link #note} renders the text alone
 * with no label, for the lines the old project printed without one.
 * Both lines open at the same column on purpose: a leading indent made of spaces cannot survive the
 * tooltip wrapper, which breaks an overlong line at its last space and would print the indent as a blank line.
 * A literal percent sign in the text has to be written {@code %%}: a bare one fails the template parse and makes the
 * whole line render raw, placeholders included. The text line carries the yellow body colour, so an escaped sign and
 * every segment that follows a placeholder or an escaped sign keep it.
 * The colon after the label comes from {@code weapon.primogemcraft.action.suffix}; an action whose label uses another
 * colour, such as {@code negative_passive}, overrides it through
 * {@code weapon.primogemcraft.action.&lt;action&gt;.suffix} so the colon matches the label it follows.
 */
public record WeaponDescription(String action, String text, List<Text> values) {
    private static final String ACTION_PREFIX = "weapon.primogemcraft.action.";
    private static final String ACTION_SUFFIX_KEY = ACTION_PREFIX + "suffix";
    private static final String SUFFIX_SUFFIX = ".suffix";
    private static final String DESCRIPTION_PREFIX = ".description.";

    public static WeaponDescription of(String action, String text, Text... values) {
        return new WeaponDescription(action, text, List.of(values));
    }

    public static WeaponDescription note(String text, Text... values) {
        return new WeaponDescription("", text, List.of(values));
    }

    public List<Text> lines(String itemPrefix) {
        var body = text.isEmpty()
                ? null
                : Text.translatable(itemPrefix + DESCRIPTION_PREFIX + this.text, values.toArray(Object[]::new)).formatted(Formatting.YELLOW);
        if (action.isEmpty()) return body == null ? List.of() : List.of(body);
        var action = Text.translatable(ACTION_PREFIX + this.action).append(suffix());
        return body == null ? List.of(action) : List.of(action, body);
    }

    private Text suffix() {
        var key = ACTION_PREFIX + action + SUFFIX_SUFFIX;
        return Text.translatable(Language.getInstance().hasTranslation(key) ? key : ACTION_SUFFIX_KEY);
    }
}
