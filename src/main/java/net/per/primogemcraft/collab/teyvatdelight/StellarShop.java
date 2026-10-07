package net.per.primogemcraft.collab.teyvatdelight;

import com.guoche.teyvatdelight.entity.katheryne.KatheryneData;
import com.guoche.teyvatdelight.KatheryneMenu;
import com.guoche.teyvatdelight.entity.katheryne.KatheryneRules;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.per.primogemcraft.enchantment.EnchantChoice;
import net.per.primogemcraft.enchantment.EnchantGrade;
import net.per.primogemcraft.system.choice.ChoiceRegistry;
import net.per.primogemcraft.system.curio.CurioChoice;
import net.per.primogemcraft.system.curio.CurioForm;
import net.per.primogemcraft.system.curio.CurioGrade;
import net.per.primogemcraft.system.curio.Curios;
import net.per.primogemcraft.system.event.EventChoice;
import net.per.primogemcraft.system.event.EventGroup;
import net.per.primogemcraft.system.event.EventRegistry;
import net.per.primogemcraft.util.PlayerItems;
import net.per.primogemcraft.registry.PGCItems;

import java.util.ArrayList;
import java.util.List;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

final class StellarShop {
    private static final int[] PRICES = {2, 6, 9, 2, 4, 4, 4, 2, 4};
    private static final String STATE_KEY = MOD_ID + ":stellar_shop";
    private static final String TEXT = "gui.primogemcraft.stellar_shop.";

    private StellarShop() {
    }

    static void handle(ServerPlayer player, StellarShopNetwork.Action action) {
        if (!(player.containerMenu instanceof KatheryneMenu menu) || menu.containerId != action.containerId()
                || !menu.stillValid(player) || !player.isAlive() || ChoiceRegistry.isPending(player)) return;
        var slot = action.slot();
        if (slot == -1) {
            sync(player, menu);
            return;
        }
        if (slot >= 7 && slot < 11) {
            exchange(player, menu, slot - 7, action);
            return;
        }
        if (slot >= StellarShopNetwork.TASK_FIRST_SLOT && slot < StellarShopNetwork.TASK_FIRST_SLOT + StellarTaskPlan.MAX_COUNT) {
            StellarTasks.submit(player, action);
            if (player.containerMenu == menu) sync(player, menu);
            return;
        }
        if (slot >= StellarShopNetwork.ENCHANT_FIRST_SLOT && slot < StellarShopNetwork.ENCHANT_FIRST_SLOT + 2)
            slot = 7 + slot - StellarShopNetwork.ENCHANT_FIRST_SLOT;
        else if (slot >= 7) return;
        if (slot < 0 || slot >= PRICES.length) return;
        if (action.shopCycle() != KatheryneData.shopCycle(player.server) || action.day() != day(player)) {
            fail(player, menu, "refreshed");
            return;
        }
        if (remaining(player, slot) <= 0) {
            fail(player, menu, "sold_out");
            return;
        }
        var currency = currency();
        if (PlayerItems.count(player, currency) < PRICES[slot]) {
            fail(player, menu, "not_enough");
            return;
        }
        if (slot >= 7) {
            if (!EnchantChoice.hasTargets(player)) {
                fail(player, menu, "unavailable");
                return;
            }
            player.closeContainer();
            if (!EnchantChoice.open(player, slot == 7 ? EnchantGrade.LOW : EnchantGrade.MEDIUM, 1, 3, 1)) return;
            PlayerItems.take(player, currency, PRICES[slot]);
            var state = state(player);
            state.putInt("bought_" + slot, state.getInt("bought_" + slot) + 1);
            player.getInventory().setChanged();
            player.containerMenu.broadcastChanges();
            return;
        }
        var options = slot < 3 ? rollCurios(player, slot) : List.<ItemStack>of();
        var group = slot >= 3 ? purchaseGroup(player, slot) : null;
        if (slot < 3 ? options.size() < 3 : group == null || group.isEmpty()) {
            fail(player, menu, "unavailable");
            return;
        }
        PlayerItems.take(player, currency, PRICES[slot]);
        var state = purchaseState(player, slot);
        state.putInt("bought_" + slot, state.getInt("bought_" + slot) + 1);
        player.getInventory().setChanged();
        player.closeContainer();
        if (slot < 3) {
            var fallback = options.get(player.getRandom().nextInt(options.size()));
            CurioChoice.open(player, Component.translatable(TEXT + "title"),
                    Component.translatable("gui.primogemcraft.curio_choice.hint"), options,
                    chosen -> Curios.give(player, chosen), () -> Curios.give(player, fallback));
        } else EventChoice.open(player, group);
    }

