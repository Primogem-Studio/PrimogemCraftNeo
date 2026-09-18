package net.per.primogemcraft.item.weapon;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.system.weapon.WeaponDescription;
import net.per.primogemcraft.system.weapon.WishWeaponShieldItem;
import net.per.primogemcraft.system.wish.WishReports;

import java.util.List;

public class ShieldOfRadiantWillItem extends WishWeaponShieldItem {
    private static final int DURABILITY = 200;
    private static final int DURABILITY_PER_REFINEMENT = 25;
    private static final String FLAVOR0 = "flavor0";
    private static final String FLAVOR1 = "flavor1";
    private static final String SEPARATOR = "separator";
    private static final String DURABILITY_TEXT = "durability";
    private static final String DURABILITY_EFFECT = "durability_effect";
    private static final String HANDY = "handy";
    private static final String HANDY_BLOCK = "handy_block";
    private static final String HANDY_COOLDOWN = "handy_cooldown";

    public ShieldOfRadiantWillItem(Properties properties) {
        super(properties.durability(DURABILITY));
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack repairCandidate) {
        return repairCandidate.is(PGCItems.TRASH_CAN_FRAGMENT.get()) || repairCandidate.is(PGCItems.ENHANCEMENT_ORE.get());
    }

    @Override
    protected void refresh(ItemStack stack) {
        var maximum = DURABILITY + DURABILITY_PER_REFINEMENT * (refinementOf(stack) - 1);
        if (stack.getOrDefault(DataComponents.MAX_DAMAGE, 0) == maximum) return;
        stack.set(DataComponents.MAX_DAMAGE, maximum);
    }

    @Override
    protected List<WeaponDescription> description(ItemStack stack) {
        return List.of(
                WeaponDescription.note(FLAVOR0),
                WeaponDescription.note(FLAVOR1),
                WeaponDescription.note(SEPARATOR),
                WeaponDescription.note(DURABILITY_TEXT),
                WeaponDescription.note(DURABILITY_EFFECT, WishReports.number(DURABILITY_PER_REFINEMENT, ChatFormatting.AQUA)),
                WeaponDescription.note(HANDY),
                WeaponDescription.note(HANDY_BLOCK),
                WeaponDescription.note(HANDY_COOLDOWN, cooldownReduction()));
    }
}
