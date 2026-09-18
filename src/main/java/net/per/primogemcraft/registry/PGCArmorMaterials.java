package net.per.primogemcraft.registry;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.Map;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public class PGCArmorMaterials {
    public static final DeferredRegister<ArmorMaterial> REGISTRY = DeferredRegister.create(Registries.ARMOR_MATERIAL, MOD_ID);

    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> COLORFUL_SUNGLASSES = REGISTRY.register("colorful_sunglasses",
            () -> new ArmorMaterial(
                    Map.of(ArmorItem.Type.HELMET, 2),
                    15,
                    SoundEvents.ARMOR_EQUIP_LEATHER,
                    () -> Ingredient.of(PGCItems.PRIMOGEM.get()),
                    List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(MOD_ID, "colorful_sunglasses"))),
                    0.0F,
                    0.0F));

    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> SPECIAL_01 = REGISTRY.register("special_01",
            () -> new ArmorMaterial(
                    Map.of(ArmorItem.Type.HELMET, 1),
                    1,
                    SoundEvents.ARMOR_EQUIP_GENERIC,
                    Ingredient::of,
                    List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(MOD_ID, "special_01"))),
                    0.0F,
                    0.0F));

    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> MORA = REGISTRY.register("mora",
            () -> new ArmorMaterial(
                    Map.of(
                            ArmorItem.Type.HELMET, 5,
                            ArmorItem.Type.CHESTPLATE, 10,
                            ArmorItem.Type.LEGGINGS, 8,
                            ArmorItem.Type.BOOTS, 5),
                    20,
                    Holder.direct(SoundEvents.EMPTY),
                    () -> Ingredient.of(PGCItems.REFINED_MORA.get()),
                    List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(MOD_ID, "mora"))),
                    4.0F,
                    0.2F));
}
