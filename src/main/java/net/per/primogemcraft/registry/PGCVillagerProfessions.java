package net.per.primogemcraft.registry;

import com.google.common.collect.ImmutableSet;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Predicate;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public class PGCVillagerProfessions {
    public static final DeferredRegister<VillagerProfession> REGISTRY = DeferredRegister.create(Registries.VILLAGER_PROFESSION, MOD_ID);

    private static final Predicate<Holder<PoiType>> PRIMOGEM_SCHOLAR_JOB_SITE = holder -> holder.is(PGCPointsOfInterest.PRIMOGEM_SCHOLAR.getKey());

    public static final DeferredHolder<VillagerProfession, VillagerProfession> PRIMOGEM_SCHOLAR = REGISTRY.register("primogem_scholar",
            () -> new VillagerProfession("primogem_scholar", PRIMOGEM_SCHOLAR_JOB_SITE, PRIMOGEM_SCHOLAR_JOB_SITE, ImmutableSet.of(), ImmutableSet.of(),
                    PGCSounds.ENHANCEMENT_SUCCESS.get()));
}
