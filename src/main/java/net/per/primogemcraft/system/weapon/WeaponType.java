package net.per.primogemcraft.system.weapon;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Locale;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public enum WeaponType {
    ONE_HANDED_SWORD("weapon/one_handed_sword", 1.0D, Attributes.ATTACK_DAMAGE),
    POLEARM("weapon/polearm", 1.3D, Attributes.ATTACK_DAMAGE),
    CLAYMORE("weapon/claymore", 1.6D, Attributes.ATTACK_DAMAGE),
    SHIELD("weapon/shield", 1.0D, Attributes.ARMOR),
    TOOL("weapon/tool", 0.7D, Attributes.MINING_EFFICIENCY),
    OTHER("weapon", 1.0D, Attributes.ATTACK_DAMAGE);

    private static final List<WeaponType> TYPES = List.of(values());
    private static final String LABEL_PREFIX = "weapon.primogemcraft.type.";

    private final TagKey<Item> tag;
    private final double coefficient;
    private final Holder<Attribute> attribute;

    WeaponType(String path, double coefficient, Holder<Attribute> attribute) {
        tag = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MOD_ID, path));
        this.coefficient = coefficient;
        this.attribute = attribute;
    }

    public double coefficient() {
        return coefficient;
    }

    public Holder<Attribute> attribute() {
        return attribute;
    }

    public String labelKey() {
        return LABEL_PREFIX + name().toLowerCase(Locale.ROOT);
    }

    public static WeaponType of(ItemStack stack) {
        for (var type : TYPES) if (stack.is(type.tag)) return type;
        return OTHER;
    }
}
