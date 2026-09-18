package net.per.primogemcraft.system.choice;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public final class ChoiceSupport {
    public static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/gui/choice_background.png");
    private static final ResourceLocation DEFAULT_SHEET = ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/gui/choice_card.png");
    private static final ResourceLocation DEFAULT_BACK = ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/gui/choice_card_back.png");
    private static final ResourceLocation CURIO_SHEET = ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/gui/curio_card.png");
    private static final ResourceLocation CURIO_BACK = ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/gui/curio_card_back.png");
    private static final ResourceLocation ENCHANT_SHEET = ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/gui/enchant_card.png");
    private static final ResourceLocation ENCHANT_BACK = ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/gui/enchant_card_back.png");
    private static final ResourceLocation EVENT_SHEET = ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/gui/event_card.png");
    private static final ResourceLocation EVENT_BACK = ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/gui/event_card_back.png");
    private static final ResourceLocation LEVEL_SHEET = ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/gui/choice_card_level.png");
    private static final int LEVEL_COLUMNS = 5;
    private static final int DEFAULT_COLUMNS = 4;
    private static final int DEFAULT_TILE_WIDTH = 16;
    private static final int DEFAULT_TILE_HEIGHT = 32;
    private static final int COLUMN_IDLE = 0;
    private static final int COLUMN_HOVERED = 1;
    private static final int COLUMN_CHOSEN = 2;
    private static final int COLUMN_DIMMED = 3;
    public static final ChoiceCardTextures DEFAULT_CARDS = new ChoiceCardTextures(DEFAULT_SHEET, DEFAULT_COLUMNS, DEFAULT_TILE_WIDTH, DEFAULT_TILE_HEIGHT, COLUMN_IDLE, COLUMN_HOVERED, COLUMN_CHOSEN, COLUMN_DIMMED, DEFAULT_BACK);
    public static final ChoiceCardTextures CURIO_CARDS = new ChoiceCardTextures(CURIO_SHEET, DEFAULT_COLUMNS, DEFAULT_TILE_WIDTH, DEFAULT_TILE_HEIGHT, COLUMN_IDLE, COLUMN_HOVERED, COLUMN_CHOSEN, COLUMN_DIMMED, CURIO_BACK);
    public static final ChoiceCardTextures ENCHANT_CARDS = new ChoiceCardTextures(ENCHANT_SHEET, DEFAULT_COLUMNS, DEFAULT_TILE_WIDTH, DEFAULT_TILE_HEIGHT, COLUMN_IDLE, COLUMN_HOVERED, COLUMN_CHOSEN, COLUMN_DIMMED, ENCHANT_BACK);
    public static final ChoiceCardTextures EVENT_CARDS = new ChoiceCardTextures(EVENT_SHEET, DEFAULT_COLUMNS, DEFAULT_TILE_WIDTH, DEFAULT_TILE_HEIGHT, COLUMN_IDLE, COLUMN_HOVERED, COLUMN_CHOSEN, COLUMN_DIMMED, EVENT_BACK);

    private ChoiceSupport() {
    }

    public static ResourceLocation texture(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    public static ChoiceCard card(ItemStack item) {
        return ChoiceCard.of(item);
    }

    public static ChoiceCard card(Component title, ItemStack item, Component description) {
        return new ChoiceCard(title, item, description, Component.empty(), null, null, null, false, false, true, ChoiceCard.AUTO_QUALITY, Component.empty(), List.of());
    }

    public static ChoiceCard card(Component title, ItemStack item, Component description, Component footnote) {
        return new ChoiceCard(title, item, description, footnote, null, null, null, false, false, true, ChoiceCard.AUTO_QUALITY, Component.empty(), List.of());
    }

    public static ChoiceSpin spin(ChoiceSpinSpeed speed) {
        return ChoiceSpin.of(speed);
    }

    public static ChoiceSpin spin(int turns, ChoiceSpinSpeed speed) {
        return ChoiceSpin.of(turns, speed);
    }

    public static ChoiceSpin spin(ChoiceCardSpin cardSpin) {
        return ChoiceSpin.of(cardSpin.turns(), cardSpin.speed());
    }

    public static ChoiceCardOverlay level(int column) {
        return ChoiceCardOverlay.of(LEVEL_SHEET, LEVEL_COLUMNS, column);
    }

    public static List<ChoiceCard> limit(List<ChoiceCard> cards, int count) {
        return List.copyOf(cards.subList(0, Math.min(count, cards.size())));
    }

    public static List<ItemStack> limitItems(List<ItemStack> stacks, int count) {
        return List.copyOf(stacks.subList(0, Math.min(count, stacks.size())));
    }

    public static List<ItemStack> distinct(List<ItemStack> rolled, ChoiceRoll source, int count, int attempts) {
        var collected = new ArrayList<ItemStack>();
        collect(collected, rolled, count);
        for (var attempt = 0; attempt < attempts && collected.size() < count; attempt++) {
            collect(collected, source.roll(), count);
        }
        return List.copyOf(collected);
    }

    private static void collect(List<ItemStack> collected, List<ItemStack> candidates, int count) {
        for (var stack : candidates) {
            if (collected.size() >= count) return;
            if (!stack.isEmpty() && !contains(collected, stack)) collected.add(stack);
        }
    }

    private static boolean contains(List<ItemStack> collected, ItemStack candidate) {
        for (var stack : collected) {
            if (ItemStack.isSameItemSameComponents(stack, candidate)) return true;
        }
        return false;
    }
}
