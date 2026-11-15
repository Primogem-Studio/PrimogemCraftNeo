import net.fabricmc.tinyremapper.OutputConsumerPath;
import net.fabricmc.tinyremapper.TinyRemapper;
import net.fabricmc.tinyremapper.TinyUtils;
import net.fabricmc.mappingio.MappingReader;
import net.fabricmc.mappingio.tree.MemoryMappingTree;

import java.io.IOException;
import java.nio.file.*;
import java.util.jar.JarFile;

public class RemapDeps {
    public static void main(String[] args) throws Exception {
        Path mappingsJar = Paths.get(args[0]);
        Path inputDir = Paths.get(args[1]);
        Path outputDir = Paths.get(args[2]);
        Path mcIntermediary = Paths.get(args[3]);
        Path mcNamed = Paths.get(args[4]);

        // Extract mappings.tiny from yarn jar
        Path mappingsFile = outputDir.resolve("mappings.tiny");
        try (JarFile jar = new JarFile(mappingsJar.toFile())) {
            var entry = jar.getEntry("mappings/mappings.tiny");
            try (var in = jar.getInputStream(entry)) {
                Files.copy(in, mappingsFile, StandardCopyOption.REPLACE_EXISTING);
            }
        }

        MemoryMappingTree tree = new MemoryMappingTree();
        MappingReader.read(mappingsFile, tree);

        Files.createDirectories(outputDir);

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(inputDir, "*.jar")) {
            for (Path input : stream) {
                Path output = outputDir.resolve(input.getFileName().toString().replace(".jar", "-yarn.jar"));
                System.out.println("Remapping " + input.getFileName() + " -> " + output.getFileName());
                TinyRemapper remapper = TinyRemapper.newRemapper()
                        .withMappings(TinyUtils.createTinyMappingProvider(mappingsFile, "intermediary", "named"))
                        .ignoreConflicts(true)
                        .build();
                remapper.readClassPath(mcIntermediary);
                remapper.readClassPath(mcNamed);
                try (OutputConsumerPath consumer = new OutputConsumerPath.Builder(output).build()) {
                    remapper.readInputs(input);
                    remapper.apply(consumer);
                }
                remapper.finish();
            }
        }
        System.out.println("Done.");
    }
}