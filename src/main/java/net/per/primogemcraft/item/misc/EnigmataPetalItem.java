package net.per.primogemcraft.item.misc;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.per.primogemcraft.item.tool.DescribedItem;
import net.per.primogemcraft.registry.PGCEntities;

public class EnigmataPetalItem extends DescribedItem {
    private static final int CAPACITY = 100;
    private static final int SPENT = 99;

    public EnigmataPetalItem(Properties properties) {
        super(properties.durability(CAPACITY).fireResistant());
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return stack.getDamageValue() == 0;
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        return 0.0F;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        var level = context.getLevel();
        var player = context.getPlayer();
        var stack = context.getItemInHand();
        var pos = context.getClickedPos();
        if (player == null || !(level instanceof ServerLevel serverLevel)) return InteractionResult.SUCCESS;
        if (stack.getDamageValue() != 0) return InteractionResult.SUCCESS;
        if (!level.getBlockState(pos.above()).isAir()) return InteractionResult.SUCCESS;
        var tower = PGCEntities.HERTA_OTHERWORLD_BRANCH_TOWER.get().spawn(serverLevel, pos.above(), MobSpawnType.MOB_SUMMONED);
        if (tower == null) return InteractionResult.SUCCESS;
        tower.setYRot(level.getRandom().nextFloat() * 360.0F);
        stack.setDamageValue(SPENT);
        player.swing(context.getHand(), true);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.3F, 0.9F + level.getRandom().nextFloat() * 0.3F);
        return InteractionResult.SUCCESS;
    }

    public boolean recall(Level level, Player player, ItemStack stack) {
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.4F, 1.0F);
        if (stack.getDamageValue() <= 1) return false;
        stack.setDamageValue(0);
        return true;
    }
}
