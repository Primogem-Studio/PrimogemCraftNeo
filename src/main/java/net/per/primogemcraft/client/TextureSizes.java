package net.per.primogemcraft.client;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.per.primogemcraft.PrimogemCraft;

import com.mojang.blaze3d.platform.NativeImage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public final class TextureSizes {
    private static final Map<ResourceLocation, int[]> SIZES = new HashMap<>();

    private TextureSizes() {
    }

    public static int[] of(ResourceLocation texture) {
        return SIZES.computeIfAbsent(texture, TextureSizes::read);
    }

    private static int[] read(ResourceLocation texture) {
        var size = new int[]{0, 0};
        var resource = Minecraft.getInstance().getResourceManager().getResource(texture);
        if (resource.isEmpty()) return size;
        try (var stream = resource.get().open(); var image = NativeImage.read(stream)) {
            size[0] = image.getWidth();
            size[1] = image.getHeight();
        } catch (IOException exception) {
            PrimogemCraft.LOGGER.warn("Unreadable texture {}", texture, exception);
        }
        return size;
    }
}
