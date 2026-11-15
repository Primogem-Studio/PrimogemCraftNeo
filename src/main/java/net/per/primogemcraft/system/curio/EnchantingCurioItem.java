package net.per.primogemcraft.system.curio;

import net.per.primogemcraft.compat.component.StackComponents;
import net.minecraft.text.Text;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.enchantment.Enchantment;
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

    public EnchantingCurioItem(CurioForm form, int integrity, Spec spec, Settings properties) {
        super(CurioTrigger.ACTIVE, form, integrity, properties);
        this.spec = spec;
    }

    @Override
    public boolean repairsCurios() {
        return spec.repairRatio() > 0.0F;
    }

    @Override
    public void pickedUp(ServerPlayerEntity player, ItemStack stack) {
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
            context.announce(Text.translatable(Curios.randomInventoryItem(player, spec.target()::matches).isEmpty() ? INVALID_KEY : USED_KEY));
            return;
        }
        int used = StackComponents.getOrDefault(target, PGCDataComponents.CURIO_COUNTER, 0);
        var rolled = roll(player, target);
        if (used > 0) CurioEnchanting.raise(rolled.preview(), 1);
        if (spec.maxUses() > 0) StackComponents.set(target, PGCDataComponents.CURIO_COUNTER, used + 1);
        offhand(player);
        context.damage(1);
        EnchantReward.open(player, List.of(rolled.option(target)));
    }

    protected Roll roll(ServerPlayerEntity player, ItemStack target) {
        var level = spec.level();
        return new Roll(CurioEnchanting.tablePoolResult(player, target, level), level);
    }

    private void offhand(ServerPlayerEntity player) {
        var offhand = spec.offhand();
        if (offhand == null) return;
        var level = MathHelper.nextInt(player.getRandom(), offhand.minLevel(), offhand.maxLevel());
        PGCEnchantments.apply(player.getWorld(), player.getOffHandStack(), offhand.enchantment(), level);
    }

    private boolean repair(ServerPlayerEntity player) {
        if (!Curios.repairRandom(player, spec.repairRatio()).isEmpty()) return true;
        return !Curios.repairRandomDurability(player, spec.repairRatio()).isEmpty();
    }

    private boolean enchantable(ItemStack stack) {
        int used = StackComponents.getOrDefault(stack, PGCDataComponents.CURIO_COUNTER, 0);
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

    public record Offhand(RegistryKey<Enchantment> enchantment, int minLevel, int maxLevel) {
    }

    public enum Target {
        ENCHANTABLE,
        ENCHANTED;

        public boolean matches(ItemStack stack) {
            return switch (this) {
                case ENCHANTABLE -> stack.isEnchantable();
                case ENCHANTED -> stack.hasEnchantments();
            };
        }
    }
}
