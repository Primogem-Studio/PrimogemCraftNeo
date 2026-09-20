package net.per.primogemcraft.item.tool;

import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.per.primogemcraft.block.ZiplineBaseBlock;
import net.per.primogemcraft.entity.misc.ZiplineAnchorEntity;
import net.per.primogemcraft.entity.misc.ZiplineCarrierEntity;
import net.per.primogemcraft.registry.PGCBlocks;
import net.per.primogemcraft.registry.PGCEntities;
import net.per.primogemcraft.registry.PGCSounds;
import net.per.primogemcraft.system.zipline.ZiplineGrip;

import java.util.List;

public class ProtocolOriginiumSwordItem extends SwordItem {
    private static final int PLACEMENT_COOLDOWN = 200;
    private static final int TOOLTIP_LINES = 9;
    public ProtocolOriginiumSwordItem(Properties properties) {
        super(Tiers.NETHERITE, properties.attributes(createAttributes(Tiers.NETHERITE, 4, -2.8F)).fireResistant());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        var prefix = stack.getDescriptionId() + ".tooltip.";
        for (var index = 0; index < TOOLTIP_LINES; index++) tooltip.add(Component.translatable(prefix + index));
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        var result = super.hurtEnemy(stack, target, attacker);
        if (!target.level().isClientSide() && target.isAlive() && !target.isPassenger() && !target.isVehicle())
            ZiplineCarrierEntity.lift(target);
        return result;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        var player = context.getPlayer();
        var level = context.getLevel();
        if (player == null) return InteractionResult.PASS;
        if (player.getVehicle() instanceof ZiplineCarrierEntity) return InteractionResult.SUCCESS;
        if (level.getBlockState(context.getClickedPos()).is(PGCBlocks.ZIPLINE_BASE)) return InteractionResult.PASS;
        if (!player.isShiftKeyDown()) return InteractionResult.PASS;
        if (!player.isCreative() && player.getCooldowns().isOnCooldown(this)) return InteractionResult.FAIL;
        if (context.getClickedFace() != Direction.UP || !player.mayBuild()) return InteractionResult.FAIL;
        var center = context.getClickedPos().above();
        for (var x = -1; x <= 1; x++) {
            for (var z = -1; z <= 1; z++) {
                for (var y = 0; y < 7; y++) {
                    var position = center.offset(x, y, z);
                    if (!level.hasChunkAt(position) || !level.getWorldBorder().isWithinBounds(position)
                            || level.isOutsideBuildHeight(position) || !level.getBlockState(position).canBeReplaced()
                            || !level.mayInteract(player, position) || !player.mayUseItemAt(position, Direction.UP, context.getItemInHand())) {
                        if (!level.isClientSide()) player.displayClientMessage(Component.translatable("message.primogemcraft.zipline_no_space"), true);
                        return InteractionResult.FAIL;
                    }
                }
                var ground = center.offset(x, -1, z);
                if (!level.getBlockState(ground).isFaceSturdy(level, ground, Direction.UP)) return InteractionResult.FAIL;
            }
        }
        if (!level.isClientSide()) {
            var anchor = new ZiplineAnchorEntity(PGCEntities.ZIPLINE_ANCHOR.get(), level);
            anchor.setPos(center.getX() + 0.5, center.getY(), center.getZ() + 0.5);
            anchor.setYRot(ZiplineGrip.modelYaw(player.getDirection().toYRot()));
            for (var index = 0; index < 9; index++)
                level.setBlock(center.offset(index % 3 - 1, 0, index / 3 - 1),
                        PGCBlocks.ZIPLINE_BASE.get().defaultBlockState().setValue(ZiplineBaseBlock.PART, index), 3);
            if (!level.addFreshEntity(anchor)) {
                for (var index = 0; index < 9; index++)
                    level.removeBlock(center.offset(index % 3 - 1, 0, index / 3 - 1), false);
                return InteractionResult.FAIL;
            }
            level.playSound(null, center, PGCSounds.ZIPLINE_PLACE.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
            if (!player.isCreative()) player.getCooldowns().addCooldown(this, PLACEMENT_COOLDOWN);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }
}
