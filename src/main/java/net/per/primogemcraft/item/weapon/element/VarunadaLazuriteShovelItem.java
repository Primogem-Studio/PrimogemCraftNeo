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

public class VarunadaLazuriteShovelItem extends WishWeaponShovelItem {
    private static final ToolMaterial TIER = new WeaponTier(1561, 8.0F, 15, WeaponTier.DIAMOND_INCORRECT,
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

    public VarunadaLazuriteShovelItem(Settings properties) {
        super(TIER, properties.fireproof(), ATTACK_DAMAGE, ATTACK_SPEED);
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
    public ActionResult useOnBlock(ItemUsageContext context) {
        var result = super.useOnBlock(context);
        var player = context.getPlayer();
        if (!(context.getWorld() instanceof ServerWorld server) || player == null || !player.isSneaking()) return result;
        var pos = context.getBlockPos();
        var block = server.getBlockState(pos).getBlock();
        if (block != Blocks.DIRT && block != Blocks.DIRT_PATH && block != Blocks.CLAY) return result;
        var stack = context.getStack();
        server.setBlockState(pos, (block == Blocks.CLAY ? Blocks.SAND : Blocks.CLAY).getDefaultState(), Block.NOTIFY_ALL);
        server.playSound(null, pos, SoundEvents.ITEM_HOE_TILL, SoundCategory.BLOCKS, SOUND_VOLUME, SOUND_PITCH);
        stack.damage(DURABILITY_LOSS, player, item -> {
        });
        if (server.getRandom().nextDouble() < FAILURE_CHANCE)
            server.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
        player.swingHand(context.getHand());
        return ActionResult.SUCCESS;
    }
}
