package net.per.primogemcraft.system.weapon;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.neoforge.event.EventHooks;
import net.per.primogemcraft.system.living.LivingItemUsePlayer;
import net.minecraft.world.level.Level;
import net.per.primogemcraft.entity.misc.WishArrowEntity;
import net.per.primogemcraft.registry.PGCDataComponents;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.system.wish.WishReports;
import net.per.primogemcraft.system.wish.WishTooltips;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class WishWeaponBowItem extends BowItem implements WishWeapon {
    private final BowAttackCycle cycle;
    private final float projectileSpeed;
    private final ResourceLocation texture;
    private final ResourceLocation arrowTexture;
    @Nullable
    private final SoundEvent attackSound;
    private final List<WeaponModifier> passives;

    public WishWeaponBowItem(Properties properties, BowAttackCycle cycle, float projectileSpeed,
                             ResourceLocation texture, ResourceLocation arrowTexture,
                             @Nullable SoundEvent attackSound, WeaponModifier... passives) {
        super(properties.stacksTo(1).attributes(ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, 0.0D,
                        AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND).build()));
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

    public ResourceLocation texture() {
        return texture;
    }

    public ResourceLocation arrowTexture() {
        return arrowTexture;
    }

    @Override
    public List<WeaponModifier> passives() {
        return passives;
    }

    @Override
    public List<WeaponModifier> conditionalPassives(Player player, ItemStack stack, int slot, int refinement) {
        if (!player.isUsingItem() || player.getUseItem() != stack || player.isPassenger()
                || !stack.has(PGCDataComponents.BOW_DRAW_DURATION.get())) return List.of();
        return List.of(WeaponModifier.conditional(Attributes.MOVEMENT_SPEED, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL,
                (owner, weapon, rank) -> BowRefinement.drawMovementBonus(rank)));
    }

    @Override
    public List<WeaponDescription> descriptions(ItemStack stack) {
        return List.of();
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(WeaponType.BOW.labelKey()));
        var passiveLines = new ArrayList<Component>();
        if (WishTooltips.showsDetails()) {
            var refinement = WeaponEnhancement.refinementOf(WishTooltips.viewer(), stack);
            var movement = 0.2D * (1.0D + BowRefinement.drawMovementBonus(refinement)) - 1.0D;
            var movementValue = WishReports.percent(movement, movement < 0.0D ? ChatFormatting.RED : ChatFormatting.YELLOW);
            passiveLines.add(Component.translatable("weapon.primogemcraft.bow.passive"));
            passiveLines.add(Component.translatable("weapon.primogemcraft.bow.tooltip.0",
                    WishReports.percent(20.0D / cycle.maximumCooldown(), ChatFormatting.AQUA),
                    WishReports.percent(20.0D / cycle.minimumCooldown(), ChatFormatting.AQUA)));
            passiveLines.add(Component.translatable("weapon.primogemcraft.bow.tooltip.1",
                    movement > 0.0D ? Component.translatable("weapon.primogemcraft.bow.positive", movementValue)
                            .withStyle(ChatFormatting.YELLOW) : movementValue));
            passiveLines.add(Component.translatable("weapon.primogemcraft.bow.tooltip.2",
                    WishReports.number(BowRefinement.targetRange(refinement), ChatFormatting.AQUA)));
        }
        tooltip.addAll(WishWeaponTooltips.lines(stack.getDescriptionId(), descriptions(stack), passiveLines));
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (level.isClientSide() || !(entity instanceof Player player)) return;
        if (stack.is(PGCItems.EXAMPLE_WISH_BOW.get())) {
            var attributes = stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
            var base = attributes.modifiers().stream().filter(entry -> entry.attribute().equals(Attributes.ATTACK_DAMAGE)
                    && entry.modifier().is(Item.BASE_ATTACK_DAMAGE_ID)).findFirst();
            if (base.isEmpty() || base.get().modifier().amount() == 19.0D
                    && base.get().modifier().operation() == AttributeModifier.Operation.ADD_VALUE)
                stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attributes.withModifierAdded(Attributes.ATTACK_DAMAGE,
                        new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, 0.0D, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND));
        }
        WeaponAttributes.refreshPassive(stack, player, slot);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        if (hand != InteractionHand.MAIN_HAND || !player.isAlive() || player.isSpectator())
            return InteractionResultHolder.fail(stack);
        var result = EventHooks.onArrowNock(stack, level, player, hand, true);
        if (result != null) return result;
        player.startUsingItem(hand);
        if (!level.isClientSide() && player.isUsingItem()) startDraw(stack, player);
        return InteractionResultHolder.consume(stack);
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
    public boolean supportsEnchantment(ItemStack stack, Holder<Enchantment> enchantment) {
        return super.supportsEnchantment(stack, enchantment) || Items.BOW.getDefaultInstance().supportsEnchantment(enchantment);
    }

    @Override
    public boolean isPrimaryItemFor(ItemStack stack, Holder<Enchantment> enchantment) {
        return super.isPrimaryItemFor(stack, enchantment) || Items.BOW.getDefaultInstance().isPrimaryItemFor(enchantment);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int remainingTicks) {
    }

    @Override
    public int getEnchantmentValue() {
        return 1;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    private void startDraw(ItemStack stack, Player player) {
        var duration = cycle.cooldown(player.getRandom().nextDouble());
        stack.set(PGCDataComponents.BOW_DRAW_DURATION.get(), duration);
        stack.set(PGCDataComponents.BOW_SHOT_TIME.get(), player.level().getGameTime() + duration);
        var owner = player instanceof LivingItemUsePlayer proxy ? proxy.owner() : player;
        WeaponAttributes.refreshPassive(stack, owner, owner.getInventory().selected);
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingTicks) {
        if (!(entity instanceof ServerPlayer player)) return;
        if (!player.isAlive() || player.isSpectator() || player.containerMenu != player.inventoryMenu
                || player.getUsedItemHand() != InteractionHand.MAIN_HAND || player.getMainHandItem() != stack
                || player.getCooldowns().isOnCooldown(this)) {
            player.stopUsingItem();
            return;
        }
        var shotTime = stack.get(PGCDataComponents.BOW_SHOT_TIME.get());
        if (shotTime == null) startDraw(stack, player);
        else if (level.getGameTime() >= shotTime) {
            shoot(player, stack);
            startDraw(stack, player);
        }
    }

    @Override
    public void onStopUsing(ItemStack stack, LivingEntity entity, int remainingTicks) {
        stack.remove(PGCDataComponents.BOW_SHOT_TIME.get());
        stack.remove(PGCDataComponents.BOW_DRAW_DURATION.get());
        if (!entity.level().isClientSide() && entity instanceof Player player) {
            var owner = player instanceof LivingItemUsePlayer proxy ? proxy.owner() : player;
            WeaponAttributes.refreshPassive(stack, owner, owner.getInventory().selected);
        }
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged || oldStack.getItem() != newStack.getItem();
    }

    private void shoot(ServerPlayer player, ItemStack stack) {
        var level = player.serverLevel();
        if (EventHooks.onArrowLoose(stack, level, player, stack.getOrDefault(PGCDataComponents.BOW_DRAW_DURATION.get(), 20), true) < 0) return;
        var offhand = player.getOffhandItem();
        var consumeAmmo = offhand.getItem() instanceof ArrowItem || offhand.is(Items.FIREWORK_ROCKET);
        var ammunition = consumeAmmo ? offhand : new ItemStack(Items.ARROW);
        var projectiles = draw(stack, ammunition, player);
        shoot(level, player, InteractionHand.MAIN_HAND, stack, projectiles, projectileSpeed, 0.0F, false, null);
        if (attackSound != null)
            level.playSound(null, player.getX(), player.getY(), player.getZ(), attackSound, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    @Override
    protected Projectile createProjectile(Level level, LivingEntity shooter, ItemStack weapon, ItemStack ammunition, boolean critical) {
        var player = (ServerPlayer) shooter;
        var owner = player instanceof LivingItemUsePlayer proxy ? proxy.owner() : player;
        Projectile projectile;
        if (ammunition.is(Items.FIREWORK_ROCKET)) {
            projectile = new FireworkRocketEntity(level, ammunition, owner, player.getX(), player.getEyeY() - 0.1D, player.getZ(), true);
        } else if (ammunition.is(Items.ARROW) || ammunition.is(Items.TIPPED_ARROW) || ammunition.is(Items.SPECTRAL_ARROW)) {
            projectile = customArrow(new WishArrowEntity((ServerLevel) level, owner, shooter, ammunition, weapon,
                    (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE), projectileSpeed, arrowTexture), ammunition, weapon);
        } else {
            projectile = super.createProjectile(level, shooter, weapon, ammunition, critical);
            projectile.setOwner(owner);
        }
        projectile.setPos(player.getX(), player.getEyeY() - 0.1D, player.getZ());
        return projectile;
    }
}
