package net.per.primogemcraft.item.misc;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.per.primogemcraft.item.tool.DescribedItem;
import net.per.primogemcraft.system.choice.ChoiceCard;
import net.per.primogemcraft.system.choice.ChoiceRegistry;
import net.per.primogemcraft.system.choice.ChoiceSpinSpeed;
import net.per.primogemcraft.system.choice.ChoiceSupport;
import net.per.primogemcraft.system.choice.ChoiceVisual;
import net.per.primogemcraft.system.curio.Curios;
import net.per.primogemcraft.system.event.EventLoot;

import java.util.ArrayList;
import java.util.List;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public class WondrousEncounterItem extends DescribedItem {
    private static final ResourceLocation LOOT = ResourceLocation.fromNamespaceAndPath(MOD_ID, "encounter/wondrous_encounter");
    private static final int CARDS = 3;
    private static final int ROLL_ATTEMPTS = 12;
    private static final String TITLE_KEY = "gui.primogemcraft.wondrous_encounter.title";
    private static final String HINT_KEY = "gui.primogemcraft.wondrous_encounter.hint";

    public WondrousEncounterItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        if (!(level instanceof ServerLevel serverLevel) || !(player instanceof ServerPlayer serverPlayer))
            return InteractionResultHolder.success(stack);
        var options = roll(serverLevel);
        if (options.isEmpty()) return InteractionResultHolder.fail(stack);
        stack.shrink(1);
        open(serverPlayer, options);
        return InteractionResultHolder.success(stack);
    }

    private List<ItemStack> roll(ServerLevel level) {
        var options = new ArrayList<ItemStack>();
        for (var attempt = 0; attempt < ROLL_ATTEMPTS * CARDS && options.size() < CARDS; attempt++) {
            for (var candidate : EventLoot.roll(level, LOOT)) {
                if (options.size() >= CARDS) break;
                if (candidate.isEmpty() || holds(options, candidate)) continue;
                options.add(candidate);
            }
        }
        return List.copyOf(options);
    }

    private boolean holds(List<ItemStack> options, ItemStack candidate) {
        for (var option : options) if (option.is(candidate.getItem())) return true;
        return false;
    }

    private void open(ServerPlayer player, List<ItemStack> options) {
        var cards = new ArrayList<ChoiceCard>();
        for (var option : options) cards.add(ChoiceSupport.card(option));
        ChoiceRegistry.open(player, Component.translatable(TITLE_KEY), Component.translatable(HINT_KEY), ChoiceVisual.ITEM_MODEL,
                ChoiceSupport.BACKGROUND, ChoiceSupport.DEFAULT_CARDS, ChoiceSupport.spin(ChoiceSpinSpeed.FASTEST), cards, index -> {
                    if (index >= 0 && index < options.size()) Curios.give(player, options.get(index).copy());
                    return true;
                });
    }
}
