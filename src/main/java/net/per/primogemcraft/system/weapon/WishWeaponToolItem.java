package net.per.primogemcraft.system.weapon;

import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.List;

public abstract class WishWeaponToolItem extends DiggerItem implements WishWeapon {
    private final List<WeaponModifier> passives;

    protected WishWeaponToolItem(Tier tier, Properties properties, TagKey<Block> mineable, float attackDamage, float attackSpeed, WeaponModifier... passives) {
        super(tier, mineable, properties.attributes(SwordItem.createAttributes(tier, attackDamage, attackSpeed)));
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
