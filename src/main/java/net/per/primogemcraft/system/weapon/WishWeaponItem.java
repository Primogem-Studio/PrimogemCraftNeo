package net.per.primogemcraft.system.weapon;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public abstract class WishWeaponItem extends SwordItem implements WishWeapon {
    private final List<WeaponModifier> passives;

    protected WishWeaponItem(Tier tier, Properties properties, WeaponModifier... passives) {
        super(tier, properties);
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
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(WeaponType.of(stack).labelKey()));
        tooltip.addAll(WishWeaponTooltips.lines(stack.getDescriptionId(), descriptions(stack)));
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (level.isClientSide() || !(entity instanceof Player player)) return;
        WeaponAttributes.refreshPassive(stack, player, slot);
    }
}
