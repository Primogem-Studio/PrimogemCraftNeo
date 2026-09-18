package net.per.primogemcraft.system.weapon;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.per.primogemcraft.component.WeaponRecovery;
import net.per.primogemcraft.config.PGCConfig;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.system.curio.Curios;

import java.util.ArrayList;
import java.util.List;

public final class WeaponEnhancement {
    public static final int MAX_REFINEMENT = 5;
    public static final int MAX_TEMPORARY_REFINEMENT = 3;
    public static final int FIRST_TIER = 30;
    public static final int SECOND_TIER = 60;

    private static final int STARGLITTER_PER_DUPLICATE = 2;
    private static final int AFTERGLOW_PER_FIVE_STAR = 1;
    private static final int NEGATIVE_LEVEL_REQUIREMENT = 200;
    private static final double PERCENT = 100.0D;
    private static final int UNKNOWN_RANK = -1;
    private static final int ORE_RANK = 0;
    private static final int FINE_ORE_RANK = 1;
    private static final int MYSTIC_ORE_RANK = 2;
    private static final int SUPERIMPOSER_RANK = 4;
    private static final int FOUR_STAR_RANK = 5;
    private static final int FIVE_STAR_RANK = 6;

    private WeaponEnhancement() {
    }

    public static int maxLevel(int refinement) {
        var cap = PGCConfig.WEAPON_MAX_LEVEL.get();
        return refinement >= MAX_REFINEMENT ? cap + PGCConfig.WEAPON_MAX_LEVEL_BONUS.get() : cap;
    }

    public static int requirement(int level) {
        if (level < 0) return NEGATIVE_LEVEL_REQUIREMENT;
        var listed = PGCConfig.WEAPON_LEVEL_XP_REQUIREMENTS.get();
        if (level >= 1 && level <= listed.size()) return listed.get(level - 1);
        var growth = PGCConfig.WEAPON_LEVEL_XP_GROWTH.get() / PERCENT;
        return (int) Math.max(1L, Math.round(PGCConfig.WEAPON_LEVEL_XP_BASE.get() * Math.pow(1.0D + growth, level - 1)));
    }

    public static int investedXp(int level) {
        var total = 0;
        for (var step = 1; step < level; step++) total += requirement(step);
        return total;
    }

    public static int xpPerOre(Item ore) {
        if (ore == PGCItems.ENHANCEMENT_ORE.get()) return PGCConfig.ENHANCEMENT_ORE_XP.get();
        if (ore == PGCItems.FINE_ENHANCEMENT_ORE.get()) return PGCConfig.FINE_ENHANCEMENT_ORE_XP.get();
        if (ore == PGCItems.MYSTIC_ENHANCEMENT_ORE.get()) return PGCConfig.MYSTIC_ENHANCEMENT_ORE_XP.get();
        return 0;
    }

    public static boolean isEnhancementOre(ItemStack stack) {
        return xpPerOre(stack.getItem()) > 0;
    }

    public static int materialRank(ItemStack stack) {
        var item = stack.getItem();
        if (item == PGCItems.MYSTIC_ENHANCEMENT_ORE.get()) return MYSTIC_ORE_RANK;
        if (item == PGCItems.FINE_ENHANCEMENT_ORE.get()) return FINE_ORE_RANK;
        if (isEnhancementOre(stack)) return ORE_RANK;
        if (stack.is(PGCItems.CUSTOM_SUPERIMPOSER.get())) return SUPERIMPOSER_RANK;
        if (item instanceof WishWeapon) return WishWeaponItem.isFiveStar(stack) ? FIVE_STAR_RANK : FOUR_STAR_RANK;
        return UNKNOWN_RANK;
    }

    public static boolean isEnhancementMaterial(ItemStack stack, ItemStack weapon) {
        if (stack.isEmpty()) return false;
        if (isEnhancementOre(stack)) return true;
        if (stack.is(PGCItems.CUSTOM_SUPERIMPOSER.get())) return true;
        return !weapon.isEmpty() && stack.getItem() == weapon.getItem();
    }

    public static boolean isEnhancedWeapon(ItemStack stack) {
        if (!(stack.getItem() instanceof WishWeapon)) return false;
        var state = WeaponState.of(stack);
        return state.level() > WeaponState.INITIAL.level() || state.xp() > 0 || state.refinements() > WeaponState.INITIAL.refinements();
    }

