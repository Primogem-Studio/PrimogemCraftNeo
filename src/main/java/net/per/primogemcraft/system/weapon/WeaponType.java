package net.per.primogemcraft.system.weapon;

import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.List;
import java.util.Locale;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public enum WeaponType {
    ONE_HANDED_SWORD("weapon/one_handed_sword", 1.0D, EntityAttributes.GENERIC_ATTACK_DAMAGE),
    POLEARM("weapon/polearm", 1.3D, EntityAttributes.GENERIC_ATTACK_DAMAGE),
    CLAYMORE("weapon/claymore", 1.6D, EntityAttributes.GENERIC_ATTACK_DAMAGE),
    SHIELD("weapon/shield", 1.0D, EntityAttributes.GENERIC_ARMOR),
    TOOL("weapon/tool", 0.7D, EntityAttributes.GENERIC_ATTACK_DAMAGE),
    OTHER("weapon", 1.0D, EntityAttributes.GENERIC_ATTACK_DAMAGE);

    private static final List<WeaponType> TYPES = List.of(values());
    private static final String LABEL_PREFIX = "weapon.primogemcraft.type.";

    private final TagKey<Item> tag;
    private final double coefficient;
    private final EntityAttribute attribute;

    WeaponType(String path, double coefficient, EntityAttribute attribute) {
        tag = TagKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, path));
        this.coefficient = coefficient;
        this.attribute = attribute;
    }

    public double coefficient() {
        return coefficient;
    }

    public EntityAttribute attribute() {
        return attribute;
    }

    public String labelKey() {
        return LABEL_PREFIX + name().toLowerCase(Locale.ROOT);
    }

    public static WeaponType of(ItemStack stack) {
        for (var type : TYPES) if (stack.isIn(type.tag)) return type;
        return OTHER;
    }
}
