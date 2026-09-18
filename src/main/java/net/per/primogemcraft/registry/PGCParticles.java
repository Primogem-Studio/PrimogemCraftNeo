package net.per.primogemcraft.registry;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public class PGCParticles {
    public static final DeferredRegister<ParticleType<?>> REGISTRY = DeferredRegister.create(Registries.PARTICLE_TYPE, MOD_ID);

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> MARA = REGISTRY.register("mara", () -> new SimpleParticleType(true));
}
