package net.per.primogemcraft.system.worldgen;

import com.mojang.serialization.Codec;
import net.minecraft.util.math.BlockPos;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.block.Block;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;
import net.minecraft.structure.processor.BlockIgnoreStructureProcessor;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;

public class StructureTemplateFeature extends Feature<StructureTemplateConfiguration> {
    public StructureTemplateFeature(Codec<StructureTemplateConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean generate(FeatureContext<StructureTemplateConfiguration> context) {
        var random = context.getRandom();
        var level = context.getWorld();
        var config = context.getConfig();
        var settings = new StructurePlacementData()
                .setRotation(config.randomRotation() ? BlockRotation.random(random) : BlockRotation.NONE)
                .setMirror(config.randomMirror() ? BlockMirror.values()[random.nextInt(2)] : BlockMirror.NONE)
                .setRandom(random)
                .setIgnoreEntities(false)
                .addProcessor(new BlockIgnoreStructureProcessor(config.ignoredBlocks().stream().map(RegistryEntry::value).toList()));
        var origin = context.getOrigin().add(StructureTemplate.transform(settings, new BlockPos(config.offset())));
        var template = level.toServerWorld().getStructureTemplateManager().getTemplateOrBlank(config.structure());
        template.place(level, origin, origin, settings, random, Block.NOTIFY_ALL);
        return true;
    }
}
