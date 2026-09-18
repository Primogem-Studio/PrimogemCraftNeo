package net.per.primogemcraft.system.tool;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import net.per.primogemcraft.registry.PGCItems;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

@EventBusSubscriber(modid = MOD_ID)
public final class ThreeMoonsBlessingEvents {
    private static final ResourceLocation MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(MOD_ID, "three_moons_blessing");
    private static final double FALL_DAMAGE_REDUCTION = -1.0D;

    private ThreeMoonsBlessingEvents() {
    }

    @SubscribeEvent
    private static void onItemAttributes(ItemAttributeModifierEvent event) {
        if (!event.getItemStack().is(PGCItems.THREE_MOONS_BLESSING.get())) return;
        event.addModifier(Attributes.FALL_DAMAGE_MULTIPLIER,
                new AttributeModifier(MODIFIER_ID, FALL_DAMAGE_REDUCTION, AttributeModifier.Operation.ADD_MULTIPLIED_BASE),
                EquipmentSlotGroup.ANY);
    }
}
