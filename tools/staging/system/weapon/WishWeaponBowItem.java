package net.per.primogemcraft.system.weapon;

import com.google.common.collect.Multimap;
import net.minecraft.util.Formatting;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.util.UseAction;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.world.World;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.event.ForgeEventFactory;
import net.per.primogemcraft.compat.component.StackComponents;
import net.per.primogemcraft.compat.item.Enchantments120;
import net.per.primogemcraft.compat.item.WeaponAttributes120;
import net.per.primogemcraft.entity.misc.WishArrowEntity;
import net.per.primogemcraft.registry.PGCDataComponents;
import net.per.primogemcraft.system.living.LivingItemUsePlayer;
import net.per.primogemcraft.system.wish.WishReports;
import net.per.primogemcraft.system.wish.WishTooltips;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public class WishWeaponBowItem extends BowItem implements WishWeapon {
    private static final float BASE_ATTACK_SPEED = -2.4F;

    private final BowAttackCycle cycle;
    private final float projectileSpeed;
    private final Identifier texture;
    private final Identifier arrowTexture;
    @Nullable
    private final SoundEvent attackSound;
    private final List<WeaponModifier> passives;

    public WishWeaponBowItem(Settings properties, BowAttackCycle cycle, float projectileSpeed,
                             Identifier texture, Identifier arrowTexture,
                             @Nullable SoundEvent attackSound, WeaponModifier... passives) {
        super(properties.stacksTo(1));
        if (!Float.isFinite(projectileSpeed) || projectileSpeed <= 0.0F || projectileSpeed > 16.0F)
            throw new IllegalArgumentException("Invalid bow projectile speed");
        this.cycle = Objects.requireNonNull(cycle);
        this.projectileSpeed = projectileSpeed;
        this.texture = Objects.requireNonNull(texture);
        this.arrowTexture = Objects.requireNonNull(arrowTexture);
        this.attackSound = attackSound;
        this.passives = List.of(passives);
    }

    public BowAttackCycle cycle() {
        return cycle;
    }

    public Identifier texture() {
        return texture;
    }

    public Identifier arrowTexture() {
        return arrowTexture;
    }

    @Override
    public List<WeaponModifier> passives() {
        return passives;
    }

    @Override
    public List<WeaponModifier> conditionalPassives(Player player, ItemStack stack, int slot, int refinement) {
        if (!player.isUsingItem() || player.getActiveItem() != stack || player.isPassenger()
                || !StackComponents.has(stack, PGCDataComponents.BOW_DRAW_DURATION)) return List.of();
        return List.of(WeaponModifier.conditional(EntityAttributes.MOVEMENT_SPEED, EntityAttributeModifier.Operation.MULTIPLY_TOTAL,
                (owner, weapon, rank) -> BowRefinement.drawMovementBonus(rank)));
    }

    @Override
    public List<WeaponDescription> descriptions(ItemStack stack) {
        return List.of();
    }

    @Override
    public Multimap<EntityAttribute, EntityAttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        var mutable = com.google.common.collect.ArrayListMultimap.<EntityAttribute, EntityAttributeModifier>create();
        mutable.putAll(super.getDefaultAttributeModifiers(slot));
        if (slot == EquipmentSlot.MAINHAND)
            WeaponAttributes120.putWeaponModifiers(mutable, 0.0F, BASE_ATTACK_SPEED);
        return com.google.common.collect.ImmutableMultimap.copyOf(mutable);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable Level level, List<Text> tooltip, TooltipContext flag) {
        tooltip.add(Text.translatable(WeaponType.BOW.labelKey()));
        var passiveLines = new ArrayList<Text>();
        if (WishTooltips.showsDetails()) {
            var refinement = WeaponEnhancement.refinementOf(WishTooltips.viewer(), stack);
            var movement = 0.2D * (1.0D + BowRefinement.drawMovementBonus(refinement)) - 1.0D;
            var movementValue = WishReports.percent(movement, movement < 0.0D ? ChatFormatting.RED : ChatFormatting.YELLOW);
            passiveLines.add(Text.translatable("weapon.primogemcraft.bow.passive"));
            passiveLines.add(Text.translatable("weapon.primogemcraft.bow.tooltip.0",
                    WishReports.percent(20.0D / cycle.maximumCooldown(), ChatFormatting.AQUA),
                    WishReports.percent(20.0D / cycle.minimumCooldown(), ChatFormatting.AQUA)));
            passiveLines.add(Text.translatable("weapon.primogemcraft.bow.tooltip.1",
                    movement > 0.0D ? Text.translatable("weapon.primogemcraft.bow.positive", movementValue)
                            .withStyle(ChatFormatting.YELLOW) : movementValue));
            passiveLines.add(Text.translatable("weapon.primogemcraft.bow.tooltip.2",
                    WishReports.number(BowRefinement.targetRange(refinement), ChatFormatting.AQUA)));
        }
        tooltip.addAll(WishWeaponTooltips.lines(stack.getDescriptionId(), descriptions(stack), passiveLines));
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (level.isClient() || !(entity instanceof Player player)) return;
        WeaponAttributes.refreshPassive(stack, player, slot);
    }

    @Override
    public TypedActionResult<ItemStack> use(Level level, Player player, Hand hand) {
        var stack = player.getItemInHand(hand);
        if (hand != Hand.MAIN_HAND || !player.isAlive() || player.isSpectator())
            return TypedActionResult.fail(stack);
        var result = ForgeEventFactory.onArrowNock(stack, level, player, hand, true);
        if (result != null) return result;
        player.startUsingItem(hand);
        if (!level.isClient() && player.isUsingItem()) startDraw(stack, player);
        return TypedActionResult.consume(stack);
    }

    @Override
    public void initializeClient(java.util.function.Consumer<net.minecraftforge.client.extensions.common.IClientItemExtensions> consumer) {
        consumer.accept(new net.per.primogemcraft.client.WishBowClientExtensions());
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return super.canApplyAtEnchantingTable(stack, enchantment)
                || Items.BOW.canApplyAtEnchantingTable(stack, enchantment);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int remainingTicks) {
    }

    @Override
    public int getEnchantmentValue() {
        return 1;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public AbstractArrow customArrow(AbstractArrow arrow) {
        return arrow;
    }

    @Override
    public Predicate<ItemStack> getAllSupportedProjectiles() {
        return ARROW_OR_FIREWORK;
    }

    @Override
    public int getDefaultProjectileRange() {
        return 15;
    }

    private void startDraw(ItemStack stack, Player player) {
        var duration = cycle.cooldown(player.getRandom().nextDouble());
        StackComponents.set(stack, PGCDataComponents.BOW_DRAW_DURATION, duration);
        StackComponents.set(stack, PGCDataComponents.BOW_SHOT_TIME, player.level().getTime() + duration);
        var owner = player instanceof LivingItemUsePlayer proxy ? proxy.owner() : player;
        WeaponAttributes.refreshPassive(stack, owner, owner.getInventory().selected);
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingTicks) {
        if (!(entity instanceof ServerPlayerEntity player)) return;
        if (!player.isAlive() || player.isSpectator() || player.containerMenu != player.inventoryMenu
                || player.getUsedItemHand() != Hand.MAIN_HAND || player.getMainHandStack() != stack
                || player.getCooldowns().isOnCooldown(this)) {
            player.stopUsingItem();
            return;
        }
        var shotTime = StackComponents.get(stack, PGCDataComponents.BOW_SHOT_TIME);
        if (shotTime == null) startDraw(stack, player);
        else if (level.getTime() >= shotTime) {
            shoot(player, stack);
            startDraw(stack, player);
        }
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged || oldStack.getItem() != newStack.getItem();
    }

    private void shoot(ServerPlayerEntity player, ItemStack stack) {
        var level = player.serverLevel();
        var charge = StackComponents.getOrDefault(stack, PGCDataComponents.BOW_DRAW_DURATION, 20);
        if (ForgeEventFactory.onArrowLoose(stack, level, player, charge, true) < 0) return;
        var offhand = player.getOffHandStack();
        var consumeAmmo = offhand.getItem() instanceof ArrowItem || offhand.is(Items.FIREWORK_ROCKET);
        var ammunition = consumeAmmo ? offhand : new ItemStack(Items.ARROW);
        var projectiles = draw(stack, ammunition, player);
        shoot(level, player, Hand.MAIN_HAND, stack, projectiles, projectileSpeed, 0.0F, false, null);
    }

    private List<ItemStack> draw(ItemStack weapon, ItemStack ammunition, LivingEntity shooter) {
        if (shooter.level().isClient()) return List.of();
        var count = Enchantments120.level(weapon, Enchantments.MULTISHOT) > 0 ? 3 : 1;
        var infinite = shooter instanceof Player player && player.getAbilities().instabuild;
        var result = new ArrayList<ItemStack>(count);
        for (var index = 0; index < count; index++) {
            var stack = index == 0 ? ammunition : ammunition.copy();
            if (stack.isEmpty() && infinite) stack = new ItemStack(Items.ARROW);
            if (stack.isEmpty()) break;
            result.add(stack);
            if (!infinite) ammunition.shrink(1);
        }
        return result;
    }

    private void shoot(Level level, LivingEntity shooter, Hand hand, ItemStack weapon,
                       List<ItemStack> projectiles, float velocity, float inaccuracy, boolean critical,
                       @Nullable LivingEntity target) {
        if (projectiles.isEmpty()) return;
        for (var index = 0; index < projectiles.size(); index++) {
            var ammunition = projectiles.get(index);
            var angle = (index - (projectiles.size() - 1) / 2.0F) * 10.0F;
            var projectile = createProjectile(level, shooter, weapon, ammunition, critical);
            shootProjectile(shooter, projectile, index, velocity, inaccuracy, angle, target);
            level.addFreshEntity(projectile);
            level.playSound(null, shooter.getX(), shooter.getY(), shooter.getZ(), SoundEvents.ARROW_SHOOT,
                    SoundSource.PLAYERS, 1.0F, 1.0F / (level.getRandom().nextFloat() * 0.4F + 1.2F) + velocity * 0.5F);
        }
        weapon.hurtAndBreak(1, shooter, entity -> entity.broadcastBreakEvent(hand));
    }

    protected Projectile createProjectile(Level level, LivingEntity shooter, ItemStack weapon, ItemStack ammunition, boolean critical) {
        var player = (ServerPlayerEntity) shooter;
        var owner = player instanceof LivingItemUsePlayer proxy ? proxy.owner() : player;
        Projectile projectile;
        if (ammunition.is(Items.FIREWORK_ROCKET)) {
            projectile = new FireworkRocketEntity(level, ammunition, owner, player.getX(), player.getEyeY() - 0.1D, player.getZ(), true);
        } else {
            var arrow = new WishArrowEntity((ServerWorld) level, owner, shooter, ammunition, weapon,
                    (float) player.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE), projectileSpeed, arrowTexture);
            if (critical) arrow.setCritArrow(true);
            var power = Enchantments120.level(weapon, Enchantments.POWER_ARROWS);
            if (power > 0) arrow.setBaseDamage(arrow.getBaseDamage() + power * 0.5D + 0.5D);
            var punch = Enchantments120.level(weapon, Enchantments.PUNCH_ARROWS);
            if (punch > 0) arrow.setKnockback(punch);
            if (Enchantments120.level(weapon, Enchantments.FLAMING_ARROWS) > 0) arrow.setSecondsOnFire(100);
            if (owner.getAbilities().instabuild) arrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
            projectile = customArrow(arrow);
        }
        projectile.setPos(player.getX(), player.getEyeY() - 0.1D, player.getZ());
        return projectile;
    }

    protected void shootProjectile(LivingEntity shooter, Projectile projectile, int index, float velocity,
                                   float inaccuracy, float angle, @Nullable LivingEntity target) {
        var forward = shooter.getLookAngle().normalize();
        var right = forward.cross(new Vec3d(0.0D, 1.0D, 0.0D)).normalize();
        if (right.lengthSqr() < 1.0E-6D) right = new Vec3d(1.0D, 0.0D, 0.0D);
        var up = right.cross(forward).normalize();
        var radians = Math.toRadians(angle);
        var direction = forward.scale(Math.cos(radians)).add(up.scale(Math.sin(radians))).normalize();
        var random = shooter.getRandom();
        direction = direction.add(random.nextTriangular(0.0D, 0.0172275D * inaccuracy),
                random.nextTriangular(0.0D, 0.0172275D * inaccuracy),
                random.nextTriangular(0.0D, 0.0172275D * inaccuracy));
        projectile.shoot(direction.x, direction.y, direction.z, velocity, inaccuracy);
    }
}
