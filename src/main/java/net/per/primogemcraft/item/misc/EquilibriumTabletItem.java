package net.per.primogemcraft.item.misc;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.per.primogemcraft.item.tool.DescribedItem;
import net.per.primogemcraft.registry.PGCSounds;
import net.per.primogemcraft.system.weapon.Equilibrium;
import net.per.primogemcraft.system.weapon.WeaponAttributes;

public class EquilibriumTabletItem extends DescribedItem {
    private static final float VOLUME = 1.0F;
    private static final float PITCH = 0.9F;

    private final int tier;

    public EquilibriumTabletItem(int tier, Properties properties) {
        super(properties);
        this.tier = tier;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        if (!(level instanceof ServerLevel server)) return InteractionResultHolder.sidedSuccess(stack, true);
        var equilibrium = Equilibrium.of(player);
        if (player.isCreative() && player.isShiftKeyDown()) {
            equilibrium.reset();
            WeaponAttributes.refreshLevels(player);
            player.displayClientMessage(Component.translatable("message.primogemcraft.equilibrium.reset"), false);
            return InteractionResultHolder.sidedSuccess(stack, false);
        }
        if (!equilibrium.advanceTo(tier)) {
            player.displayClientMessage(Component.translatable("message.primogemcraft.equilibrium.already"), false);
            return InteractionResultHolder.sidedSuccess(stack, false);
        }
        WeaponAttributes.refreshLevels(player);
        stack.shrink(1);
        server.playSound(null, player.getX(), player.getY(), player.getZ(), PGCSounds.ENHANCEMENT_SUCCESS.get(), SoundSource.PLAYERS, VOLUME, PITCH);
        return InteractionResultHolder.sidedSuccess(stack, false);
    }
}
