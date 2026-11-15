package net.per.primogemcraft.item.curio;

import net.minecraft.text.Text;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;
import net.per.primogemcraft.registry.PGCSounds;
import net.per.primogemcraft.system.curio.CurioForm;
import net.per.primogemcraft.system.curio.CurioItem;
import net.per.primogemcraft.system.curio.CurioTrigger;
import net.per.primogemcraft.system.curio.Curios;

public class EmptyCandleFlameItem extends CurioItem {
    private static final String INVALID_KEY = "message.primogemcraft.curio.empty_candle_flame.invalid";
    private static final String BROKEN_KEY = "message.primogemcraft.curio.broken";
    private static final int USE_DURATION = 32;
    private static final float FULL_REPAIR = 1.0F;
    private static final float BREAK_CHANCE = 0.5F;

    public EmptyCandleFlameItem(Settings properties) {
        super(CurioTrigger.RIGHT_CLICK, CurioForm.FUSION, properties);
    }

    @Override
    public boolean repairsCurios() {
        return true;
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return net.minecraft.util.UseAction.DRINK;
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        return USE_DURATION;
    }

    @Override
    public TypedActionResult<ItemStack> use(World level, PlayerEntity player, Hand hand) {
        player.setCurrentHand(hand);
        return TypedActionResult.consume(player.getStackInHand(hand));
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World level, LivingEntity entity) {
        if (!(entity instanceof ServerPlayerEntity player)) return stack;
        var repaired = Curios.repairRandom(player, FULL_REPAIR);
        if (repaired.isEmpty()) {
            player.sendMessage(Text.translatable(INVALID_KEY), false);
            return stack;
        }
        Curios.give(player, repaired.copy());
        if (player.getRandom().nextFloat() >= BREAK_CHANCE) return stack;
        player.sendMessage(Text.translatable(BROKEN_KEY, stack.getName()), false);
        player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), PGCSounds.CURIO_BROKEN.get(), SoundCategory.PLAYERS, 1.0F, 1.0F);
        stack.decrement(1);
        return stack;
    }
}
