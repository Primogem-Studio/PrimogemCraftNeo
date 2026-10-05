package net.per.primogemcraft.collab.teyvatdelight;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.random.RandomGenerator;

record StellarTaskPlan(int kind, List<Ingredient> ingredients, String reward, int amount, int grade) {
    static final int EVENT = 0;
    static final int MATERIAL = 2;
    static final int ENCHANT = 3;
    static final int MAX_COUNT = 9;
    private static final List<Reward> REWARDS = List.of(
            new Reward("enhancement_ore", 120, 12, 32, 1, 3),
            new Reward("fine_enhancement_ore", 100, 6, 16, 1, 3),
            new Reward("dust_of_azoth", 90, 4, 12, 1, 3),
            new Reward("cosmic_fragment", 150, 32, 64, 1, 3),
            new Reward("elemental_dissolving_bead_dust", 70, 3, 8, 1, 3),
            new Reward("unidentified_doll", 60, 2, 4, 2, 4),
            new Reward("acquaint_fate", 80, 2, 5, 2, 4),
            new Reward("intertwined_fate", 60, 2, 4, 2, 4),
            new Reward("star_rail_special_pass", 12, 1, 3, 2, 4),
            new Reward("lucent_afterglow", 24, 2, 6, 2, 4),
            new Reward("primogem", 70, 4, 12, 1, 3),
            new Reward("mora", 70, 24, 64, 1, 3),
            new Reward("teyvatdelight:primogem", 40, 3, 8, 1, 3),
            new Reward("teyvatdelight:mora", 40, 16, 48, 1, 3));

    static List<StellarTaskPlan> generate(RandomGenerator random, List<String> foods) {
        var result = new ArrayList<StellarTaskPlan>();
        var count = random.nextInt(6, MAX_COUNT + 1);
        for (var index = 0; index < count; index++) {
            var kind = random.nextInt(10);
            if (kind == 0) {
                result.add(new StellarTaskPlan(EVENT, ingredients(random, foods, 1, 2), "", 1, 0));
            } else if (kind < 3) {
                var grade = random.nextInt(1, 4);
                result.add(new StellarTaskPlan(ENCHANT, ingredients(random, foods, 1, 3), "", 1, grade));
            } else {
                var reward = reward(random);
                result.add(new StellarTaskPlan(MATERIAL,
                        ingredients(random, foods, reward.minCost(), reward.maxCost()),
                        reward.item(), Math.max(1, random.nextInt(reward.minAmount(), reward.maxAmount() + 1) / 2), 0));
            }
        }
        return List.copyOf(result);
    }

    private static Reward reward(RandomGenerator random) {
        var roll = random.nextInt(REWARDS.stream().mapToInt(Reward::weight).sum());
        for (var reward : REWARDS) {
            roll -= reward.weight();
            if (roll < 0) return reward;
        }
        throw new IllegalStateException("Empty stellar task reward pool");
    }

    private static List<Ingredient> ingredients(RandomGenerator random, List<String> foods, int minimum, int maximum) {
        if (foods.isEmpty() || random.nextInt(4) == 0)
            return List.of(new Ingredient(random.nextBoolean() ? "trash" : "pleasant_looking_trash", 1));
        var pool = new ArrayList<>(new LinkedHashSet<>(foods));
        var total = random.nextInt(minimum, maximum + 1);
        var limit = Math.min(2, pool.size());
        var types = random.nextInt(1, Math.min(limit, total) + 1);
        var counts = new int[types];
        java.util.Arrays.fill(counts, 1);
        for (var index = types; index < total; index++) counts[random.nextInt(types)]++;
        var result = new ArrayList<Ingredient>();
        for (var count : counts) result.add(new Ingredient(pool.remove(random.nextInt(pool.size())), count));
        return List.copyOf(result);
    }

    record Ingredient(String item, int count) {
    }

    private record Reward(String item, int weight, int minAmount, int maxAmount, int minCost, int maxCost) {
    }
}
