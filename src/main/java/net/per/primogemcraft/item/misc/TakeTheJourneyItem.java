package net.per.primogemcraft.item.misc;

import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.per.primogemcraft.compat.item.DiscItem;
import net.per.primogemcraft.registry.PGCSounds;

/**
 * 唱片「踏上旅途」：右键/对方块右键可直接播放，带冷却。
 *
 * <p>1.21 用 jukeboxPlayable 组件；1.20.1 改为继承 {@link DiscItem}（RecordItem），
 * 比较器输出 5、时长 150 秒取自原 data/jukebox_song/take_the_journey.json。
 * 唱片机播放由 RecordItem 提供，手持播放与冷却逻辑保持不变。
 */
public class TakeTheJourneyItem extends DiscItem {
    private static final int COMPARATOR_OUTPUT = 5;
    private static final int LENGTH_SECONDS = 150;
    private static final int COOLDOWN_TICKS = 1280;
    private static final float VOLUME = 0.4F;
    private static final float PITCH = 1.0F;

    public TakeTheJourneyItem(Properties properties) {
        super(COMPARATOR_OUTPUT, PGCSounds.TAKE_THE_JOURNEY::get,
                properties.stacksTo(1).rarity(Rarity.RARE), LENGTH_SECONDS);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        play(level, player);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        // 先让 RecordItem 处理唱片机插入；未插入成功时再走手持播放
        var result = super.useOn(context);
        if (result.consumesAction()) return result;
        var player = context.getPlayer();
        if (player != null) play(context.getLevel(), player);
        return InteractionResult.SUCCESS;
    }

    private void play(Level level, Player player) {
        if (level.isClientSide()) return;
        level.playSound(null, player.getX(), player.getY(), player.getZ(), PGCSounds.TAKE_THE_JOURNEY.get(), SoundSource.PLAYERS, VOLUME, PITCH);
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
    }
}
