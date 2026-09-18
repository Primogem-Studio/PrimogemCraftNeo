package net.per.primogemcraft.system.worldgen;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class StructureTemplateFeature extends Feature<StructureTemplateConfiguration> {
    public StructureTemplateFeature(Codec<StructureTemplateConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<StructureTemplateConfiguration> context) {
        var random = context.random();
        var level = context.level();
        var config = context.config();
        var settings = new StructurePlaceSettings()
                .setRotation(config.randomRotation() ? Rotation.getRandom(random) : Rotation.NONE)
                .setMirror(config.randomMirror() ? Mirror.values()[random.nextInt(2)] : Mirror.NONE)
                .setRandom(random)
                .setIgnoreEntities(false)
                .addProcessor(new BlockIgnoreProcessor(config.ignoredBlocks().stream().map(Holder::value).toList()));
        var origin = context.origin().offset(StructureTemplate.calculateRelativePosition(settings, new BlockPos(config.offset())));
        var template = level.getLevel().getStructureManager().getOrCreate(config.structure());
        template.placeInWorld(level, origin, origin, settings, random, Block.UPDATE_CLIENTS);
        return true;
    }
}
