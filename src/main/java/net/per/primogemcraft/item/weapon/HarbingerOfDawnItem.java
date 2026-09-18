package net.per.primogemcraft.item.weapon;

import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.system.weapon.*;
import net.per.primogemcraft.system.wish.WishReports;

import java.util.List;

public class HarbingerOfDawnItem extends WishWeaponItem {
    private static final Tier TIER = new WeaponTier(1500, 4.0F, 15, WeaponTier.WOODEN_INCORRECT, PGCItems.FINE_ENHANCEMENT_ORE);

    private static final double HEALTHY_RATIO = 0.9D;
    private static final double ATTACK_BASE = 0.14D;
    private static final double ATTACK_STEP = 0.035D;
    private static final float ATTACK_DAMAGE = 5.0F;
    private static final float ATTACK_SPEED = -3.0F;
    private static final String PASSIVE_ACTION = "passive";
    private static final String ATTACK_TEXT = "attack";

    public HarbingerOfDawnItem(Properties properties) {
        super(TIER, properties.attributes(SwordItem.createAttributes(TIER, ATTACK_DAMAGE, ATTACK_SPEED)).fireResistant());
    }

    @Override
    public List<WeaponModifier> conditionalPassives(Player player, ItemStack stack, int slot, int refinement) {
        if (isHeld(player, slot, stack)) return List.of();
        return List.of(WeaponModifier.conditional(Attributes.ATTACK_DAMAGE, AttributeModifier.Operation.ADD_MULTIPLIED_BASE,
                (owner, worn, value) -> owner.getHealth() >= owner.getMaxHealth() * HEALTHY_RATIO ? ATTACK_BASE + ATTACK_STEP * (value - 1) : 0.0D));
    }

    @Override
    protected List<WeaponDescription> description(ItemStack stack) {
        var state = WeaponState.of(stack);
        return List.of(WeaponDescription.of(PASSIVE_ACTION, ATTACK_TEXT,
                WishReports.percent(ATTACK_BASE + ATTACK_STEP * (state.refinements() - 1), ChatFormatting.AQUA)));
    }
}
