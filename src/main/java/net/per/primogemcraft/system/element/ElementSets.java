package net.per.primogemcraft.system.element;
import net.minecraft.registry.entry.RegistryEntry;
import net.per.primogemcraft.compat.component.VanillaComponents;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.trim.ArmorTrimPattern;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.per.primogemcraft.registry.PGCEffects;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.system.curio.compat.CuriosIntegration;
import net.per.primogemcraft.util.PGCTimer;
import java.util.*;
import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public final class ElementSets {
    public static final double BEAD_POINTS = 8.0D;

    private static final Identifier UPGRADE_II = id("elemental_upgrade_ii");
    private static final Identifier UPGRADE_III = id("elemental_upgrade_iii");
    private static final Identifier UPGRADE_IV = id("elemental_upgrade_iv");
    private static final Identifier SILENCE = new Identifier("silence");
    private static final double DEFAULT_TRIM_WEIGHT = 0.5D;
    private static final double WAX_SEAL_BONUS = 0.25D;
    private static final int INTERVAL = 10;
    private static final String TIMER = "element_sets";
    private static final double BURNING_RETALIATION_HEALTH_RATIO = 0.1D;
    private static final int BURNING_SECONDS_PER_AMPLIFIER = 3;
    private static final int COOLDOWN_TICKS_PER_AMPLIFIER = 40;

    private ElementSets() {
    }

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (var player : server.getPlayerManager().getPlayerList()) {
                if (!PGCTimer.isDone(player, TIMER)) continue;
                PGCTimer.set(player, TIMER, INTERVAL);
                evaluate(player);
            }
        });
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            var attacker = source.getAttacker();
            if (attacker == null) return true;
            retaliateBurning(entity, attacker);
            retaliateFreeze(entity, attacker);
            return true;
        });
    }

    public static void evaluate(ServerPlayerEntity player) {
        var totals = new EnumMap<Element, Double>(Element.class);
        for (var stack : player.getInventory().armor) accumulate(player, stack, totals);
        var beads = collectBeads(player);
        for (var element : Element.values()) {
            var points = totals.getOrDefault(element, 0.0D);
            if (beads.contains(element)) points = Math.max(points, BEAD_POINTS);
            if (points > 0.0D && Element.holdsWaxSeal(player, element)) points *= 1.0D + WAX_SEAL_BONUS;
            element.apply(player, points);
        }
    }

    private static void retaliateBurning(LivingEntity victim, Entity attacker) {
        var effect = victim.getStatusEffect(PGCEffects.BURNING_RETALIATION.get());
        if (effect == null) return;
        if (attacker instanceof LivingEntity living && living.hasStatusEffect(PGCEffects.RETALIATION_COOLDOWN.get())) return;
        var amplifier = effect.getAmplifier();
        if (amplifier <= 0) return;
        attacker.damage(victim.getWorld().getDamageSources().lava(), (float) (victim.getHealth() * amplifier * BURNING_RETALIATION_HEALTH_RATIO));
        attacker.setOnFireFor((int) (amplifier * BURNING_SECONDS_PER_AMPLIFIER));
        if (attacker instanceof LivingEntity living)
            living.addStatusEffect(new StatusEffectInstance(PGCEffects.RETALIATION_COOLDOWN.get(), amplifier * COOLDOWN_TICKS_PER_AMPLIFIER, 0, false, false));
    }

    private static void retaliateFreeze(LivingEntity victim, Entity attacker) {
        var effect = victim.getStatusEffect(PGCEffects.POWDER_SNOW_BACKLASH.get());
        if (effect == null || !(attacker instanceof LivingEntity living)) return;
        var amplifier = effect.getAmplifier();
        living.addStatusEffect(new StatusEffectInstance(PGCEffects.PERSISTENT_FREEZE.get(), amplifier * COOLDOWN_TICKS_PER_AMPLIFIER, amplifier, false, false));
    }

    /**
     * 1.20.1 keeps trim materials/patterns in data-driven registries, so they must be
     * resolved through a level's registry access rather than BuiltInRegistries.
     */
    private static <T> net.minecraft.registry.Registry<T> trimRegistry(ServerPlayerEntity player,
                                                                  net.minecraft.registry.RegistryKey<net.minecraft.registry.Registry<T>> key) {
        return player.getWorld().getRegistryManager().get(key);
    }

    private static void accumulate(ServerPlayerEntity player, ItemStack stack, Map<Element, Double> totals) {
        var trim = VanillaComponents.trim(stack).orElse(null);
        if (trim == null) return;
        var materialHolder = trimRegistry(player, net.minecraft.registry.RegistryKeys.TRIM_MATERIAL)
                .getEntry(net.minecraft.registry.RegistryKey.of(net.minecraft.registry.RegistryKeys.TRIM_MATERIAL, trim.material())).orElse(null);
        if (materialHolder == null) return;
        var element = Element.ofTrimMaterial(materialHolder);
        if (element == null) return;
        var patternHolder = trimRegistry(player, net.minecraft.registry.RegistryKeys.TRIM_PATTERN)
                .getEntry(net.minecraft.registry.RegistryKey.of(net.minecraft.registry.RegistryKeys.TRIM_PATTERN, trim.pattern())).orElse(null);
        if (patternHolder == null) return;
        totals.merge(element, trimWeight(patternHolder), Double::sum);
    }

    private static double trimWeight(RegistryEntry<ArmorTrimPattern> pattern) {
        var location = pattern.getKey().map(RegistryKey::getValue).orElse(null);
        if (UPGRADE_III.equals(location)) return 1.5D;
        if (UPGRADE_IV.equals(location) || SILENCE.equals(location)) return 2.0D;
        if (UPGRADE_II.equals(location)) return 1.0D;
        return DEFAULT_TRIM_WEIGHT;
    }

    private static Set<Element> collectBeads(PlayerEntity player) {
        var beads = EnumSet.noneOf(Element.class);
        collectBeads(player.getInventory().main, beads);
        collectBeads(player.getInventory().offHand, beads);
        collectBeads(CuriosIntegration.equipped(player), beads);
        return beads;
    }

    private static void collectBeads(List<ItemStack> stacks, Set<Element> beads) {
        for (var stack : stacks) {
            if (!stack.isOf(PGCItems.ELEMENTAL_MOLTEN_BEAD.get())) continue;
            var element = Element.of(stack);
            if (element != null) beads.add(element);
        }
    }

    private static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }
}
