package net.per.primogemcraft.system.event;

import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.per.primogemcraft.system.choice.*;

import java.util.ArrayList;

public final class EventChoice {
    public static final String HINT_KEY = "gui.primogemcraft.event.hint";
    public static final String GROUP_ID_KEY = "gui.primogemcraft.event.group_id";
    public static final String CARD_ID_KEY = "gui.primogemcraft.event.card_id";
    public static final Identifier ICON = ChoiceSupport.texture("textures/gui/event_question.png");

    private EventChoice() {
    }

    public static void open(ServerPlayerEntity player, EventGroup group) {
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

    private static Text subtitle(EventGroup group) {
        return Text.empty()
                .append(Text.translatable(GROUP_ID_KEY, group.number()))
                .append(Text.translatable(HINT_KEY));
    }

    private static ChoiceCard card(RandomEvent event, ServerPlayerEntity player) {
        var context = EventContext.of(player, event);
        return ChoiceSupport.card(event.title(), ItemStack.EMPTY, event.description())
                .withTextTooltip()
                .withBadge(Text.translatable(CARD_ID_KEY, event.number()))
                .withUnmet(event.unmet(context))
                .withEnabled(event.available(context));
    }
}
