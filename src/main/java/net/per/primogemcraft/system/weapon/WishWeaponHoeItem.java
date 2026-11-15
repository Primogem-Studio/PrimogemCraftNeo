package net.per.primogemcraft.system.weapon;

import net.minecraft.text.Text;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.world.World;
import net.minecraft.client.item.TooltipContext;

import java.util.List;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.per.primogemcraft.compat.item.WeaponAttributes120;

public abstract class WishWeaponHoeItem extends HoeItem implements WishWeapon {
    protected final float baseAttackDamage;
    protected final float baseAttackSpeed;

    private final List<WeaponModifier> passives;

    protected WishWeaponHoeItem(ToolMaterial material, Item.Settings settings, float attackDamage, float attackSpeed, WeaponModifier... passives) {
        super(material, 0, attackSpeed, settings);
        this.baseAttackDamage = attackDamage;
        this.baseAttackSpeed = attackSpeed;
        this.passives = List.of(passives);
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
    public void appendTooltip(ItemStack stack, @org.jetbrains.annotations.Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable(WeaponType.of(stack).labelKey()));
        tooltip.addAll(WishWeaponTooltips.lines(stack.getTranslationKey(), descriptions(stack)));
    }

    public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
        return false;
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (world.isClient() || !(entity instanceof PlayerEntity player)) return;
        WeaponAttributes.refreshPassive(stack, player, slot);
    }

    @Override
    public com.google.common.collect.Multimap<EntityAttribute, EntityAttributeModifier> getAttributeModifiers(EquipmentSlot slot) {
        var modifiers = com.google.common.collect.ImmutableMultimap.<EntityAttribute, EntityAttributeModifier>builder();
        modifiers.putAll(super.getAttributeModifiers(slot));
        if (slot == EquipmentSlot.MAINHAND) {
            var mutable = com.google.common.collect.ArrayListMultimap.<EntityAttribute, EntityAttributeModifier>create();
            mutable.putAll(modifiers.build());
            WeaponAttributes120.putWeaponModifiers(mutable, baseAttackDamage, baseAttackSpeed);
            return com.google.common.collect.ImmutableMultimap.copyOf(mutable);
        }
        return modifiers.build();
    }
}
