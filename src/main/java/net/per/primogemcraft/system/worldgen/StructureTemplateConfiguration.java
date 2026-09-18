package net.per.primogemcraft.system.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public record StructureTemplateConfiguration(ResourceLocation structure, boolean randomRotation, boolean randomMirror,
                                             HolderSet<Block> ignoredBlocks, Vec3i offset) implements FeatureConfiguration {
    public static final Codec<StructureTemplateConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("structure").forGetter(StructureTemplateConfiguration::structure),
            Codec.BOOL.optionalFieldOf("random_rotation", false).forGetter(StructureTemplateConfiguration::randomRotation),
            Codec.BOOL.optionalFieldOf("random_mirror", false).forGetter(StructureTemplateConfiguration::randomMirror),
            RegistryCodecs.homogeneousList(Registries.BLOCK).fieldOf("ignored_blocks").forGetter(StructureTemplateConfiguration::ignoredBlocks),
            Vec3i.offsetCodec(48).optionalFieldOf("offset", Vec3i.ZERO).forGetter(StructureTemplateConfiguration::offset)
    ).apply(instance, StructureTemplateConfiguration::new));
}
