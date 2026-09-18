package net.per.primogemcraft.item.weapon.element;

import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.system.element.Element;
import net.per.primogemcraft.system.weapon.WeaponDescription;
import net.per.primogemcraft.system.weapon.WeaponState;
import net.per.primogemcraft.system.weapon.WeaponTier;
import net.per.primogemcraft.system.weapon.WishWeaponHoeItem;
import net.per.primogemcraft.system.wish.WishReports;
import net.per.primogemcraft.system.wish.WishTooltips;

import java.util.ArrayList;
import java.util.List;

public class ShivadaJadeHoeItem extends WishWeaponHoeItem {
    private static final Tier TIER = new WeaponTier(1561, 7.0F, 10, WeaponTier.DIAMOND_INCORRECT,
            PGCItems.PRIMOGEM, PGCItems.SHIVADA_JADE_SLIVER);

    private static final float ATTACK_DAMAGE = 3.0F;
    private static final float ATTACK_SPEED = -3.0F;
    private static final int COOLDOWN = 10;
    private static final int DURABILITY_LOSS = 1;
    private static final float SOUND_VOLUME = 0.5F;
    private static final float SOUND_PITCH = 1.0F;
    private static final String SNEAK_USE_BLOCK = "sneak_use_block";
    private static final String SNOW_TEXT = "snow";
    private static final String NO_REFINEMENT_TEXT = "no_refinement";

    public ShivadaJadeHoeItem(Properties properties) {
        super(TIER, properties.fireResistant(), ATTACK_DAMAGE, ATTACK_SPEED);
    }

    @Override
    protected List<WeaponDescription> description(ItemStack stack) {
        var sealed = Element.holdsWaxSeal(WishTooltips.viewer(), Element.CRYO);
        var refinement = WeaponState.of(stack).refinements();
        var descriptions = new ArrayList<WeaponDescription>();
        if (sealed) descriptions.add(WeaponDescription.note(NO_REFINEMENT_TEXT));
        descriptions.add(WeaponDescription.of(SNEAK_USE_BLOCK, SNOW_TEXT,
                WishReports.number(ElementWeapons.seconds(refinement, COOLDOWN, false, sealed), ChatFormatting.AQUA)));
        return List.copyOf(descriptions);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        var result = super.useOn(context);
        var player = context.getPlayer();
        if (!(context.getLevel() instanceof ServerLevel server) || player == null || !player.isShiftKeyDown()) return result;
        var pos = context.getClickedPos();
        var above = pos.above();
        if (server.getBlockState(above).is(Blocks.SNOW))
            server.setBlock(above, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        else if (server.getBlockState(above).isAir() && !server.getBlockState(pos).is(Blocks.SNOW))
            server.setBlock(above, Blocks.SNOW.defaultBlockState(), Block.UPDATE_ALL);
        server.playSound(null, pos, SoundEvents.SNOW_FALL, SoundSource.BLOCKS, SOUND_VOLUME, SOUND_PITCH);
        var stack = context.getItemInHand();
        player.getCooldowns().addCooldown(stack.getItem(), ElementWeapons.ticks(player, stack, Element.CRYO, COOLDOWN, false));
        stack.hurtAndBreak(DURABILITY_LOSS, server, player, item -> {
        });
        player.swing(context.getHand(), true);
        return InteractionResult.SUCCESS;
    }
}
