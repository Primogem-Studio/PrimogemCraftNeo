package net.per.primogemcraft.item.curio;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.Enchantments;
import net.per.primogemcraft.enchantment.EnchantCost;
import net.per.primogemcraft.enchantment.EnchantGrade;
import net.per.primogemcraft.enchantment.EnchantOption;
import net.per.primogemcraft.enchantment.EnchantReward;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.system.curio.*;

import java.util.List;

public class FortuneGlueItem extends CurioItem {
    private static final String NO_TARGET_KEY = "message.primogemcraft.curio.fortune_glue.no_target";
    private static final String DONE_KEY = "message.primogemcraft.curio.fortune_glue.done";
    private static final String HINT_TIMER = "curio/fortune_glue_hint";
    private static final int HINT_TICKS = 600;
    private static final int LEVEL = 3;
    private static final double CHANCE = 0.12D;

    private static final List<ResourceKey<Enchantment>> CANDIDATES = List.of(
            Enchantments.DEPTH_STRIDER, Enchantments.FORTUNE, Enchantments.LOOTING, Enchantments.LOYALTY,
            Enchantments.LUCK_OF_THE_SEA, Enchantments.LURE, Enchantments.QUICK_CHARGE, Enchantments.RESPIRATION,
            Enchantments.RIPTIDE, Enchantments.SOUL_SPEED, Enchantments.SWEEPING_EDGE, Enchantments.THORNS);
    private static final ResourceKey<Enchantment> FALLBACK = Enchantments.UNBREAKING;

    public FortuneGlueItem(Properties properties) {
        super(CurioTrigger.ACTIVE, CurioForm.NORMAL, 1, () -> new ItemStack(PGCItems.DAMAGED_FORTUNE_GLUE.get()), properties);
    }

    @Override
    public void pickedUp(ServerPlayer player, ItemStack stack) {
        trigger(CurioContext.of(player, stack, form()));
    }

    @Override
    public void presence(CurioContext context) {
        trigger(context);
    }

    private void trigger(CurioContext context) {
        var player = context.player();
        var target = Curios.randomInventoryItem(player, stack -> stack.isEnchantable() || stack.isEnchanted());
        if (target.isEmpty()) {
            if (context.ready(HINT_TIMER, HINT_TICKS)) context.announce(Component.translatable(NO_TARGET_KEY));
            return;
        }
        var chosen = FALLBACK;
        for (var candidate : CANDIDATES) {
            if (!context.chance(CHANCE)) continue;
            chosen = candidate;
            break;
        }
        var enchantments = context.level().registryAccess().registryOrThrow(Registries.ENCHANTMENT);
        var preview = CurioEnchanting.withEnchantment(target, new EnchantmentInstance(enchantments.getHolderOrThrow(chosen), LEVEL));
        context.announce(Component.translatable(DONE_KEY));
        context.damage(1);
        EnchantReward.open(player, List.of(EnchantOption.of(target, preview, EnchantGrade.of(LEVEL), LEVEL, EnchantCost.free())));
    }
}
