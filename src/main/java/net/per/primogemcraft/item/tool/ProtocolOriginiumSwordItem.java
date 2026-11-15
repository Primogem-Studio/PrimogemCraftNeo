package net.per.primogemcraft.item.tool;

import net.minecraft.world.World;
import net.minecraft.util.math.Direction;
import net.minecraft.text.Text;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.ActionResult;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterials;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemUsageContext;
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
    public ProtocolOriginiumSwordItem(Settings properties) {
        super(ToolMaterials.NETHERITE, 4, -2.8F, properties.fireproof());
    }

    @Override
    public void appendTooltip(ItemStack stack, @org.jetbrains.annotations.Nullable World level, List<Text> tooltip, TooltipContext flag) {
        var prefix = stack.getTranslationKey() + ".tooltip.";
        for (var index = 0; index < TOOLTIP_LINES; index++) tooltip.add(Text.translatable(prefix + index));
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        var result = super.postHit(stack, target, attacker);
        if (!target.getWorld().isClient() && target.isAlive() && !target.hasVehicle() && !target.hasPassengers())
            ZiplineCarrierEntity.lift(target);
        return result;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        var player = context.getPlayer();
        var level = context.getWorld();
        if (player == null) return ActionResult.PASS;
        if (player.getVehicle() instanceof ZiplineCarrierEntity) return ActionResult.SUCCESS;
        if (level.getBlockState(context.getBlockPos()).isOf(PGCBlocks.ZIPLINE_BASE.get())) return ActionResult.PASS;
        if (!player.isSneaking()) return ActionResult.PASS;
        if (!player.isCreative() && player.getItemCooldownManager().isCoolingDown(this)) return ActionResult.FAIL;
        if (context.getSide() != Direction.UP || !player.canModifyBlocks()) return ActionResult.FAIL;
        var center = context.getBlockPos().up();
        for (var x = -1; x <= 1; x++) {
            for (var z = -1; z <= 1; z++) {
                for (var y = 0; y < 7; y++) {
                    var position = center.add(x, y, z);
                    if (!level.isChunkLoaded(position) || !level.getWorldBorder().contains(position)
                            || level.isOutOfHeightLimit(position) || !level.getBlockState(position).isReplaceable()
                            || !level.canPlayerModifyAt(player, position) || !player.canPlaceOn(position, Direction.UP, context.getStack())) {
                        if (!level.isClient()) player.sendMessage(Text.translatable("message.primogemcraft.zipline_no_space"), true);
                        return ActionResult.FAIL;
                    }
                }
                var ground = center.add(x, -1, z);
                if (!level.getBlockState(ground).isSideSolidFullSquare(level, ground, Direction.UP)) return ActionResult.FAIL;
            }
        }
        if (!level.isClient()) {
            var anchor = new ZiplineAnchorEntity(PGCEntities.ZIPLINE_ANCHOR.get(), level);
            anchor.setPos(center.getX() + 0.5, center.getY(), center.getZ() + 0.5);
            anchor.setYaw(ZiplineGrip.modelYaw(player.getMovementDirection().asRotation()));
            for (var index = 0; index < 9; index++)
                level.setBlockState(center.add(index % 3 - 1, 0, index / 3 - 1),
                        PGCBlocks.ZIPLINE_BASE.get().getDefaultState().with(ZiplineBaseBlock.PART, index), 3);
            if (!level.spawnEntity(anchor)) {
                for (var index = 0; index < 9; index++)
                    level.removeBlock(center.add(index % 3 - 1, 0, index / 3 - 1), false);
                return ActionResult.FAIL;
            }
            level.playSound(null, center, PGCSounds.ZIPLINE_PLACE.get(), SoundCategory.BLOCKS, 1.0F, 1.0F);
            if (!player.isCreative()) player.getItemCooldownManager().set(this, PLACEMENT_COOLDOWN);
        }
        return ActionResult.success(level.isClient());
    }
}
