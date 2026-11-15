package net.per.primogemcraft.system.weapon;

import net.minecraft.text.Text;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.world.World;
import net.minecraft.block.Block;
import net.minecraft.client.item.TooltipContext;

import java.util.List;
import net.per.primogemcraft.compat.item.WeaponAttributes120;

public abstract class WishWeaponToolItem extends MiningToolItem implements WishWeapon {
    protected final float baseAttackDamage;
    protected final float baseAttackSpeed;

    private final List<WeaponModifier> passives;

    protected WishWeaponToolItem(ToolMaterial material, Item.Settings settings, TagKey<Block> mineable, float attackDamage, float attackSpeed, WeaponModifier... passives) {
        super(attackDamage, attackSpeed, material, mineable, settings);
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
}
