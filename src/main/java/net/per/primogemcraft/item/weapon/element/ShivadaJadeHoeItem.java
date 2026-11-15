package net.per.primogemcraft.item.weapon.element;

import net.minecraft.util.Formatting;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.ActionResult;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterial;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
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
    private static final ToolMaterial TIER = new WeaponTier(1561, 7.0F, 10, WeaponTier.DIAMOND_INCORRECT,
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

    public ShivadaJadeHoeItem(Settings properties) {
        super(TIER, properties.fireproof(), ATTACK_DAMAGE, ATTACK_SPEED);
    }

    @Override
    protected List<WeaponDescription> description(ItemStack stack) {
        var sealed = Element.holdsWaxSeal(WishTooltips.viewer(), Element.CRYO);
        var refinement = WeaponState.of(stack).refinements();
        var descriptions = new ArrayList<WeaponDescription>();
        if (sealed) descriptions.add(WeaponDescription.note(NO_REFINEMENT_TEXT));
        descriptions.add(WeaponDescription.of(SNEAK_USE_BLOCK, SNOW_TEXT,
                WishReports.number(ElementWeapons.seconds(refinement, COOLDOWN, false, sealed), Formatting.AQUA)));
        return List.copyOf(descriptions);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        var result = super.useOnBlock(context);
        var player = context.getPlayer();
        if (!(context.getWorld() instanceof ServerWorld server) || player == null || !player.isSneaking()) return result;
        var pos = context.getBlockPos();
        var above = pos.up();
        if (server.getBlockState(above).isOf(Blocks.SNOW))
            server.setBlockState(above, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
        else if (server.getBlockState(above).isAir() && !server.getBlockState(pos).isOf(Blocks.SNOW))
            server.setBlockState(above, Blocks.SNOW.getDefaultState(), Block.NOTIFY_ALL);
        server.playSound(null, pos, SoundEvents.BLOCK_POWDER_SNOW_FALL, SoundCategory.BLOCKS, SOUND_VOLUME, SOUND_PITCH);
        var stack = context.getStack();
        player.getItemCooldownManager().set(stack.getItem(), ElementWeapons.ticks(player, stack, Element.CRYO, COOLDOWN, false));
        stack.damage(DURABILITY_LOSS, player, item -> {
        });
        player.swingHand(context.getHand());
        return ActionResult.SUCCESS;
    }
}
