package net.per.primogemcraft.system.curio;

import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.per.primogemcraft.item.misc.OtherworldBankbook;
import net.per.primogemcraft.registry.PGCDataComponents;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.registry.PGCSounds;
import net.per.primogemcraft.system.curio.compat.CuriosIntegration;
import net.per.primogemcraft.system.curio.effect.CurioEffects;
import net.per.primogemcraft.util.PlayerFlags;

import java.util.*;
import java.util.function.Predicate;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public final class Curios {
    private static final String BROKEN_KEY = "message.primogemcraft.curio.broken";
    private static final String REPAIRED_KEY = "message.primogemcraft.curio.repaired";
    private static final float FULL_REPAIR = 1.0F;
    private static final TagKey<Item> DAMAGED = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "curio/bad"));
    private static final TagKey<Item> DIE_EXEMPT = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "curio/special/code"));
    private static final ResourceLocation REPAIRS = ResourceLocation.fromNamespaceAndPath(MOD_ID, "curio/repairs");
    private static Map<Item, Item> intact;

    private Curios() {
    }

    public static boolean isCurio(ItemStack stack) {
        return !stack.isEmpty() && stack.is(CurioForm.ANY) && !stack.is(DAMAGED);
    }

    public static boolean repairsCurios(ItemStack stack) {
        return stack.getItem() instanceof CurioItem curio && curio.repairsCurios();
    }

    public static int refinementBonus(Player player) {
        var counted = new HashSet<Item>();
        var bonus = 0;
        for (var stack : player.getInventory().items) bonus += refinementBonus(stack, counted);
        for (var stack : player.getInventory().offhand) bonus += refinementBonus(stack, counted);
        for (var stack : CuriosIntegration.equipped(player)) bonus += refinementBonus(stack, counted);
        return bonus;
    }

    public static List<ItemStack> inventory(ServerPlayer player) {
        var stacks = new ArrayList<ItemStack>();
        for (var stack : player.getInventory().items) if (!stack.isEmpty()) stacks.add(stack);
        for (var stack : player.getInventory().offhand) if (!stack.isEmpty()) stacks.add(stack);
        return stacks;
    }

    public static ItemStack randomInventoryItem(ServerPlayer player, Predicate<ItemStack> valid) {
        var candidates = new ArrayList<ItemStack>();
        for (var stack : inventory(player)) if (valid.test(stack)) candidates.add(stack);
        return random(candidates, player.getRandom());
    }

    public static ItemStack repairRandom(ServerPlayer player, float ratio) {
        return repairRandom(player, ratio, ItemStack.EMPTY);
    }

    public static ItemStack repairRandom(ServerPlayer player, int amount) {
        if (amount <= 0) return ItemStack.EMPTY;
        var candidates = new ArrayList<ItemStack>();
        collectCandidates(player, candidates, Curios::partiallyUsed);
        var target = random(candidates, player.getRandom());
        return !target.isEmpty() && repair(player, target, amount) ? target : ItemStack.EMPTY;
    }

    public static ItemStack repairRandom(ServerPlayer player, float ratio, ItemStack source) {
        var candidates = new ArrayList<ItemStack>();
        collectCandidates(player, candidates, stack -> stack != source && repairable(stack, ratio));
        if (ratio >= FULL_REPAIR) {
            for (var stack : repairTargets(player)) if (stack != source && intactForm(stack) != null) candidates.add(stack);
        }
        if (candidates.isEmpty())
            collectCandidates(player, candidates, stack -> stack != source && partiallyUsed(stack));
        var target = random(candidates, player.getRandom());
        if (target.isEmpty()) return ItemStack.EMPTY;
        var intact = intactForm(target);
        if (intact == null) return repair(player, target, ratio) ? target : ItemStack.EMPTY;
        var restored = new ItemStack(intact);
        target.shrink(1);
        repaired(player, restored);
        give(player, restored);
        return restored;
    }

    public static ItemStack repairRandomDurability(ServerPlayer player, float ratio) {
        var candidates = new ArrayList<ItemStack>();
        for (var stack : repairTargets(player)) {
            if (!stack.isDamageableItem() || stack.getDamageValue() <= 0) continue;
            if (stack.getDamageValue() < (int) (stack.getMaxDamage() * ratio)) continue;
            candidates.add(stack);
        }
        var target = random(candidates, player.getRandom());
        return !target.isEmpty() && repairDurability(player, target, ratio) ? target : ItemStack.EMPTY;
    }

    public static Item intactForm(ItemStack stack) {
        return intact().get(stack.getItem());
    }

    private static Map<Item, Item> intact() {
        if (intact == null) {
            intact = Map.of(
                    PGCItems.DAMAGED_CASKET_OF_INACCURACY.get(), PGCItems.CASKET_OF_INACCURACY.get(),
                    PGCItems.DAMAGED_COSMIC_BIG_LOTTO.get(), PGCItems.COSMIC_BIG_LOTTO.get(),
                    PGCItems.DAMAGED_INTERASTRAL_BIG_LOTTO.get(), PGCItems.INTERASTRAL_BIG_LOTTO.get(),
                    PGCItems.DAMAGED_FORTUNE_GLUE.get(), PGCItems.FORTUNE_GLUE.get());
        }
        return intact;
    }

    public static ResourceLocation idOf(Item item) {
        return BuiltInRegistries.ITEM.getKey(item);
    }

    public static void repair(ItemStack stack, int amount) {
        var bar = stack.get(PGCDataComponents.CUSTOM_BAR.get());
        if (bar == null || amount <= 0) return;
        stack.set(PGCDataComponents.CUSTOM_BAR.get(), bar.reducedBy(amount));
    }

    public static boolean repair(ServerPlayer player, ItemStack stack, int amount) {
        var bar = stack.get(PGCDataComponents.CUSTOM_BAR.get());
        if (bar == null || amount <= 0) return false;
        var before = bar.numerator();
        repair(stack, amount);
        var after = stack.get(PGCDataComponents.CUSTOM_BAR.get());
        if (after == null || after.numerator() >= before) return false;
        repaired(player, stack);
        return true;
    }

    public static boolean repair(ServerPlayer player, ItemStack stack, float ratio) {
        var bar = stack.get(PGCDataComponents.CUSTOM_BAR.get());
        if (bar == null) return false;
        return repair(player, stack, (int) (bar.denominator() * ratio));
    }

    public static boolean repairDurability(ServerPlayer player, ItemStack stack, float ratio) {
        if (!stack.isDamageableItem() || stack.getDamageValue() <= 0) return false;
        var restored = (int) (stack.getMaxDamage() * ratio);
        if (restored <= 0) return false;
        stack.setDamageValue(Math.max(0, stack.getDamageValue() - restored));
        repaired(player, stack);
        return true;
    }

    public static void repaired(ServerPlayer player, ItemStack stack) {
        player.displayClientMessage(Component.translatable(REPAIRED_KEY, stack.getHoverName()), false);
        PlayerFlags.of(player).advance(REPAIRS);
    }

    public static int repairs(ServerPlayer player) {
        return PlayerFlags.of(player).counter(REPAIRS);
    }

    public static int damagedCount(ServerPlayer player) {
        var count = 0;
        for (var stack : inventory(player)) if (stack.is(DAMAGED)) count += stack.getCount();
        return count;
    }

    public static CurioForm formOf(ItemStack stack) {
        for (var form : CurioForm.values()) if (stack.is(form.tag())) return form;
        return null;
    }

    public static ItemStack randomCurio(RandomSource random) {
        return randomCurio(random, CurioForm.ANY);
    }

    public static ItemStack randomCurio(RandomSource random, TagKey<Item> tag) {
        return randomCurio(random, tag, null);
    }

    public static ItemStack randomCurio(RandomSource random, TagKey<Item> tag, Item excluded) {
        var tagged = BuiltInRegistries.ITEM.getTag(tag);
        if (tagged.isEmpty()) return ItemStack.EMPTY;
        var items = tagged.get().stream().map(Holder::value).filter(item -> item != excluded && item != PGCItems.INTEGRATED_CODE.get()).toList();
        if (items.isEmpty()) return ItemStack.EMPTY;
        return new ItemStack(items.get(random.nextInt(items.size())));
    }

    public static boolean hasCurios(TagKey<Item> tag) {
        var tagged = BuiltInRegistries.ITEM.getTag(tag);
        return tagged.isPresent() && tagged.get().stream().anyMatch(holder -> holder.value() != PGCItems.INTEGRATED_CODE.get());
    }

    public static boolean marked(ItemStack stack, ResourceLocation mark) {
        var marks = stack.get(PGCDataComponents.CURIO_MARKS.get());
        return marks != null && marks.contains(mark);
    }

    public static void mark(ItemStack stack, ResourceLocation mark) {
        if (marked(stack, mark)) return;
        var marks = new ArrayList<ResourceLocation>();
        var existing = stack.get(PGCDataComponents.CURIO_MARKS.get());
        if (existing != null) marks.addAll(existing);
        marks.add(mark);
        stack.set(PGCDataComponents.CURIO_MARKS.get(), List.copyOf(marks));
    }

    public static void give(ServerPlayer player, ItemStack stack) {
        if (stack.isEmpty()) return;
        var giving = stack.copy();
        player.getInventory().add(giving);
        if (giving.isEmpty()) return;
        var level = player.level();
        var dropped = new ItemEntity(level, player.getX(), player.getY(), player.getZ(), giving);
        dropped.setDefaultPickUpDelay();
        level.addFreshEntity(dropped);
    }

    public static boolean equip(ServerPlayer player, ItemStack stack) {
        if (!isCurio(stack) && !OtherworldBankbook.isBankbook(stack)) return false;
        return CuriosIntegration.equip(player, stack);
    }

    public static List<CurioContext> held(ServerPlayer player) {
        var contexts = new ArrayList<CurioContext>();
        for (var stack : player.getInventory().items) collect(player, stack, contexts);
        for (var stack : player.getInventory().offhand) collect(player, stack, contexts);
        CuriosIntegration.collect(player, contexts);
        return contexts;
    }

    public static List<ItemStack> convert(CurioContext context, double bonusRatio) {
        var player = context.player();
        var inventory = player.getInventory();
        var converting = context.stack().getItem();
        var kinds = new LinkedHashSet<Item>();
        if (isCurio(context.stack())) kinds.add(converting);
        for (var stack : inventory.items) if (convertible(stack)) kinds.add(stack.getItem());
        for (var stack : inventory.offhand) if (convertible(stack)) kinds.add(stack.getItem());
        for (var stack : CuriosIntegration.equipped(player)) if (convertible(stack)) kinds.add(stack.getItem());
        var pending = new LinkedHashSet<>(kinds);
        pending.remove(converting);
        var consumed = consume(inventory.items, pending);
        consumed += consume(inventory.offhand, pending);
        consumed += CuriosIntegration.consume(player, pending);
        if (consumed > 0)
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(), PGCSounds.CURIO_BROKEN.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        CurioEffects.clear(player);
        var others = Math.max(0, kinds.size() - 1);
        var rolls = kinds.size() + (int) Math.floor(others * bonusRatio);
        var pool = context.form() == CurioForm.FUSION ? CurioForm.FUSION : CurioForm.NORMAL;
        var rewards = new ArrayList<ItemStack>();
        for (var index = 0; index < rolls; index++) {
            var reward = randomCurio(context.random(), pool.tag(), converting);
            if (!reward.isEmpty()) rewards.add(context.reward(reward));
        }
        return List.copyOf(rewards);
    }

    private static boolean convertible(ItemStack stack) {
        return isCurio(stack) && !stack.is(DIE_EXEMPT);
    }

    private static int refinementBonus(ItemStack stack, Set<Item> counted) {
        if (!(stack.getItem() instanceof CurioItem curio) || !isCurio(stack)) return 0;
        if (!counted.add(stack.getItem())) return 0;
        return curio.refinementBonus();
    }

    private static ItemStack random(List<ItemStack> candidates, RandomSource random) {
        return candidates.isEmpty() ? ItemStack.EMPTY : candidates.get(random.nextInt(candidates.size()));
    }

    private static void collectCandidates(ServerPlayer player, List<ItemStack> candidates, Predicate<ItemStack> valid) {
        for (var context : held(player)) {
            var stack = context.stack();
            if (repairsCurios(stack)) continue;
            if (stack.getItem() instanceof CurioItem curio && curio.barFillsUp()) continue;
            if (valid.test(stack)) candidates.add(stack);
        }
    }

    private static List<ItemStack> repairTargets(ServerPlayer player) {
        var stacks = inventory(player);
        stacks.addAll(CuriosIntegration.equipped(player));
        return stacks;
    }

    private static boolean repairable(ItemStack stack, float ratio) {
        var bar = stack.get(PGCDataComponents.CUSTOM_BAR.get());
        if (bar == null) return false;
        var limit = bar.denominator() - 1;
        if (limit <= 0) return false;
        return bar.numerator() >= Mth.clamp((int) (bar.denominator() * ratio), 1, limit);
    }

    private static boolean partiallyUsed(ItemStack stack) {
        var bar = stack.get(PGCDataComponents.CUSTOM_BAR.get());
        return bar != null && bar.numerator() > 0;
    }

    public static void broken(ServerPlayer player, ItemStack stack) {
        if (stack.isEmpty()) return;
        player.displayClientMessage(Component.translatable(BROKEN_KEY, stack.getHoverName()), false);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), PGCSounds.CURIO_BROKEN.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    private static int consume(NonNullList<ItemStack> stacks, Set<Item> pending) {
        var consumed = 0;
        for (var stack : stacks) {
            if (pending.isEmpty()) break;
            if (stack.isEmpty() || !pending.remove(stack.getItem())) continue;
            stack.shrink(1);
            consumed++;
        }
        return consumed;
    }

    public static void signal(ServerPlayer player, CurioImpact impact) {
        for (var context : held(player))
            if (context.stack().getItem() instanceof CurioItem curio) curio.impacted(context, impact);
    }

    public static boolean survivesDeath(ServerPlayer player, CurioImpact impact) {
        for (var context : held(player))
            if (context.stack().getItem() instanceof CurioItem curio && curio.survivesDeath(context, impact))
                return true;
        return false;
    }

    public static boolean absorbsDamage(ServerPlayer player, CurioImpact impact) {
        for (var context : held(player))
            if (context.stack().getItem() instanceof CurioItem curio && curio.absorbsDamage(context, impact))
                return true;
        return false;
    }

    public static void tickEquipped(ServerPlayer player) {
        CuriosIntegration.tick(player);
    }

    private static void collect(ServerPlayer player, ItemStack stack, List<CurioContext> output) {
        if (stack.getItem() instanceof CurioItem curio) output.add(CurioContext.of(player, stack, curio.form()));
    }

    public static List<CurioItem> getCurios() {
        return BuiltInRegistries.ITEM.stream().filter(item -> item instanceof CurioItem).map(item -> (CurioItem) item).toList();
    }
}
