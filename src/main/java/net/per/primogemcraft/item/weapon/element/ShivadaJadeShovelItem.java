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
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.system.weapon.WeaponDescription;
import net.per.primogemcraft.system.weapon.WeaponTier;
import net.per.primogemcraft.system.weapon.WishWeaponShovelItem;

import java.util.List;
import java.util.Set;

public class ShivadaJadeShovelItem extends WishWeaponShovelItem {
    private static final ToolMaterial TIER = new WeaponTier(1561, 8.0F, 12, WeaponTier.DIAMOND_INCORRECT,
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

    public ShivadaJadeShovelItem(Settings properties) {
        super(TIER, properties.fireproof(), ATTACK_DAMAGE, ATTACK_SPEED);
    }

    @Override
    protected List<WeaponDescription> description(ItemStack stack) {
        return List.of(
                WeaponDescription.of(RIGHT_CLICK_BLOCK, ICE_TEXT),
                WeaponDescription.note(NO_REFINEMENT_TEXT));
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        var result = super.useOnBlock(context);
        var player = context.getPlayer();
        if (!(context.getWorld() instanceof ServerWorld server) || player == null) return result;
        var pos = context.getBlockPos();
        if (!ICE_BLOCKS.contains(server.getBlockState(pos).getBlock())) return result;
        server.setBlockState(pos, Blocks.BLUE_ICE.getDefaultState(), Block.NOTIFY_ALL);
        server.playSound(null, pos, SoundEvents.ENTITY_TNT_PRIMED, SoundCategory.BLOCKS, SOUND_VOLUME, SOUND_PITCH);
        var stack = context.getStack();
        stack.damage(DURABILITY_LOSS, player, item -> {
        });
        player.getItemCooldownManager().set(stack.getItem(), COOLDOWN);
        player.swingHand(context.getHand());
        return ActionResult.SUCCESS;
    }
}
