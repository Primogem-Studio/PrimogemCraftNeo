package net.per.primogemcraft.system.event;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.per.primogemcraft.enchantment.EnchantChoice;
import net.per.primogemcraft.enchantment.EnchantGrade;
import net.per.primogemcraft.item.misc.OtherworldBankbook;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.system.choice.*;
import net.per.primogemcraft.system.curio.CurioChoice;
import net.per.primogemcraft.system.curio.Curios;
import net.per.primogemcraft.system.living.LivingItemAPI;
import net.per.primogemcraft.util.PGCTimer;
import net.per.primogemcraft.util.PlayerItems;

import java.util.ArrayList;
import java.util.List;

public final class EventContext {
    private static final String DENY_GATE = "event/deny";
    private static final String CHAIN_GATE = "event/chain";
    private static final int DENY_GATE_TICKS = 100;
    private static final int CHAIN_GATE_TICKS = 20;
    private static final int OPTION_ATTEMPTS = 20;
    private static final String DENY_KEY = "message.primogemcraft.event.deny";
    private static final String LOOT_KEY = "gui.primogemcraft.event.loot";
    private static final String QUOTA_WORLD_KEY = "message.primogemcraft.event.quota.world";
    private static final String QUOTA_PLAYER_KEY = "message.primogemcraft.event.quota.player";
    private static final String QUOTA_OVERFLOW_KEY = "message.primogemcraft.event.quota.overflow";
    private static final String QUOTA_LIMIT_KEY = "message.primogemcraft.event.quota.limit";
    private static final String QUOTA_PLENTIFUL_KEY = "message.primogemcraft.event.quota.plentiful";
    private static final String QUOTA_SCARCE_KEY = "message.primogemcraft.event.quota.scarce";

    private final ServerPlayer player;
    private final ServerLevel level;
    private final RandomEvent event;
    private final RandomSource random;

    private EventContext(ServerPlayer player, RandomEvent event) {
        this.player = player;
        this.level = player.serverLevel();
        this.event = event;
        this.random = player.getRandom();
    }

    public static EventContext of(ServerPlayer player, RandomEvent event) {
        return new EventContext(player, event);
    }

    public ServerPlayer player() {
        return player;
    }

    public ServerLevel level() {
        return level;
    }

    public RandomEvent event() {
        return event;
    }

    public RandomSource random() {
        return random;
    }

    public boolean chance(double probability) {
        return random.nextDouble() < probability;
    }

    public int range(int min, int max) {
        return Mth.nextInt(random, min, max);
    }

    public boolean gate(String name, int ticks) {
        if (!ready(name)) return false;
        PGCTimer.set(player, name, ticks);
        return true;
    }

    public boolean ready(String name) {
        return PGCTimer.isDone(player, name);
    }

    public boolean chainReady() {
        return ready(CHAIN_GATE);
    }

    public boolean prompt(Component message) {
        return prompt(message, false);
    }

    public boolean prompt(Component message, boolean actionBar) {
        player.displayClientMessage(message, actionBar);
        return true;
    }

    public boolean give(ItemStack stack) {
        OtherworldBankbook.give(player, stack);
        return true;
    }

    public boolean livingItems(int count, ItemStack stack) {
        for (var index = 0; index < count; index++) LivingItemAPI.summonInfinite(level, player, stack.copy());
        return true;
    }

    public boolean deny() {
        if (PGCTimer.isDone(player, DENY_GATE)) {
            PGCTimer.set(player, DENY_GATE, DENY_GATE_TICKS);
            player.displayClientMessage(Component.translatable(DENY_KEY), false);
        }
        return false;
    }

    public boolean fragments(int amount) {
        if (!hasFragments(amount)) return deny();
        return OtherworldBankbook.payFragments(player, amount);
    }

    public boolean hasFragments(int amount) {
        return OtherworldBankbook.canPayFragments(player, amount);
    }

    public boolean afterglow(int amount) {
        if (!hasAfterglow(amount)) return deny();
        PlayerItems.take(player, PGCItems.LUCENT_AFTERGLOW.get(), amount);
        return true;
    }

    public boolean hasAfterglow(int amount) {
        return PlayerItems.count(player, PGCItems.LUCENT_AFTERGLOW.get()) >= amount;
    }

    public boolean hasCurios(TagKey<Item> tag) {
        return Curios.hasCurios(tag);
    }

    public boolean hasEnchantTargets() {
        return EnchantChoice.hasTargets(player);
    }

    public boolean hasHealth(double ratio) {
        return player.getHealth() >= player.getMaxHealth() * (float) ratio;
    }

