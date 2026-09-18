package net.per.primogemcraft.system.element;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.armortrim.TrimPattern;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.per.primogemcraft.registry.PGCEffects;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.system.curio.compat.CuriosIntegration;
import net.per.primogemcraft.util.PGCTimer;

import java.util.*;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

@EventBusSubscriber(modid = MOD_ID)
public final class ElementSets {
    public static final double BEAD_POINTS = 8.0D;

    private static final ResourceLocation UPGRADE_II = id("elemental_upgrade_ii");
    private static final ResourceLocation UPGRADE_III = id("elemental_upgrade_iii");
    private static final ResourceLocation UPGRADE_IV = id("elemental_upgrade_iv");
    private static final ResourceLocation SILENCE = ResourceLocation.withDefaultNamespace("silence");
    private static final double DEFAULT_TRIM_WEIGHT = 0.5D;
    private static final double WAX_SEAL_BONUS = 0.25D;
    private static final int INTERVAL = 10;
    private static final String TIMER = "element_sets";
    private static final double BURNING_RETALIATION_HEALTH_RATIO = 0.1D;
    private static final int BURNING_SECONDS_PER_AMPLIFIER = 3;
    private static final int COOLDOWN_TICKS_PER_AMPLIFIER = 40;

    private ElementSets() {
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!PGCTimer.isDone(player, TIMER)) return;
        PGCTimer.set(player, TIMER, INTERVAL);
        evaluate(player);
    }

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        var attacker = event.getSource().getEntity();
        if (attacker == null) return;
        retaliateBurning(event.getEntity(), attacker);
        retaliateFreeze(event.getEntity(), attacker);
    }

    public static void evaluate(ServerPlayer player) {
        var totals = new EnumMap<Element, Double>(Element.class);
        for (var stack : player.getInventory().armor) accumulate(stack, totals);
        var beads = collectBeads(player);
        for (var element : Element.values()) {
            var points = totals.getOrDefault(element, 0.0D);
            if (beads.contains(element)) points = Math.max(points, BEAD_POINTS);
            if (points > 0.0D && Element.holdsWaxSeal(player, element)) points *= 1.0D + WAX_SEAL_BONUS;
            element.apply(player, points);
        }
    }

    private static void retaliateBurning(LivingEntity victim, Entity attacker) {
        var effect = victim.getEffect(PGCEffects.BURNING_RETALIATION);
        if (effect == null) return;
        if (attacker instanceof LivingEntity living && living.hasEffect(PGCEffects.RETALIATION_COOLDOWN)) return;
        var amplifier = effect.getAmplifier();
        if (amplifier <= 0) return;
        attacker.hurt(victim.level().damageSources().lava(), (float) (victim.getHealth() * amplifier * BURNING_RETALIATION_HEALTH_RATIO));
        attacker.igniteForSeconds(amplifier * BURNING_SECONDS_PER_AMPLIFIER);
        if (attacker instanceof LivingEntity living)
            living.addEffect(new MobEffectInstance(PGCEffects.RETALIATION_COOLDOWN, amplifier * COOLDOWN_TICKS_PER_AMPLIFIER, 0, false, false));
    }

    private static void retaliateFreeze(LivingEntity victim, Entity attacker) {
        var effect = victim.getEffect(PGCEffects.POWDER_SNOW_BACKLASH);
        if (effect == null || !(attacker instanceof LivingEntity living)) return;
        var amplifier = effect.getAmplifier();
        living.addEffect(new MobEffectInstance(PGCEffects.PERSISTENT_FREEZE, amplifier * COOLDOWN_TICKS_PER_AMPLIFIER, amplifier, false, false));
    }

    private static void accumulate(ItemStack stack, Map<Element, Double> totals) {
        var trim = stack.get(DataComponents.TRIM);
        if (trim == null) return;
        var element = Element.ofTrimMaterial(trim.material());
        if (element == null) return;
        totals.merge(element, trimWeight(trim.pattern()), Double::sum);
    }

    private static double trimWeight(Holder<TrimPattern> pattern) {
        var location = pattern.unwrapKey().map(ResourceKey::location).orElse(null);
        if (UPGRADE_III.equals(location)) return 1.5D;
        if (UPGRADE_IV.equals(location) || SILENCE.equals(location)) return 2.0D;
        if (UPGRADE_II.equals(location)) return 1.0D;
        return DEFAULT_TRIM_WEIGHT;
    }

    private static Set<Element> collectBeads(Player player) {
        var beads = EnumSet.noneOf(Element.class);
        collectBeads(player.getInventory().items, beads);
        collectBeads(player.getInventory().offhand, beads);
        collectBeads(CuriosIntegration.equipped(player), beads);
        return beads;
    }

    private static void collectBeads(List<ItemStack> stacks, Set<Element> beads) {
        for (var stack : stacks) {
            if (!stack.is(PGCItems.ELEMENTAL_MOLTEN_BEAD.get())) continue;
            var element = Element.of(stack);
            if (element != null) beads.add(element);
        }
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
