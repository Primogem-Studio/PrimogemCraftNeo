package net.per.primogemcraft.item.weapon;

import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.phys.AABB;
import net.per.primogemcraft.component.WeaponCharge;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.system.weapon.*;
import net.per.primogemcraft.system.wish.WishReports;

import java.util.List;

public class DeathmatchItem extends WishWeaponItem {
    private static final Tier TIER = new WeaponTier(1024, 4.0F, 20, WeaponTier.WOODEN_INCORRECT, PGCItems.AGNIDUS_AGATE_SLIVER);

    private static final double RADIUS = 2.0D;
    private static final double SOLO_ATTACK_BASE = 0.28D;
    private static final double SOLO_ATTACK_STEP = 0.07D;
    private static final double CROWD_ATTACK_BASE = 0.16D;
    private static final double CROWD_ATTACK_STEP = 0.04D;
    private static final double ARMOR_BASE = 4.0D;
    private static final double ARMOR_STEP = 1.0D;
    private static final float ATTACK_DAMAGE = 7.0F;
    private static final float ATTACK_SPEED = -2.0F;
    private static final String NORMAL_ATTACK = "normal_attack";
    private static final String PASSIVE_EFFECT_ACTION = "passive_effect";
    private static final String CROWD_TEXT = "crowd";
    private static final String SOLO_TEXT = "solo";
    private static final String NONE_TEXT = "none";
    private static final String ARMOR_TEXT = "armor";

    public DeathmatchItem(Properties properties) {
        super(TIER, properties.attributes(SwordItem.createAttributes(TIER, ATTACK_DAMAGE, ATTACK_SPEED)).fireResistant(),
                WeaponModifier.of(Attributes.ARMOR, ARMOR_BASE, ARMOR_STEP, AttributeModifier.Operation.ADD_VALUE));
    }

    @Override
    public List<WeaponModifier> conditionalPassives(Player player, ItemStack stack, int slot, int refinement) {
        if (isHeld(player, slot, stack)) return List.of();
        var nearby = WeaponCharge.of(stack);
        if (nearby <= 0) return List.of();
        var attack = nearby > 1
                ? CROWD_ATTACK_BASE + CROWD_ATTACK_STEP * (refinement - 1)
                : SOLO_ATTACK_BASE + SOLO_ATTACK_STEP * (refinement - 1);
        return List.of(
                WeaponModifier.conditional(Attributes.ATTACK_DAMAGE, AttributeModifier.Operation.ADD_MULTIPLIED_BASE, (owner, worn, value) -> attack),
                WeaponModifier.conditional(Attributes.ARMOR, AttributeModifier.Operation.ADD_MULTIPLIED_BASE,
                        (owner, worn, value) -> nearby > 1 ? CROWD_ATTACK_BASE + CROWD_ATTACK_STEP * (value - 1) : 0.0D));
    }

    @Override
    protected List<WeaponDescription> description(ItemStack stack) {
        var state = WeaponState.of(stack);
        var refinement = state.refinements();
        return List.of(
                WeaponDescription.of(NORMAL_ATTACK, CROWD_TEXT,
                        WishReports.percent(CROWD_ATTACK_BASE + CROWD_ATTACK_STEP * (refinement - 1), ChatFormatting.AQUA)),
                WeaponDescription.of(NORMAL_ATTACK, SOLO_TEXT,
                        WishReports.percent(SOLO_ATTACK_BASE + SOLO_ATTACK_STEP * (refinement - 1), ChatFormatting.AQUA)),
                WeaponDescription.note(NONE_TEXT),
                WeaponDescription.of(PASSIVE_EFFECT_ACTION, ARMOR_TEXT,
                        WishReports.number(ARMOR_BASE + ARMOR_STEP * (refinement - 1), ChatFormatting.AQUA)));
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        var result = super.hurtEnemy(stack, target, attacker);
        if (!(attacker.level() instanceof ServerLevel level)) return result;
        var box = new AABB(target.position(), target.position()).inflate(RADIUS);
        var nearby = 0;
        for (var candidate : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (candidate == attacker || !candidate.isAlive()) continue;
            if (candidate instanceof Mob mob && mob.getTarget() == attacker) nearby++;
            else if (candidate instanceof Player) nearby++;
        }
        WeaponCharge.set(stack, nearby);
        return result;
    }
}
