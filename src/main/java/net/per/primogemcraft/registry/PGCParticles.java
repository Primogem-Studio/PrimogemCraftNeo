package net.per.primogemcraft.registry;

import net.minecraft.particle.ParticleType;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.registry.RegistryKeys;
import net.per.primogemcraft.compat.registries.DeferredHolder;
import net.per.primogemcraft.compat.registries.DeferredRegister;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public class PGCParticles {
    public static final DeferredRegister<ParticleType<?>> REGISTRY = DeferredRegister.create(RegistryKeys.PARTICLE_TYPE, MOD_ID);

    public static final DeferredHolder<ParticleType<?>, net.minecraft.particle.DefaultParticleType> MARA = REGISTRY.register("mara", () -> net.fabricmc.fabric.api.particle.v1.FabricParticleTypes.simple(true));
}
