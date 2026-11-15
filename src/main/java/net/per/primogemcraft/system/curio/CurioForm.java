package net.per.primogemcraft.system.curio;

import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.item.Item;

public enum CurioForm {
    NEGATIVE("negative", 0xFF5555),
    NORMAL("normal", 0x7CFCFF),
    FUSION("fusion", 0xFF7CFF);

    public static final TagKey<Item> ANY = common("curio");

    private final TagKey<Item> tag;
    private final int color;

    CurioForm(String path, int color) {
        tag = common("curio/" + path);
        this.color = color;
    }

    public TagKey<Item> tag() {
        return tag;
    }

    public int color() {
        return color;
    }

    private static TagKey<Item> common(String path) {
        return TagKey.of(RegistryKeys.ITEM, Identifier.of("c", path));
    }
}
