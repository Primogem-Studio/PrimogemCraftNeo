package net.per.primogemcraft.system.element;

import net.per.primogemcraft.compat.attachment.Attachments;
import net.per.primogemcraft.compat.component.StackComponents;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.trim.ArmorTrimMaterial;
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
        public void apply(ServerPlayerEntity player, double points) {
            var mode = AnemoEffectMode.of(player);
            if (points >= 2.0D && mode.allowsSlowFalling()) show(player, StatusEffects.SLOW_FALLING, 100, (int) (points * 0.5D - 1.0D));
            if (points >= 4.0D && mode.allowsJump()) show(player, StatusEffects.JUMP_BOOST, 100, (int) (points + 2.0D));
            if (points >= 8.0D) silent(player, PGCEffects.FLIGHT.get(), 60, 1);
        }
    },
    GEO("geo", 2, 0) {
        @Override
        public void apply(ServerPlayerEntity player, double points) {
            if (points >= 2.0D) show(player, StatusEffects.RESISTANCE, 100, amplifier(points - 7.0D));
            if (points < 4.0D) return;
            if (player.hasStatusEffect(PGCEffects.DAMAGE_ABSORPTION_COOLDOWN.get()) || player.hasStatusEffect(StatusEffects.ABSORPTION)) return;
            show(player, StatusEffects.ABSORPTION, 900, (int) (points * 0.5D - 1.0D));
            silent(player, PGCEffects.DAMAGE_ABSORPTION_COOLDOWN.get(), 900, 0);
        }
    },
    ELECTRO("electro", 2, 0) {
        @Override
        public void apply(ServerPlayerEntity player, double points) {
            if (points >= 2.0D && player.hasStatusEffect(PGCEffects.MAGATAMA.get()))
                show(player, StatusEffects.RESISTANCE, 60, amplifier(points - 6.0D));
            if (points < 4.0D || !player.isTouchingWaterOrRain()) return;
            show(player, StatusEffects.STRENGTH, 60, amplifier(points * 0.5D - 2.0D));
            show(player, StatusEffects.SPEED, 60, amplifier(points * 0.5D - 3.0D));
        }
    },
    DENDRO("dendro", 2, 0) {
        @Override
        public void apply(ServerPlayerEntity player, double points) {
            if (points >= 2.0D && PGCTimer.isDone(player, SATURATION_TIMER)) {
                PGCTimer.set(player, SATURATION_TIMER, 400 - (int) points * 2);
                show(player, StatusEffects.SATURATION, 10, 0);
            }
            if (points < 4.0D) {
                player.removeStatusEffect(PGCEffects.DENDRO_SET.get());
                return;
            }
            player.addStatusEffect(new StatusEffectInstance(PGCEffects.DENDRO_SET.get(), -1, (int) (points - 1.0D), false, false), null);
        }
    },
    HYDRO("hydro", 2, 0) {
        @Override
        public void apply(ServerPlayerEntity player, double points) {
            if (points >= 2.0D) show(player, StatusEffects.CONDUIT_POWER, 80, 0);
            if (points < 4.0D || player.hasStatusEffect(PGCEffects.PURIFIED_ARMOR_EFFECT_LIMIT.get()) || !player.isAlive()) return;
            if (points < 6.0D) {
                player.heal((float) (player.getMaxHealth() * points * 0.5D * 0.02D));
                silent(player, PGCEffects.PURIFIED_ARMOR_EFFECT_LIMIT.get(), (int) (points * 20.0D), 0);
                return;
            }
            player.heal((float) (player.getMaxHealth() * points * 0.5D * 0.03D));
            silent(player, PGCEffects.PURIFIED_ARMOR_EFFECT_LIMIT.get(), (int) (points * 10.0D), 0);
        }
    },
    PYRO("pyro", 2, 2) {
        @Override
        public void apply(ServerPlayerEntity player, double points) {
            if (points >= 2.0D) show(player, StatusEffects.FIRE_RESISTANCE, 80, 0);
            if (points < 4.0D) return;
            var amplifier = (int) Math.round(points - 1.0D);
            var current = player.getStatusEffect(PGCEffects.BURNING_RETALIATION.get());
            if (current != null && current.getAmplifier() != amplifier) player.removeStatusEffect(PGCEffects.BURNING_RETALIATION.get());
            silent(player, PGCEffects.BURNING_RETALIATION.get(), 60, amplifier);
        }
    },
    CRYO("cryo", 2, 4) {
        @Override
        public void apply(ServerPlayerEntity player, double points) {
            if (points >= 2.0D) show(player, PGCEffects.WARM_POWDER_SNOW.get(), 60, (int) ((points - 1.0D) * 0.5D));
            if (points >= 4.0D) show(player, PGCEffects.POWDER_SNOW_BACKLASH.get(), 60, (int) ((points - 1.0D) * 0.5D));
            var walksOnPowderSnow = points >= 2.0D;
            if (Attachments.get(player, PGCAttachments.POWDER_SNOW_WALK) != walksOnPowderSnow) Attachments.set(player, PGCAttachments.POWDER_SNOW_WALK, walksOnPowderSnow);
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

    public abstract void apply(ServerPlayerEntity player, double points);

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
        var index = StackComponents.get(stack, PGCDataComponents.ELEMENT_TYPE);
        return index == null ? null : byIndex(index);
    }

    public static Element ofTrimMaterial(RegistryEntry<ArmorTrimMaterial> material) {
        if (material == null) return null;
        var location = material.getKey().map(RegistryKey::getValue).orElse(null);
        if (location == null || !MOD_ID.equals(location.getNamespace())) return null;
        for (var element : values()) if (element.id.equals(location.getPath())) return element;
        return null;
    }

    public static boolean holdsWaxSeal(PlayerEntity player, Element element) {
        if (player == null) return false;
        if (net.per.primogemcraft.compat.item.Inventories120.any(player.getInventory(), stack -> isSealOf(stack, element))) return true;
        for (var stack : CuriosIntegration.equipped(player)) if (isSealOf(stack, element)) return true;
        return false;
    }

    private static boolean isSealOf(ItemStack stack, Element element) {
        return stack.getItem() instanceof WaxSealItem seal && seal.element() == element;
    }

    private static void show(LivingEntity entity, StatusEffect effect, int duration, int amplifier) {
        entity.addStatusEffect(new StatusEffectInstance(effect, duration, amplifier, true, false));
    }

    private static int amplifier(double value) {
        return Math.max(0, (int) value);
    }

    private static void silent(LivingEntity entity, StatusEffect effect, int duration, int amplifier) {
        entity.addStatusEffect(new StatusEffectInstance(effect, duration, amplifier, false, false));
    }
}
