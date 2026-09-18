package net.per.primogemcraft.item.curio;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.LivingEntity;
import net.per.primogemcraft.enchantment.EnchantCost;
import net.per.primogemcraft.enchantment.EnchantGrade;
import net.per.primogemcraft.enchantment.EnchantOption;
import net.per.primogemcraft.enchantment.EnchantReward;
import net.per.primogemcraft.registry.PGCDataComponents;
import net.per.primogemcraft.system.curio.*;

import java.util.List;

public class BeaconColoringPasteItem extends CurioItem {
    private static final double BASE_CHANCE = 0.01D;
    private static final double DECAY = 0.1D;
    private static final int LEVEL = 1;
    private static final float VOLUME = 1.0F;
    private static final float PITCH = 1.0F;

    public BeaconColoringPasteItem(Properties properties) {
        super(CurioTrigger.ACTIVE, CurioForm.NORMAL, properties);
    }

    @Override
    public void impacted(CurioContext context, CurioImpact impact) {
        if (impact.signal() != CurioSignal.KILL) return;
        if (!(impact.subject() instanceof LivingEntity victim)) return;
        if (!victim.getType().is(EntityTypeTags.UNDEAD) && !victim.getType().is(EntityTypeTags.ARTHROPOD)) return;
        var player = context.player();
        var target = player.getMainHandItem();
        if (!target.isEnchantable()) return;
        var counter = PGCDataComponents.CURIO_COUNTER.get();
        int triggers = target.getOrDefault(counter, 0);
        if (!context.chance(BASE_CHANCE * Math.pow(DECAY, triggers))) return;
        var preview = CurioEnchanting.tablePoolResult(player, target, LEVEL);
        target.set(counter, triggers + 1);
        impact.level().playSound(null, victim.getX(), victim.getY(), victim.getZ(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, VOLUME, PITCH);
        EnchantReward.open(player, List.of(EnchantOption.of(target, preview, EnchantGrade.of(LEVEL), LEVEL, EnchantCost.free())));
    }
}
