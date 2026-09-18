package net.per.primogemcraft.item.curio;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
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

    public EmptyCandleFlameItem(Properties properties) {
        super(CurioTrigger.RIGHT_CLICK, CurioForm.FUSION, properties);
    }

    @Override
    public boolean repairsCurios() {
        return true;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return USE_DURATION;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!(entity instanceof ServerPlayer player)) return stack;
        var repaired = Curios.repairRandom(player, FULL_REPAIR);
        if (repaired.isEmpty()) {
            player.displayClientMessage(Component.translatable(INVALID_KEY), false);
            return stack;
        }
        Curios.give(player, repaired.copy());
        if (player.getRandom().nextFloat() >= BREAK_CHANCE) return stack;
        player.displayClientMessage(Component.translatable(BROKEN_KEY, stack.getHoverName()), false);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), PGCSounds.CURIO_BROKEN.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        stack.shrink(1);
        return stack;
    }
}
