package net.per.primogemcraft.system.curio;

import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public enum CurioGrade {
    B("b"),
    A("a"),
    S("s");

    private final TagKey<Item> normal;
    private final TagKey<Item> fusion;

    CurioGrade(String path) {
        normal = common("curio/normal/" + path);
        fusion = common("curio/normal/fusion/" + path);
    }

    public TagKey<Item> tag(CurioForm form) {
        return form == CurioForm.FUSION ? fusion : normal;
    }

    public int tier(CurioForm form) {
        return ordinal() + (form == CurioForm.FUSION ? 1 : 0);
    }

    public int stars() {
        return ordinal() + 1;
    }

    public static CurioGrade of(ItemStack stack) {
        var form = Curios.formOf(stack);
        if (form == null) return null;
        for (var grade : values()) if (stack.isIn(grade.tag(form))) return grade;
        return null;
    }

    private static TagKey<Item> common(String path) {
        return TagKey.of(RegistryKeys.ITEM, Identifier.of("c", path));
    }
}
