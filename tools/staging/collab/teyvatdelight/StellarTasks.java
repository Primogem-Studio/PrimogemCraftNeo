package net.per.primogemcraft.collab.teyvatdelight;

import net.minecraft.registry.Registries;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
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

    static List<StellarShopNetwork.Task> snapshot(ServerPlayerEntity player) {
        var tasks = tasks(player);
        var result = new ArrayList<StellarShopNetwork.Task>();
        for (var index = 0; index < tasks.size(); index++) {
            var task = tasks.getCompound(index);
            var ingredients = ingredients(task);
            var kind = task.getInt("kind");
            var reward = new ItemStack(kind == StellarTaskPlan.EVENT ? Items.BOOK
                    : kind == StellarTaskPlan.ENCHANT ? Items.ENCHANTED_BOOK : item(task.getString("reward")), task.getInt("amount"));
            var title = kind == StellarTaskPlan.EVENT ? Text.translatable(TEXT + "unknown_event")
                    : kind == StellarTaskPlan.ENCHANT ? Text.translatable(grade(task).translationKey())
                    : reward.getItem() == PGCItems.PRIMOGEM.get() || reward.getItem() == PGCItems.MORA.get()
                    ? Text.translatable(TEXT + "currency_craft", reward.getName())
                    : BuiltInRegistries.ITEM.getKey(reward.getItem()).getNamespace().equals("teyvatdelight")
                    ? Text.translatable(TEXT + "currency_teyvat", reward.getName()) : reward.getName();
            var completed = task.getBoolean("completed");
            result.add(new StellarShopNetwork.Task(ingredients, reward, title, completed,
                    !completed && canPay(player, ingredients) && rewardAvailable(player, task), task.getLong("token")));
        }
        return result;
    }

    static void submit(ServerPlayerEntity player, StellarShopNetwork.Action action) {
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

    private static NbtList tasks(ServerPlayerEntity player) {
        var data = player.primogemcraft$getPersistentData();
        if (!data.contains(Player.PERSISTED_NBT_TAG)) data.put(Player.PERSISTED_NBT_TAG, new NbtCompound());
        var persisted = data.getCompound(Player.PERSISTED_NBT_TAG);
        var day = StellarShop.day(player);
        var state = persisted.getCompound(STATE_KEY);
        if (state.getInt("version") == 2 && state.contains("tasks", NbtElement.LIST_TYPE) && day <= state.getLong("day"))
            return state.getList("tasks", NbtElement.COMPOUND_TYPE);
        var previous = state.contains("tasks", NbtElement.LIST_TYPE) && day <= state.getLong("day")
                ? state.getList("tasks", NbtElement.COMPOUND_TYPE) : persisted.contains(STATE_KEY) ? new NbtList()
                : StellarShop.state(player).getList("tasks", NbtElement.COMPOUND_TYPE);
        var random = new Random(player.server.overworld().getSeed() ^ player.getUuid().getMostSignificantBits()
                ^ Long.rotateLeft(player.getUuid().getLeastSignificantBits(), 23) ^ day);
        var foods = BuiltInRegistries.ITEM.stream()
                .filter(item -> item.isEdible()
                        && item != PGCItems.PRIMOGEM.get() && item != PGCItems.GENESIS_CRYSTAL.get()
                        && item != PGCItems.TRASH.get() && item != PGCItems.PLEASANT_LOOKING_TRASH.get())
                .map(item -> BuiltInRegistries.ITEM.getKey(item).toString()).sorted().toList();
        var tasks = new NbtList();
        for (var plan : StellarTaskPlan.generate(random, foods)) {
            var task = new NbtCompound();
            task.putInt("kind", plan.kind());
            task.putInt("grade", plan.grade());
            task.putString("reward", plan.reward());
            task.putInt("amount", plan.amount());
            task.putLong("token", random.nextLong());
            task.putBoolean("completed", tasks.size() < previous.size() && previous.getCompound(tasks.size()).getBoolean("completed"));
            var ingredients = new NbtList();
            for (var ingredient : plan.ingredients()) {
                var entry = new NbtCompound();
                entry.putString("item", ingredient.item());
                entry.putInt("count", ingredient.count());
                ingredients.add(entry);
            }
            task.put("ingredients", ingredients);
            tasks.add(task);
        }
        state = new NbtCompound();
        state.putInt("version", 2);
        state.putLong("day", day);
        state.put("tasks", tasks);
        persisted.put(STATE_KEY, state);
        return tasks;
    }

    private static List<ItemStack> ingredients(NbtCompound task) {
        var entries = task.getList("ingredients", NbtElement.COMPOUND_TYPE);
        var result = new ArrayList<ItemStack>();
        for (var index = 0; index < entries.size(); index++) {
            var entry = entries.getCompound(index);
            result.add(new ItemStack(item(entry.getString("item")), entry.getInt("count")));
        }
        return result;
    }

    private static boolean canPay(ServerPlayerEntity player, List<ItemStack> ingredients) {
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

    private static boolean rewardAvailable(ServerPlayerEntity player, NbtCompound task) {
        return switch (task.getInt("kind")) {
            case StellarTaskPlan.EVENT -> !EventRegistry.groups().isEmpty();
            case StellarTaskPlan.ENCHANT -> task.getInt("grade") >= 0 && task.getInt("grade") < EnchantGrade.values().length
                    && EnchantChoice.hasTargets(player);
            case StellarTaskPlan.MATERIAL -> item(task.getString("reward")) != Items.AIR
                    && task.getInt("amount") > 0 && task.getInt("amount") <= 32;
            default -> false;
        };
    }

    private static EnchantGrade grade(NbtCompound task) {
        var grade = task.getInt("grade");
        return EnchantGrade.values()[net.minecraft.util.Mth.clamp(grade, 0, EnchantGrade.values().length - 1)];
    }

    private static EventGroup randomGroup(ServerPlayerEntity player) {
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
        var location = id.contains(":") ? Identifier.tryParse(id) : Identifier.tryBuild(MOD_ID, id);
        return location == null ? Items.AIR : BuiltInRegistries.ITEM.get(location);
    }

    private static void message(ServerPlayerEntity player, String key) {
        player.sendMessage(Text.translatable(TEXT + key), false);
    }
}