    public static List<ItemStack> inherit(ItemStack weapon, ItemStack material) {
        var refunds = new ArrayList<ItemStack>();
        if (!(weapon.getItem() instanceof WishWeapon)) return List.copyOf(refunds);
        var source = WeaponState.of(material);
        var state = WeaponState.of(weapon);
        var body = source.duplicateLayers() + 1;
        var superimposer = source.superimposerLayers();
        var room = Math.max(0, MAX_REFINEMENT - state.refinements());
        var applied = Math.min(body + superimposer, room);
        var appliedBody = Math.min(body, applied);
        var appliedSuperimposer = applied - appliedBody;
        if (applied > 0) state = state.duplicatedBy(appliedBody).superimposedBy(appliedSuperimposer);
        refundRefinements(refunds, body - appliedBody, superimposer - appliedSuperimposer, WishWeapon.isFiveStar(material));

        var transferred = investedXp(source.level()) + source.xp();
        var invested = investedXp(state.level()) + state.xp();
        var cap = maxLevel(state.refinements());
        var level = state.level();
        var xp = state.xp() + transferred;
        while (level < cap && xp >= requirement(level)) {
            xp -= requirement(level);
            level++;
        }
        if (level >= cap) {
            level = cap;
            xp = 0;
        }
        var overflow = transferred - (investedXp(level) + xp - invested);
        WeaponState.set(weapon, state.leveledTo(level, xp));
        if (overflow > 0) refundXp(refunds, overflow);
        return List.copyOf(refunds);
    }

    public static Preview preview(ItemStack weapon, List<ItemStack> materials, double discount) {
        var state = WeaponState.of(weapon);
        var refinement = state.refinements();
        var layers = 0;
        var transferred = 0;
        for (var material : materials) {
            if (isEnhancedWeapon(material)) {
                var source = WeaponState.of(material);
                layers += source.duplicateLayers() + 1 + source.superimposerLayers();
                transferred += investedXp(source.level()) + source.xp();
                continue;
            }
            layers += Math.max(0, gainOf(weapon, material));
        }
        var applied = Math.clamp(MAX_REFINEMENT - refinement, 0, layers);
        var cap = maxLevel(state.refinements() + applied);
        var level = state.level();
        var xp = state.xp() + transferred;
        while (level < cap && xp >= requirement(level)) {
            xp -= requirement(level);
            level++;
        }
        if (level >= cap) {
            level = cap;
            xp = 0;
        }
        for (var material : materials) {
            var value = xpPerOre(material.getItem());
            if (value <= 0 || level >= cap) continue;
            var consumed = material.getCount() - (int) (material.getCount() * discount);
            for (var step = 0; step < consumed && level < cap; step++) {
                xp += value;
                while (level < cap && xp >= requirement(level)) {
                    xp -= requirement(level);
                    level++;
                }
            }
        }
        return new Preview(level, level >= cap ? 0 : xp, refinement + applied);
    }

    public static int levelOf(ItemStack weapon) {
        return WeaponState.of(weapon).level();
    }

    public static boolean grantLevels(ItemStack weapon, int amount, int cap) {
        var state = WeaponState.of(weapon);
        if (amount <= 0 || state.level() >= cap) return false;
        WeaponState.set(weapon, state.leveledTo(Math.min(cap, state.level() + amount), state.xp()));
        return true;
    }

    public static void setLevel(ItemStack weapon, int level) {
        var state = WeaponState.of(weapon);
        WeaponState.set(weapon, state.leveledTo(level, state.xp()));
    }

    public static boolean canGrantRefinement(ItemStack weapon, Player player) {
        if (!(weapon.getItem() instanceof WishWeapon)) return true;
        var state = WeaponState.of(weapon);
        return state.refinements() >= MAX_REFINEMENT && extraRefinement(player, weapon) >= MAX_TEMPORARY_REFINEMENT;
    }

    public static boolean grantRefinement(ItemStack weapon, Player player) {
        if (canGrantRefinement(weapon, player)) return false;
        var state = WeaponState.of(weapon);
        var permanent = state.refinements() < MAX_REFINEMENT;
        WeaponState.set(weapon, permanent ? state.duplicatedBy(1) : state.temporarilyBy(1));
        return true;
    }

