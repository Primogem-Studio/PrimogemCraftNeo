package net.per.primogemcraft.compat.item;

import net.minecraft.sound.SoundEvent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MusicDiscItem;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.world.World;
import net.minecraft.text.Text;
import net.per.primogemcraft.item.tool.DescribedItem;

import java.util.List;
import java.util.function.Supplier;

/**
 * 唱片物品（1.20.1 实现）。
 *
 * <p>1.21 把唱片做成「普通物品 + {@code jukeboxPlayable} 组件 + 数据驱动的
 * {@code jukebox_song} JSON」；1.20.1 没有该系统，唱片必须是
 * {@link RecordItem}，比较器输出与时长由构造参数给出。
 *
 * <p>本类沿用原 JSON 中的 {@code comparator_output} 与 {@code length_in_seconds}，
 * 并复用项目的多行描述提示渲染，保持物品提示与原版一致。
 */
public class DiscItem extends MusicDiscItem {

    public DiscItem(int comparatorOutput, Supplier<SoundEvent> sound, Settings settings, int lengthSeconds) {
        super(comparatorOutput, sound.get(), settings, lengthSeconds * 20);
    }

    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        DescribedItem.appendDescription(stack, tooltip);
    }
}
