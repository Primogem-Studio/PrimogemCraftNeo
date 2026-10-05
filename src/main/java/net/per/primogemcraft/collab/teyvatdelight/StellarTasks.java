package net.per.primogemcraft.collab.teyvatdelight;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.per.primogemcraft.enchantment.EnchantChoice;
import net.per.primogemcraft.enchantment.EnchantGrade;
import net.per.primogemcraft.item.misc.OtherworldBankbook;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.system.choice.ChoiceRegistry;
import net.per.primogemcraft.system.event.EventChoice;
import net.per.primogemcraft.system.event.EventGroup;
import net.per.primogemcraft.system.event.EventRegistry;
import net.per.primogemcraft.util.PlayerItems;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

final class StellarTasks {
    private static final String TEXT = "gui.primogemcraft.stellar_tasks.";
    private static final String STATE_KEY = MOD_ID + ":stellar_tasks";

    private StellarTasks() {
    }

    static List<StellarShopNetwork.Task> snapshot(ServerPlayer player) {
        var tasks = tasks(player);
        var result = new ArrayList<StellarShopNetwork.Task>();
        for (var index = 0; index < tasks.size(); index++) {
            var task = tasks.getCompound(index);
            var ingredients = ingredients(task);
            var kind = task.getInt("kind");
            var reward = new ItemStack(kind == StellarTaskPlan.EVENT ? Items.BOOK
                    : kind == StellarTaskPlan.ENCHANT ? Items.ENCHANTED_BOOK : item(task.getString("reward")), task.getInt("amount"));
            var title = kind == StellarTaskPlan.EVENT ? Component.translatable(TEXT + "unknown_event")
                    : kind == StellarTaskPlan.ENCHANT ? Component.translatable(grade(task).translationKey())
                    : reward.getItem() == PGCItems.PRIMOGEM.get() || reward.getItem() == PGCItems.MORA.get()
                    ? Component.translatable(TEXT + "currency_craft", reward.getHoverName())
                    : BuiltInRegistries.ITEM.getKey(reward.getItem()).getNamespace().equals("teyvatdelight")
                    ? Component.translatable(TEXT + "currency_teyvat", reward.getHoverName()) : reward.getHoverName();
            var completed = task.getBoolean("completed");
            result.add(new StellarShopNetwork.Task(ingredients, reward, title, completed,
                    !completed && canPay(player, ingredients) && rewardAvailable(player, task), task.getLong("token")));
        }
        return result;
    }

    static void submit(ServerPlayer player, StellarShopNetwork.Action action) {
        if (action.day() != StellarShop.day(player)) {
            message(player, "refreshed");
            return;
        }
        var tasks = tasks(player);
        var index = action.slot() - StellarShopNetwork.TASK_FIRST_SLOT;
        if (index < 0 || index >= tasks.size()) return;
        var task = tasks.getCompound(index);
        if (action.shopCycle() != task.getLong("token")) {
            message(player, "refreshed");
            return;
        }
        if (task.getBoolean("completed") || ChoiceRegistry.isPending(player)) return;
        var ingredients = ingredients(task);
        if (!canPay(player, ingredients)) {
            message(player, "not_enough");
            return;
        }
        if (!rewardAvailable(player, task)) {
            message(player, "unavailable");
            return;
        }
        var kind = task.getInt("kind");
        if (kind == StellarTaskPlan.EVENT) {
            var group = randomGroup(player);
            if (group == null) {
                message(player, "unavailable");
                return;
            }
            player.closeContainer();
            EventChoice.open(player, group);
            if (!ChoiceRegistry.isPending(player)) return;
        } else if (kind == StellarTaskPlan.ENCHANT) {
            player.closeContainer();
            if (!EnchantChoice.open(player, grade(task), 1, 3, 1)) return;
        }
        for (var ingredient : ingredients) PlayerItems.take(player, ingredient.getItem(), ingredient.getCount());
        task.putBoolean("completed", true);
        if (kind == StellarTaskPlan.MATERIAL)
            OtherworldBankbook.give(player, new ItemStack(item(task.getString("reward")), task.getInt("amount")));
        player.getInventory().setChanged();
        player.containerMenu.broadcastChanges();
    }

