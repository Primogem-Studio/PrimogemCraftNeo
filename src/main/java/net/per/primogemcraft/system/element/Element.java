package net.per.primogemcraft.system.element;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.armortrim.TrimMaterial;
import net.per.primogemcraft.item.curio.WaxSealItem;
import net.per.primogemcraft.registry.PGCAttachments;
import net.per.primogemcraft.registry.PGCDataComponents;
import net.per.primogemcraft.registry.PGCEffects;
import net.per.primogemcraft.system.curio.compat.CuriosIntegration;
import net.per.primogemcraft.util.PGCTimer;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public enum Element {
    ANEMO("anemo", 3, 2) {
        @Override
        public void apply(ServerPlayer player, double points) {
            var mode = AnemoEffectMode.of(player);
            if (points >= 2.0D && mode.allowsSlowFalling()) show(player, MobEffects.SLOW_FALLING, 100, (int) (points * 0.5D - 1.0D));
            if (points >= 4.0D && mode.allowsJump()) show(player, MobEffects.JUMP, 100, (int) (points + 2.0D));
            if (points >= 8.0D) silent(player, PGCEffects.FLIGHT, 60, 1);
        }
    },
    GEO("geo", 2, 0) {
        @Override
        public void apply(ServerPlayer player, double points) {
            if (points >= 2.0D) show(player, MobEffects.DAMAGE_RESISTANCE, 100, amplifier(points - 7.0D));
            if (points < 4.0D) return;
            if (player.hasEffect(PGCEffects.DAMAGE_ABSORPTION_COOLDOWN) || player.hasEffect(MobEffects.ABSORPTION)) return;
            show(player, MobEffects.ABSORPTION, 900, (int) (points * 0.5D - 1.0D));
            silent(player, PGCEffects.DAMAGE_ABSORPTION_COOLDOWN, 900, 0);
        }
    },
    ELECTRO("electro", 2, 0) {
        @Override
        public void apply(ServerPlayer player, double points) {
            if (points >= 2.0D && player.hasEffect(PGCEffects.MAGATAMA))
                show(player, MobEffects.DAMAGE_RESISTANCE, 60, amplifier(points - 6.0D));
            if (points < 4.0D || !player.isInWaterRainOrBubble()) return;
            show(player, MobEffects.DAMAGE_BOOST, 60, amplifier(points * 0.5D - 2.0D));
            show(player, MobEffects.MOVEMENT_SPEED, 60, amplifier(points * 0.5D - 3.0D));
        }
    },
    DENDRO("dendro", 2, 0) {
        @Override
        public void apply(ServerPlayer player, double points) {
            if (points >= 2.0D && PGCTimer.isDone(player, SATURATION_TIMER)) {
                PGCTimer.set(player, SATURATION_TIMER, 400 - (int) points * 2);
                show(player, MobEffects.SATURATION, 10, 0);
            }
            if (points < 4.0D) {
                player.removeEffect(PGCEffects.DENDRO_SET);
                return;
            }
            player.forceAddEffect(new MobEffectInstance(PGCEffects.DENDRO_SET, -1, (int) (points - 1.0D), false, false), null);
        }
    },
    HYDRO("hydro", 2, 0) {
        @Override
        public void apply(ServerPlayer player, double points) {
            if (points >= 2.0D) show(player, MobEffects.CONDUIT_POWER, 80, 0);
            if (points < 4.0D || player.hasEffect(PGCEffects.PURIFIED_ARMOR_EFFECT_LIMIT) || !player.isAlive()) return;
            if (points < 6.0D) {
                player.heal((float) (player.getMaxHealth() * points * 0.5D * 0.02D));
                silent(player, PGCEffects.PURIFIED_ARMOR_EFFECT_LIMIT, (int) (points * 20.0D), 0);
                return;
            }
            player.heal((float) (player.getMaxHealth() * points * 0.5D * 0.03D));
            silent(player, PGCEffects.PURIFIED_ARMOR_EFFECT_LIMIT, (int) (points * 10.0D), 0);
        }
    },
    PYRO("pyro", 2, 2) {
        @Override
        public void apply(ServerPlayer player, double points) {
            if (points >= 2.0D) show(player, MobEffects.FIRE_RESISTANCE, 80, 0);
            if (points < 4.0D) return;
            var amplifier = (int) Math.round(points - 1.0D);
            var current = player.getEffect(PGCEffects.BURNING_RETALIATION);
            if (current != null && current.getAmplifier() != amplifier) player.removeEffect(PGCEffects.BURNING_RETALIATION);
            silent(player, PGCEffects.BURNING_RETALIATION, 60, amplifier);
        }
    },
    CRYO("cryo", 2, 4) {
        @Override
        public void apply(ServerPlayer player, double points) {
            if (points >= 2.0D) show(player, PGCEffects.WARM_POWDER_SNOW, 60, (int) ((points - 1.0D) * 0.5D));
            if (points >= 4.0D) show(player, PGCEffects.POWDER_SNOW_BACKLASH, 60, (int) ((points - 1.0D) * 0.5D));
            var walksOnPowderSnow = points >= 2.0D;
            if (player.getData(PGCAttachments.POWDER_SNOW_WALK) != walksOnPowderSnow) player.setData(PGCAttachments.POWDER_SNOW_WALK, walksOnPowderSnow);
        }
    };

    private static final String SATURATION_TIMER = "element_dendro_saturation";

    private final String id;
    private final int trimTooltipShiftLines;
    private final int trimTooltipControlLines;

    Element(String id, int trimTooltipShiftLines, int trimTooltipControlLines) {
        this.id = id;
        this.trimTooltipShiftLines = trimTooltipShiftLines;
        this.trimTooltipControlLines = trimTooltipControlLines;
    }

    public String id() {
        return id;
    }

    public int trimTooltipShiftLines() {
        return trimTooltipShiftLines;
    }

    public int trimTooltipControlLines() {
        return trimTooltipControlLines;
    }

    public abstract void apply(ServerPlayer player, double points);

    /**
     * The value this element is stored as in the {@code element_type} component.
     */
    public int index() {
        return ordinal() + 1;
    }

    public static Element byIndex(int index) {
        for (var element : values()) if (element.index() == index) return element;
        return null;
    }

    public static Element of(ItemStack stack) {
        var index = stack.get(PGCDataComponents.ELEMENT_TYPE.get());
        return index == null ? null : byIndex(index);
    }

    public static Element ofTrimMaterial(Holder<TrimMaterial> material) {
        var location = material.unwrapKey().map(ResourceKey::location).orElse(null);
        if (location == null || !MOD_ID.equals(location.getNamespace())) return null;
        for (var element : values()) if (element.id.equals(location.getPath())) return element;
        return null;
    }

    public static boolean holdsWaxSeal(Player player, Element element) {
        if (player == null) return false;
        if (player.getInventory().contains(stack -> isSealOf(stack, element))) return true;
        for (var stack : CuriosIntegration.equipped(player)) if (isSealOf(stack, element)) return true;
        return false;
    }

    private static boolean isSealOf(ItemStack stack, Element element) {
        return stack.getItem() instanceof WaxSealItem seal && seal.element() == element;
    }

    private static void show(LivingEntity entity, Holder<MobEffect> effect, int duration, int amplifier) {
        entity.addEffect(new MobEffectInstance(effect, duration, amplifier, true, false));
    }

    private static int amplifier(double value) {
        return Math.max(0, (int) value);
    }

    private static void silent(LivingEntity entity, Holder<MobEffect> effect, int duration, int amplifier) {
        entity.addEffect(new MobEffectInstance(effect, duration, amplifier, false, false));
    }
}