    public boolean health(double ratio) {
        if (!hasHealth(ratio)) return deny();
        player.hurt(level.damageSources().generic(), player.getMaxHealth() * (float) ratio);
        return true;
    }

    public boolean enchant(EnchantGrade grade) {
        if (!hasEnchantTargets()) return deny();
        return EnchantChoice.open(player, grade);
    }

    public boolean enchant(EnchantGrade grade, int fragments) {
        if (!hasEnchantTargets()) return deny();
        if (!hasFragments(fragments)) return deny();
        if (!EnchantChoice.open(player, grade)) return false;
        return OtherworldBankbook.payFragments(player, fragments);
    }

    public boolean curios(TagKey<Item> tag, int count) {
        var options = rollCurios(tag, count);
        if (options.isEmpty()) return deny();
        CurioChoice.open(player, event.title(), Component.translatable(EventChoice.HINT_KEY), options, chosen -> Curios.give(player, chosen));
        return true;
    }

    public boolean lootTable(ResourceLocation table, int count) {
        var options = ChoiceSupport.distinct(List.of(), () -> EventLoot.roll(level, table), count, OPTION_ATTEMPTS);
        if (options.isEmpty()) return deny();
        return choose(options, Component.translatable(LOOT_KEY));
    }

    public boolean choose(List<ItemStack> options, Component title) {
        if (options.isEmpty()) return deny();
        var cards = new ArrayList<ChoiceCard>();
        for (var option : options) cards.add(ChoiceSupport.card(option));
        ChoiceRegistry.open(player, title, Component.translatable(EventChoice.HINT_KEY), ChoiceVisual.ITEM_MODEL, ChoiceSupport.BACKGROUND, ChoiceSupport.DEFAULT_CARDS, ChoiceSupport.spin(ChoiceSpinSpeed.FASTEST), cards, index -> {
            if (index >= 0 && index < options.size()) give(options.get(index));
            return true;
        });
        return true;
    }

    public boolean group(EventGroup group) {
        if (group == null || group.isEmpty()) return deny();
        EventRegistry.trigger(player, group);
        return true;
    }

    public boolean group(int groupNumber) {
        return group(EventRegistry.group(groupNumber));
    }

    public boolean randomEvents(int count) {
        if (!gate(CHAIN_GATE, CHAIN_GATE_TICKS)) return false;
        var numbers = new ArrayList<Integer>();
        for (var index = 0; index < count; index++) {
            var number = EventRegistry.randomEvent(random);
            if (EventRegistry.event(number) != null) numbers.add(number);
        }
        EventChain.events(player, numbers);
        return !numbers.isEmpty();
    }

    public boolean worldQuota(int amount) {
        if (amount > 0 && EventQuota.available(level) >= EventQuota.limit(level)) {
            EventQuota.addPlayerStored(player, amount);
            player.displayClientMessage(Component.translatable(QUOTA_OVERFLOW_KEY), false);
        } else {
            EventQuota.addAvailable(level, amount);
        }
        return prompt(Component.translatable(QUOTA_WORLD_KEY, quota(EventQuota.available(level))));
    }

    public boolean playerQuota(int amount) {
        EventQuota.addPlayerStored(player, amount);
        return prompt(Component.translatable(QUOTA_PLAYER_KEY, quota(EventQuota.playerStored(player))));
    }

    public boolean worldLimit(int amount) {
        EventQuota.addLimit(level, amount);
        player.displayClientMessage(Component.translatable(QUOTA_LIMIT_KEY, quota(EventQuota.limit(level))), false);
        level.getServer().getPlayerList().broadcastSystemMessage(Component.translatable(amount > 0 ? QUOTA_PLENTIFUL_KEY : QUOTA_SCARCE_KEY, player.getDisplayName()), false);
        return true;
    }

    private static Component quota(int value) {
        return Component.literal(Integer.toString(value)).withStyle(ChatFormatting.GREEN);
    }

    private List<ItemStack> rollCurios(TagKey<Item> tag, int count) {
        var options = new ArrayList<ItemStack>();
        for (var attempt = 0; attempt < OPTION_ATTEMPTS && options.size() < count; attempt++) {
            var rolled = Curios.randomCurio(random, tag);
            if (rolled.isEmpty()) return List.of();
            if (contains(options, rolled)) continue;
            options.add(rolled);
        }
        return options;
    }

    private static boolean contains(List<ItemStack> options, ItemStack candidate) {
        for (var option : options) if (option.is(candidate.getItem())) return true;
        return false;
    }
}
