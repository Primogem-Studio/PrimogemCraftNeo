package net.per.primogemcraft.item.misc;

import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Rarity;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;
import net.per.primogemcraft.item.tool.DescribedItem;

public class TheBeginningOfEverythingItem extends DescribedItem {
    private static final int HEAL_TICKS = 600;
    private static final int HEAL_AMPLIFIER = 3;
    private static final int COOLDOWN_TICKS = 400;
    private static final int USE_DURATION = 32;

    public TheBeginningOfEverythingItem(Settings properties) {
        super(properties.fireproof().rarity(Rarity.EPIC));
    }

    @Override
    public TypedActionResult<ItemStack> use(World level, PlayerEntity player, Hand hand) {
        var stack = player.getStackInHand(hand);
        player.setCurrentHand(hand);
        if (!level.isClient()) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.INSTANT_HEALTH, HEAL_TICKS, HEAL_AMPLIFIER));
            player.getItemCooldownManager().set(this, COOLDOWN_TICKS);
        }
        return TypedActionResult.consume(stack);
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return net.minecraft.util.UseAction.EAT;
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        return USE_DURATION;
    }

    @Override
    public SoundEvent getEatSound() {
        return SoundEvents.INTENTIONALLY_EMPTY;
    }
}
