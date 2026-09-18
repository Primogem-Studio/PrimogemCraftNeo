package net.per.primogemcraft.item.weapon.element;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.per.primogemcraft.registry.PGCBlocks;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.system.weapon.WeaponDescription;
import net.per.primogemcraft.system.weapon.WeaponTier;
import net.per.primogemcraft.system.weapon.WishWeaponHoeItem;

import java.util.List;

public class AgnidusAgateHoeItem extends WishWeaponHoeItem {
    private static final Tier TIER = new WeaponTier(1561, 8.0F, 10, WeaponTier.NETHERITE_INCORRECT,
            PGCItems.PRIMOGEM, PGCItems.AGNIDUS_AGATE_SLIVER);

    private static final float ATTACK_DAMAGE = 3.0F;
    private static final float ATTACK_SPEED = -3.0F;
    private static final int DURABILITY_LOSS = 1;
    private static final float SOUND_VOLUME = 1.0F;
    private static final float SOUND_PITCH = 0.9F;
    private static final String PASSIVE_ACTION = "passive";
    private static final String NETHERRACK_TEXT = "netherrack";
    private static final String NO_REFINEMENT_TEXT = "no_refinement";

    public AgnidusAgateHoeItem(Properties properties) {
        super(TIER, properties.fireResistant(), ATTACK_DAMAGE, ATTACK_SPEED);
    }

    @Override
    protected List<WeaponDescription> description(ItemStack stack) {
        return List.of(
                WeaponDescription.of(PASSIVE_ACTION, NETHERRACK_TEXT),
                WeaponDescription.note(NO_REFINEMENT_TEXT));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        var result = super.useOn(context);
        var player = context.getPlayer();
        if (!(context.getLevel() instanceof ServerLevel server) || player == null) return result;
        var pos = context.getClickedPos();
        if (!server.getBlockState(pos).is(Blocks.NETHERRACK)) return result;
        var stack = context.getItemInHand();
        server.setBlock(pos, PGCBlocks.NETHERRACK_FARMLAND.get().defaultBlockState(), Block.UPDATE_ALL);
        server.playSound(null, pos, SoundEvents.NETHERRACK_HIT, SoundSource.BLOCKS, SOUND_VOLUME, SOUND_PITCH);
        stack.hurtAndBreak(DURABILITY_LOSS, server, player, item -> {
        });
        player.swing(context.getHand(), true);
        return InteractionResult.SUCCESS;
    }
}
