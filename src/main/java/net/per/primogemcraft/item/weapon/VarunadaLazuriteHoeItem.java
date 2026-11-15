package net.per.primogemcraft.item.weapon;

import net.minecraft.util.math.BlockPos;
import net.minecraft.sound.SoundEvents;
import net.minecraft.sound.SoundCategory;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.ActionResult;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterial;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.block.Blocks;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.system.weapon.WeaponDescription;
import net.per.primogemcraft.system.weapon.WeaponTier;
import net.per.primogemcraft.system.weapon.WishWeaponToolItem;

import java.util.List;
import net.minecraft.util.Hand;


public class VarunadaLazuriteHoeItem extends WishWeaponToolItem {
    private static final ToolMaterial TIER = new WeaponTier(1561, 10.0F, 15, WeaponTier.DIAMOND_INCORRECT,
            PGCItems.PRIMOGEM, PGCItems.VARUNADA_LAZURITE_SLIVER);

    private static final float ATTACK_DAMAGE = 9.0F;
    private static final float ATTACK_SPEED = -2.8F;
    private static final float TILL_VOLUME = 1.0F;
    private static final String PASSIVE_ACTION = "passive";
    private static final String TILL_TEXT = "till";
    private static final String NOTE_TEXT = "note";

    public VarunadaLazuriteHoeItem(Settings properties) {
        super(TIER, properties.fireproof(), BlockTags.HOE_MINEABLE, ATTACK_DAMAGE, ATTACK_SPEED);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        var result = super.useOnBlock(context);
        var level = context.getWorld();
        if (level.isClient()) return result;
        var pos = context.getBlockPos();
        var state = level.getBlockState(pos);
        if (!state.isIn(BlockTags.DIRT) || !level.getBlockState(pos.up()).isAir()) return result;
        var replacement = state.isOf(Blocks.COARSE_DIRT) ? Blocks.DIRT.getDefaultState() : Blocks.FARMLAND.getDefaultState();
        level.setBlockState(pos, replacement, 3);
        level.playSound(null, BlockPos.ofFloored(context.getHitPos()), SoundEvents.ITEM_HOE_TILL, SoundCategory.NEUTRAL, TILL_VOLUME, 1.0F);
        var stack = context.getStack();
        var player = context.getPlayer();
        if (player != null) stack.damage(1, player, p -> p.sendEquipmentBreakStatus(
                p.getActiveHand() == net.minecraft.util.Hand.MAIN_HAND
                        ? net.minecraft.entity.EquipmentSlot.MAINHAND : net.minecraft.entity.EquipmentSlot.OFFHAND));
        return ActionResult.SUCCESS;
    }

    @Override
    protected List<WeaponDescription> description(ItemStack stack) {
        return List.of(
                WeaponDescription.of(PASSIVE_ACTION, TILL_TEXT),
                WeaponDescription.note(NOTE_TEXT));
    }
}
