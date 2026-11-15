package net.per.primogemcraft.item.misc;

import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Rarity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.world.World;
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

    public ChasingStarlightWithYouItem(Settings properties) {
        super(COMPARATOR_OUTPUT, PGCSounds.CHASING_STARLIGHT_WITH_YOU::get,
                properties.maxCount(1).rarity(Rarity.RARE), LENGTH_SECONDS);
    }

    @Override
    public TypedActionResult<ItemStack> use(World level, PlayerEntity player, Hand hand) {
        var stack = player.getStackInHand(hand);
        if (level.isClient() || !player.isSneaking()) return TypedActionResult.success(stack, level.isClient());
        var enchantments = new HashMap<>(EnchantmentHelper.get(stack));
        if (enchantments.getOrDefault(Enchantments.FIRE_ASPECT, 0) > 0) enchantments.remove(Enchantments.FIRE_ASPECT);
        else enchantments.put(Enchantments.FIRE_ASPECT, 1);
        EnchantmentHelper.set(enchantments, stack);
        player.swingHand(hand);
        return TypedActionResult.success(stack, false);
    }
}
