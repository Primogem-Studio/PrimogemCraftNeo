package net.per.primogemcraft.item.weapon.element;

import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.system.element.Element;
import net.per.primogemcraft.system.weapon.WeaponDescription;
import net.per.primogemcraft.system.weapon.WeaponState;
import net.per.primogemcraft.system.weapon.WeaponTier;
import net.per.primogemcraft.system.weapon.WishWeaponShovelItem;
import net.per.primogemcraft.system.wish.WishReports;
import net.per.primogemcraft.system.wish.WishTooltips;

import java.util.ArrayList;
import java.util.List;

public class AgnidusAgateShovelItem extends WishWeaponShovelItem {
    private static final Tier TIER = new WeaponTier(1561, 8.0F, 14, WeaponTier.DIAMOND_INCORRECT,
            PGCItems.PRIMOGEM, PGCItems.AGNIDUS_AGATE_SLIVER);

    private static final float ATTACK_DAMAGE = 3.0F;
    private static final float ATTACK_SPEED = -3.0F;
    private static final double TRIGGER_CHANCE = 0.2D;
    private static final double COOLDOWN_TICKS = 100.0D;
    private static final int MAX_OUTPUT = 4;
    private static final int DURABILITY_LOSS = 1;
    private static final int PICKUP_DELAY = 10;
    private static final float SOUND_VOLUME = 1.0F;
    private static final float SOUND_PITCH = 1.0F;
    private static final String SNEAK_USE_BLOCK = "sneak_use_block";
    private static final String SMELTING_TEXT = "smelting";
    private static final String REFINEMENT_TEXT = "refinement";

    public AgnidusAgateShovelItem(Properties properties) {
        super(TIER, properties.fireResistant(), ATTACK_DAMAGE, ATTACK_SPEED);
    }

    @Override
    protected List<WeaponDescription> description(ItemStack stack) {
        var sealed = Element.holdsWaxSeal(WishTooltips.viewer(), Element.PYRO);
        var refinement = WeaponState.of(stack).refinements();
        var descriptions = new ArrayList<WeaponDescription>();
        if (sealed) descriptions.add(WeaponDescription.note(REFINEMENT_TEXT));
        descriptions.add(WeaponDescription.of(SNEAK_USE_BLOCK, SMELTING_TEXT,
                WishReports.percent(ElementWeapons.scaled(refinement, TRIGGER_CHANCE, true, sealed), ChatFormatting.AQUA),
                WishReports.number(ElementWeapons.seconds(refinement, COOLDOWN_TICKS, false, sealed), ChatFormatting.AQUA)));
        return List.copyOf(descriptions);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        var result = super.useOn(context);
        var player = context.getPlayer();
        if (!(context.getLevel() instanceof ServerLevel server) || player == null || !player.isShiftKeyDown()) return result;
        var pos = context.getClickedPos();
        var input = new SingleRecipeInput(new ItemStack(server.getBlockState(pos).getBlock()));
        var smelted = server.getRecipeManager().getRecipeFor(RecipeType.SMELTING, input, server)
                .map(recipe -> recipe.value().getResultItem(server.registryAccess()).copy())
                .orElse(ItemStack.EMPTY);
        if (smelted.isEmpty() || smelted.is(Blocks.AIR.asItem())) return result;
        var stack = context.getItemInHand();
        server.playSound(null, pos, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, SOUND_VOLUME, SOUND_PITCH);
        if (server.getRandom().nextDouble() < ElementWeapons.scaled(player, stack, Element.PYRO, TRIGGER_CHANCE, true)) {
            smelted.setCount(Mth.nextInt(server.getRandom(), 1, MAX_OUTPUT));
            var center = Vec3.atCenterOf(pos);
            var dropped = new ItemEntity(server, center.x, center.y, center.z, smelted);
            dropped.setPickUpDelay(PICKUP_DELAY);
            server.addFreshEntity(dropped);
        }
        server.destroyBlock(pos, false);
        stack.hurtAndBreak(DURABILITY_LOSS, server, player, item -> {
        });
        player.getCooldowns().addCooldown(stack.getItem(), ElementWeapons.ticks(player, stack, Element.PYRO, COOLDOWN_TICKS, false));
        return InteractionResult.SUCCESS;
    }
}
