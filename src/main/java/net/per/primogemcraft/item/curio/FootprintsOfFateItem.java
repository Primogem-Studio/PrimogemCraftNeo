package net.per.primogemcraft.item.curio;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.per.primogemcraft.registry.PGCDataComponents;
import net.per.primogemcraft.system.curio.CurioContext;
import net.per.primogemcraft.system.curio.CurioForm;
import net.per.primogemcraft.system.curio.CurioItem;
import net.per.primogemcraft.system.curio.CurioTrigger;
import net.per.primogemcraft.system.wish.WishReports;
import net.per.primogemcraft.system.wish.WishTooltips;

import java.util.List;

public class FootprintsOfFateItem extends CurioItem {
    public static final int USES = 5;
    public static final int USES_PER_TRIGGER = 1;

    private static final double MIN_CHANCE = 0.01D;
    private static final double MAX_CHANCE = 0.6D;
    private static final double MIN_DISCOUNT = 0.01D;
    private static final double MAX_DISCOUNT = 0.25D;
    private static final String TOOLTIP_PREFIX = ".tooltip.";

    public FootprintsOfFateItem(Properties properties) {
        super(CurioTrigger.ENHANCE, CurioForm.NORMAL, USES, properties.rarity(Rarity.UNCOMMON));
    }

    public static Roll roll(ItemStack stack) {
        return stack.getOrDefault(PGCDataComponents.FOOTPRINTS_ROLL.get(), Roll.NONE);
    }

    public static Roll ensure(ItemStack stack, RandomSource random, boolean creative) {
        var roll = stack.get(PGCDataComponents.FOOTPRINTS_ROLL.get());
        if (roll != null) return roll;
        var value = new Roll(creative ? 1.0D : Mth.nextDouble(random, MIN_CHANCE, MAX_CHANCE), Mth.nextDouble(random, MIN_DISCOUNT, MAX_DISCOUNT));
        stack.set(PGCDataComponents.FOOTPRINTS_ROLL.get(), value);
        return value;
    }

    public static double discount(Roll roll, RandomSource random) {
        return Mth.nextDouble(random, MIN_DISCOUNT, roll.discount());
    }

    @Override
    public void presence(CurioContext context) {
        ensure(context.stack(), context.random(), context.player().isCreative());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        appendTrigger(stack, tooltip);
        var prefix = stack.getDescriptionId() + TOOLTIP_PREFIX;
        tooltip.add(Component.translatable(prefix + 0));
        var roll = roll(stack);
        if (WishTooltips.showsDetails()) {
            tooltip.add(Component.translatable(prefix + 1, WishReports.percent(roll.chance(), ChatFormatting.AQUA)));
            tooltip.add(Component.translatable(prefix + 2,
                    WishReports.percent(MIN_DISCOUNT, ChatFormatting.GOLD),
                    WishReports.percent(roll.discount(), ChatFormatting.GOLD)));
        } else {
            tooltip.add(Component.translatable(prefix + 3));
        }
        appendBar(stack, tooltip);
    }

    public record Roll(double chance, double discount) {
        public static final Roll NONE = new Roll(0.0D, 0.0D);

        public static final Codec<Roll> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.DOUBLE.fieldOf("chance").forGetter(Roll::chance),
                Codec.DOUBLE.fieldOf("discount").forGetter(Roll::discount)
        ).apply(instance, Roll::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, Roll> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.DOUBLE, Roll::chance,
                ByteBufCodecs.DOUBLE, Roll::discount,
                Roll::new);
    }
}
