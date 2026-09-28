package net.per.primogemcraft.item.weapon;

import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
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
    public SlingshotItem(Properties properties) {
        super(properties.fireResistant(), new BowAttackCycle(10, 20, 4), 3.0F,
                ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/item/slingshot.png"),
                ResourceLocation.withDefaultNamespace("textures/entity/projectiles/arrow.png"), SoundEvents.ARROW_SHOOT,
                WeaponModifier.conditional(Attributes.ATTACK_DAMAGE, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL,
                        (player, stack, refinement) -> BowRefinement.slingshotDamageBonus(refinement)));
    }

    @Override
    public List<WeaponDescription> descriptions(ItemStack stack) {
        var refinement = WeaponEnhancement.refinementOf(WishTooltips.viewer(), stack);
        return List.of(WeaponDescription.of("passive", "damage",
                WishReports.percent(BowRefinement.slingshotDamageBonus(refinement), ChatFormatting.AQUA)));
    }
}
