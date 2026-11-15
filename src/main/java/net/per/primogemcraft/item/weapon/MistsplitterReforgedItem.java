package net.per.primogemcraft.item.weapon;

import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.world.World;
import net.minecraft.util.math.Vec3d;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.system.element.Element;
import net.per.primogemcraft.system.element.ElementDamageOptions;
import net.per.primogemcraft.system.weapon.*;
import net.per.primogemcraft.system.wish.WishReports;

import java.util.List;

public class MistsplitterReforgedItem extends WishWeaponItem {
    private static final ToolMaterial TIER = new WeaponTier(3000, 4.0F, 20, WeaponTier.WOODEN_INCORRECT, PGCItems.ETERNAL_METAL);

    private static final double RESTORE_PER_REFINEMENT = 0.5D;
    private static final double THUNDER_PER_REFINEMENT = 0.3D;
    private static final int FOOD_COST = 9;
    private static final int MIN_FOOD = 9;
    private static final double RADIUS = 8.0D;
    private static final int THUNDER_COOLDOWN = 40;
    private static final int SATURATION_TICKS = 5;
    private static final float SACRIFICE_RATIO = 0.2F;
    private static final float ATTACK_DAMAGE = 8.0F;
    private static final float ATTACK_SPEED = -2.4F;
    private static final String NORMAL_ATTACK = "normal_attack";
    private static final String RIGHT_CLICK = "right_click";
    private static final String SNEAK_USE = "sneak_use";
    private static final String DURABILITY_TEXT = "durability";
    private static final String THUNDER_TEXT = "thunder";
    private static final String SATURATION_TEXT = "saturation";

    public MistsplitterReforgedItem(Settings properties) {
        super(TIER, ATTACK_DAMAGE, ATTACK_SPEED, properties.fireproof());
    }

    @Override
    protected List<WeaponDescription> description(ItemStack stack) {
        var state = WeaponState.of(stack);
        var refinement = state.refinements();
        return List.of(
                WeaponDescription.of(NORMAL_ATTACK, DURABILITY_TEXT,
                        WishReports.number(RESTORE_PER_REFINEMENT * refinement, Formatting.AQUA)),
                WeaponDescription.of(RIGHT_CLICK, THUNDER_TEXT,
                        WishReports.percent(thunderRatio(refinement), Formatting.AQUA),
                        WishReports.number(THUNDER_COOLDOWN / 20, Formatting.AQUA)),
                WeaponDescription.of(SNEAK_USE, SATURATION_TEXT,
                        WishReports.number(refinement, Formatting.AQUA)));
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker.getWorld() instanceof ServerWorld)
            WeaponAttributes.restoreDurability(stack, restorePoints(WeaponState.of(stack).refinements()));
        return super.postHit(stack, target, attacker);
    }

    @Override
    public TypedActionResult<ItemStack> use(World level, PlayerEntity player, Hand hand) {
        var stack = player.getStackInHand(hand);
        if (level.isClient() || player.getItemCooldownManager().isCoolingDown(stack.getItem())) return super.use(level, player, hand);
        var refinement = WeaponState.of(stack).refinements();
        if (player.isSneaking()) {
            player.damage(player.getDamageSources().genericKill(), player.getMaxHealth() * SACRIFICE_RATIO);
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.SATURATION, SATURATION_TICKS, refinement - 1, false, false));
            player.getItemCooldownManager().set(stack.getItem(), THUNDER_COOLDOWN);
            return super.use(level, player, hand);
        }
        if (player.getHungerManager().getFoodLevel() < MIN_FOOD) return super.use(level, player, hand);
        player.getItemCooldownManager().set(stack.getItem(), THUNDER_COOLDOWN);
        var damage = (float) (player.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE) * thunderRatio(refinement));
        if (!(level instanceof ServerWorld server)) return super.use(level, player, hand);
        for (var candidate : server.getEntitiesByClass(LivingEntity.class, player.getBoundingBox().expand(RADIUS), e -> true))
            if (candidate != player && candidate.isAlive() && candidate instanceof MobEntity mob && mob.getTarget() == player) {
                WeaponDamage.extraHit(candidate,
                        WeaponDamage.lightning(server, Element.ELECTRO, candidate, player, ElementDamageOptions.DETACHED), damage);
                strike(server, candidate.getBlockPos());
                player.getHungerManager().setFoodLevel(Math.max(0, player.getHungerManager().getFoodLevel() - FOOD_COST));
            }
        server.playSound(null, player.getBlockPos(), SoundEvents.ITEM_TRIDENT_THUNDER, SoundCategory.PLAYERS, 2.0F, 0.3F);
        return super.use(level, player, hand);
    }

    private static void strike(ServerWorld level, BlockPos pos) {
        var bolt = EntityType.LIGHTNING_BOLT.create(level);
        if (bolt == null) return;
        bolt.refreshPositionAfterTeleport(Vec3d.ofBottomCenter(pos));
        bolt.setCosmetic(true);
        level.spawnEntity(bolt);
    }

    private static int restorePoints(int refinement) {
        return (int) Math.ceil(RESTORE_PER_REFINEMENT * refinement);
    }

    private static double thunderRatio(int refinement) {
        return THUNDER_PER_REFINEMENT * refinement;
    }
}
