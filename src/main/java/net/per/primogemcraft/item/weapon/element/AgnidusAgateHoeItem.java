package net.per.primogemcraft.item.weapon.element;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.ActionResult;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterial;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.per.primogemcraft.registry.PGCBlocks;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.system.weapon.WeaponDescription;
import net.per.primogemcraft.system.weapon.WeaponTier;
import net.per.primogemcraft.system.weapon.WishWeaponHoeItem;

import java.util.List;

public class AgnidusAgateHoeItem extends WishWeaponHoeItem {
    private static final ToolMaterial TIER = new WeaponTier(1561, 8.0F, 10, WeaponTier.NETHERITE_INCORRECT,
            PGCItems.PRIMOGEM, PGCItems.AGNIDUS_AGATE_SLIVER);

    private static final float ATTACK_DAMAGE = 3.0F;
    private static final float ATTACK_SPEED = -3.0F;
    private static final int DURABILITY_LOSS = 1;
    private static final float SOUND_VOLUME = 1.0F;
    private static final float SOUND_PITCH = 0.9F;
    private static final String PASSIVE_ACTION = "passive";
    private static final String NETHERRACK_TEXT = "netherrack";
    private static final String NO_REFINEMENT_TEXT = "no_refinement";

    public AgnidusAgateHoeItem(Settings properties) {
        super(TIER, properties.fireproof(), ATTACK_DAMAGE, ATTACK_SPEED);
    }

    @Override
    protected List<WeaponDescription> description(ItemStack stack) {
        return List.of(
                WeaponDescription.of(PASSIVE_ACTION, NETHERRACK_TEXT),
                WeaponDescription.note(NO_REFINEMENT_TEXT));
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        var result = super.useOnBlock(context);
        var player = context.getPlayer();
        if (!(context.getWorld() instanceof ServerWorld server) || player == null) return result;
        var pos = context.getBlockPos();
        if (!server.getBlockState(pos).isOf(Blocks.NETHERRACK)) return result;
        var stack = context.getStack();
        server.setBlockState(pos, PGCBlocks.NETHERRACK_FARMLAND.get().getDefaultState(), Block.NOTIFY_ALL);
        server.playSound(null, pos, SoundEvents.BLOCK_NETHERRACK_HIT, SoundCategory.BLOCKS, SOUND_VOLUME, SOUND_PITCH);
        stack.damage(DURABILITY_LOSS, player, item -> {
        });
        player.swingHand(context.getHand());
        return ActionResult.SUCCESS;
    }
}
