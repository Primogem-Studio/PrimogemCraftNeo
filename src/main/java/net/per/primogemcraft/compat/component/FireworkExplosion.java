package net.per.primogemcraft.compat.component;

import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtElement;

import java.util.ArrayList;
import java.util.List;

/**
 * 1.21 {@code FireworkExplosion} 的兼容替代。
 *
 * <p>1.21 把烟花爆炸形状做成数据组件记录；1.20.1 用 NBT
 * （{@code Fireworks.Explosions} 列表，每项含 {@code Type/Colors/FadeColors/Flicker/Trail}）。
 * 本类保留 1.21 的构造与 {@link Shape} 枚举形态，并提供与原版一致的 NBT 读写，
 * 因此生成的烟花在 1.20.1 能被原版渲染与发射逻辑正确识别。
 */
public record FireworkExplosion(Shape shape, IntList colors, IntList fadeColors, boolean hasTrail, boolean hasTwinkle) {

    /** 与 1.21 的形状枚举同名同序，序号即原版 NBT 的 Type 值。 */
    public enum Shape {
        SMALL_BALL,
        LARGE_BALL,
        STAR,
        CREEPER,
        BURST;

        public int id() {
            return ordinal();
        }
    }

    /** 1.21 的 {@code FireworkExplosion.DEFAULT}：小型球、白色、无尾迹。 */
    public static final FireworkExplosion DEFAULT =
            new FireworkExplosion(Shape.SMALL_BALL, IntList.of(0xFFFFFF), IntList.of(), false, false);

    /** 序列化为原版爆炸条目 NBT。 */
    public NbtCompound save() {
        var tag = new NbtCompound();
        tag.putByte("Type", (byte) shape.id());
        tag.putIntArray("Colors", colors.toIntArray());
        tag.putIntArray("FadeColors", fadeColors.toIntArray());
        tag.putBoolean("Flicker", hasTwinkle);
        tag.putBoolean("Trail", hasTrail);
        return tag;
    }

    public static FireworkExplosion load(NbtCompound tag) {
        var shape = Shape.values()[Math.min(tag.getByte("Type"), Shape.values().length - 1)];
        return new FireworkExplosion(shape,
                IntList.of(tag.getIntArray("Colors")),
                IntList.of(tag.getIntArray("FadeColors")),
                tag.getBoolean("Trail"),
                tag.getBoolean("Flicker"));
    }

    /** 把一组爆炸包装成 1.20.1 的 {@code Fireworks} 子标签。 */
    public static NbtCompound saveFireworks(List<FireworkExplosion> explosions, int flight) {
        var fireworks = new NbtCompound();
        fireworks.putByte("Flight", (byte) flight);
        var list = new NbtList();
        for (var explosion : explosions) list.add(explosion.save());
        fireworks.put("Explosions", list);
        var root = new NbtCompound();
        root.put("Fireworks", fireworks);
        return root;
    }

    /** 读取 {@code Fireworks} 子标签中的爆炸列表。 */
    public static List<FireworkExplosion> loadFireworks(NbtCompound root) {
        var result = new ArrayList<FireworkExplosion>();
        if (!root.contains("Fireworks", NbtElement.COMPOUND_TYPE)) return result;
        var list = root.getCompound("Fireworks").getList("Explosions", NbtElement.COMPOUND_TYPE);
        for (var i = 0; i < list.size(); i++) result.add(load(list.getCompound(i)));
        return result;
    }
}
