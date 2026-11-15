package net.per.primogemcraft.item.misc;

import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;
import net.per.primogemcraft.item.tool.DescribedItem;

public class OrdinaryCoffeeItem extends DescribedItem {
    private static final int USE_DURATION = 16;
    private static final float EXPLOSION_RADIUS = 4.0F;
    private static final int EXPLOSION_SCATTER = 10;

    public OrdinaryCoffeeItem(Settings properties) {
        super(properties);
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return net.minecraft.util.UseAction.DRINK;
    }

    @Override
    public SoundEvent getDrinkSound() {
        return SoundEvents.INTENTIONALLY_EMPTY;
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        return USE_DURATION;
    }

    @Override
    public TypedActionResult<ItemStack> use(World level, PlayerEntity player, Hand hand) {
        player.setCurrentHand(hand);
        return TypedActionResult.consume(player.getStackInHand(hand));
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World level, LivingEntity entity) {
        var result = super.finishUsing(stack, level, entity);
        if (level.isClient()) return result;
        var random = level.getRandom();
        level.createExplosion(null, entity.getX() + MathHelper.nextInt(random, -EXPLOSION_SCATTER, EXPLOSION_SCATTER), entity.getY(),
                entity.getZ() + MathHelper.nextInt(random, -EXPLOSION_SCATTER, EXPLOSION_SCATTER), EXPLOSION_RADIUS,
                net.minecraft.world.World.ExplosionSourceType.MOB);
        return result;
    }
}
