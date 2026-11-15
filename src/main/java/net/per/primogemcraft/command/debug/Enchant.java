package net.per.primogemcraft.command.debug;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.enchantment.EnchantmentHelper;
import com.mojang.brigadier.arguments.StringArgumentType;
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

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("primogemcraft").then(CommandManager.literal("enchant").requires(source -> source.hasPermissionLevel(2))
                .then(CommandManager.literal("choose")
                        .executes(context -> choose(context.getSource().getPlayerOrThrow(), null, false))
                        .then(CommandManager.literal("fixed").executes(context -> choose(context.getSource().getPlayerOrThrow(), null, true)))
                        .then(CommandManager.argument("grade", com.mojang.brigadier.arguments.StringArgumentType.word())
                                .executes(context -> choose(context.getSource().getPlayerOrThrow(), context.getArgument("grade", EnchantGrade.class), false))
                                .then(CommandManager.literal("fixed").executes(context -> choose(context.getSource().getPlayerOrThrow(), context.getArgument("grade", EnchantGrade.class), true)))
                        )
                )
        ));
    }

    private static int choose(ServerPlayerEntity player, EnchantGrade grade, boolean fixedPreview) {
        var target = player.getMainHandStack();
        if (!target.isEnchantable()) {
            player.sendMessage(Text.translatable(INVALID_TARGET_KEY), true);
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

    private static EnchantOption option(ServerPlayerEntity player, EnchantGrade grade, boolean fixedPreview, ItemStack target) {
        var level = grade.rollLevel(player.getRandom());
        var cost = EnchantCost.levels(grade.minLevel());
        var result = CurioEnchanting.tablePoolResult(player, target, level);
        return EnchantOption.of(target, fixedPreview ? result : book(result), grade, level, cost);
    }

    private static ItemStack book(ItemStack result) {
        var book = new ItemStack(Items.ENCHANTED_BOOK);
        EnchantmentHelper.set(EnchantmentHelper.get(result), book);
        return book;
    }
}
