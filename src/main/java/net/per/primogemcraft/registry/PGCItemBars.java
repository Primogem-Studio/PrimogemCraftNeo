package net.per.primogemcraft.registry;

import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;
import net.per.primogemcraft.component.CustomBar;
import net.per.primogemcraft.item.misc.BlessingOfTheWelkinMoonItem;
import net.per.primogemcraft.item.misc.ExperienceBookItem;
import net.per.primogemcraft.system.curio.Curios;
import net.per.primogemcraft.system.wish.WishValue;

import java.util.ArrayList;
import java.util.List;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

@EventBusSubscriber(modid = MOD_ID)
public final class PGCItemBars {
    private static final int EXPERIENCE_BOOK_COLOR = 0x6A75EA;

    private static List<Entry> entries;

    private PGCItemBars() {
    }

    public static List<Entry> entries() {
        if (entries == null) entries = build();
        return entries;
    }

    @SubscribeEvent
    private static void modifyDefaultComponents(ModifyDefaultComponentsEvent event) {
        for (var entry : entries())
            event.modify(entry.item(), patch -> patch.set(PGCDataComponents.CUSTOM_BAR.get(), entry.bar()));
    }

    private static List<Entry> build() {
        var built = new ArrayList<Entry>();
        built.add(new Entry(PGCItems.WISH_CORE.get(), new CustomBar(0, WishValue.CAPACITY, true), 0xFECCFF, false));
        built.add(new Entry(PGCItems.BLESSING_OF_THE_WELKIN_MOON.get(),
                new CustomBar(0, BlessingOfTheWelkinMoonItem.CAPACITY, true), 0x55FFFF, true));
        for (var holder : PGCItems.REGISTRY.getEntries())
            if (holder.get() instanceof ExperienceBookItem book)
                built.add(new Entry(book, new CustomBar(0, book.capacity(), true), EXPERIENCE_BOOK_COLOR, false));
        for (var curio : Curios.getCurios()) {
            var capacity = curio.barCapacity();
            built.add(new Entry(curio, new CustomBar(0, capacity, capacity > 1), curio.form().color(), !curio.barFillsUp()));
        }
        return List.copyOf(built);
    }

    public record Entry(Item item, CustomBar bar, int color, boolean depleting) {
    }
}
