package net.per.primogemcraft.item.weapon;

import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.recipe.Ingredient;
import net.per.primogemcraft.enchantment.TheHunt;
import net.per.primogemcraft.system.weapon.*;
import net.per.primogemcraft.system.wish.WishReports;
import net.per.primogemcraft.util.PlayerFlags;
import net.per.primogemcraft.util.TemporaryAttributes;

import java.util.List;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public class BlackTasselItem extends WishWeaponItem {
    private static final ToolMaterial TIER = new WeaponTier(320, 4.0F, 10, WeaponTier.WOODEN_INCORRECT, Ingredient.ofItems(net.minecraft.item.Items.IRON_INGOT));

    private static final Identifier STATE_KEY = Identifier.of(MOD_ID, "black_tassel_state");
    private static final Identifier IMMOBILIZE_ID = Identifier.of(MOD_ID, "weapon/immobilize");

    private static final int NO_STATE = 0;
    private static final int BONUS_STATE = 1;
    private static final int PENALTY_STATE = 2;
    private static final double IMMOBILIZE_SPEED = -100.0D;
    private static final int IMMOBILIZE_TICKS_BASE = 40;
    private static final int IMMOBILIZE_TICKS_STEP = 10;
    private static final int IMMOBILIZE_COOLDOWN = 80;
    private static final double POWER_BASE = 0.4D;
    private static final double POWER_STEP = 0.1D;
    private static final double NON_UNDEAD_PENALTY = -1.0D;
    private static final float ATTACK_DAMAGE = 6.0F;
    private static final float ATTACK_SPEED = -2.0F;
    private static final String NORMAL_ATTACK = "normal_attack";
    private static final String PASSIVE_ACTION = "passive";
    private static final String SPECIAL_PASSIVE = "special_passive";
    private static final String SLOWNESS_TEXT = "slowness";
    private static final String POWER_TEXT = "power";
    private static final String PENALTY_TEXT = "penalty";
    private static final String ABUNDANCE_TEXT = "abundance";
    private static final String ABUNDANCE_COOLDOWN_TEXT = "abundance_cooldown";
    private static final String ABUNDANCE_TRANSFER_TEXT = "abundance_transfer";
    private static final String ABUNDANCE_CAP_TEXT = "abundance_cap";

    public BlackTasselItem(Settings properties) {
        super(TIER, ATTACK_DAMAGE, ATTACK_SPEED, properties.fireproof());
    }

    @Override
    public List<WeaponModifier> conditionalPassives(PlayerEntity player, ItemStack stack, int slot, int refinement) {
        if (isHeld(player, slot, stack)) return List.of();
        var state = PlayerFlags.of(player).counter(STATE_KEY);
        if (state == NO_STATE) return List.of();
        return List.of(WeaponModifier.conditional(EntityAttributes.GENERIC_ATTACK_DAMAGE, EntityAttributeModifier.Operation.MULTIPLY_BASE,
                (owner, worn, value) -> state == BONUS_STATE ? POWER_BASE + POWER_STEP * (value - 1) : NON_UNDEAD_PENALTY));
    }

    @Override
    protected List<WeaponDescription> description(ItemStack stack) {
        var refinement = WeaponState.of(stack).refinements();
        return List.of(
                WeaponDescription.of(NORMAL_ATTACK, SLOWNESS_TEXT,
                        WishReports.number(immobilizeSeconds(refinement), Formatting.AQUA),
                        WishReports.number(IMMOBILIZE_COOLDOWN / 20, Formatting.AQUA)),
                WeaponDescription.of(PASSIVE_ACTION, POWER_TEXT,
                        WishReports.percent(POWER_BASE + POWER_STEP * (refinement - 1), Formatting.AQUA)),
                WeaponDescription.note(PENALTY_TEXT),
                WeaponDescription.of(SPECIAL_PASSIVE, ABUNDANCE_TEXT),
                WeaponDescription.note(ABUNDANCE_COOLDOWN_TEXT,
                        WishReports.number(TheHunt.cooldownTicks(TheHunt.level(stack)) / 20, Formatting.AQUA)),
                WeaponDescription.note(ABUNDANCE_TRANSFER_TEXT),
                WeaponDescription.note(ABUNDANCE_CAP_TEXT));
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        var result = super.postHit(stack, target, attacker);
        if (!(attacker.getWorld() instanceof ServerWorld) || !(attacker instanceof PlayerEntity player)) return result;
        var flags = PlayerFlags.of(player);
        if (target.getGroup() != net.minecraft.entity.EntityGroup.UNDEAD) {
            flags.set(STATE_KEY, PENALTY_STATE);
            return result;
        }
        flags.set(STATE_KEY, BONUS_STATE);
        if (player.getItemCooldownManager().isCoolingDown(stack.getItem())) return result;
        TemporaryAttributes.apply(target, IMMOBILIZE_ID, EntityAttributes.GENERIC_MOVEMENT_SPEED, IMMOBILIZE_SPEED,
                EntityAttributeModifier.Operation.ADDITION, immobilizeTicks(WeaponState.of(stack).refinements()));
        player.getItemCooldownManager().set(stack.getItem(), IMMOBILIZE_COOLDOWN);
        return result;
    }

    private static int immobilizeTicks(int refinement) {
        return IMMOBILIZE_TICKS_BASE + IMMOBILIZE_TICKS_STEP * (refinement - 1);
    }

    private static double immobilizeSeconds(int refinement) {
        return immobilizeTicks(refinement) / 20.0D;
    }
}
