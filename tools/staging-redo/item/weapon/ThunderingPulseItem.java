package net.per.primogemcraft.item.weapon;

import net.minecraft.util.Formatting;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.Identifier;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.world.World;
import net.minecraft.util.math.Vec3d;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.per.primogemcraft.entity.misc.WishArrowEntity;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.registry.PGCSounds;
import net.per.primogemcraft.system.weapon.BowAttackCycle;
import net.per.primogemcraft.system.weapon.ThunderingPulseStats;
import net.per.primogemcraft.system.weapon.WeaponDescription;
import net.per.primogemcraft.system.weapon.WeaponEnhancement;
import net.per.primogemcraft.system.weapon.WishWeaponBowItem;
import net.per.primogemcraft.system.wish.WishReports;
import net.per.primogemcraft.system.wish.WishTooltips;
import net.per.primogemcraft.util.PGCTimer;

import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public final class ThunderingPulseItem extends WishWeaponBowItem {
    private static final String EMPOWERMENT = "thundering_pulse_empowerment";
    private static final String EMPOWERMENT_COOLDOWN = "thundering_pulse_empowerment_cooldown";
    private static final String DASH = "thundering_pulse_dash";
    private static final List<PendingArrow> PENDING_ARROWS = new ArrayList<>();

    public ThunderingPulseItem(Settings properties) {
        super(properties.fireResistant(), new BowAttackCycle(5, 10, 5), 3.0F,
                Identifier.fromNamespaceAndPath(MOD_ID, "textures/item/thundering_pulse.png"),
                Identifier.fromNamespaceAndPath("minecraft", "textures/entity/projectiles/arrow.png"), SoundEvents.ARROW_SHOOT);
    }

    @Override
    public List<WeaponDescription> descriptions(ItemStack stack) {
        var refinement = WeaponEnhancement.refinementOf(WishTooltips.viewer(), stack);
        return List.of(
                WeaponDescription.of("left_click", "dash",
                        WishReports.number(ThunderingPulseStats.dashSpeed(refinement), Formatting.AQUA),
                        WishReports.number(ThunderingPulseStats.dashCooldown(refinement) / 20.0D, Formatting.AQUA)),
                WeaponDescription.of("sneak_use", "empowerment",
                        WishReports.number(ThunderingPulseStats.duration(refinement) / 20.0D, Formatting.AQUA),
                        WishReports.number(ThunderingPulseStats.maximumPiercing(refinement), Formatting.AQUA),
                        WishReports.number(ThunderingPulseStats.empowermentCooldown(refinement) / 20.0D, Formatting.AQUA),
                        WishReports.percent(ThunderingPulseStats.empowermentDamageBonus(refinement), Formatting.AQUA)),
                refinement < 5 ? WeaponDescription.of("special_effect", "fire_arrow_locked")
                        : WeaponDescription.of("special_effect", "fire_arrow",
                        WishReports.number(ThunderingPulseStats.fireArrowChance(refinement), Formatting.AQUA),
                        WishReports.number(ThunderingPulseStats.fireArrowDamageMultiplier(refinement) * 100.0D, Formatting.AQUA)));
    }

    public static void dash(ServerPlayerEntity player) {
        var stack = player.getMainHandStack();
        if (!stack.is(PGCItems.THUNDERING_PULSE.get()) || !player.isAlive() || player.isSpectator() || player.isPassenger()
                || player.containerMenu != player.inventoryMenu || !PGCTimer.isDone(player, DASH)) return;
        var refinement = WeaponEnhancement.refinementOf(player, stack);
        PGCTimer.set(player, DASH, ThunderingPulseStats.dashCooldown(refinement));
        player.stopUsingItem();
        player.setDeltaMovement(player.getLookAngle().scale(ThunderingPulseStats.dashSpeed(refinement)));
        player.hurtMarked = true;
        player.level().playSound(null, player.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundCategory.PLAYERS, 1.0F, 1.4F);
    }

    @Override
    public TypedActionResult<ItemStack> use(Level level, Player player, Hand hand) {
        if (!player.isShiftKeyDown()) return super.use(level, player, hand);
        var stack = player.getItemInHand(hand);
        if (hand != Hand.MAIN_HAND || !player.isAlive() || player.isSpectator())
            return TypedActionResult.fail(stack);
        if (level.isClient()) return TypedActionResult.consume(stack);
        if (player.containerMenu != player.inventoryMenu || !PGCTimer.isDone(player, EMPOWERMENT_COOLDOWN))
            return TypedActionResult.fail(stack);
        var refinement = WeaponEnhancement.refinementOf(player, stack);
        PGCTimer.set(player, EMPOWERMENT, ThunderingPulseStats.duration(refinement));
        PGCTimer.set(player, EMPOWERMENT_COOLDOWN, ThunderingPulseStats.empowermentCooldown(refinement));
        if (player instanceof ServerPlayerEntity serverPlayer)
            serverPlayer.serverLevel().sendParticles(ParticleTypes.FLAME, player.getX(), player.getEyeY(), player.getZ(),
                    12, 0.25D, 0.25D, 0.25D, 0.02D);
        level.playSound(null, player.blockPosition(), SoundEvents.FIRECHARGE_USE, SoundCategory.PLAYERS, 0.8F, 1.2F);
        return TypedActionResult.consume(stack);
    }

    @Override
    protected Projectile createProjectile(Level level, LivingEntity shooter, ItemStack weapon, ItemStack ammunition, boolean critical) {
        var projectile = super.createProjectile(level, shooter, weapon, ammunition, critical);
        if (projectile instanceof WishArrowEntity arrow && arrow.getOwner() instanceof Player player
                && !PGCTimer.isDone(player, EMPOWERMENT)) {
            var refinement = WeaponEnhancement.refinementOf(player, weapon);
            arrow.empower(1 + player.getRandom().nextInt(ThunderingPulseStats.maximumPiercing(refinement)),
                    ThunderingPulseStats.empowermentDamageBonus(refinement));
        }
        return projectile;
    }

    @Override
    protected void shootProjectile(LivingEntity shooter, Projectile projectile, int index, float velocity,
                                   float inaccuracy, float angle, @Nullable LivingEntity target) {
        super.shootProjectile(shooter, projectile, index, velocity, inaccuracy, angle, target);
        if (!(shooter.level() instanceof ServerWorld level) || !(projectile instanceof WishArrowEntity arrow)
                || !(arrow.getOwner() instanceof Player owner) || PGCTimer.isDone(owner, EMPOWERMENT)) return;
        var weapon = arrow.getWeaponItem();
        var refinement = WeaponEnhancement.refinementOf(owner, weapon);
        var chance = ThunderingPulseStats.fireArrowChance(refinement);
        if (chance == 0 || owner.getRandom().nextInt(100) >= chance) return;
        var damage = (float) (shooter.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE)
                * ThunderingPulseStats.fireArrowDamageMultiplier(refinement));
        PENDING_ARROWS.add(new PendingArrow(level, shooter, owner, weapon.copy(), damage, velocity,
                arrowTexture(), arrow.getDeltaMovement(), level.getTime() + 2));
    }

    /** Forge 用 ServerTickEvent(END)；Fabric 的 END_SERVER_TICK 语义等价。 */
    public static void registerEvents() {
        ServerTickEvents.END_SERVER_TICK.register(server -> onServerTick());
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> PENDING_ARROWS.clear());
    }

    private static void onServerTick() {
        var iterator = PENDING_ARROWS.iterator();
        while (iterator.hasNext()) {
            var pending = iterator.next();
            var level = pending.level();
            var shooter = pending.shooter();
            var owner = pending.owner();
            if (!shooter.isAlive() || shooter.isRemoved() || shooter.level() != level
                    || !owner.isAlive()
                    || owner.isRemoved() || owner.isSpectator() || owner.level() != level) {
                iterator.remove();
                continue;
            }
            if (level.getTime() < pending.shotTime()) continue;
            iterator.remove();
            var fireArrow = new WishArrowEntity(level, owner, shooter, new ItemStack(Items.ARROW), pending.weapon(),
                    pending.damage(), pending.velocity(), pending.texture());
            fireArrow.makeFireArrow();
            var direction = pending.direction();
            fireArrow.shoot(direction.x, direction.y, direction.z, pending.velocity(), 0.0F);
            if (level.addFreshEntity(fireArrow))
                level.playSound(null, shooter.getX(), shooter.getY(), shooter.getZ(),
                        PGCSounds.THUNDERING_PULSE_FIRE_ARROW.get(), SoundCategory.PLAYERS, 1.0F, 1.0F);
        }
    }

    private record PendingArrow(ServerWorld level, LivingEntity shooter, Player owner, ItemStack weapon,
                                float damage, float velocity, Identifier texture, Vec3d direction, long shotTime) {
    }
}
