package net.per.primogemcraft.item.tool;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.per.primogemcraft.registry.PGCEffects;
import net.per.primogemcraft.system.weapon.WeaponTier;

import java.util.List;

public class StrangePrimogemSwordItem extends SwordItem {
    private static final Tier TIER = WeaponTier.of(20, 10.0F, 100, WeaponTier.NETHERITE_INCORRECT, Items.BEDROCK);
    private static final float ATTACK_DAMAGE = -0.7F;
    private static final float ATTACK_SPEED = -2.8F;
    private static final float TARGET_HEALTH_LIMIT = 10.0F;
    private static final int EFFECT_TICKS = 1200;
    private static final int BREAK_AMOUNT = 100;
    private static final double BREAK_CHANCE = 0.1D;
    private static final float SOUND_VOLUME = 1.0F;
    private static final float SOUND_PITCH = 1.0F;
    private static final int TOOLTIP_LINES = 5;

    public StrangePrimogemSwordItem(Properties properties) {
        super(TIER, properties.attributes(SwordItem.createAttributes(TIER, ATTACK_DAMAGE, ATTACK_SPEED)).fireResistant());
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        var result = super.hurtEnemy(stack, target, attacker);
        if (target.level().isClientSide()) return result;
        if (target.getHealth() > TARGET_HEALTH_LIMIT || target.hasEffect(PGCEffects.YIJI)) return result;
        target.addEffect(new MobEffectInstance(PGCEffects.YIJI, EFFECT_TICKS, 0));
        target.level().playSound(null, target.blockPosition(), SoundEvents.GLASS_BREAK, SoundSource.NEUTRAL, SOUND_VOLUME, SOUND_PITCH);
        if (target.level() instanceof ServerLevel server && server.getRandom().nextDouble() < BREAK_CHANCE)
            stack.hurtAndBreak(BREAK_AMOUNT, server, attacker, item -> {
            });
        return result;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        var prefix = stack.getDescriptionId() + ".tooltip.";
        for (var index = 0; index < TOOLTIP_LINES; index++) tooltip.add(Component.translatable(prefix + index));
    }
}
