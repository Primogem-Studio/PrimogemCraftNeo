package net.per.primogemcraft.item.misc;

import net.per.primogemcraft.compat.component.StackComponents;
import net.minecraft.text.Text;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.world.World;
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

    public TreatedFineOreItem(Settings properties) {
        super(properties.maxCount(1).fireproof());
    }

    @Override
    public TypedActionResult<ItemStack> use(World level, PlayerEntity player, Hand hand) {
        var stack = player.getStackInHand(hand);
        var recovery = StackComponents.get(stack, PGCDataComponents.WEAPON_RECOVERY);
        if (recovery == null) return TypedActionResult.pass(stack);
        if (player instanceof ServerPlayerEntity server) {
            for (var reward : WeaponEnhancement.refund(recovery)) PlayerItems.give(server, reward);
            stack.decrement(1);
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BLOCK_ANVIL_DESTROY, SoundCategory.PLAYERS, SOUND_VOLUME, SOUND_PITCH);
        }
        return TypedActionResult.success(stack, level.isClient());
    }

    @Override
    public void appendTooltip(ItemStack stack, @org.jetbrains.annotations.Nullable World level, List<Text> tooltip, TooltipContext flag) {
        super.appendTooltip(stack, level, tooltip, flag);
        var prefix = stack.getTranslationKey();
        var recovery = StackComponents.get(stack, PGCDataComponents.WEAPON_RECOVERY);
        if (recovery == null) {
            tooltip.add(Text.translatable(prefix + EMPTY_SUFFIX));
            return;
        }
        for (var reward : WeaponEnhancement.refund(recovery))
            tooltip.add(Text.translatable(prefix + REFUND_SUFFIX, reward.getCount(), reward.getName()));
    }
}
