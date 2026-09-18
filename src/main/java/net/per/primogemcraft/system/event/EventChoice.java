package net.per.primogemcraft.system.event;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.per.primogemcraft.system.choice.*;

import java.util.ArrayList;

public final class EventChoice {
    public static final String HINT_KEY = "gui.primogemcraft.event.hint";
    public static final String GROUP_ID_KEY = "gui.primogemcraft.event.group_id";
    public static final String CARD_ID_KEY = "gui.primogemcraft.event.card_id";
    public static final ResourceLocation ICON = ChoiceSupport.texture("textures/gui/event_question.png");

    private EventChoice() {
    }

    public static void open(ServerPlayer player, EventGroup group) {
        var numbers = new ArrayList<Integer>();
        var cards = new ArrayList<ChoiceCard>();
        var allConditional = true;
        var forced = false;
        for (var number : group.events()) {
            var event = EventRegistry.event(number);
            if (event == null) continue;
            numbers.add(number);
            cards.add(card(event, player));
            if (!event.conditional()) allConditional = false;
            if (event.forced()) forced = true;
        }
        if (cards.isEmpty()) return;
        var leaveIndex = ChoiceRequest.NO_LEAVE;
        if (allConditional && !forced) {
            var exit = RandomEvents.leaveEvent();
            leaveIndex = cards.size();
            numbers.add(exit.number());
            cards.add(card(exit, player));
        }
        ChoiceRegistry.open(player, group.title(), subtitle(group), ChoiceVisual.SCENARIO_TEXTURE, ICON, ChoiceSupport.EVENT_CARDS, ChoiceSupport.spin(ChoiceSpinSpeed.FASTEST), ChoiceRegistry.DEFAULT_SETTLE_TICKS, ChoiceMode.CONFIRM, leaveIndex, cards, index -> {
            if (index < 0 || index >= numbers.size()) return true;
            return EventRegistry.run(player, numbers.get(index));
        });
    }

    private static Component subtitle(EventGroup group) {
        return Component.empty()
                .append(Component.translatable(GROUP_ID_KEY, group.number()))
                .append(Component.translatable(HINT_KEY));
    }

    private static ChoiceCard card(RandomEvent event, ServerPlayer player) {
        var context = EventContext.of(player, event);
        return ChoiceSupport.card(event.title(), ItemStack.EMPTY, event.description())
                .withTextTooltip()
                .withBadge(Component.translatable(CARD_ID_KEY, event.number()))
                .withUnmet(event.unmet(context))
                .withEnabled(event.available(context));
    }
}
