package net.per.primogemcraft.item.misc;

import net.per.primogemcraft.compat.component.StackComponents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.block.Block;
import net.per.primogemcraft.item.tool.DescribedItem;
import net.per.primogemcraft.registry.PGCDataComponents;

public class MoraCompassItem extends DescribedItem {
    private static final int COOLDOWN_TICKS = 20;
    private static final int BOTTOM_Y = -64;

    private final int radius;
    private final TagKey<Block> targets;

    public MoraCompassItem(Settings properties, int radius, TagKey<Block> targets) {
        super(properties);
        this.radius = radius;
        this.targets = targets;
    }

    @Override
    public TypedActionResult<ItemStack> use(World level, PlayerEntity player, Hand hand) {
        var stack = player.getStackInHand(hand);
        if (!(player instanceof ServerPlayerEntity serverPlayer)) return TypedActionResult.success(stack, true);
        if (player.isSneaking()) record(serverPlayer, stack);
        else search(serverPlayer, stack);
        return TypedActionResult.success(stack, false);
    }

    private void record(ServerPlayerEntity player, ItemStack stack) {
        var below = player.getBlockPos().down();
        var state = player.getWorld().getBlockState(below);
        if (targets != null && !state.isIn(targets)) {
            player.sendMessage(Text.translatable("message.primogemcraft.compass.restricted"));
            return;
        }
        StackComponents.set(stack, PGCDataComponents.COMPASS_TARGET, Registries.ITEM.getId(state.getBlock().asItem()));
        player.sendMessage(Text.translatable("message.primogemcraft.compass.recorded", state.getBlock().getName()), true);
    }

    private void search(ServerPlayerEntity player, ItemStack stack) {
        player.getItemCooldownManager().set(this, COOLDOWN_TICKS);
        var target = StackComponents.get(stack, PGCDataComponents.COMPASS_TARGET);
        if (target == null) {
            player.sendMessage(Text.translatable("message.primogemcraft.compass.no_target"));
            return;
        }
        var wanted = Registries.BLOCK.get(target);
        player.sendMessage(Text.translatable("message.primogemcraft.compass.searching"), true);
        var level = player.getWorld();
        var origin = player.getBlockPos();
        for (var offsetX = -radius; offsetX <= radius; offsetX++) {
            for (var offsetZ = -radius; offsetZ <= radius; offsetZ++) {
                for (var y = origin.getY(); y >= BOTTOM_Y; y--) {
                    var pos = new BlockPos(origin.getX() + offsetX, y, origin.getZ() + offsetZ);
                    var state = level.getBlockState(pos);
                    if (state.getBlock() != wanted) continue;
                    player.sendMessage(Text.translatable("message.primogemcraft.compass.found",
                            pos.getX(), pos.getY(), pos.getZ(), state.getBlock().getName()));
                }
            }
        }
    }
}
