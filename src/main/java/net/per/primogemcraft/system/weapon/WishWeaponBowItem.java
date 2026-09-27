package net.per.primogemcraft.system.weapon;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.per.primogemcraft.entity.misc.WishArrowEntity;
import net.per.primogemcraft.registry.PGCDataComponents;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.util.PGCTimer;
import net.per.primogemcraft.system.wish.WishReports;
import net.per.primogemcraft.system.wish.WishTooltips;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

@EventBusSubscriber(modid = MOD_ID)
public class WishWeaponBowItem extends Item implements WishWeapon {
    public static final boolean SHOW_IN_CREATIVE_TAB = false;
    private static final String SHOT_TIMER = "weapon/bow_shot";
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
    public List<WeaponDescription> descriptions(ItemStack stack) {
        return List.of();
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(WeaponType.BOW.labelKey()));
        if (WishTooltips.showsDetails()) {
            tooltip.addAll(WeaponDescription.of("normal_attack", "shoot",
                    WishReports.number(cycle.minimumCooldown() / 20.0D, ChatFormatting.AQUA),
                    WishReports.number(cycle.maximumCooldown() / 20.0D, ChatFormatting.AQUA)).lines("weapon.primogemcraft.bow"));
            tooltip.addAll(WeaponDescription.note("ammunition").lines("weapon.primogemcraft.bow"));
        }
        tooltip.addAll(WishWeaponTooltips.lines(stack.getDescriptionId(), descriptions(stack)));
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
        return InteractionResultHolder.fail(player.getItemInHand(hand));
    }

    @Override
    public boolean canAttackBlock(BlockState state, Level level, BlockPos pos, Player player) {
        return false;
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        return true;
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged || oldStack.getItem() != newStack.getItem();
    }

    @SubscribeEvent
    public static void blockAttack(PlayerInteractEvent.LeftClickBlock event) {
        if (event.getEntity().getMainHandItem().getItem() instanceof WishWeaponBowItem) event.setCanceled(true);
    }

    public static void shoot(ServerPlayer player) {
        var stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof WishWeaponBowItem bow) || !player.isAlive() || player.isSpectator()
                || player.isUsingItem() || player.containerMenu != player.inventoryMenu
                || !PGCTimer.isDone(player, SHOT_TIMER) || player.getCooldowns().isOnCooldown(bow)) return;
        var level = player.serverLevel();
        var offhand = player.getOffhandItem();
        var consumeAmmo = offhand.getItem() instanceof ArrowItem || offhand.is(Items.FIREWORK_ROCKET);
        var ammunition = consumeAmmo ? offhand.copyWithCount(1) : new ItemStack(Items.ARROW);
        Projectile projectile;
        if (ammunition.is(Items.FIREWORK_ROCKET)) {
            projectile = new FireworkRocketEntity(level, ammunition, player,
                    player.getX(), player.getEyeY() - 0.1D, player.getZ(), true);
        } else {
            projectile = new WishArrowEntity(level, player, ammunition, stack,
                    (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE),
                    bow.projectileSpeed, bow.arrowTexture);
        }
        projectile.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, bow.projectileSpeed, 0.0F);
        if (!level.addFreshEntity(projectile)) return;
        if (consumeAmmo) offhand.shrink(1);
        PGCTimer.set(player, SHOT_TIMER, bow.cycle.cooldown(player.getRandom().nextDouble()));
        stack.set(PGCDataComponents.BOW_SHOT_TIME.get(), level.getGameTime());
        if (bow.attackSound != null)
            level.playSound(null, player.getX(), player.getY(), player.getZ(), bow.attackSound, SoundSource.PLAYERS, 1.0F, 1.0F);
    }
}
