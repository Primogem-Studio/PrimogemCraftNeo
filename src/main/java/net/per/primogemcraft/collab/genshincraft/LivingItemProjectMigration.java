package net.per.primogemcraft.collab.genshincraft;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Set;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public final class LivingItemProjectMigration {
    private static final Set<String> ACTIONS = Set.of("ring", "line", "wedge", "move", "attack", "follow", "recall", "return");

    private LivingItemProjectMigration() {
    }

    /** Migrates saved action primitives before the upstream design screen loads them. */
    public static void migrate(Path file) throws IOException {
        if (!Files.exists(file)) return;
        var original = Files.readString(file);
        var root = JsonParser.parseString(original).getAsJsonObject();
        if (!root.has("version") || root.get("version").getAsInt() != 1 || !root.has("projects")) return;
        var changed = false;
        for (var project : root.getAsJsonObject("projects").entrySet()) {
            var design = project.getValue().getAsJsonObject();
            if (!design.has("sequences")) continue;
            for (var entry : design.getAsJsonArray("sequences")) {
                var sequence = entry.getAsJsonObject();
                if (sequence.has("main")) changed |= migrateRow(sequence.getAsJsonObject("main"));
                if (sequence.has("subs")) {
                    for (var row : sequence.getAsJsonArray("subs")) changed |= migrateRow(row.getAsJsonObject());
                }
            }
        }
        if (!changed) return;
        var backup = Files.createTempFile(file.getParent(), "blueprint_projects.pre-living-item-", ".json.bak");
        Files.writeString(backup, original);
        var temporary = Files.createTempFile(file.getParent(), "blueprint_projects-", ".tmp");
        try {
            Files.writeString(temporary, new GsonBuilder().setPrettyPrinting().create().toJson(root) + "\n");
            try {
                Files.move(temporary, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    private static boolean migrateRow(JsonObject row) throws IOException {
        var primitive = row.get("primitive");
        if (primitive == null || !primitive.isJsonPrimitive()) return false;
        var id = primitive.getAsString();
        var prefix = MOD_ID + ":living_item_";
        if (!id.startsWith(prefix) || !ACTIONS.contains(id.substring(prefix.length()))) return false;
        var decorators = row.has("decorators") ? row.getAsJsonArray("decorators") : new JsonArray();
        if (decorators.size() >= 32) throw new IOException("Legacy living-item row has no free decorator slot");
        var migrated = new JsonArray();
        migrated.add(id);
        migrated.addAll(decorators);
        row.addProperty("primitive", prefix + "selection");
        row.add("decorators", migrated);
        return true;
    }
}
