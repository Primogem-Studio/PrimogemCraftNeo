package net.per.primogemcraft.item.weapon;

import net.minecraft.util.Formatting;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.per.primogemcraft.registry.PGCEffects;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.system.weapon.TrashCanTenacityEffect;
import net.per.primogemcraft.system.weapon.WeaponDescription;
import net.per.primogemcraft.system.weapon.WishWeaponShieldItem;
import net.per.primogemcraft.system.wish.WishReports;

import java.util.List;

public class IronTrashCanLidItem extends WishWeaponShieldItem {
    private static final int DURABILITY = 1500;
    private static final int MAX_NEGATE_AMPLIFIER = 4;
    private static final String FLAVOR0 = "flavor0";
    private static final String SEPARATOR = "separator";
    private static final String TENACITY = "tenacity";
    private static final String TENACITY_CHANCE = "tenacity_chance";
    private static final String TENACITY_HIT = "tenacity_hit";
    private static final String HANDY = "handy";
    private static final String HANDY_BLOCK = "handy_block";
    private static final String HANDY_COOLDOWN = "handy_cooldown";

    public IronTrashCanLidItem(Settings properties) {
        super(properties.maxDamage(DURABILITY));
    }

    @Override
    public boolean canRepair(ItemStack stack, ItemStack repairCandidate) {
        return repairCandidate.isOf(PGCItems.ENHANCEMENT_ORE.get()) || repairCandidate.isOf(Items.IRON_INGOT);
    }

    @Override
    protected StatusEffect heldEffect(ItemStack stack) {
        return PGCEffects.TRASH_CAN_TENACITY.get();
    }

    @Override
    protected int heldAmplifier(ItemStack stack) {
        return Math.min(refinementOf(stack), MAX_NEGATE_AMPLIFIER + 1) - 1;
    }

    @Override
    protected List<WeaponDescription> description(ItemStack stack) {
        return List.of(
                WeaponDescription.note(FLAVOR0),
                WeaponDescription.note(SEPARATOR),
                WeaponDescription.note(TENACITY),
                WeaponDescription.note(TENACITY_CHANCE,
                        WishReports.percent(TrashCanTenacityEffect.NEGATE_BASE_CHANCE, Formatting.AQUA)),
                WeaponDescription.note(TENACITY_HIT,
                        WishReports.percent(TrashCanTenacityEffect.NEGATE_CHANCE_PER_LEVEL, Formatting.AQUA),
                        WishReports.percent(TrashCanTenacityEffect.negateChance(MAX_NEGATE_AMPLIFIER), Formatting.AQUA)),
                WeaponDescription.note(HANDY),
                WeaponDescription.note(HANDY_BLOCK),
                WeaponDescription.note(HANDY_COOLDOWN, cooldownReduction()));
    }
}
