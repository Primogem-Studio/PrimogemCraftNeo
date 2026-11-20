package net.per.primogemcraft.item.weapon;

import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.sound.SoundEvents;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.per.primogemcraft.system.weapon.BowAttackCycle;
import net.per.primogemcraft.system.weapon.BowRefinement;
import net.per.primogemcraft.system.weapon.WeaponDescription;
import net.per.primogemcraft.system.weapon.WeaponEnhancement;
import net.per.primogemcraft.system.weapon.WeaponModifier;
import net.per.primogemcraft.system.weapon.WishWeaponBowItem;
import net.per.primogemcraft.system.wish.WishReports;
import net.per.primogemcraft.system.wish.WishTooltips;

import java.util.List;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public final class SlingshotItem extends WishWeaponBowItem {
    public SlingshotItem(Settings properties) {
        super(properties.fireResistant(), new BowAttackCycle(10, 20, 4), 3.0F,
                Identifier.fromNamespaceAndPath(MOD_ID, "textures/item/slingshot.png"),
                Identifier.fromNamespaceAndPath("minecraft", "textures/entity/projectiles/arrow.png"), SoundEvents.ARROW_SHOOT,
                WeaponModifier.conditional(EntityAttributes.GENERIC_ATTACK_DAMAGE, EntityAttributeModifier.Operation.MULTIPLY_TOTAL,
                        (player, stack, refinement) -> BowRefinement.slingshotDamageBonus(refinement)));
    }

    @Override
    public List<WeaponDescription> descriptions(ItemStack stack) {
        var refinement = WeaponEnhancement.refinementOf(WishTooltips.viewer(), stack);
        return List.of(WeaponDescription.of("passive", "damage",
                WishReports.percent(BowRefinement.slingshotDamageBonus(refinement), ChatFormatting.AQUA)));
    }
}
