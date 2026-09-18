package net.per.primogemcraft.item.weapon;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.system.weapon.WeaponDescription;
import net.per.primogemcraft.system.weapon.WeaponTier;
import net.per.primogemcraft.system.weapon.WishWeaponToolItem;

import java.util.List;


public class VarunadaLazuriteHoeItem extends WishWeaponToolItem {
    private static final Tier TIER = new WeaponTier(1561, 10.0F, 15, WeaponTier.DIAMOND_INCORRECT,
            PGCItems.PRIMOGEM, PGCItems.VARUNADA_LAZURITE_SLIVER);

    private static final float ATTACK_DAMAGE = 9.0F;
    private static final float ATTACK_SPEED = -2.8F;
    private static final float TILL_VOLUME = 1.0F;
    private static final String PASSIVE_ACTION = "passive";
    private static final String TILL_TEXT = "till";
    private static final String NOTE_TEXT = "note";

    public VarunadaLazuriteHoeItem(Properties properties) {
        super(TIER, properties.fireResistant(), BlockTags.MINEABLE_WITH_HOE, ATTACK_DAMAGE, ATTACK_SPEED);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        var result = super.useOn(context);
        var level = context.getLevel();
        if (level.isClientSide()) return result;
        var pos = context.getClickedPos();
        var state = level.getBlockState(pos);
        if (!state.is(BlockTags.DIRT) || !level.getBlockState(pos.above()).isAir()) return result;
        var replacement = state.is(Blocks.COARSE_DIRT) ? Blocks.DIRT.defaultBlockState() : Blocks.FARMLAND.defaultBlockState();
        level.setBlock(pos, replacement, 3);
        level.playSound(null, BlockPos.containing(context.getClickLocation()), SoundEvents.HOE_TILL, SoundSource.NEUTRAL, TILL_VOLUME, 1.0F);
        var stack = context.getItemInHand();
        var player = context.getPlayer();
        if (player != null) stack.hurtAndBreak(1, player, player.getUsedItemHand() == net.minecraft.world.InteractionHand.MAIN_HAND
                ? net.minecraft.world.entity.EquipmentSlot.MAINHAND : net.minecraft.world.entity.EquipmentSlot.OFFHAND);
        return InteractionResult.SUCCESS;
    }

    @Override
    protected List<WeaponDescription> description(ItemStack stack) {
        return List.of(
                WeaponDescription.of(PASSIVE_ACTION, TILL_TEXT),
                WeaponDescription.note(NOTE_TEXT));
    }
}
