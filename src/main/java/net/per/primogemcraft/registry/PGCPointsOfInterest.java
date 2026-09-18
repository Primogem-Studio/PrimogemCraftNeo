package net.per.primogemcraft.registry;

import com.google.common.collect.ImmutableSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public class PGCPointsOfInterest {
    public static final DeferredRegister<PoiType> REGISTRY = DeferredRegister.create(Registries.POINT_OF_INTEREST_TYPE, MOD_ID);

    public static final DeferredHolder<PoiType, PoiType> PRIMOGEM_SCHOLAR = REGISTRY.register("primogem_scholar",
            () -> new PoiType(ImmutableSet.copyOf(PGCBlocks.GORGEOUS_SMITHING_TABLE.get().getStateDefinition().getPossibleStates()), 1, 1));
}
