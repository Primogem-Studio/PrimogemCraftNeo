package net.per.primogemcraft.collab.genshincraft;

import net.hacker.genshincraft.element.Element.Type;
import net.hacker.genshincraft.element.ElementDamageSource;
import net.minecraft.world.damagesource.DamageSource;
import net.per.primogemcraft.system.element.Element;
import net.per.primogemcraft.system.element.ElementDamageOptions;
import net.per.primogemcraft.system.element.ElementStyle;

import static net.hacker.genshincraft.element.Element.fromType;

/**
 * GenshinCraft 联动桥接（1.20.1 / GenshinCraft 1.4.2）。
 *
 * <p>仅在检测到 GenshinCraft 安装时注册，因此未安装时不会加载本类、
 * 也不会解析任何 GenshinCraft 类型。
 *
 * <p><b>移植说明</b>：原 1.21.1 版本对接 GenshinCraft 3.1.3，1.20.1 只有 1.4.2，
 * 两者 API 差异较大。1.4.2 中<b>不存在</b>以下 API，相关联动已移除：
 * <ul>
 *   <li>{@code GenshinComponents.PERMANENT_INFUSION} / {@code PermanentInfusion}
 *       —— 武器默认元素附魔（1.4.2 无数据组件系统）</li>
 *   <li>{@code EffectRender} / {@code RegisterEffectRenderEvent}
 *       —— 自定义效果的 HUD 图标渲染</li>
 *   <li>{@code TrounceBlossom} / {@code AddTrounceBlossomLootEvent}
 *       —— 征讨之花战利品注入</li>
 *   <li>{@code GenshinCapabilities.BOSS_BAR} / {@code LivingBossBarRender}
 *       —— 丰饶僵尸的 Boss 血条</li>
 * </ul>
 * 元素伤害转换是核心联动，1.4.2 完整支持，予以保留。
 */
public final class GenshinCraftBridge {
    private static final float QUANTITY = 1.0F;
    /** 1.4.2 的 fromType 需要元素强度参数；1.0 表示标准强度。 */
    private static final float STRENGTH = 1.0F;

    private GenshinCraftBridge() {
    }

    static DamageSource element(DamageSource origin, Element element, ElementStyle style, ElementDamageOptions options) {
        var source = new ElementDamageSource(origin, fromType(type(element), QUANTITY, STRENGTH));
        // 1.4.2 的 ElementDamageSource 没有 lunar()/stellar() 风格标记，
        // 也没有 setCritical()；这两项语义在 1.4.2 无法表达，予以省略。
        if (options == null) return source;
        return source.setApply(options.attachesElement())
                .setCooldown(options.cooldown())
                .setKnockback(options.knockback());
    }

    static Element elementOf(DamageSource source) {
        return source instanceof ElementDamageSource elementSource ? element(elementSource.element.getType()) : null;
    }

    private static Type type(Element element) {
        return switch (element) {
            case ANEMO -> Type.Anemo;
            case GEO -> Type.Geo;
            case ELECTRO -> Type.Electro;
            case DENDRO -> Type.Dendro;
            case HYDRO -> Type.Hydro;
            case PYRO -> Type.Pyro;
            case CRYO -> Type.Cryo;
        };
    }

    private static Element element(Type type) {
        if (type == Type.Anemo) return Element.ANEMO;
        if (type == Type.Geo) return Element.GEO;
        if (type == Type.Electro) return Element.ELECTRO;
        if (type == Type.Dendro) return Element.DENDRO;
        if (type == Type.Hydro) return Element.HYDRO;
        if (type == Type.Pyro) return Element.PYRO;
        if (type == Type.Cryo) return Element.CRYO;
        return null;
    }
}
