package net.per.primogemcraft.collab.teyvatdelight;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.random.RandomGenerator;

record StellarTaskPlan(int kind, List<Ingredient> ingredients, String reward, int amount, int grade) {
    static final int EVENT = 0;
    static final int MATERIAL = 2;
    static final int ENCHANT = 3;
    private static final List<Reward> REWARDS = List.of(
            new Reward("enhancement_ore", 250, 2, 4, 2, 4, false),
            new Reward("fine_enhancement_ore", 160, 1, 2, 3, 5, false),
            new Reward("dust_of_azoth", 130, 1, 2, 3, 5, false),
            new Reward("cosmic_fragment", 210, 4, 8, 3, 5, false),
            new Reward("elemental_dissolving_bead_dust", 80, 1, 1, 4, 6, false),
            new Reward("unidentified_doll", 40, 1, 1, 6, 8, true),
            new Reward("acquaint_fate", 60, 1, 1, 6, 8, true),
            new Reward("intertwined_fate", 40, 1, 1, 7, 8, true),
            new Reward("star_rail_special_pass", 1, 1, 1, 8, 8, true),
            new Reward("lucent_afterglow", 3, 1, 1, 8, 8, true),
            new Reward("primogem", 10, 1, 1, 4, 6, false),
            new Reward("mora", 10, 4, 8, 3, 5, false),
            new Reward("teyvatdelight:primogem", 3, 1, 1, 4, 6, false),
            new Reward("teyvatdelight:mora", 3, 4, 8, 3, 5, false));

    static List<StellarTaskPlan> generate(RandomGenerator random, List<String> foods) {
        var result = new ArrayList<StellarTaskPlan>();
        if (!foods.isEmpty()) {
            var kind = random.nextInt(4) == 0 ? ENCHANT : EVENT;
            var grade = kind == ENCHANT && random.nextInt(4) == 0 ? 1 : 0;
            result.add(new StellarTaskPlan(kind, ingredients(random, foods, grade == 1 ? 6 : 4, grade == 1 ? 8 : 6, false), "", 1, grade));
            var rare = false;
            for (var index = 0; index < 2; index++) {
                var reward = reward(random, rare);
                rare |= reward.rare();
                result.add(new StellarTaskPlan(MATERIAL,
                        ingredients(random, foods, reward.minCost(), reward.maxCost(), reward.rare()),
                        reward.item(), random.nextInt(reward.minAmount(), reward.maxAmount() + 1), 0));
            }
        }
        var scrap = random.nextBoolean();
        result.add(new StellarTaskPlan(MATERIAL, List.of(new Ingredient("trash", 1)),
                scrap ? "enhancement_ore" : "cosmic_fragment", 2 * (scrap ? random.nextInt(1, 3) : random.nextInt(3, 6)), 0));
        var prettyReward = random.nextInt(3);
        result.add(new StellarTaskPlan(MATERIAL, List.of(new Ingredient("pleasant_looking_trash", 1)),
                prettyReward == 0 ? "fine_enhancement_ore" : prettyReward == 1 ? "dust_of_azoth" : "cosmic_fragment",
                2 * (prettyReward == 2 ? random.nextInt(5, 9) : random.nextInt(1, 3)), 0));
        return List.copyOf(result);
    }

    private static Reward reward(RandomGenerator random, boolean rareUsed) {
        var pool = REWARDS.stream().filter(reward -> !rareUsed || !reward.rare()).toList();
        var roll = random.nextInt(pool.stream().mapToInt(Reward::weight).sum());
        for (var reward : pool) {
            roll -= reward.weight();
            if (roll < 0) return reward;
        }
        throw new IllegalStateException("Empty stellar task reward pool");
    }

    private static List<Ingredient> ingredients(RandomGenerator random, List<String> foods, int minimum, int maximum, boolean rare) {
        var pool = new ArrayList<>(new LinkedHashSet<>(foods));
        var total = random.nextInt(minimum, maximum + 1);
        var limit = Math.min(3, pool.size());
        var types = rare ? limit : random.nextInt(1, Math.min(limit, total) + 1);
        var counts = new int[types];
        java.util.Arrays.fill(counts, 1);
        for (var index = types; index < total; index++) counts[random.nextInt(types)]++;
        var result = new ArrayList<Ingredient>();
        for (var count : counts) result.add(new Ingredient(pool.remove(random.nextInt(pool.size())), count));
        return List.copyOf(result);
    }

    record Ingredient(String item, int count) {
    }

    private record Reward(String item, int weight, int minAmount, int maxAmount, int minCost, int maxCost, boolean rare) {
    }
}