    private static void sync(ServerPlayer player, KatheryneMenu menu) {
        var groups = dailyGroups(player);
        var offers = new ArrayList<StellarShopNetwork.Offer>();
        for (var slot = 0; slot < PRICES.length; slot++) {
            var title = slot < 3 ? Component.translatable(TEXT + "curio", CurioGrade.values()[slot].name())
                    : slot >= 7 ? Component.translatable((slot == 7 ? EnchantGrade.LOW : EnchantGrade.MEDIUM).translationKey())
                    : slot == 3 ? Component.translatable(TEXT + "random_event")
                    : slot - 4 < groups.size() ? groups.get(slot - 4).title() : Component.translatable(TEXT + "unavailable");
            var available = slot < 3 ? curioPool(slot).size() >= 3
                    : slot >= 7 ? EnchantChoice.hasTargets(player)
                    : slot == 3 ? !EventRegistry.events().isEmpty() : slot - 4 < groups.size();
            offers.add(new StellarShopNetwork.Offer(title, PRICES[slot], remaining(player, slot), available));
        }
        PacketDistributor.sendToPlayer(player, new StellarShopNetwork.Snapshot(menu.containerId,
                KatheryneData.shopCycle(player.server), day(player), PlayerItems.count(player, currency()), offers,
                new StellarShopNetwork.Details(KatheryneRules.secondsUntil(player.server, KatheryneRules.shopRefreshTime()),
                        KatheryneRules.secondsUntil(player.server, 0), CurrencyExchange.revision(), exchanges(player), StellarTasks.snapshot(player))));
    }

    private static List<StellarShopNetwork.Exchange> exchanges(ServerPlayer player) {
        var result = new ArrayList<StellarShopNetwork.Exchange>();
        for (var index = 0; index < CurrencyExchange.trades().size(); index++) {
            var trade = CurrencyExchange.trades().get(index);
            var remaining = exchangeRemaining(player, index);
            result.add(new StellarShopNetwork.Exchange(trade.cost(), trade.reward(), remaining,
                    trade.enabled() && remaining != 0 && PlayerItems.count(player, exchangeItem(index, false)) >= trade.cost()));
        }
        return result;
    }

    private static String exchangeKey(int index) {
        return index == 0 ? "exchange_primogem" : index == 2 ? "exchange_mora" : "exchange_" + index;
    }

    private static int exchangeRemaining(ServerPlayer player, int index) {
        var trade = CurrencyExchange.trades().get(index);
        if (!trade.enabled()) return 0;
        return trade.limit() < 0 ? -1 : Math.max(0, trade.limit() - state(player).getInt(exchangeKey(index)));
    }

