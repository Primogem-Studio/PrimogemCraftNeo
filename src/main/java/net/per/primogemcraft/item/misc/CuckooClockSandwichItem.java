package net.per.primogemcraft.item.misc;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.per.primogemcraft.item.curio.BlackForestCuckooClockItem;
import net.per.primogemcraft.item.curio.InvalidThoughtCodeMachineItem;
import net.per.primogemcraft.item.curio.PerpetualCuckooClockItem;
import net.per.primogemcraft.item.curio.WhimsicalFanciesMachineryCrewItem;
import net.per.primogemcraft.item.tool.DescribedItem;
import net.per.primogemcraft.registry.PGCEffects;
import net.per.primogemcraft.system.curio.effect.LottoPunishmentEffect;

public class CuckooClockSandwichItem extends DescribedItem {
    private static final float VOLUME = 0.4F;
    private static final float PUNISHMENT_DAMAGE = 2.0F;

    public CuckooClockSandwichItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        var result = super.finishUsingItem(stack, level, entity);
        if (!(entity instanceof ServerPlayer player)) return result;
        InvalidThoughtCodeMachineItem.clear(player);
        PerpetualCuckooClockItem.clear(player);
        BlackForestCuckooClockItem.clear(player);
        WhimsicalFanciesMachineryCrewItem.clear(player);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ANVIL_PLACE, SoundSource.PLAYERS, VOLUME, 1.0F);
        if (player.hasEffect(PGCEffects.LOTTO_PUNISHMENT)) LottoPunishmentEffect.cure(player);
        else player.hurt(level.damageSources().fallingBlock(null), PUNISHMENT_DAMAGE);
        return result;
    }
}
