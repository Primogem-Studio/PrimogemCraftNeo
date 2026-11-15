package net.per.primogemcraft.registry;

import com.google.common.collect.ImmutableSet;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.poi.PointOfInterestType;
import net.minecraft.village.VillagerProfession;
import net.per.primogemcraft.compat.registries.DeferredHolder;
import net.per.primogemcraft.compat.registries.DeferredRegister;

import java.util.function.Predicate;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public class PGCVillagerProfessions {
    public static final DeferredRegister<VillagerProfession> REGISTRY = DeferredRegister.create(RegistryKeys.VILLAGER_PROFESSION, MOD_ID);

    private static final Predicate<RegistryEntry<PointOfInterestType>> PRIMOGEM_SCHOLAR_JOB_SITE = holder -> holder.matchesKey(PGCPointsOfInterest.PRIMOGEM_SCHOLAR.getRegistryKey());

    public static final DeferredHolder<VillagerProfession, VillagerProfession> PRIMOGEM_SCHOLAR = REGISTRY.register("primogem_scholar",
            () -> new VillagerProfession("primogem_scholar", PRIMOGEM_SCHOLAR_JOB_SITE, PRIMOGEM_SCHOLAR_JOB_SITE, ImmutableSet.of(), ImmutableSet.of(),
                    PGCSounds.ENHANCEMENT_SUCCESS.get()));
}