    public static int refinementOf(Player player, ItemStack weapon) {
        return WeaponState.of(weapon).refinements() + extraRefinement(player, weapon);
    }

    public static int extraRefinement(Player player, ItemStack weapon) {
        if (!(weapon.getItem() instanceof WishWeapon)) return 0;
        var temporary = WeaponState.of(weapon).temporaryRefinements();
        var bonus = player == null ? 0 : Curios.refinementBonus(player);
        return Math.min(MAX_TEMPORARY_REFINEMENT, temporary + bonus);
    }

    public static int enhance(ItemStack weapon, ItemStack ore, double discount) {
        var value = xpPerOre(ore.getItem());
        if (value <= 0) return 0;
        var state = WeaponState.of(weapon);
        var cap = maxLevel(state.refinements());
        var consumed = 0;
        while (consumed < ore.getCount() && state.level() < cap) {
            state = advance(state, value, cap);
            consumed++;
        }
        if (consumed <= 0) return 0;
        WeaponState.set(weapon, state);
        ore.shrink(Mth.clamp(consumed - (int) (consumed * discount), 0, consumed));
        return consumed;
    }

    public static boolean refine(ItemStack weapon, ItemStack material) {
        if (!(weapon.getItem() instanceof WishWeapon)) return false;
        var gain = gainOf(weapon, material);
        if (gain <= 0) return false;
        var state = WeaponState.of(weapon);
        if (state.refinements() + gain > MAX_REFINEMENT) return false;
        WeaponState.set(weapon, isSuperimposer(material) ? state.superimposedBy(gain) : state.duplicatedBy(gain));
        return true;
    }

    public static List<ItemStack> refund(WeaponRecovery recovery) {
        var state = recovery.state();
        var stacks = new ArrayList<ItemStack>();
        refundXp(stacks, investedXp(state.level()) + state.xp());
        refundRefinements(stacks, state.duplicateLayers(), state.superimposerLayers(), recovery.fiveStar());
        if (recovery.fiveStar()) add(stacks, PGCItems.LUCENT_AFTERGLOW.get(), AFTERGLOW_PER_FIVE_STAR);
        return List.copyOf(stacks);
    }

    private static void addXp(List<ItemStack> stacks, Item ore, int xp) {
        add(stacks, ore, xp / xpPerOre(ore));
    }

    private static void refundXp(List<ItemStack> stacks, int xp) {
        var firstTier = investedXp(FIRST_TIER);
        var secondTier = investedXp(SECOND_TIER);
        addXp(stacks, PGCItems.ENHANCEMENT_ORE.get(), Math.min(xp, firstTier));
        addXp(stacks, PGCItems.FINE_ENHANCEMENT_ORE.get(), Math.max(0, Math.min(xp, secondTier) - firstTier));
        addXp(stacks, PGCItems.MYSTIC_ENHANCEMENT_ORE.get(), Math.max(0, xp - secondTier));
    }

    private static void refundRefinements(List<ItemStack> stacks, int bodyLayers, int superimposerLayers, boolean fiveStar) {
        if (fiveStar) {
            add(stacks, PGCItems.CUSTOM_SUPERIMPOSER.get(), bodyLayers + superimposerLayers);
            return;
        }
        add(stacks, PGCItems.MASTERLESS_STARGLITTER.get(), bodyLayers * STARGLITTER_PER_DUPLICATE);
        add(stacks, PGCItems.CUSTOM_SUPERIMPOSER.get(), superimposerLayers);
    }

    private static void add(List<ItemStack> stacks, Item item, int count) {
        if (count > 0) stacks.add(new ItemStack(item, count));
    }

    private static WeaponState advance(WeaponState state, int value, int cap) {
        var xp = state.xp() + value;
        var level = state.level();
        while (level < cap && xp >= requirement(level)) {
            xp -= requirement(level);
            level++;
        }
        return level >= cap ? state.leveledTo(cap, 0) : state.leveledTo(level, xp);
    }

    private static int gainOf(ItemStack weapon, ItemStack material) {
        if (isSuperimposer(material)) return 1;
        if (material.getItem() != weapon.getItem()) return 0;
        return WeaponState.of(material).refinements();
    }

    private static boolean isSuperimposer(ItemStack material) {
        return material.is(PGCItems.CUSTOM_SUPERIMPOSER.get());
    }

    public record Preview(int level, int xp, int refinement) {
    }
}
