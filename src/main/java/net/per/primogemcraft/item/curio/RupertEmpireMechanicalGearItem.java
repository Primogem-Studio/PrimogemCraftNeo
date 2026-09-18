package net.per.primogemcraft.item.curio;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.per.primogemcraft.item.misc.OtherworldBankbook;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.system.curio.*;
import net.per.primogemcraft.util.PlayerFlags;
import net.per.primogemcraft.util.PlayerItems;

import java.util.List;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public class RupertEmpireMechanicalGearItem extends CurioItem {
    private static final String GIFT_KEY = "message.primogemcraft.curio.rupert_empire_mechanical_gear.gift";
    private static final String PURGE_KEY = "message.primogemcraft.curio.rupert_empire_mechanical_gear.purge";
    private static final ResourceLocation PROGRESS = ResourceLocation.fromNamespaceAndPath(MOD_ID, "mechanical_gear_progress");
    private static final int REQUIRED_FRAGMENTS = 45;
    private static final int CYCLE = 24000;
    private static final int MIN_GIFT = 10;
    private static final int MAX_GIFT = 20;
    private static final int PURGE_THRESHOLD = 64;
    private static final int MIN_MATERIALS = 1;
    private static final int MAX_MATERIALS = 2;

    private static final List<ItemLike> MATERIALS = List.of(
            Items.RAW_GOLD, Items.GOLD_INGOT, Items.LAPIS_LAZULI, Items.EMERALD, Items.PRISMARINE_SHARD,
            Items.IRON_INGOT, Items.DIAMOND, Items.RAW_IRON, Items.ECHO_SHARD, Items.REDSTONE,
            Items.NETHERITE_SCRAP, Items.QUARTZ, Items.RAW_COPPER, Items.COPPER_INGOT, Items.AMETHYST_SHARD,
            PGCItems.PRIMOGEM);

    public RupertEmpireMechanicalGearItem(Properties properties) {
        super(CurioTrigger.ACTIVE, CurioForm.NORMAL, 1, properties);
    }

    @Override
    public void presence(CurioContext context) {
        var player = context.player();
        var fragment = PGCItems.COSMIC_FRAGMENT.get();
        if (PlayerItems.count(player, fragment) < REQUIRED_FRAGMENTS) return;
        var flags = PlayerFlags.of(player);
        if (flags.advance(PROGRESS) < CYCLE) return;
        flags.set(PROGRESS, 0);
        var gift = Mth.nextInt(context.random(), MIN_GIFT, MAX_GIFT);
        OtherworldBankbook.give(player, new ItemStack(fragment, gift));
        context.announce(Component.translatable(GIFT_KEY, context.stack().getHoverName(), gift));
        var held = PlayerItems.count(player, fragment);
        if (held <= PURGE_THRESHOLD) return;
        PlayerItems.take(player, fragment, held);
        context.announce(Component.translatable(PURGE_KEY, context.stack().getHoverName()));
        for (var index = 0; index < Mth.nextInt(context.random(), MIN_MATERIALS, MAX_MATERIALS); index++)
            Curios.give(player, new ItemStack(MATERIALS.get(context.random().nextInt(MATERIALS.size()))));
        context.damage(1);
    }
}
