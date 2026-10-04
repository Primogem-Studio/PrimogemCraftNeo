package net.per.primogemcraft.collab.teyvatdelight;

import com.guoche.teyvatdelight.KatheryneData;
import com.guoche.teyvatdelight.KatheryneMenu;
import com.guoche.teyvatdelight.KatheryneRules;
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
import net.per.primogemcraft.system.choice.ChoiceRegistry;
import net.per.primogemcraft.system.curio.CurioChoice;
import net.per.primogemcraft.system.curio.CurioForm;
import net.per.primogemcraft.system.curio.CurioGrade;
import net.per.primogemcraft.system.curio.Curios;
import net.per.primogemcraft.system.event.EventChoice;
import net.per.primogemcraft.system.event.EventGroup;
import net.per.primogemcraft.system.event.EventRegistry;
import net.per.primogemcraft.util.PlayerItems;
import net.per.primogemcraft.config.TeyvatExchangeConfig;
import net.per.primogemcraft.registry.PGCItems;

import java.util.ArrayList;
import java.util.List;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

final class StellarShop {
    private static final int[] PRICES = {2, 6, 9, 4, 8, 8, 8};
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
        var options = slot < 3 ? rollCurios(player, slot) : List.<ItemStack>of();
        var group = slot >= 3 ? purchaseGroup(player, slot) : null;
        if (slot < 3 ? options.size() < 3 : group == null || group.isEmpty()) {
            fail(player, menu, "unavailable");
            return;
        }
        PlayerItems.take(player, currency, PRICES[slot]);
        if (slot < 3) KatheryneData.get(player.server).recordPurchase(player, curioOffer(slot));
        else {
            var state = state(player);
            state.putInt("bought_" + slot, state.getInt("bought_" + slot) + 1);
        }
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
                    : slot == 3 ? Component.translatable(TEXT + "random_event")
                    : slot - 4 < groups.size() ? groups.get(slot - 4).title() : Component.translatable(TEXT + "unavailable");
            var available = slot < 3 ? curioPool(slot).size() >= 3
                    : slot == 3 ? !EventRegistry.events().isEmpty() : slot - 4 < groups.size();
            offers.add(new StellarShopNetwork.Offer(title, PRICES[slot], remaining(player, slot), available));
        }
        PacketDistributor.sendToPlayer(player, new StellarShopNetwork.Snapshot(menu.containerId,
                KatheryneData.shopCycle(player.server), day(player), PlayerItems.count(player, currency()), offers,
                new StellarShopNetwork.Details(KatheryneRules.secondsUntil(player.server, KatheryneRules.shopRefreshTime()),
                        KatheryneRules.secondsUntil(player.server, 0), exchanges(player))));
    }

    private static List<StellarShopNetwork.Exchange> exchanges(ServerPlayer player) {
        var result = new ArrayList<StellarShopNetwork.Exchange>();
        for (var index = 0; index < 4; index++) {
            var cost = exchangeAmount(index, false);
            result.add(new StellarShopNetwork.Exchange(cost, exchangeAmount(index, true), exchangeRemaining(player, index),
                    TeyvatExchangeConfig.ENABLED.get() && exchangeRemaining(player, index) > 0 && PlayerItems.count(player, exchangeItem(index, false)) >= cost));
        }
        return result;
    }

    private static int exchangeRemaining(ServerPlayer player, int index) {
        var limit = (index < 2 ? TeyvatExchangeConfig.PRIMOGEM_DAILY_LIMIT : TeyvatExchangeConfig.MORA_DAILY_LIMIT).get();
        return Math.max(0, limit - state(player).getInt(index < 2 ? "exchange_primogem" : "exchange_mora"));
    }

    private static int exchangeAmount(int index, boolean output) {
        var craft = (index % 2 == 0) != output;
        return index < 2 ? (craft ? TeyvatExchangeConfig.PRIMOGEM_CRAFT_AMOUNT : TeyvatExchangeConfig.PRIMOGEM_TEYVAT_AMOUNT).get()
                : (craft ? TeyvatExchangeConfig.MORA_CRAFT_AMOUNT : TeyvatExchangeConfig.MORA_TEYVAT_AMOUNT).get();
    }

    private static Item exchangeItem(int index, boolean output) {
        var craft = (index % 2 == 0) != output;
        if (craft) return index < 2 ? PGCItems.PRIMOGEM.get() : PGCItems.MORA.get();
        return BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("teyvatdelight", index < 2 ? "primogem" : "mora"));
    }

    private static void exchange(ServerPlayer player, KatheryneMenu menu, int index, StellarShopNetwork.Action action) {
        var cost = exchangeAmount(index, false);
        var reward = exchangeAmount(index, true);
        if (!TeyvatExchangeConfig.ENABLED.get() || action.shopCycle() != cost * 65L + reward || action.day() != day(player)) {
            fail(player, menu, "refreshed");
            return;
        }
        if (exchangeRemaining(player, index) <= 0) {
            fail(player, menu, "sold_out");
            return;
        }
        var input = exchangeItem(index, false);
        if (PlayerItems.count(player, input) < cost) {
            fail(player, menu, "exchange_short");
            return;
        }
        PlayerItems.take(player, input, cost);
        var state = state(player);
        var key = index < 2 ? "exchange_primogem" : "exchange_mora";
        state.putInt(key, state.getInt(key) + 1);
        PlayerItems.give(player, new ItemStack(exchangeItem(index, true), reward));
        player.getInventory().setChanged();
        menu.broadcastChanges();
        player.displayClientMessage(Component.translatable(TEXT + "exchange_done", cost, input.getDescription(),
                reward, exchangeItem(index, true).getDescription()), true);
        sync(player, menu);
    }

    private static void fail(ServerPlayer player, KatheryneMenu menu, String message) {
        player.displayClientMessage(Component.translatable(TEXT + message), false);
        sync(player, menu);
    }

    private static Item currency() {
        return BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("teyvatdelight", "primogem"));
    }

    private static KatheryneRules.ShopOffer curioOffer(int slot) {
        return new KatheryneRules.ShopOffer(STATE_KEY + "/curio_" + slot, List.of(), List.of(), 1);
    }

    private static int remaining(ServerPlayer player, int slot) {
        if (slot < 3) return KatheryneData.get(player.server).remainingFor(player, curioOffer(slot));
        return Math.max(0, (slot == 3 ? 2 : 1) - state(player).getInt("bought_" + slot));
    }

    private static long day(ServerPlayer player) {
        return Math.floorDiv(player.server.overworld().getDayTime(), 24000L);
    }

    private static CompoundTag state(ServerPlayer player) {
        var data = player.getPersistentData();
        if (!data.contains(Player.PERSISTED_NBT_TAG)) data.put(Player.PERSISTED_NBT_TAG, new CompoundTag());
        var persisted = data.getCompound(Player.PERSISTED_NBT_TAG);
        var state = persisted.getCompound(STATE_KEY);
        var day = day(player);
        if (!state.contains("day") || state.getLong("day") != day) {
            state = new CompoundTag();
            state.putLong("day", day);
            persisted.put(STATE_KEY, state);
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
