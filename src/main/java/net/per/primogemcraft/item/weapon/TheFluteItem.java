package net.per.primogemcraft.item.weapon;

import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.sound.SoundEvents;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.world.World;
import net.per.primogemcraft.component.WeaponCharge;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.registry.PGCSounds;
import net.per.primogemcraft.system.element.Element;
import net.per.primogemcraft.system.element.ElementDamage;
import net.per.primogemcraft.system.weapon.*;
import net.per.primogemcraft.system.wish.WishReports;

import java.util.List;

public class TheFluteItem extends WishWeaponItem {
    private static final ToolMaterial TIER = new WeaponTier(2000, 4.0F, 12, WeaponTier.WOODEN_INCORRECT, PGCItems.FINE_ENHANCEMENT_ORE);

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

    public TheFluteItem(Settings properties) {
        super(TIER, ATTACK_DAMAGE, ATTACK_SPEED, properties.fireproof());
    }

    @Override
    protected List<WeaponDescription> description(ItemStack stack) {
        var refinement = WeaponState.of(stack).refinements();
        return List.of(
                WeaponDescription.of(NORMAL_ATTACK, DETONATE_TEXT,
                        WishReports.percent(damageRatio(refinement), Formatting.AQUA)),
                WeaponDescription.of(RIGHT_CLICK, HEAL_TEXT,
                        WishReports.percent(healRatio(refinement), Formatting.AQUA)));
    }

    @Override
    public WeaponStacks stacks(ItemStack stack) {
        return WeaponStacks.temporary(WeaponCharge.of(stack), MAX_NOTES);
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        var result = super.postHit(stack, target, attacker);
        if (!(attacker.getWorld() instanceof net.minecraft.server.world.ServerWorld level)) return result;
        var notes = WeaponCharge.of(stack);
        if (notes < MAX_NOTES) {
            if (attacker instanceof PlayerEntity player) {
                if (player.getItemCooldownManager().isCoolingDown(stack.getItem())) return result;
                player.getItemCooldownManager().set(stack.getItem(), NOTE_COOLDOWN);
            }
            WeaponCharge.set(stack, notes + 1);
            level.playSound(null, attacker.getBlockPos(), SoundEvents.BLOCK_NOTE_BLOCK_FLUTE.value(), SoundCategory.PLAYERS,
                    NOTE_VOLUME, MathHelper.nextFloat(level.getRandom(), NOTE_PITCH_MIN, NOTE_PITCH_MAX));
            return result;
        }
        var damage = (float) (attacker.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE) * damageRatio(WeaponState.of(stack).refinements()));
        var struck = 0;
        for (var candidate : level.getEntitiesByClass(LivingEntity.class, target.getBoundingBox().expand(RADIUS), e -> true))
            if (candidate != attacker && candidate.isAlive() && struck < MAX_TARGETS) {
                WeaponDamage.extraHit(candidate, ElementDamage.of(Element.ANEMO, level.getDamageSources().indirectMagic(candidate, attacker)), damage);
                struck++;
            }
        if (struck == 0) return result;
        WeaponCharge.set(stack, 0);
        level.playSound(null, BlockPos.ofFloored(target.getPos()), PGCSounds.VARUNADA_LAZURITE_BURST.get(), SoundCategory.PLAYERS,
                BURST_VOLUME, BURST_PITCH);
        return result;
    }

    @Override
    public TypedActionResult<ItemStack> use(World level, PlayerEntity player, Hand hand) {
        var stack = player.getStackInHand(hand);
        if (level.isClient() || player.getItemCooldownManager().isCoolingDown(stack.getItem())) return super.use(level, player, hand);
        var notes = WeaponCharge.of(stack);
        if (notes <= 0) return super.use(level, player, hand);
        var health = player.getHealth();
        var missing = player.getMaxHealth() - health;
        player.setHealth((float) (health + missing * healRatio(WeaponState.of(stack).refinements()) * notes));
        for (var index = 0; index < notes * 2; index++)
            level.playSound(null, player.getBlockPos(), SoundEvents.BLOCK_NOTE_BLOCK_FLUTE.value(), SoundCategory.PLAYERS, NOTE_VOLUME,
                    MathHelper.nextFloat(level.getRandom(), NOTE_PITCH_MIN, NOTE_PITCH_MAX));
        WeaponCharge.set(stack, 0);
        player.getItemCooldownManager().set(stack.getItem(), RIGHT_CLICK_COOLDOWN);
        return super.use(level, player, hand);
    }

    private static double damageRatio(int refinement) {
        return DAMAGE_BASE + DAMAGE_STEP * (refinement - 1);
    }

    private static double healRatio(int refinement) {
        return HEAL_BASE + HEAL_STEP * (refinement - 1);
    }
}
