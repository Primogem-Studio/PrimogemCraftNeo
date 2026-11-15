package net.per.primogemcraft.system.wish;

import net.minecraft.text.Text;
import net.minecraft.text.MutableText;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public enum WishBanner {
    INTERTWINED("intertwined"),
    ACQUAINT("acquaint");

    private final String id;

    WishBanner(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public boolean countsPity() {
        return this == INTERTWINED;
    }

    public String key() {
        return "wish." + MOD_ID + ".banner." + id;
    }

    public MutableText title() {
        return Text.translatable(key());
    }
}
