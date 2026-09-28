package net.per.primogemcraft.item.weapon;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
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

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

@EventBusSubscriber(modid = MOD_ID)
public final class ThunderingPulseItem extends WishWeaponBowItem {
    private static final String EMPOWERMENT = "thundering_pulse_empowerment";
    private static final String EMPOWERMENT_COOLDOWN = "thundering_pulse_empowerment_cooldown";
    private static final String DASH = "thundering_pulse_dash";
    private static final List<PendingArrow> PENDING_ARROWS = new ArrayList<>();

    public ThunderingPulseItem(Properties properties) {
        super(properties.fireResistant(), new BowAttackCycle(5, 10, 5), 3.0F,
                ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/item/thundering_pulse.png"),
                ResourceLocation.withDefaultNamespace("textures/entity/projectiles/arrow.png"), SoundEvents.ARROW_SHOOT);
    }

    @Override
    public List<WeaponDescription> descriptions(ItemStack stack) {
        var refinement = WeaponEnhancement.refinementOf(WishTooltips.viewer(), stack);
        return List.of(
                WeaponDescription.of("left_click", "dash",
                        WishReports.number(ThunderingPulseStats.dashSpeed(refinement), ChatFormatting.AQUA),
                        WishReports.number(ThunderingPulseStats.dashCooldown(refinement) / 20.0D, ChatFormatting.AQUA)),
                WeaponDescription.of("sneak_use", "empowerment",
                        WishReports.number(ThunderingPulseStats.duration(refinement) / 20.0D, ChatFormatting.AQUA),
                        WishReports.number(ThunderingPulseStats.maximumPiercing(refinement), ChatFormatting.AQUA),
                        WishReports.number(ThunderingPulseStats.empowermentCooldown(refinement) / 20.0D, ChatFormatting.AQUA)),
                refinement < 5 ? WeaponDescription.of("special_effect", "fire_arrow_locked")
                        : WeaponDescription.of("special_effect", "fire_arrow",
                                WishReports.number(ThunderingPulseStats.fireArrowChance(refinement), ChatFormatting.AQUA),
                                WishReports.number(ThunderingPulseStats.fireArrowDamageMultiplier(refinement) * 100.0D, ChatFormatting.AQUA)));
    }

    public static void dash(ServerPlayer player) {
        var stack = player.getMainHandItem();
        if (!stack.is(PGCItems.THUNDERING_PULSE.get()) || !player.isAlive() || player.isSpectator() || player.isPassenger()
                || player.containerMenu != player.inventoryMenu || !PGCTimer.isDone(player, DASH)) return;
        var refinement = WeaponEnhancement.refinementOf(player, stack);
        PGCTimer.set(player, DASH, ThunderingPulseStats.dashCooldown(refinement));
        player.stopUsingItem();
        player.setDeltaMovement(player.getLookAngle().scale(ThunderingPulseStats.dashSpeed(refinement)));
        player.hurtMarked = true;
        player.level().playSound(null, player.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0F, 1.4F);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!player.isShiftKeyDown()) return super.use(level, player, hand);
        var stack = player.getItemInHand(hand);
        if (hand != InteractionHand.MAIN_HAND || !player.isAlive() || player.isSpectator())
            return InteractionResultHolder.fail(stack);
        if (level.isClientSide()) return InteractionResultHolder.consume(stack);
        if (player.containerMenu != player.inventoryMenu || !PGCTimer.isDone(player, EMPOWERMENT_COOLDOWN))
            return InteractionResultHolder.fail(stack);
        var refinement = WeaponEnhancement.refinementOf(player, stack);
        PGCTimer.set(player, EMPOWERMENT, ThunderingPulseStats.duration(refinement));
        PGCTimer.set(player, EMPOWERMENT_COOLDOWN, ThunderingPulseStats.empowermentCooldown(refinement));
        if (player instanceof ServerPlayer serverPlayer)
            serverPlayer.serverLevel().sendParticles(ParticleTypes.FLAME, player.getX(), player.getEyeY(), player.getZ(),
                    12, 0.25D, 0.25D, 0.25D, 0.02D);
        level.playSound(null, player.blockPosition(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.8F, 1.2F);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    protected Projectile createProjectile(Level level, LivingEntity shooter, ItemStack weapon, ItemStack ammunition, boolean critical) {
        var projectile = super.createProjectile(level, shooter, weapon, ammunition, critical);
        if (projectile instanceof WishArrowEntity arrow && arrow.getOwner() instanceof Player player
                && !PGCTimer.isDone(player, EMPOWERMENT)) {
            var refinement = WeaponEnhancement.refinementOf(player, weapon);
            arrow.empower(1 + player.getRandom().nextInt(ThunderingPulseStats.maximumPiercing(refinement)));
        }
        return projectile;
    }

    @Override
    protected void shootProjectile(LivingEntity shooter, Projectile projectile, int index, float velocity,
                                   float inaccuracy, float angle, @Nullable LivingEntity target) {
        super.shootProjectile(shooter, projectile, index, velocity, inaccuracy, angle, target);
        if (!(shooter.level() instanceof ServerLevel level) || !(projectile instanceof WishArrowEntity arrow)
                || !(arrow.getOwner() instanceof Player owner) || PGCTimer.isDone(owner, EMPOWERMENT)) return;
        var weapon = arrow.getWeaponItem();
        var refinement = WeaponEnhancement.refinementOf(owner, weapon);
        var chance = ThunderingPulseStats.fireArrowChance(refinement);
        if (chance == 0 || owner.getRandom().nextInt(100) >= chance) return;
        var damage = (float) (shooter.getAttributeValue(Attributes.ATTACK_DAMAGE)
                * ThunderingPulseStats.fireArrowDamageMultiplier(refinement));
        PENDING_ARROWS.add(new PendingArrow(level, shooter, owner, weapon.copy(), damage, velocity,
                arrowTexture(), arrow.getDeltaMovement(), level.getGameTime() + 2));
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
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
            if (level.getGameTime() < pending.shotTime()) continue;
            iterator.remove();
            var fireArrow = new WishArrowEntity(level, owner, shooter, new ItemStack(Items.ARROW), pending.weapon(),
                    pending.damage(), pending.velocity(), pending.texture());
            fireArrow.makeFireArrow();
            var direction = pending.direction();
            fireArrow.shoot(direction.x, direction.y, direction.z, pending.velocity(), 0.0F);
            if (level.addFreshEntity(fireArrow))
                level.playSound(null, shooter.getX(), shooter.getY(), shooter.getZ(),
                        PGCSounds.THUNDERING_PULSE_FIRE_ARROW.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        }
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        PENDING_ARROWS.clear();
    }

    private record PendingArrow(ServerLevel level, LivingEntity shooter, Player owner, ItemStack weapon,
                                float damage, float velocity, ResourceLocation texture, Vec3 direction, long shotTime) {
    }
}
