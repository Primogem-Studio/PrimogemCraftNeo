package net.per.primogemcraft.command.debug;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.server.command.EnumArgument;
import net.per.primogemcraft.system.curio.CurioChoice;
import net.per.primogemcraft.system.curio.CurioForm;
import net.per.primogemcraft.system.curio.CurioGrade;
import net.per.primogemcraft.system.curio.Curios;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Curio {
    private static final int CHOICE_OPTIONS = 3;

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("primogemcraft").then(Commands.literal("curio").requires(c -> c.hasPermission(2))
                .then(Commands.literal("random")
                        .then(Commands.argument("count", IntegerArgumentType.integer(1, 64)).executes(c -> {
                            var player = c.getSource().getPlayerOrException();
                            var count = IntegerArgumentType.getInteger(c, "count");
                            for (var index = 0; index < count; index++)
                                Curios.give(player, Curios.randomCurio(player.getRandom()));
                            return count;
                        }))
                ).then(Commands.literal("choose")
                        .then(Commands.literal("random").executes(c -> choose(c.getSource().getPlayerOrException(), CurioForm.ANY))
                        ).then(Commands.argument("grade", EnumArgument.enumArgument(CurioGrade.class)).executes(c -> choose(c.getSource().getPlayerOrException(), tagOf(c, CurioForm.NORMAL)))
                                .then(Commands.literal("fusion").executes(c -> choose(c.getSource().getPlayerOrException(), tagOf(c, CurioForm.FUSION))))
                        )
                ).then(Commands.literal("give")
                        .then(Commands.argument("curios", StringArgumentType.greedyString()).executes(Curio::give))
                )
        ));
    }

    private static int give(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var player = context.getSource().getPlayerOrException();
        var given = 0;
        for (var token : StringArgumentType.getString(context, "curios").split(",")) {
            var id = ResourceLocation.tryParse(token.trim());
            if (id == null) continue;
            var item = BuiltInRegistries.ITEM.getOptional(id);
            if (item.isEmpty()) continue;
            Curios.give(player, new ItemStack(item.get()));
            given++;
        }
        return given;
    }

    private static TagKey<Item> tagOf(CommandContext<CommandSourceStack> context, CurioForm form) {
        return context.getArgument("grade", CurioGrade.class).tag(form);
    }

    private static int choose(ServerPlayer player, TagKey<Item> tag) {
        var options = roll(player, tag);
        CurioChoice.open(player, options, chosen -> Curios.give(player, chosen));
        return options.size();
    }

    private static List<ItemStack> roll(ServerPlayer player, TagKey<Item> tag) {
        var tagged = BuiltInRegistries.ITEM.getTag(tag);
        if (tagged.isEmpty()) return List.of();
        var items = new ArrayList<Item>();
        for (var holder : tagged.get()) items.add(holder.value());
        var random = player.getRandom();
        for (var index = items.size() - 1; index > 0; index--)
            Collections.swap(items, index, random.nextInt(index + 1));
        var stacks = new ArrayList<ItemStack>();
        for (var index = 0; index < CHOICE_OPTIONS; index++) stacks.add(new ItemStack(items.get(index % items.size())));
        return List.copyOf(stacks);
    }
}
