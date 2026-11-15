package net.per.primogemcraft.registry;

import com.google.common.collect.ImmutableSet;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.poi.PointOfInterestType;
import net.per.primogemcraft.compat.registries.DeferredHolder;
import net.per.primogemcraft.compat.registries.DeferredRegister;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public class PGCPointsOfInterest {
    public static final DeferredRegister<PointOfInterestType> REGISTRY = DeferredRegister.create(RegistryKeys.POINT_OF_INTEREST_TYPE, MOD_ID);

    public static final DeferredHolder<PointOfInterestType, PointOfInterestType> PRIMOGEM_SCHOLAR = REGISTRY.register("primogem_scholar",
            () -> new PointOfInterestType(ImmutableSet.copyOf(PGCBlocks.GORGEOUS_SMITHING_TABLE.get().getStateManager().getStates()), 1, 1));
}
