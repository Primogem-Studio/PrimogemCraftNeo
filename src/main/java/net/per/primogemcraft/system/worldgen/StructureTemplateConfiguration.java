package net.per.primogemcraft.system.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.RegistryCodecs;
import net.minecraft.util.math.Vec3i;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.block.Block;
import net.minecraft.world.gen.feature.FeatureConfig;

public record StructureTemplateConfiguration(Identifier structure, boolean randomRotation, boolean randomMirror,
                                             RegistryEntryList<Block> ignoredBlocks, Vec3i offset) implements FeatureConfig {
    public static final Codec<StructureTemplateConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("structure").forGetter(StructureTemplateConfiguration::structure),
            Codec.BOOL.optionalFieldOf("random_rotation", false).forGetter(StructureTemplateConfiguration::randomRotation),
            Codec.BOOL.optionalFieldOf("random_mirror", false).forGetter(StructureTemplateConfiguration::randomMirror),
            RegistryCodecs.entryList(RegistryKeys.BLOCK).fieldOf("ignored_blocks").forGetter(StructureTemplateConfiguration::ignoredBlocks),
            Vec3i.createOffsetCodec(48).optionalFieldOf("offset", Vec3i.ZERO).forGetter(StructureTemplateConfiguration::offset)
    ).apply(instance, StructureTemplateConfiguration::new));
}
