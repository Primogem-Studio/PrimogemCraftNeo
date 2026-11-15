package net.per.primogemcraft.item.weapon;

import net.minecraft.util.Formatting;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.util.math.Box;
import net.per.primogemcraft.component.WeaponCharge;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.system.weapon.*;
import net.per.primogemcraft.system.wish.WishReports;

import java.util.List;

public class DeathmatchItem extends WishWeaponItem {
    private static final ToolMaterial TIER = new WeaponTier(1024, 4.0F, 20, WeaponTier.WOODEN_INCORRECT, PGCItems.AGNIDUS_AGATE_SLIVER);

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

    public DeathmatchItem(Settings properties) {
        super(TIER, ATTACK_DAMAGE, ATTACK_SPEED, properties.fireproof(), WeaponModifier.of(EntityAttributes.GENERIC_ARMOR, ARMOR_BASE, ARMOR_STEP, EntityAttributeModifier.Operation.ADDITION));
    }

    @Override
    public List<WeaponModifier> conditionalPassives(PlayerEntity player, ItemStack stack, int slot, int refinement) {
        if (isHeld(player, slot, stack)) return List.of();
        var nearby = WeaponCharge.of(stack);
        if (nearby <= 0) return List.of();
        var attack = nearby > 1
                ? CROWD_ATTACK_BASE + CROWD_ATTACK_STEP * (refinement - 1)
                : SOLO_ATTACK_BASE + SOLO_ATTACK_STEP * (refinement - 1);
        return List.of(
                WeaponModifier.conditional(EntityAttributes.GENERIC_ATTACK_DAMAGE, EntityAttributeModifier.Operation.MULTIPLY_BASE, (owner, worn, value) -> attack),
                WeaponModifier.conditional(EntityAttributes.GENERIC_ARMOR, EntityAttributeModifier.Operation.MULTIPLY_BASE,
                        (owner, worn, value) -> nearby > 1 ? CROWD_ATTACK_BASE + CROWD_ATTACK_STEP * (value - 1) : 0.0D));
    }

    @Override
    protected List<WeaponDescription> description(ItemStack stack) {
        var state = WeaponState.of(stack);
        var refinement = state.refinements();
        return List.of(
                WeaponDescription.of(NORMAL_ATTACK, CROWD_TEXT,
                        WishReports.percent(CROWD_ATTACK_BASE + CROWD_ATTACK_STEP * (refinement - 1), Formatting.AQUA)),
                WeaponDescription.of(NORMAL_ATTACK, SOLO_TEXT,
                        WishReports.percent(SOLO_ATTACK_BASE + SOLO_ATTACK_STEP * (refinement - 1), Formatting.AQUA)),
                WeaponDescription.note(NONE_TEXT),
                WeaponDescription.of(PASSIVE_EFFECT_ACTION, ARMOR_TEXT,
                        WishReports.number(ARMOR_BASE + ARMOR_STEP * (refinement - 1), Formatting.AQUA)));
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        var result = super.postHit(stack, target, attacker);
        if (!(attacker.getWorld() instanceof ServerWorld level)) return result;
        var box = new Box(target.getPos(), target.getPos()).expand(RADIUS);
        var nearby = 0;
        for (var candidate : level.getEntitiesByClass(LivingEntity.class, box, e -> true)) {
            if (candidate == attacker || !candidate.isAlive()) continue;
            if (candidate instanceof MobEntity mob && mob.getTarget() == attacker) nearby++;
            else if (candidate instanceof PlayerEntity) nearby++;
        }
        WeaponCharge.set(stack, nearby);
        return result;
    }
}
