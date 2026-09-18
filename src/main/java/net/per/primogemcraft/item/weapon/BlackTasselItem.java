package net.per.primogemcraft.item.weapon;

import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.per.primogemcraft.enchantment.TheHunt;
import net.per.primogemcraft.system.weapon.*;
import net.per.primogemcraft.system.wish.WishReports;
import net.per.primogemcraft.util.PlayerFlags;
import net.per.primogemcraft.util.TemporaryAttributes;

import java.util.List;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public class BlackTasselItem extends WishWeaponItem {
    private static final Tier TIER = new WeaponTier(320, 4.0F, 10, WeaponTier.WOODEN_INCORRECT, Ingredient.of(net.minecraft.world.item.Items.IRON_INGOT));

    private static final ResourceLocation STATE_KEY = ResourceLocation.fromNamespaceAndPath(MOD_ID, "black_tassel_state");
    private static final ResourceLocation IMMOBILIZE_ID = ResourceLocation.fromNamespaceAndPath(MOD_ID, "weapon/immobilize");

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

    public BlackTasselItem(Properties properties) {
        super(TIER, properties.attributes(SwordItem.createAttributes(TIER, ATTACK_DAMAGE, ATTACK_SPEED)).fireResistant());
    }

    @Override
    public List<WeaponModifier> conditionalPassives(Player player, ItemStack stack, int slot, int refinement) {
        if (isHeld(player, slot, stack)) return List.of();
        var state = PlayerFlags.of(player).counter(STATE_KEY);
        if (state == NO_STATE) return List.of();
        return List.of(WeaponModifier.conditional(Attributes.ATTACK_DAMAGE, AttributeModifier.Operation.ADD_MULTIPLIED_BASE,
                (owner, worn, value) -> state == BONUS_STATE ? POWER_BASE + POWER_STEP * (value - 1) : NON_UNDEAD_PENALTY));
    }

    @Override
    protected List<WeaponDescription> description(ItemStack stack) {
        var refinement = WeaponState.of(stack).refinements();
        return List.of(
                WeaponDescription.of(NORMAL_ATTACK, SLOWNESS_TEXT,
                        WishReports.number(immobilizeSeconds(refinement), ChatFormatting.AQUA),
                        WishReports.number(IMMOBILIZE_COOLDOWN / 20, ChatFormatting.AQUA)),
                WeaponDescription.of(PASSIVE_ACTION, POWER_TEXT,
                        WishReports.percent(POWER_BASE + POWER_STEP * (refinement - 1), ChatFormatting.AQUA)),
                WeaponDescription.note(PENALTY_TEXT),
                WeaponDescription.of(SPECIAL_PASSIVE, ABUNDANCE_TEXT),
                WeaponDescription.note(ABUNDANCE_COOLDOWN_TEXT,
                        WishReports.number(TheHunt.cooldownTicks(TheHunt.level(stack)) / 20, ChatFormatting.AQUA)),
                WeaponDescription.note(ABUNDANCE_TRANSFER_TEXT),
                WeaponDescription.note(ABUNDANCE_CAP_TEXT));
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        var result = super.hurtEnemy(stack, target, attacker);
        if (!(attacker.level() instanceof ServerLevel) || !(attacker instanceof Player player)) return result;
        var flags = PlayerFlags.of(player);
        if (!target.getType().is(EntityTypeTags.UNDEAD)) {
            flags.set(STATE_KEY, PENALTY_STATE);
            return result;
        }
        flags.set(STATE_KEY, BONUS_STATE);
        if (player.getCooldowns().isOnCooldown(stack.getItem())) return result;
        TemporaryAttributes.apply(target, IMMOBILIZE_ID, Attributes.MOVEMENT_SPEED, IMMOBILIZE_SPEED,
                AttributeModifier.Operation.ADD_VALUE, immobilizeTicks(WeaponState.of(stack).refinements()));
        player.getCooldowns().addCooldown(stack.getItem(), IMMOBILIZE_COOLDOWN);
        return result;
    }

    private static int immobilizeTicks(int refinement) {
        return IMMOBILIZE_TICKS_BASE + IMMOBILIZE_TICKS_STEP * (refinement - 1);
    }

    private static double immobilizeSeconds(int refinement) {
        return immobilizeTicks(refinement) / 20.0D;
    }
}
