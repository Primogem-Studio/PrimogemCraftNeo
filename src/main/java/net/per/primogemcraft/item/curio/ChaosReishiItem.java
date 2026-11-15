package net.per.primogemcraft.item.curio;

import net.per.primogemcraft.compat.component.StackComponents;
import net.per.primogemcraft.compat.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.per.primogemcraft.registry.PGCDataComponents;
import net.per.primogemcraft.system.curio.*;

public class ChaosReishiItem extends CurioItem {
    private static final String EXHAUSTED_KEY = "message.primogemcraft.curio.chaos_reishi.exhausted";
    private static final int KILLS = 64;
    private static final int MAX_USES = 2;
    private static final int LEVEL = 1;
    private static final float VOLUME = 1.0F;
    private static final float PITCH = 1.0F;

    public ChaosReishiItem(Properties properties) {
        super(CurioTrigger.ACTIVE, CurioForm.NORMAL, properties);
    }

    @Override
    public void impacted(CurioContext context, CurioImpact impact) {
        if (impact.signal() != CurioSignal.KILL) return;
        if (!(impact.subject() instanceof LivingEntity victim)) return;
        if (victim.getMobType() != net.minecraft.world.entity.MobType.UNDEAD && victim.getMobType() != net.minecraft.world.entity.MobType.ARTHROPOD) return;
        var counter = PGCDataComponents.CURIO_COUNTER.get();
        var progress = StackComponents.getOrDefault(context.stack(), counter, 0) + 1;
        if (progress < KILLS) {
            StackComponents.set(context.stack(), counter, progress);
            return;
        }
        enchant(context, counter);
    }

    private void enchant(CurioContext context, DataComponentType<Integer> counter) {
        var player = context.player();
        var target = Curios.randomInventoryItem(player, this::enchantable);
        if (target.isEmpty()) {
            if (!Curios.randomInventoryItem(player, ItemStack::isEnchantable).isEmpty())
                context.announce(Component.translatable(EXHAUSTED_KEY));
            return;
        }
        CurioEnchanting.tablePool(player, target, LEVEL);
        StackComponents.set(target, counter, StackComponents.getOrDefault(target, counter, 0) + 1);
        StackComponents.set(context.stack(), counter, 0);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, VOLUME, PITCH);
    }

    private boolean enchantable(ItemStack stack) {
        if (stack.getItem() instanceof ChaosReishiItem) return false;
        int used = StackComponents.getOrDefault(stack, PGCDataComponents.CURIO_COUNTER, 0);
        if (used >= MAX_USES) return false;
        return used > 0 || stack.isEnchantable();
    }
}
