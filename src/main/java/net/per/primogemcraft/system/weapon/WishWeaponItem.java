package net.per.primogemcraft.system.weapon;

import net.minecraft.text.Text;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.world.World;

import java.util.List;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.per.primogemcraft.compat.item.WeaponAttributes120;

public abstract class WishWeaponItem extends SwordItem implements WishWeapon {
    protected final float baseAttackDamage;
    protected final float baseAttackSpeed;

    private final List<WeaponModifier> passives;

    protected WishWeaponItem(ToolMaterial tier, float attackDamage, float attackSpeed, Settings properties, WeaponModifier... passives) {
        super(tier, 0, attackSpeed, properties);
        this.baseAttackDamage = attackDamage;
        this.baseAttackSpeed = attackSpeed;
        this.passives = List.of(passives);
    }

    public static boolean isFiveStar(ItemStack stack) {
        return WishWeapon.isFiveStar(stack);
    }

    @Override
    public List<WeaponModifier> passives() {
        return passives;
    }

    @Override
    public List<WeaponDescription> descriptions(ItemStack stack) {
        return description(stack);
    }

    protected abstract List<WeaponDescription> description(ItemStack stack);

    @Override
    public void appendTooltip(ItemStack stack, @org.jetbrains.annotations.Nullable World level, List<Text> tooltip, TooltipContext flag) {
        tooltip.add(Text.translatable(WeaponType.of(stack).labelKey()));
        tooltip.addAll(WishWeaponTooltips.lines(stack.getTranslationKey(), descriptions(stack)));
    }

    public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
        return false;
    }

    @Override
    public void inventoryTick(ItemStack stack, World level, Entity entity, int slot, boolean selected) {
        if (level.isClient() || !(entity instanceof PlayerEntity player)) return;
        WeaponAttributes.refreshPassive(stack, player, slot);
    }

    @Override
    public com.google.common.collect.Multimap<net.minecraft.entity.attribute.EntityAttribute, EntityAttributeModifier> getAttributeModifiers(EquipmentSlot slot) {
        var modifiers = com.google.common.collect.ImmutableMultimap.<net.minecraft.entity.attribute.EntityAttribute, EntityAttributeModifier>builder();
        modifiers.putAll(super.getAttributeModifiers(slot));
        if (slot == EquipmentSlot.MAINHAND) {
            var mutable = com.google.common.collect.ArrayListMultimap.<net.minecraft.entity.attribute.EntityAttribute, EntityAttributeModifier>create();
            mutable.putAll(modifiers.build());
            WeaponAttributes120.putWeaponModifiers(mutable, baseAttackDamage, baseAttackSpeed);
            return com.google.common.collect.ImmutableMultimap.copyOf(mutable);
        }
        return modifiers.build();
    }
}
