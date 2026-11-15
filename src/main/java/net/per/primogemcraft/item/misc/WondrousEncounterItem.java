package net.per.primogemcraft.item.misc;

import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
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
    private static final Identifier LOOT = Identifier.of(MOD_ID, "encounter/wondrous_encounter");
    private static final int CARDS = 3;
    private static final int ROLL_ATTEMPTS = 12;
    private static final String TITLE_KEY = "gui.primogemcraft.wondrous_encounter.title";
    private static final String HINT_KEY = "gui.primogemcraft.wondrous_encounter.hint";

    public WondrousEncounterItem(Settings properties) {
        super(properties);
    }

    @Override
    public TypedActionResult<ItemStack> use(World level, PlayerEntity player, Hand hand) {
        var stack = player.getStackInHand(hand);
        if (!(level instanceof ServerWorld serverLevel) || !(player instanceof ServerPlayerEntity serverPlayer))
            return TypedActionResult.success(stack);
        var options = roll(serverLevel);
        if (options.isEmpty()) return TypedActionResult.fail(stack);
        stack.decrement(1);
        open(serverPlayer, options);
        return TypedActionResult.success(stack);
    }

    private List<ItemStack> roll(ServerWorld level) {
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
        for (var option : options) if (option.isOf(candidate.getItem())) return true;
        return false;
    }

    private void open(ServerPlayerEntity player, List<ItemStack> options) {
        var cards = new ArrayList<ChoiceCard>();
        for (var option : options) cards.add(ChoiceSupport.card(option));
        ChoiceRegistry.open(player, Text.translatable(TITLE_KEY), Text.translatable(HINT_KEY), ChoiceVisual.ITEM_MODEL,
                ChoiceSupport.BACKGROUND, ChoiceSupport.DEFAULT_CARDS, ChoiceSupport.spin(ChoiceSpinSpeed.FASTEST), cards, index -> {
                    if (index >= 0 && index < options.size()) Curios.give(player, options.get(index).copy());
                    return true;
                });
    }
}
