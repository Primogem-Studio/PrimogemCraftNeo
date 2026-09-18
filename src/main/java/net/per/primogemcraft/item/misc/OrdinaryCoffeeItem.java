package net.per.primogemcraft.item.misc;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.per.primogemcraft.item.tool.DescribedItem;

public class OrdinaryCoffeeItem extends DescribedItem {
    private static final int USE_DURATION = 16;
    private static final float EXPLOSION_RADIUS = 4.0F;
    private static final int EXPLOSION_SCATTER = 10;

    public OrdinaryCoffeeItem(Properties properties) {
        super(properties);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    public SoundEvent getDrinkingSound() {
        return SoundEvents.EMPTY;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return USE_DURATION;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        var result = super.finishUsingItem(stack, level, entity);
        if (level.isClientSide()) return result;
        var random = level.getRandom();
        level.explode(null, entity.getX() + Mth.nextInt(random, -EXPLOSION_SCATTER, EXPLOSION_SCATTER), entity.getY(),
                entity.getZ() + Mth.nextInt(random, -EXPLOSION_SCATTER, EXPLOSION_SCATTER), EXPLOSION_RADIUS,
                Level.ExplosionInteraction.MOB);
        return result;
    }
}
