package net.per.primogemcraft.item.misc;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.per.primogemcraft.compat.item.DiscItem;
import net.per.primogemcraft.registry.PGCSounds;

import java.util.HashMap;

/**
 * 唱片「逐星之旅」：潜行右键切换火焰附魔。
 *
 * <p>1.21 用 jukeboxPlayable 组件把普通物品变成唱片；1.20.1 改为继承
 * {@link DiscItem}（RecordItem），比较器输出 15、时长 400 秒取自原
 * data/jukebox_song/chasing_starlight_with_you.json。
 *
 * <p>1.20.1 的附魔是注册表对象与 Map 模型，故附魔读写改用
 * {@code EnchantmentHelper.getEnchantments}/{@code setEnchantments}。
 */
public class ChasingStarlightWithYouItem extends DiscItem {
    private static final int COMPARATOR_OUTPUT = 15;
    private static final int LENGTH_SECONDS = 400;

    public ChasingStarlightWithYouItem(Properties properties) {
        super(COMPARATOR_OUTPUT, PGCSounds.CHASING_STARLIGHT_WITH_YOU::get,
                properties.stacksTo(1).rarity(Rarity.RARE), LENGTH_SECONDS);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        if (level.isClientSide() || !player.isShiftKeyDown()) return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        var enchantments = new HashMap<>(EnchantmentHelper.getEnchantments(stack));
        if (enchantments.getOrDefault(Enchantments.FIRE_ASPECT, 0) > 0) enchantments.remove(Enchantments.FIRE_ASPECT);
        else enchantments.put(Enchantments.FIRE_ASPECT, 1);
        EnchantmentHelper.setEnchantments(enchantments, stack);
        player.swing(hand, true);
        return InteractionResultHolder.sidedSuccess(stack, false);
    }
}