    private static ListTag tasks(ServerPlayer player) {
        var data = player.getPersistentData();
        if (!data.contains(Player.PERSISTED_NBT_TAG)) data.put(Player.PERSISTED_NBT_TAG, new CompoundTag());
        var persisted = data.getCompound(Player.PERSISTED_NBT_TAG);
        var day = StellarShop.day(player);
        var state = persisted.getCompound(STATE_KEY);
        if (state.getInt("version") == 2 && state.contains("tasks", Tag.TAG_LIST) && day <= state.getLong("day"))
            return state.getList("tasks", Tag.TAG_COMPOUND);
        var previous = state.contains("tasks", Tag.TAG_LIST) && day <= state.getLong("day")
                ? state.getList("tasks", Tag.TAG_COMPOUND) : persisted.contains(STATE_KEY) ? new ListTag()
                : StellarShop.state(player).getList("tasks", Tag.TAG_COMPOUND);
        var random = new Random(player.server.overworld().getSeed() ^ player.getUUID().getMostSignificantBits()
                ^ Long.rotateLeft(player.getUUID().getLeastSignificantBits(), 23) ^ day);
        var foods = BuiltInRegistries.ITEM.stream()
                .filter(item -> item.components().has(DataComponents.FOOD)
                        && item != PGCItems.PRIMOGEM.get() && item != PGCItems.GENESIS_CRYSTAL.get()
                        && item != PGCItems.TRASH.get() && item != PGCItems.PLEASANT_LOOKING_TRASH.get())
                .map(item -> BuiltInRegistries.ITEM.getKey(item).toString()).sorted().toList();
        var tasks = new ListTag();
        for (var plan : StellarTaskPlan.generate(random, foods)) {
            var task = new CompoundTag();
            task.putInt("kind", plan.kind());
            task.putInt("grade", plan.grade());
            task.putString("reward", plan.reward());
            task.putInt("amount", plan.amount());
            task.putLong("token", random.nextLong());
            task.putBoolean("completed", tasks.size() < previous.size() && previous.getCompound(tasks.size()).getBoolean("completed"));
            var ingredients = new ListTag();
            for (var ingredient : plan.ingredients()) {
                var entry = new CompoundTag();
                entry.putString("item", ingredient.item());
                entry.putInt("count", ingredient.count());
                ingredients.add(entry);
            }
            task.put("ingredients", ingredients);
            tasks.add(task);
        }
        state = new CompoundTag();
        state.putInt("version", 2);
        state.putLong("day", day);
        state.put("tasks", tasks);
        persisted.put(STATE_KEY, state);
        return tasks;
    }

    private static List<ItemStack> ingredients(CompoundTag task) {
        var entries = task.getList("ingredients", Tag.TAG_COMPOUND);
        var result = new ArrayList<ItemStack>();
        for (var index = 0; index < entries.size(); index++) {
            var entry = entries.getCompound(index);
            result.add(new ItemStack(item(entry.getString("item")), entry.getInt("count")));
        }
        return result;
    }

    private static boolean canPay(ServerPlayer player, List<ItemStack> ingredients) {
        if (ingredients.isEmpty() || ingredients.size() > 3) return false;
        var seen = new HashSet<Item>();
        var total = 0;
        for (var ingredient : ingredients) {
            if (ingredient.isEmpty() || !seen.add(ingredient.getItem())
                    || PlayerItems.count(player, ingredient.getItem()) < ingredient.getCount()) return false;
            total += ingredient.getCount();
        }
        return total <= 8;
    }

    private static boolean rewardAvailable(ServerPlayer player, CompoundTag task) {
        return switch (task.getInt("kind")) {
            case StellarTaskPlan.EVENT -> !EventRegistry.groups().isEmpty();
            case StellarTaskPlan.ENCHANT -> task.getInt("grade") >= 0 && task.getInt("grade") < EnchantGrade.values().length
                    && EnchantChoice.hasTargets(player);
            case StellarTaskPlan.MATERIAL -> item(task.getString("reward")) != Items.AIR
                    && task.getInt("amount") > 0 && task.getInt("amount") <= 32;
            default -> false;
        };
    }

    private static EnchantGrade grade(CompoundTag task) {
        var grade = task.getInt("grade");
        return EnchantGrade.values()[Math.clamp(grade, 0, EnchantGrade.values().length - 1)];
    }

    private static EventGroup randomGroup(ServerPlayer player) {
        var groups = new ArrayList<>(EventRegistry.groups());
        while (!groups.isEmpty()) {
            var roll = player.getRandom().nextInt(groups.stream().mapToInt(EventGroup::weight).sum());
            var index = 0;
            while ((roll -= groups.get(index).weight()) >= 0) index++;
            var group = groups.remove(index);
            var events = group.events().stream().filter(number -> EventRegistry.event(number) != null).toList();
            if (!events.isEmpty()) return new EventGroup(group.number(), group.title(), group.texture(), group.weight(), () -> events);
        }
        return null;
    }

    private static Item item(String id) {
        var location = id.contains(":") ? ResourceLocation.tryParse(id) : ResourceLocation.tryBuild(MOD_ID, id);
        return location == null ? Items.AIR : BuiltInRegistries.ITEM.get(location);
    }

    private static void message(ServerPlayer player, String key) {
        player.displayClientMessage(Component.translatable(TEXT + key), false);
    }
}
