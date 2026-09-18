package net.per.primogemcraft.item.misc;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.per.primogemcraft.item.tool.DescribedItem;
import net.per.primogemcraft.registry.PGCDataComponents;
import net.per.primogemcraft.system.weapon.WeaponEnhancement;
import net.per.primogemcraft.util.PlayerItems;

import java.util.List;

public class TreatedFineOreItem extends DescribedItem {
    private static final String EMPTY_SUFFIX = ".empty";
    private static final String REFUND_SUFFIX = ".refund";
    private static final float SOUND_VOLUME = 1.0F;
    private static final float SOUND_PITCH = 1.0F;

    public TreatedFineOreItem(Properties properties) {
        super(properties.stacksTo(1).fireResistant());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        var recovery = stack.get(PGCDataComponents.WEAPON_RECOVERY.get());
        if (recovery == null) return InteractionResultHolder.pass(stack);
        if (player instanceof ServerPlayer server) {
            for (var reward : WeaponEnhancement.refund(recovery)) PlayerItems.give(server, reward);
            stack.shrink(1);
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ANVIL_DESTROY, SoundSource.PLAYERS, SOUND_VOLUME, SOUND_PITCH);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        var prefix = stack.getDescriptionId();
        var recovery = stack.get(PGCDataComponents.WEAPON_RECOVERY.get());
        if (recovery == null) {
            tooltip.add(Component.translatable(prefix + EMPTY_SUFFIX));
            return;
        }
        for (var reward : WeaponEnhancement.refund(recovery))
            tooltip.add(Component.translatable(prefix + REFUND_SUFFIX, reward.getCount(), reward.getHoverName()));
    }
}
