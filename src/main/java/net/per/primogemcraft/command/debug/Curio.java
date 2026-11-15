package net.per.primogemcraft.command.debug;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.per.primogemcraft.system.curio.CurioChoice;
import net.per.primogemcraft.system.curio.CurioForm;
import net.per.primogemcraft.system.curio.CurioGrade;
import net.per.primogemcraft.system.curio.Curios;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Curio {
    private static final int CHOICE_OPTIONS = 3;

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("primogemcraft").then(CommandManager.literal("curio").requires(c -> c.hasPermissionLevel(2))
                .then(CommandManager.literal("random")
                        .then(CommandManager.argument("count", IntegerArgumentType.integer(1, 64)).executes(c -> {
                            var player = c.getSource().getPlayerOrThrow();
                            var count = IntegerArgumentType.getInteger(c, "count");
                            for (var index = 0; index < count; index++)
                                Curios.give(player, Curios.randomCurio(player.getRandom()));
                            return count;
                        }))
                ).then(CommandManager.literal("choose")
                        .then(CommandManager.literal("random").executes(c -> choose(c.getSource().getPlayerOrThrow(), CurioForm.ANY))
                        ).then(CommandManager.argument("grade", com.mojang.brigadier.arguments.StringArgumentType.word()).executes(c -> choose(c.getSource().getPlayerOrThrow(), tagOf(c, CurioForm.NORMAL)))
                                .then(CommandManager.literal("fusion").executes(c -> choose(c.getSource().getPlayerOrThrow(), tagOf(c, CurioForm.FUSION))))
                        )
                ).then(CommandManager.literal("give")
                        .then(CommandManager.argument("curios", StringArgumentType.greedyString()).executes(Curio::give))
                )
        ));
    }

    private static int give(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        var player = context.getSource().getPlayerOrThrow();
        var given = 0;
        for (var token : StringArgumentType.getString(context, "curios").split(",")) {
            var id = Identifier.tryParse(token.trim());
            if (id == null) continue;
            var item = Registries.ITEM.getOrEmpty(id);
            if (item.isEmpty()) continue;
            Curios.give(player, new ItemStack(item.get()));
            given++;
        }
        return given;
    }

    private static TagKey<Item> tagOf(CommandContext<ServerCommandSource> context, CurioForm form) {
        return context.getArgument("grade", CurioGrade.class).tag(form);
    }

    private static int choose(ServerPlayerEntity player, TagKey<Item> tag) {
        var options = roll(player, tag);
        CurioChoice.open(player, options, chosen -> Curios.give(player, chosen));
        return options.size();
    }

    private static List<ItemStack> roll(ServerPlayerEntity player, TagKey<Item> tag) {
        var tagged = Registries.ITEM.getEntryList(tag);
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
