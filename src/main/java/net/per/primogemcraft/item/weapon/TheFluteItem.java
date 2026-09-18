package net.per.primogemcraft.item.weapon;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;
import net.per.primogemcraft.component.WeaponCharge;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.registry.PGCSounds;
import net.per.primogemcraft.system.element.Element;
import net.per.primogemcraft.system.element.ElementDamage;
import net.per.primogemcraft.system.weapon.*;
import net.per.primogemcraft.system.wish.WishReports;

import java.util.List;

public class TheFluteItem extends WishWeaponItem {
    private static final Tier TIER = new WeaponTier(2000, 4.0F, 12, WeaponTier.WOODEN_INCORRECT, PGCItems.FINE_ENHANCEMENT_ORE);

    private static final int MAX_NOTES = 5;
    private static final int MAX_TARGETS = 5;
    private static final int NOTE_COOLDOWN = 40;
    private static final int RIGHT_CLICK_COOLDOWN = 30;
    private static final double RADIUS = 8.0D;
    private static final double HEAL_BASE = 0.05D;
    private static final double HEAL_STEP = 0.0125D;
    private static final double DAMAGE_BASE = 1.0D;
    private static final double DAMAGE_STEP = 0.25D;
    private static final float NOTE_VOLUME = 1.6F;
    private static final float NOTE_PITCH_MIN = 0.3F;
    private static final float NOTE_PITCH_MAX = 5.0F;
    private static final float BURST_VOLUME = 2.0F;
    private static final float BURST_PITCH = 0.3F;
    private static final float ATTACK_DAMAGE = 7.0F;
    private static final float ATTACK_SPEED = -3.0F;
    private static final String NORMAL_ATTACK = "normal_attack";
    private static final String RIGHT_CLICK = "right_click";
    private static final String DETONATE_TEXT = "detonate";
    private static final String HEAL_TEXT = "heal";

    public TheFluteItem(Properties properties) {
        super(TIER, properties.attributes(SwordItem.createAttributes(TIER, ATTACK_DAMAGE, ATTACK_SPEED)).fireResistant());
    }

    @Override
    protected List<WeaponDescription> description(ItemStack stack) {
        var refinement = WeaponState.of(stack).refinements();
        return List.of(
                WeaponDescription.of(NORMAL_ATTACK, DETONATE_TEXT,
                        WishReports.percent(damageRatio(refinement), ChatFormatting.AQUA)),
                WeaponDescription.of(RIGHT_CLICK, HEAL_TEXT,
                        WishReports.percent(healRatio(refinement), ChatFormatting.AQUA)));
    }

    @Override
    public WeaponStacks stacks(ItemStack stack) {
        return WeaponStacks.temporary(WeaponCharge.of(stack), MAX_NOTES);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        var result = super.hurtEnemy(stack, target, attacker);
        if (!(attacker.level() instanceof net.minecraft.server.level.ServerLevel level)) return result;
        var notes = WeaponCharge.of(stack);
        if (notes < MAX_NOTES) {
            if (attacker instanceof Player player) {
                if (player.getCooldowns().isOnCooldown(stack.getItem())) return result;
                player.getCooldowns().addCooldown(stack.getItem(), NOTE_COOLDOWN);
            }
            WeaponCharge.set(stack, notes + 1);
            level.playSound(null, attacker.blockPosition(), SoundEvents.NOTE_BLOCK_FLUTE.value(), SoundSource.PLAYERS,
                    NOTE_VOLUME, Mth.nextFloat(level.getRandom(), NOTE_PITCH_MIN, NOTE_PITCH_MAX));
            return result;
        }
        var damage = (float) (attacker.getAttributeValue(Attributes.ATTACK_DAMAGE) * damageRatio(WeaponState.of(stack).refinements()));
        var struck = 0;
        for (var candidate : level.getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(RADIUS)))
            if (candidate != attacker && candidate.isAlive() && struck < MAX_TARGETS) {
                WeaponDamage.extraHit(candidate, ElementDamage.of(Element.ANEMO, level.damageSources().indirectMagic(candidate, attacker)), damage);
                struck++;
            }
        if (struck == 0) return result;
        WeaponCharge.set(stack, 0);
        level.playSound(null, BlockPos.containing(target.position()), PGCSounds.VARUNADA_LAZURITE_BURST.get(), SoundSource.PLAYERS,
                BURST_VOLUME, BURST_PITCH);
        return result;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        if (level.isClientSide() || player.getCooldowns().isOnCooldown(stack.getItem())) return super.use(level, player, hand);
        var notes = WeaponCharge.of(stack);
        if (notes <= 0) return super.use(level, player, hand);
        var health = player.getHealth();
        var missing = player.getMaxHealth() - health;
        player.setHealth((float) (health + missing * healRatio(WeaponState.of(stack).refinements()) * notes));
        for (var index = 0; index < notes * 2; index++)
            level.playSound(null, player.blockPosition(), SoundEvents.NOTE_BLOCK_FLUTE.value(), SoundSource.PLAYERS, NOTE_VOLUME,
                    Mth.nextFloat(level.getRandom(), NOTE_PITCH_MIN, NOTE_PITCH_MAX));
        WeaponCharge.set(stack, 0);
        player.getCooldowns().addCooldown(stack.getItem(), RIGHT_CLICK_COOLDOWN);
        return super.use(level, player, hand);
    }

    private static double damageRatio(int refinement) {
        return DAMAGE_BASE + DAMAGE_STEP * (refinement - 1);
    }

    private static double healRatio(int refinement) {
        return HEAL_BASE + HEAL_STEP * (refinement - 1);
    }
}
