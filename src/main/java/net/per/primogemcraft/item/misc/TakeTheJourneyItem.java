package net.per.primogemcraft.item.misc;

import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Hand;
import net.minecraft.util.ActionResult;
import net.minecraft.util.TypedActionResult;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.Rarity;
import net.minecraft.world.World;
import net.per.primogemcraft.compat.item.DiscItem;
import net.per.primogemcraft.registry.PGCSounds;

/**
 * 唱片「踏上旅途」：右键/对方块右键可直接播放，带冷却。
 */
public class TakeTheJourneyItem extends DiscItem {
    private static final int COMPARATOR_OUTPUT = 5;
    private static final int LENGTH_SECONDS = 150;
    private static final int COOLDOWN_TICKS = 1280;
    private static final float VOLUME = 0.4F;
    private static final float PITCH = 1.0F;

    public TakeTheJourneyItem(Settings settings) {
        super(COMPARATOR_OUTPUT, PGCSounds.TAKE_THE_JOURNEY::get,
                settings.maxCount(1).rarity(Rarity.RARE), LENGTH_SECONDS);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        var stack = player.getStackInHand(hand);
        play(world, player);
        return TypedActionResult.success(stack, world.isClient());
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        var result = super.useOnBlock(context);
        if (result.isAccepted()) return result;
        var player = context.getPlayer();
        if (player != null) play(context.getWorld(), player);
        return ActionResult.SUCCESS;
    }

    private void play(World world, PlayerEntity player) {
        if (world.isClient()) return;
        world.playSound(null, player.getX(), player.getY(), player.getZ(), PGCSounds.TAKE_THE_JOURNEY.get(), SoundCategory.PLAYERS, VOLUME, PITCH);
        player.getItemCooldownManager().set(this, COOLDOWN_TICKS);
    }
}
