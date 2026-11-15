package net.per.primogemcraft.item.tool;

import net.minecraft.world.World;
import net.minecraft.text.Text;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.sound.SoundCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.client.item.TooltipContext;
import net.per.primogemcraft.registry.PGCEffects;
import net.per.primogemcraft.system.weapon.WeaponTier;

import java.util.List;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.per.primogemcraft.compat.item.WeaponAttributes120;

public class StrangePrimogemSwordItem extends SwordItem {
    private static final ToolMaterial TIER = WeaponTier.of(20, 10.0F, 100, WeaponTier.NETHERITE_INCORRECT, Items.BEDROCK);
    private static final float ATTACK_DAMAGE = -0.7F;
    private static final float ATTACK_SPEED = -2.8F;
    private static final float TARGET_HEALTH_LIMIT = 10.0F;
    private static final int EFFECT_TICKS = 1200;
    private static final int BREAK_AMOUNT = 100;
    private static final double BREAK_CHANCE = 0.1D;
    private static final float SOUND_VOLUME = 1.0F;
    private static final float SOUND_PITCH = 1.0F;
    private static final int TOOLTIP_LINES = 6;

    public StrangePrimogemSwordItem(Settings properties) {
        super(TIER, 0, ATTACK_SPEED, properties.fireproof());
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        var result = super.postHit(stack, target, attacker);
        if (target.getWorld().isClient()) return result;
        if (target.getHealth() > TARGET_HEALTH_LIMIT || target.hasStatusEffect(PGCEffects.YIJI.get())) return result;
        target.addStatusEffect(new StatusEffectInstance(PGCEffects.YIJI.get(), EFFECT_TICKS, 0));
        target.getWorld().playSound(null, target.getBlockPos(), SoundEvents.BLOCK_GLASS_BREAK, SoundCategory.NEUTRAL, SOUND_VOLUME, SOUND_PITCH);
        if (target.getWorld() instanceof ServerWorld server && server.getRandom().nextDouble() < BREAK_CHANCE)
            stack.damage(BREAK_AMOUNT, attacker, item -> {
            });
        return result;
    }

    @Override
    public void appendTooltip(ItemStack stack, @org.jetbrains.annotations.Nullable World level, List<Text> tooltip, TooltipContext flag) {
        var prefix = stack.getTranslationKey() + ".tooltip.";
        for (var index = 0; index < TOOLTIP_LINES; index++) tooltip.add(Text.translatable(prefix + index));
    }

    @Override
    public com.google.common.collect.Multimap<net.minecraft.entity.attribute.EntityAttribute, EntityAttributeModifier> getAttributeModifiers(EquipmentSlot slot) {
        var mutable = com.google.common.collect.ArrayListMultimap.<net.minecraft.entity.attribute.EntityAttribute, EntityAttributeModifier>create();
        mutable.putAll(super.getAttributeModifiers(slot));
        if (slot == EquipmentSlot.MAINHAND)
            WeaponAttributes120.putWeaponModifiers(mutable, ATTACK_DAMAGE, ATTACK_SPEED);
        return com.google.common.collect.ImmutableMultimap.copyOf(mutable);
    }
}
