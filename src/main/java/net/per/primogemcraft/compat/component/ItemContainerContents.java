package net.per.primogemcraft.compat.component;

import com.mojang.serialization.Codec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * 1.21 {@code ItemContainerContents} 的兼容替代（袋子/容器物品的内容物）。
 *
 * <p>1.20.1 没有该组件类型，这里用一个不可变的物品堆列表承载，
 * 序列化沿用原版容器 NBT 格式（{@code Items} 列表，每项带 {@code Slot}），
 * 以便与 {@code ContainerHelper} 等原版工具互通。
 */
public final class ItemContainerContents {

    public static final ItemContainerContents EMPTY = new ItemContainerContents(List.of());

    /** 与原版的 listOf 编解码语义一致：编码为 Items 列表。 */
    public static final Codec<ItemContainerContents> CODEC = Codec.list(ItemStack.CODEC)
            .xmap(ItemContainerContents::fromItems, contents -> contents.items);

    private final List<ItemStack> items;

    private ItemContainerContents(List<ItemStack> items) {
        this.items = items;
    }

    public static ItemContainerContents fromItems(List<ItemStack> items) {
        var copy = new ArrayList<ItemStack>(items.size());
        for (var item : items) copy.add(item.copy());
        return new ItemContainerContents(List.copyOf(copy));
    }

    public int getSlots() {
        return items.size();
    }

    public ItemStack getStackInSlot(int slot) {
        if (slot < 0 || slot >= items.size()) return ItemStack.EMPTY;
        return items.get(slot);
    }

    /** 序列化为原版容器 NBT（Items 列表）。 */
    public CompoundTag save() {
        var tag = new CompoundTag();
        var list = new ListTag();
        for (var index = 0; index < items.size(); index++) {
            var stack = items.get(index);
            if (stack.isEmpty()) continue;
            var entry = new CompoundTag();
            entry.putByte("Slot", (byte) index);
            stack.save(entry);
            list.add(entry);
        }
        tag.put("Items", list);
        return tag;
    }

    public static ItemContainerContents load(CompoundTag tag) {
        var list = tag.getList("Items", Tag.TAG_COMPOUND);
        var items = new ArrayList<ItemStack>();
        for (var index = 0; index < list.size(); index++) {
            var entry = list.getCompound(index);
            var slot = entry.getByte("Slot") & 0xFF;
            while (items.size() <= slot) items.add(ItemStack.EMPTY);
            items.set(slot, ItemStack.of(entry));
        }
        return new ItemContainerContents(List.copyOf(items));
    }
}
