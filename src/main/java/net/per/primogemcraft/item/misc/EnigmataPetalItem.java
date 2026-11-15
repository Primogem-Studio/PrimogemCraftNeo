package net.per.primogemcraft.item.misc;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.ActionResult;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.world.World;
import net.minecraft.block.BlockState;
import net.per.primogemcraft.item.tool.DescribedItem;
import net.per.primogemcraft.registry.PGCEntities;

public class EnigmataPetalItem extends DescribedItem {
    private static final int CAPACITY = 100;
    private static final int SPENT = 99;

    public EnigmataPetalItem(Settings properties) {
        super(properties.maxDamage(CAPACITY).fireproof());
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return stack.getDamage() == 0;
    }

    @Override
    public float getMiningSpeedMultiplier(ItemStack stack, BlockState state) {
        return 0.0F;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        var level = context.getWorld();
        var player = context.getPlayer();
        var stack = context.getStack();
        var pos = context.getBlockPos();
        if (player == null || !(level instanceof ServerWorld serverLevel)) return ActionResult.SUCCESS;
        if (stack.getDamage() != 0) return ActionResult.SUCCESS;
        if (!level.getBlockState(pos.up()).isAir()) return ActionResult.SUCCESS;
        var tower = PGCEntities.HERTA_OTHERWORLD_BRANCH_TOWER.get().spawn(serverLevel, pos.up(), SpawnReason.MOB_SUMMONED);
        if (tower == null) return ActionResult.SUCCESS;
        tower.setYaw(level.getRandom().nextFloat() * 360.0F);
        stack.setDamage(SPENT);
        player.swingHand(context.getHand());
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 0.3F, 0.9F + level.getRandom().nextFloat() * 0.3F);
        return ActionResult.SUCCESS;
    }

    public boolean recall(World level, PlayerEntity player, ItemStack stack) {
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.PLAYERS, 0.4F, 1.0F);
        if (stack.getDamage() <= 1) return false;
        stack.setDamage(0);
        return true;
    }
}
