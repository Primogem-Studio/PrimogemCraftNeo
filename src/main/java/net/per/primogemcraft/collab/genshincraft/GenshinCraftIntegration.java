package net.per.primogemcraft.collab.genshincraft;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.per.primogemcraft.config.PGCConfig;
import net.per.primogemcraft.system.element.Element;
import net.per.primogemcraft.system.element.ElementDamageOptions;
import net.per.primogemcraft.system.element.ElementStyle;

/**
 * The GenshinCraft half of the element system. GenshinCraft is an optional dependency: every accessor here
 * answers for this mod alone while the mod is absent, and only then does {@link GenshinCraftBridge} — the
 * one class that touches GenshinCraft types — take over.
 */
public final class GenshinCraftIntegration {
    private static final String GENSHINCRAFT_MOD_ID = "genshincraft";
    private static final double DEFAULT_HEALTH_CONVERSION = 1.0D;
    private static final int DEFAULT_SCALE = 1;
    private static final boolean loaded = FabricLoader.getInstance().isModLoaded(GENSHINCRAFT_MOD_ID);

    private GenshinCraftIntegration() {
    }

    public static void register() {
        // GenshinCraft 1.4.2 缺少 3.1.3 的效果渲染/战利品/能力 API，
        // 桥接已不含事件订阅，无需注册到模组事件总线。
    }

    public static DamageSource replace(DamageSource origin, Element element, ElementStyle style, ElementDamageOptions options) {
        return loaded ? GenshinCraftBridge.element(origin, element, style, options) : origin;
    }

    public static DamageSource damageOf(ItemStack stack, DamageSource origin) {
        if (!loaded) return origin;
        try {
            var helper = Class.forName("net.hacker.genshincraft.misc.Helper");
            var method = helper.getMethod("getElementDamage", ItemStack.class, DamageSource.class);
            return (DamageSource) method.invoke(null, stack, origin);
        } catch (ReflectiveOperationException e) {
            return origin;
        }
    }

    public static Element elementOf(DamageSource source) {
        return loaded ? GenshinCraftBridge.elementOf(source) : null;
    }

    public static double healthConversionScale() {
        return loaded ? PGCConfig.GENSHINCRAFT_HEALTH_CONVERSION_SCALE.get() : DEFAULT_HEALTH_CONVERSION;
    }

    public static int regenerationScale() {
        return loaded ? PGCConfig.GENSHINCRAFT_REGENERATION_SCALE.get() : DEFAULT_SCALE;
    }

    public static int bubbleHeal() {
        return loaded ? PGCConfig.GENSHINCRAFT_BUBBLE_HEAL.get() : DEFAULT_SCALE;
    }

    public static int freezeDamageScale() {
        return loaded ? PGCConfig.GENSHINCRAFT_FREEZE_DAMAGE_SCALE.get() : DEFAULT_SCALE;
    }

    public static double geoWeaknessAmplifier(double value) {
        return loaded ? PGCConfig.GENSHINCRAFT_GEO_WEAKNESS_AMPLIFIER.get() : value;
    }
}
