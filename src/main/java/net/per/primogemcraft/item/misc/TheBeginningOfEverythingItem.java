package net.per.primogemcraft.item.misc;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.per.primogemcraft.item.tool.DescribedItem;

public class TheBeginningOfEverythingItem extends DescribedItem {
    private static final int HEAL_TICKS = 600;
    private static final int HEAL_AMPLIFIER = 3;
    private static final int COOLDOWN_TICKS = 400;
    private static final int USE_DURATION = 32;

    public TheBeginningOfEverythingItem(Properties properties) {
        super(properties.fireResistant().rarity(Rarity.EPIC));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        player.startUsingItem(hand);
        if (!level.isClientSide()) {
            player.addEffect(new MobEffectInstance(MobEffects.HEAL, HEAL_TICKS, HEAL_AMPLIFIER));
            player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
        }
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.EAT;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return USE_DURATION;
    }

    @Override
    public SoundEvent getEatingSound() {
        return SoundEvents.EMPTY;
    }
}
