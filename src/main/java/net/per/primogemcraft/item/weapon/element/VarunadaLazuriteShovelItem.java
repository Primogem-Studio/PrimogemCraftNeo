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
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.system.weapon.WeaponDescription;
import net.per.primogemcraft.system.weapon.WeaponTier;
import net.per.primogemcraft.system.weapon.WishWeaponShovelItem;

import java.util.List;

public class VarunadaLazuriteShovelItem extends WishWeaponShovelItem {
    private static final Tier TIER = new WeaponTier(1561, 8.0F, 15, WeaponTier.DIAMOND_INCORRECT,
            PGCItems.PRIMOGEM, PGCItems.VARUNADA_LAZURITE_SLIVER);

    private static final float ATTACK_DAMAGE = 5.0F;
    private static final float ATTACK_SPEED = -3.0F;
    private static final int DURABILITY_LOSS = 10;
    private static final double FAILURE_CHANCE = 0.25D;
    private static final float SOUND_VOLUME = 1.0F;
    private static final float SOUND_PITCH = 1.0F;
    private static final String SNEAK_USE_BLOCK = "sneak_use_block";
    private static final String CLAY_TEXT = "clay";
    private static final String SAND_TEXT = "sand";
    private static final String FAILURE_TEXT = "failure";
    private static final String NO_REFINEMENT_TEXT = "no_refinement";

    public VarunadaLazuriteShovelItem(Properties properties) {
        super(TIER, properties.fireResistant(), ATTACK_DAMAGE, ATTACK_SPEED);
    }

    @Override
    protected List<WeaponDescription> description(ItemStack stack) {
        return List.of(
                WeaponDescription.of(SNEAK_USE_BLOCK, CLAY_TEXT),
                WeaponDescription.note(SAND_TEXT),
                WeaponDescription.note(FAILURE_TEXT),
                WeaponDescription.note(NO_REFINEMENT_TEXT));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        var result = super.useOn(context);
        var player = context.getPlayer();
        if (!(context.getLevel() instanceof ServerLevel server) || player == null || !player.isShiftKeyDown()) return result;
        var pos = context.getClickedPos();
        var block = server.getBlockState(pos).getBlock();
        if (block != Blocks.DIRT && block != Blocks.DIRT_PATH && block != Blocks.CLAY) return result;
        var stack = context.getItemInHand();
        server.setBlock(pos, (block == Blocks.CLAY ? Blocks.SAND : Blocks.CLAY).defaultBlockState(), Block.UPDATE_ALL);
        server.playSound(null, pos, SoundEvents.HOE_TILL, SoundSource.BLOCKS, SOUND_VOLUME, SOUND_PITCH);
        stack.hurtAndBreak(DURABILITY_LOSS, server, player, item -> {
        });
        if (server.getRandom().nextDouble() < FAILURE_CHANCE)
            server.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        player.swing(context.getHand(), true);
        return InteractionResult.SUCCESS;
    }
}
