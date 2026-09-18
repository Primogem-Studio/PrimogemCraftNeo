package net.per.primogemcraft.system.wish;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.per.primogemcraft.registry.PGCSounds;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public enum WishRarity {
    BLUE("blue"),
    PURPLE("purple"),
    GOLD("gold");

    private final String id;

    WishRarity(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public String translationKey() {
        return "wish." + MOD_ID + ".rarity." + id;
    }

    public String lootTablePath() {
        return "wish/" + id;
    }

    public ResourceLocation lootTable() {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, lootTablePath());
    }

    public ResourceLocation texture() {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/entity/wish_entity_" + id + ".png");
    }

    public SoundEvent sound() {
        return switch (this) {
            case BLUE -> PGCSounds.WISH_BLUE.get();
            case PURPLE -> PGCSounds.WISH_PURPLE.get();
            case GOLD -> PGCSounds.WISH_GOLD.get();
        };
    }
}
