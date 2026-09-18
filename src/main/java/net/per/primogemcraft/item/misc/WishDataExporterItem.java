package net.per.primogemcraft.item.misc;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.per.primogemcraft.registry.PGCAttachments;
import net.per.primogemcraft.system.wish.WishReports;

import java.util.List;

public class WishDataExporterItem extends Item {
    public WishDataExporterItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        if (player instanceof ServerPlayer sp) {
            for (var line : WishReports.record(sp, sp.getData(PGCAttachments.WISH_PITY.get()))) {
                sp.displayClientMessage(line, false);
            }
        }
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.primogemcraft.wish_data_exporter.tooltip.0"));
    }
}
