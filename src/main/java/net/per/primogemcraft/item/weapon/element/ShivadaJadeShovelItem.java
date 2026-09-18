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
import java.util.Set;

public class ShivadaJadeShovelItem extends WishWeaponShovelItem {
    private static final Tier TIER = new WeaponTier(1561, 8.0F, 12, WeaponTier.DIAMOND_INCORRECT,
            PGCItems.PRIMOGEM, PGCItems.SHIVADA_JADE_SLIVER);

    private static final Set<Block> ICE_BLOCKS = Set.of(Blocks.ICE, Blocks.PACKED_ICE, Blocks.FROSTED_ICE);
    private static final float ATTACK_DAMAGE = 3.0F;
    private static final float ATTACK_SPEED = -3.0F;
    private static final int COOLDOWN = 5;
    private static final int DURABILITY_LOSS = 1;
    private static final float SOUND_VOLUME = 1.0F;
    private static final float SOUND_PITCH = 30.0F;
    private static final String RIGHT_CLICK_BLOCK = "right_click_block";
    private static final String ICE_TEXT = "ice";
    private static final String NO_REFINEMENT_TEXT = "no_refinement";

    public ShivadaJadeShovelItem(Properties properties) {
        super(TIER, properties.fireResistant(), ATTACK_DAMAGE, ATTACK_SPEED);
    }

    @Override
    protected List<WeaponDescription> description(ItemStack stack) {
        return List.of(
                WeaponDescription.of(RIGHT_CLICK_BLOCK, ICE_TEXT),
                WeaponDescription.note(NO_REFINEMENT_TEXT));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        var result = super.useOn(context);
        var player = context.getPlayer();
        if (!(context.getLevel() instanceof ServerLevel server) || player == null) return result;
        var pos = context.getClickedPos();
        if (!ICE_BLOCKS.contains(server.getBlockState(pos).getBlock())) return result;
        server.setBlock(pos, Blocks.BLUE_ICE.defaultBlockState(), Block.UPDATE_ALL);
        server.playSound(null, pos, SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, SOUND_VOLUME, SOUND_PITCH);
        var stack = context.getItemInHand();
        stack.hurtAndBreak(DURABILITY_LOSS, server, player, item -> {
        });
        player.getCooldowns().addCooldown(stack.getItem(), COOLDOWN);
        player.swing(context.getHand(), true);
        return InteractionResult.SUCCESS;
    }
}
