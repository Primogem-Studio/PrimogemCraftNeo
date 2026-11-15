package net.per.primogemcraft.system.curio;

import net.per.primogemcraft.compat.component.StackComponents;
import net.minecraft.text.Text;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.world.World;
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

    public CurioItem(CurioTrigger trigger, CurioForm form, Settings properties) {
        this(trigger, form, 0, () -> ItemStack.EMPTY, properties);
    }

    public CurioItem(CurioTrigger trigger, CurioForm form, int integrity, Settings properties) {
        this(trigger, form, integrity, () -> ItemStack.EMPTY, properties);
    }

    public CurioItem(CurioTrigger trigger, CurioForm form, int integrity, Supplier<ItemStack> byproduct, Settings properties) {
        super(properties.maxCount(1));
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
    public void appendTooltip(ItemStack stack, @org.jetbrains.annotations.Nullable World level, List<Text> tooltip, TooltipContext flag) {
        appendTrigger(stack, tooltip);
        super.appendTooltip(stack, level, tooltip, flag);
        appendBar(stack, tooltip);
    }

    protected void appendTrigger(ItemStack stack, List<Text> tooltip) {
        if (described(stack)) tooltip.add(trigger.label());
    }

    protected void appendBar(ItemStack stack, List<Text> tooltip) {
        var bar = StackComponents.get(stack, PGCDataComponents.CUSTOM_BAR);
        if (bar == null || !bar.visible() || bar.denominator() <= 0) return;
        var fillsUp = barFillsUp();
        var shown = fillsUp ? bar.numerator() : bar.remaining();
        tooltip.add(Text.translatable(fillsUp ? PROGRESS_KEY : DURABILITY_KEY, shown, bar.denominator()));
    }

    @Override
    public TypedActionResult<ItemStack> use(World level, PlayerEntity player, Hand hand) {
        var stack = player.getStackInHand(hand);
        var food = stack.getItem().getFoodComponent();
        if (food != null && player.canConsume(food.isAlwaysEdible())) {
            player.setCurrentHand(hand);
            return TypedActionResult.consume(stack);
        }
        if (player instanceof ServerPlayerEntity server) activated(CurioContext.of(server, stack, form));
        return TypedActionResult.success(stack, level.isClient());
    }

    @Override
    public void inventoryTick(ItemStack stack, World level, Entity entity, int slot, boolean selected) {
        if (!(entity instanceof ServerPlayerEntity player) || stack.isEmpty()) return;
        var context = CurioContext.of(player, stack, form);
        if (context.ready("curio_inventory_" + slot, presenceInterval())) presence(context);
    }

    public void activated(CurioContext context) {
    }

    public void pickedUp(ServerPlayerEntity player, ItemStack stack) {
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
