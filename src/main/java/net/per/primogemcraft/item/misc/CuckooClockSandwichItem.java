package net.per.primogemcraft.item.misc;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.sound.SoundCategory;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
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

    public CuckooClockSandwichItem(Settings properties) {
        super(properties);
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World level, LivingEntity entity) {
        var result = super.finishUsing(stack, level, entity);
        if (!(entity instanceof ServerPlayerEntity player)) return result;
        InvalidThoughtCodeMachineItem.clear(player);
        PerpetualCuckooClockItem.clear(player);
        BlackForestCuckooClockItem.clear(player);
        WhimsicalFanciesMachineryCrewItem.clear(player);
        player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BLOCK_ANVIL_PLACE, SoundCategory.PLAYERS, VOLUME, 1.0F);
        if (player.hasStatusEffect(PGCEffects.LOTTO_PUNISHMENT.get())) LottoPunishmentEffect.cure(player);
        else player.damage(level.getDamageSources().fallingBlock(null), PUNISHMENT_DAMAGE);
        return result;
    }
}
