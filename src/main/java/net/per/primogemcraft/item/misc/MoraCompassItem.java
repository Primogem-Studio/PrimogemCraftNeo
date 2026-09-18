package net.per.primogemcraft.item.misc;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.per.primogemcraft.item.tool.DescribedItem;
import net.per.primogemcraft.registry.PGCDataComponents;

public class MoraCompassItem extends DescribedItem {
    private static final int COOLDOWN_TICKS = 20;
    private static final int BOTTOM_Y = -64;

    private final int radius;
    private final TagKey<Block> targets;

    public MoraCompassItem(Properties properties, int radius, TagKey<Block> targets) {
        super(properties);
        this.radius = radius;
        this.targets = targets;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResultHolder.sidedSuccess(stack, true);
        if (player.isShiftKeyDown()) record(serverPlayer, stack);
        else search(serverPlayer, stack);
        return InteractionResultHolder.sidedSuccess(stack, false);
    }

    private void record(ServerPlayer player, ItemStack stack) {
        var below = player.blockPosition().below();
        var state = player.level().getBlockState(below);
        if (targets != null && !state.is(targets)) {
            player.sendSystemMessage(Component.translatable("message.primogemcraft.compass.restricted"));
            return;
        }
        stack.set(PGCDataComponents.COMPASS_TARGET.get(), BuiltInRegistries.ITEM.getKey(state.getBlock().asItem()));
        player.displayClientMessage(Component.translatable("message.primogemcraft.compass.recorded", state.getBlock().getName()), true);
    }

    private void search(ServerPlayer player, ItemStack stack) {
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
        var target = stack.get(PGCDataComponents.COMPASS_TARGET.get());
        if (target == null) {
            player.sendSystemMessage(Component.translatable("message.primogemcraft.compass.no_target"));
            return;
        }
        var wanted = BuiltInRegistries.BLOCK.get(target);
        player.displayClientMessage(Component.translatable("message.primogemcraft.compass.searching"), true);
        var level = player.level();
        var origin = player.blockPosition();
        for (var offsetX = -radius; offsetX <= radius; offsetX++) {
            for (var offsetZ = -radius; offsetZ <= radius; offsetZ++) {
                for (var y = origin.getY(); y >= BOTTOM_Y; y--) {
                    var pos = new BlockPos(origin.getX() + offsetX, y, origin.getZ() + offsetZ);
                    var state = level.getBlockState(pos);
                    if (state.getBlock() != wanted) continue;
                    player.sendSystemMessage(Component.translatable("message.primogemcraft.compass.found",
                            pos.getX(), pos.getY(), pos.getZ(), state.getBlock().getName()));
                }
            }
        }
    }
}
