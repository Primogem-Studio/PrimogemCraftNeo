package net.per.primogemcraft.system.weapon;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;

import java.util.List;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.per.primogemcraft.compat.item.WeaponAttributes120;

public abstract class WishWeaponShovelItem extends ShovelItem implements WishWeapon {
    protected final float baseAttackDamage;
    protected final float baseAttackSpeed;

    private final List<WeaponModifier> passives;

    protected WishWeaponShovelItem(Tier tier, Properties properties, float attackDamage, float attackSpeed, WeaponModifier... passives) {
        super(tier, attackDamage, attackSpeed, properties);
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
    public void appendHoverText(ItemStack stack, @org.jetbrains.annotations.Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(WeaponType.of(stack).labelKey()));
        tooltip.addAll(WishWeaponTooltips.lines(stack.getDescriptionId(), descriptions(stack)));
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (level.isClientSide() || !(entity instanceof Player player)) return;
        WeaponAttributes.refreshPassive(stack, player, slot);
    }
}
