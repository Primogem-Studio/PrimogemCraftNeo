package net.per.primogemcraft.system.event;

import net.minecraft.text.Text;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.item.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public interface EventCondition {
    String FRAGMENTS_KEY = "gui.primogemcraft.event.requirement.fragments";
    String AFTERGLOW_KEY = "gui.primogemcraft.event.requirement.afterglow";
    String HEALTH_KEY = "gui.primogemcraft.event.requirement.health";
    String ENCHANT_TARGETS_KEY = "gui.primogemcraft.event.requirement.enchant_targets";
    String CURIOS_KEY = "gui.primogemcraft.event.requirement.curios";
    String CHAIN_KEY = "gui.primogemcraft.event.requirement.chain";

    boolean met(EventContext context);

    List<Text> unmet(EventContext context);

    static EventCondition all(EventCondition... conditions) {
        return new All(List.of(conditions));
    }

    static EventCondition fragments(int amount) {
        return new Requirement(Text.translatable(FRAGMENTS_KEY, amount), context -> context.hasFragments(amount));
    }

    static EventCondition afterglow(int amount) {
        return new Requirement(Text.translatable(AFTERGLOW_KEY, amount), context -> context.hasAfterglow(amount));
    }

    static EventCondition health(double ratio) {
        return new Requirement(Text.translatable(HEALTH_KEY, Math.round(ratio * 100.0D)), context -> context.hasHealth(ratio));
    }

    static EventCondition enchantTargets() {
        return new Requirement(Text.translatable(ENCHANT_TARGETS_KEY), EventContext::hasEnchantTargets);
    }

    static EventCondition curios(TagKey<Item> tag) {
        return new Requirement(Text.translatable(CURIOS_KEY, tag.id().toString()), context -> context.hasCurios(tag));
    }

    static EventCondition chain() {
        return new Requirement(Text.translatable(CHAIN_KEY), EventContext::chainReady);
    }

    record Requirement(Text label, Predicate<EventContext> check) implements EventCondition {
        @Override
        public boolean met(EventContext context) {
            return check.test(context);
        }

        @Override
        public List<Text> unmet(EventContext context) {
            return met(context) ? List.of() : List.of(label);
        }
    }

    record All(List<EventCondition> conditions) implements EventCondition {
        @Override
        public boolean met(EventContext context) {
            for (var condition : conditions) {
                if (!condition.met(context)) return false;
            }
            return true;
        }

        @Override
        public List<Text> unmet(EventContext context) {
            var lines = new ArrayList<Text>();
            for (var condition : conditions) lines.addAll(condition.unmet(context));
            return List.copyOf(lines);
        }
    }
}
