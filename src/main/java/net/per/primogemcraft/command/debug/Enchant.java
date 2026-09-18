package net.per.primogemcraft.command.debug;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.neoforged.neoforge.server.command.EnumArgument;
import net.per.primogemcraft.enchantment.EnchantChoice;
import net.per.primogemcraft.enchantment.EnchantCost;
import net.per.primogemcraft.enchantment.EnchantGrade;
import net.per.primogemcraft.enchantment.EnchantOption;
import net.per.primogemcraft.system.curio.CurioEnchanting;

import java.util.ArrayList;
import java.util.List;

public class Enchant {
    private static final int OPTION_COUNT = 3;
    private static final String INVALID_TARGET_KEY = "message.primogemcraft.enchant.invalid_target";

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("primogemcraft").then(Commands.literal("enchant").requires(source -> source.hasPermission(2))
                .then(Commands.literal("choose")
                        .executes(context -> choose(context.getSource().getPlayerOrException(), null, false))
                        .then(Commands.literal("fixed").executes(context -> choose(context.getSource().getPlayerOrException(), null, true)))
                        .then(Commands.argument("grade", EnumArgument.enumArgument(EnchantGrade.class))
                                .executes(context -> choose(context.getSource().getPlayerOrException(), context.getArgument("grade", EnchantGrade.class), false))
                                .then(Commands.literal("fixed").executes(context -> choose(context.getSource().getPlayerOrException(), context.getArgument("grade", EnchantGrade.class), true)))
                        )
                )
        ));
    }

    private static int choose(ServerPlayer player, EnchantGrade grade, boolean fixedPreview) {
        var target = player.getMainHandItem();
        if (!target.isEnchantable()) {
            player.displayClientMessage(Component.translatable(INVALID_TARGET_KEY), true);
            return 0;
        }
        var grades = new ArrayList<EnchantGrade>();
        if (grade == null) grades.addAll(List.of(EnchantGrade.values()));
        else for (var index = 0; index < OPTION_COUNT; index++) grades.add(grade);
        var options = new ArrayList<EnchantOption>();
        for (var each : grades) options.add(option(player, each, fixedPreview, target));
        EnchantChoice.open(player, options, 1);
        return options.size();
    }

    private static EnchantOption option(ServerPlayer player, EnchantGrade grade, boolean fixedPreview, ItemStack target) {
        var level = grade.rollLevel(player.getRandom());
        var cost = EnchantCost.levels(grade.minLevel());
        var result = CurioEnchanting.tablePoolResult(player, target, level);
        return EnchantOption.of(target, fixedPreview ? result : book(result), grade, level, cost);
    }

    private static ItemStack book(ItemStack result) {
        var book = new ItemStack(Items.ENCHANTED_BOOK);
        EnchantmentHelper.setEnchantments(book, EnchantmentHelper.getEnchantmentsForCrafting(result));
        return book;
    }
}
