package net.per.primogemcraft.item.misc;

import net.minecraft.text.Text;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.per.primogemcraft.item.tool.DescribedItem;
import net.per.primogemcraft.registry.PGCSounds;
import net.per.primogemcraft.system.weapon.Equilibrium;
import net.per.primogemcraft.system.weapon.WeaponAttributes;

public class EquilibriumTabletItem extends DescribedItem {
    private static final float VOLUME = 1.0F;
    private static final float PITCH = 0.9F;

    private final int tier;

    public EquilibriumTabletItem(int tier, Settings properties) {
        super(properties);
        this.tier = tier;
    }

    @Override
    public TypedActionResult<ItemStack> use(World level, PlayerEntity player, Hand hand) {
        var stack = player.getStackInHand(hand);
        if (!(level instanceof ServerWorld server)) return TypedActionResult.success(stack, true);
        var equilibrium = Equilibrium.of(player);
        if (player.isCreative() && player.isSneaking()) {
            equilibrium.reset();
            WeaponAttributes.refreshLevels(player);
            player.sendMessage(Text.translatable("message.primogemcraft.equilibrium.reset"), false);
            return TypedActionResult.success(stack, false);
        }
        if (!equilibrium.advanceTo(tier)) {
            player.sendMessage(Text.translatable("message.primogemcraft.equilibrium.already"), false);
            return TypedActionResult.success(stack, false);
        }
        WeaponAttributes.refreshLevels(player);
        stack.decrement(1);
        server.playSound(null, player.getX(), player.getY(), player.getZ(), PGCSounds.ENHANCEMENT_SUCCESS.get(), SoundCategory.PLAYERS, VOLUME, PITCH);
        return TypedActionResult.success(stack, false);
    }
}
