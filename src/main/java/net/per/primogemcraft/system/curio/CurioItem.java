package net.per.primogemcraft.system.curio;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.per.primogemcraft.item.tool.DescribedItem;
import net.per.primogemcraft.registry.PGCDataComponents;

import java.util.List;
import java.util.function.Supplier;

public class CurioItem extends DescribedItem {
    private static final String DURABILITY_KEY = "curio.primogemcraft.bar.durability";
    private static final String PROGRESS_KEY = "curio.primogemcraft.bar.progress";
    private final CurioTrigger trigger;
    private final CurioForm form;
    private final int integrity;
    private final Supplier<ItemStack> byproduct;

    public CurioItem(CurioTrigger trigger, CurioForm form, Properties properties) {
        this(trigger, form, 0, () -> ItemStack.EMPTY, properties);
    }

    public CurioItem(CurioTrigger trigger, CurioForm form, int integrity, Properties properties) {
        this(trigger, form, integrity, () -> ItemStack.EMPTY, properties);
    }

    public CurioItem(CurioTrigger trigger, CurioForm form, int integrity, Supplier<ItemStack> byproduct, Properties properties) {
        super(properties.stacksTo(1));
        this.trigger = trigger;
        this.form = form;
        this.integrity = integrity;
        this.byproduct = byproduct;
    }

    public CurioForm form() {
        return form;
    }

    public int integrity() {
        return integrity;
    }

    public ItemStack byproduct() {
        return byproduct.get();
    }

    public int presenceInterval() {
        return 1;
    }

    public int barCapacity() {
        return integrity;
    }

    public boolean barFillsUp() {
        return false;
    }

    public boolean repairsCurios() {
        return false;
    }

    public boolean discountsHertaShop() {
        return false;
    }

    public int refinementBonus() {
        return 0;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        appendTrigger(stack, tooltip);
        super.appendHoverText(stack, context, tooltip, flag);
        appendBar(stack, tooltip);
    }

    protected void appendTrigger(ItemStack stack, List<Component> tooltip) {
        if (described(stack)) tooltip.add(trigger.label());
    }

    protected void appendBar(ItemStack stack, List<Component> tooltip) {
        var bar = stack.get(PGCDataComponents.CUSTOM_BAR.get());
        if (bar == null || !bar.visible() || bar.denominator() <= 0) return;
        var fillsUp = barFillsUp();
        var shown = fillsUp ? bar.numerator() : bar.remaining();
        tooltip.add(Component.translatable(fillsUp ? PROGRESS_KEY : DURABILITY_KEY, shown, bar.denominator()));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        var food = stack.getFoodProperties(player);
        if (food != null && player.canEat(food.canAlwaysEat())) {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(stack);
        }
        if (player instanceof ServerPlayer server) activated(CurioContext.of(server, stack, form));
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (!(entity instanceof ServerPlayer player) || stack.isEmpty()) return;
        var context = CurioContext.of(player, stack, form);
        if (context.ready("curio_inventory_" + slot, presenceInterval())) presence(context);
    }

    public void activated(CurioContext context) {
    }

    public void pickedUp(ServerPlayer player, ItemStack stack) {
    }

    public void presence(CurioContext context) {
    }

    public void impacted(CurioContext context, CurioImpact impact) {
    }

    public boolean survivesDeath(CurioContext context, CurioImpact impact) {
        return false;
    }

    public boolean absorbsDamage(CurioContext context, CurioImpact impact) {
        return false;
    }

    public void broken(CurioContext context) {
    }
}
