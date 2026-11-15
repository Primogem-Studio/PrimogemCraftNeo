package net.per.primogemcraft.item.misc;

import net.per.primogemcraft.compat.attachment.Attachments;
import net.minecraft.text.Text;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.world.World;
import net.per.primogemcraft.registry.PGCAttachments;
import net.per.primogemcraft.system.wish.WishReports;

import java.util.List;

public class WishDataExporterItem extends Item {
    public WishDataExporterItem(Settings properties) {
        super(properties);
    }

    @Override
    public TypedActionResult<ItemStack> use(World level, PlayerEntity player, Hand hand) {
        var stack = player.getStackInHand(hand);
        if (player instanceof ServerPlayerEntity sp) {
            for (var line : WishReports.record(sp, Attachments.get(sp, PGCAttachments.WISH_PITY.get()))) {
                sp.sendMessage(line, false);
            }
        }
        return TypedActionResult.consume(stack);
    }

    @Override
    public void appendTooltip(ItemStack stack, @org.jetbrains.annotations.Nullable World level, List<Text> tooltip, TooltipContext flag) {
        tooltip.add(Text.translatable("item.primogemcraft.wish_data_exporter.tooltip.0"));
    }
}