    private static Item exchangeItem(int index, boolean output) {
        var craft = (index % 2 == 0) != output;
        if (craft) return index < 2 ? PGCItems.PRIMOGEM.get() : PGCItems.MORA.get();
        return BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("teyvatdelight", index < 2 ? "primogem" : "mora"));
    }

    private static void exchange(ServerPlayer player, KatheryneMenu menu, int index, StellarShopNetwork.Action action) {
        if (index >= CurrencyExchange.trades().size() || action.shopCycle() != CurrencyExchange.revision() || action.day() != day(player)) {
            fail(player, menu, "refreshed");
            return;
        }
        var trade = CurrencyExchange.trades().get(index);
        if (exchangeRemaining(player, index) == 0) {
            fail(player, menu, "sold_out");
            return;
        }
        var input = exchangeItem(index, false);
        if (PlayerItems.count(player, input) < trade.cost()) {
            fail(player, menu, "exchange_short");
            return;
        }
        PlayerItems.take(player, input, trade.cost());
        if (trade.limit() >= 0) {
            var state = state(player);
            var key = exchangeKey(index);
            state.putInt(key, state.getInt(key) + 1);
        }
        PlayerItems.give(player, new ItemStack(exchangeItem(index, true), trade.reward()));
        player.getInventory().setChanged();
        menu.broadcastChanges();
        sync(player, menu);
    }

    private static void fail(ServerPlayer player, KatheryneMenu menu, String message) {
        player.displayClientMessage(Component.translatable(TEXT + message), false);
        sync(player, menu);
    }

    private static Item currency() {
        return BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("teyvatdelight", "primogem"));
    }

    private static int remaining(ServerPlayer player, int slot) {
        return Math.max(0, (slot == 3 ? 2 : 1) - purchaseState(player, slot).getInt("bought_" + slot));
    }

    private static CompoundTag purchaseState(ServerPlayer player, int slot) {
        return slot < 3 ? state(player, STATE_KEY + "/curios", "cycle", KatheryneData.shopCycle(player.server)) : state(player);
    }

    static long day(ServerPlayer player) {
        return Math.floorDiv(player.server.overworld().getDayTime(), 24000L);
    }

    static CompoundTag state(ServerPlayer player) {
        return state(player, STATE_KEY, "day", day(player));
    }

    private static CompoundTag state(ServerPlayer player, String key, String period, long cycle) {
        var data = player.getPersistentData();
        if (!data.contains(Player.PERSISTED_NBT_TAG)) data.put(Player.PERSISTED_NBT_TAG, new CompoundTag());
        var persisted = data.getCompound(Player.PERSISTED_NBT_TAG);
        var state = persisted.getCompound(key);
        if (!state.contains(period) || state.getLong(period) != cycle) {
            state = new CompoundTag();
            state.putLong(period, cycle);
            persisted.put(key, state);
        }
        return state;
    }

    private static List<Item> curioPool(int slot) {
        var pool = new ArrayList<Item>();
        BuiltInRegistries.ITEM.getTag(CurioGrade.values()[slot].tag(CurioForm.NORMAL))
                .ifPresent(tag -> tag.forEach(holder -> pool.add(holder.value())));
        return pool;
    }

    private static List<ItemStack> rollCurios(ServerPlayer player, int slot) {
        var pool = curioPool(slot);
        var result = new ArrayList<ItemStack>();
        while (result.size() < 3 && !pool.isEmpty())
            result.add(new ItemStack(pool.remove(player.getRandom().nextInt(pool.size()))));
        return result;
    }

    private static List<EventGroup> dailyGroups(ServerPlayer player) {
        var pool = new ArrayList<>(EventRegistry.groups());
        var random = RandomSource.create(player.server.overworld().getSeed() ^ player.getUUID().getMostSignificantBits()
                ^ player.getUUID().getLeastSignificantBits() ^ day(player));
        var result = new ArrayList<EventGroup>();
        while (result.size() < 3 && !pool.isEmpty()) result.add(pool.remove(random.nextInt(pool.size())));
        return result;
    }

    private static EventGroup purchaseGroup(ServerPlayer player, int slot) {
        if (slot == 3) {
            var event = EventRegistry.event(EventRegistry.randomEvent(player.getRandom()));
            return event == null ? null : EventGroup.of(Component.translatable(TEXT + "random_event"), event);
        }
        var groups = dailyGroups(player);
        if (slot - 4 >= groups.size()) return null;
        var group = groups.get(slot - 4);
        var events = group.events().stream().filter(number -> EventRegistry.event(number) != null).toList();
        return new EventGroup(group.number(), group.title(), group.texture(), group.weight(), () -> events);
    }
}
