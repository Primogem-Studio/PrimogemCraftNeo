package net.per.primogemcraft.system.curio;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.per.primogemcraft.enchantment.EnchantCost;
import net.per.primogemcraft.enchantment.EnchantGrade;
import net.per.primogemcraft.enchantment.EnchantOption;
import net.per.primogemcraft.enchantment.EnchantReward;
import net.per.primogemcraft.enchantment.PGCEnchantments;
import net.per.primogemcraft.registry.PGCDataComponents;

import java.util.List;

public class EnchantingCurioItem extends CurioItem {
    private static final String INVALID_KEY = "message.primogemcraft.curio.enchanting.invalid";
    private static final String USED_KEY = "message.primogemcraft.curio.enchanting.used";
    private static final String HINT_TIMER = "curio/enchant_hint";
    private static final int HINT_TICKS = 600;

    private final Spec spec;

    public EnchantingCurioItem(CurioForm form, int integrity, Spec spec, Properties properties) {
        super(CurioTrigger.ACTIVE, form, integrity, properties);
        this.spec = spec;
    }

    @Override
    public boolean repairsCurios() {
        return spec.repairRatio() > 0.0F;
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
        var repaired = spec.repairRatio() > 0.0F && repair(player);
        var target = Curios.randomInventoryItem(player, this::enchantable);
        if (target.isEmpty()) {
            if (repaired) {
                context.damage(1);
                return;
            }
            if (!context.ready(HINT_TIMER, HINT_TICKS)) return;
            context.announce(Component.translatable(Curios.randomInventoryItem(player, spec.target()::matches).isEmpty() ? INVALID_KEY : USED_KEY));
            return;
        }
        int used = target.getOrDefault(PGCDataComponents.CURIO_COUNTER.get(), 0);
        var rolled = roll(player, target);
        if (used > 0) CurioEnchanting.raise(rolled.preview(), 1);
        if (spec.maxUses() > 0) target.set(PGCDataComponents.CURIO_COUNTER.get(), used + 1);
        offhand(player);
        context.damage(1);
        EnchantReward.open(player, List.of(rolled.option(target)));
    }

    protected Roll roll(ServerPlayer player, ItemStack target) {
        var level = spec.level();
        return new Roll(CurioEnchanting.tablePoolResult(player, target, level), level);
    }

    private void offhand(ServerPlayer player) {
        var offhand = spec.offhand();
        if (offhand == null) return;
        var level = Mth.nextInt(player.getRandom(), offhand.minLevel(), offhand.maxLevel());
        PGCEnchantments.apply(player.level(), player.getOffhandItem(), offhand.enchantment(), level);
    }

    private boolean repair(ServerPlayer player) {
        if (!Curios.repairRandom(player, spec.repairRatio()).isEmpty()) return true;
        return !Curios.repairRandomDurability(player, spec.repairRatio()).isEmpty();
    }

    private boolean enchantable(ItemStack stack) {
        int used = stack.getOrDefault(PGCDataComponents.CURIO_COUNTER.get(), 0);
        if (spec.maxUses() > 0 && used >= spec.maxUses()) return false;
        return used > 0 || spec.target().matches(stack);
    }

    public record Spec(int level, Target target, int maxUses, float repairRatio, Offhand offhand) {
        public Spec(int level, Target target, int maxUses, float repairRatio) {
            this(level, target, maxUses, repairRatio, null);
        }
    }

    public record Roll(ItemStack preview, int level) {
        EnchantOption option(ItemStack target) {
            return EnchantOption.of(target, preview, EnchantGrade.of(level), level, EnchantCost.free());
        }
    }

    public record Offhand(ResourceKey<Enchantment> enchantment, int minLevel, int maxLevel) {
    }

    public enum Target {
        ENCHANTABLE,
        ENCHANTED;

        public boolean matches(ItemStack stack) {
            return switch (this) {
                case ENCHANTABLE -> stack.isEnchantable();
                case ENCHANTED -> stack.isEnchanted();
            };
        }
    }
}
