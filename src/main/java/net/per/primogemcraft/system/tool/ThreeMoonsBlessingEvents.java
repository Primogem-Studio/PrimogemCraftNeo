package net.per.primogemcraft.system.tool;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.tag.DamageTypeTags;
import net.per.primogemcraft.compat.event.EventDispatch;
import net.per.primogemcraft.registry.PGCItems;

public final class ThreeMoonsBlessingEvents {
    private ThreeMoonsBlessingEvents() {
    }

    public static void register() {
        EventDispatch.onDamageModify((entity, source, amount) -> {
            if (!(entity instanceof PlayerEntity player)) return amount;
            if (!source.isIn(DamageTypeTags.IS_FALL)) return amount;
            if (!player.getMainHandStack().isOf(PGCItems.THREE_MOONS_BLESSING.get())
                    && !player.getOffHandStack().isOf(PGCItems.THREE_MOONS_BLESSING.get())) return amount;
            return 0.0F;
        });
    }
}