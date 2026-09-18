package net.per.primogemcraft.item.curio;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.per.primogemcraft.registry.PGCDataComponents;
import net.per.primogemcraft.system.curio.CurioContext;
import net.per.primogemcraft.system.curio.CurioForm;
import net.per.primogemcraft.system.curio.CurioItem;
import net.per.primogemcraft.system.curio.CurioTrigger;
import net.per.primogemcraft.system.weapon.WeaponAttributes;
import net.per.primogemcraft.system.weapon.WeaponEnhancement;
import net.per.primogemcraft.system.weapon.WeaponState;
import net.per.primogemcraft.system.weapon.WishWeapon;

import java.util.List;

public class CharmonyFestivalItem extends CurioItem {
    private static final String COOLDOWN = "curio/charmony_festival";
    private static final String TOOLTIP_SUFFIX = ".tooltip.";
    private static final String POINTS_SUFFIX = ".tooltip.points";
    private static final String UNAVAILABLE_KEY = "message.primogemcraft.curio.charmony_festival.unavailable";
    private static final int COOLDOWN_TICKS = 24000;
    private static final int FIXED_LINES = 4;
    private static final int BLANK_LINE = 4;
    private static final int TAIL_LINE = 5;
    private static final double BREAK_CHANCE = 0.3D;
    private static final float RESTORED_HEALTH = 0.5F;
    private static final int RESTORED_FOOD = 10;
    private static final float RESTORED_SATURATION = 10.0F;

    public CharmonyFestivalItem(Properties properties) {
        super(CurioTrigger.RIGHT_CLICK, CurioForm.FUSION, 1, properties);
    }

    @Override
    public void presence(CurioContext context) {
        if (!context.ready(COOLDOWN, COOLDOWN_TICKS)) return;
        var player = context.player();
        player.heal(player.getMaxHealth() * RESTORED_HEALTH);
        var food = player.getFoodData();
        food.setFoodLevel(food.getFoodLevel() + RESTORED_FOOD);
        food.setSaturation(food.getSaturationLevel() + RESTORED_SATURATION);
        context.stack().set(PGCDataComponents.CURIO_COUNTER.get(), points(context.stack()) + 1);
    }

    @Override
    public void activated(CurioContext context) {
        var stack = context.stack();
        var points = points(stack);
        var weapon = context.player().getOffhandItem();
        if (points <= 0 || !(weapon.getItem() instanceof WishWeapon) || WeaponEnhancement.levelOf(weapon) >= WeaponState.INITIAL.level()) {
            context.announce(Component.translatable(UNAVAILABLE_KEY));
            return;
        }
        WeaponEnhancement.setLevel(weapon, WeaponState.INITIAL.level());
        WeaponAttributes.refreshLevel(weapon, context.player());
        stack.set(PGCDataComponents.CURIO_COUNTER.get(), points - 1);
        if (context.chance(BREAK_CHANCE)) context.damage(1);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        appendTrigger(stack, tooltip);
        var prefix = stack.getDescriptionId() + TOOLTIP_SUFFIX;
        for (var index = 0; index < FIXED_LINES; index++) tooltip.add(Component.translatable(prefix + index));
        tooltip.add(Component.translatable(prefix + BLANK_LINE));
        tooltip.add(Component.translatable(stack.getDescriptionId() + POINTS_SUFFIX, points(stack)));
        tooltip.add(Component.translatable(prefix + TAIL_LINE));
    }

    private static int points(ItemStack stack) {
        return stack.getOrDefault(PGCDataComponents.CURIO_COUNTER.get(), 0);
    }
}
