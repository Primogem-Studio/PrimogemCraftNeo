package net.per.primogemcraft.command.debug;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.neoforged.neoforge.server.command.EnumArgument;
import net.per.primogemcraft.entity.misc.WishEntity;
import net.per.primogemcraft.system.wish.WishBanner;
import net.per.primogemcraft.system.wish.WishRarity;
import net.per.primogemcraft.system.wish.WishResult;
import net.per.primogemcraft.system.wish.WishRoller;

import java.util.ArrayList;

public class Wish {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("primogemcraft").then(
                Commands.literal("wish").requires(c -> c.hasPermission(2)
                ).then(Commands.argument("count", IntegerArgumentType.integer(1)
                ).then(Commands.argument("banner", EnumArgument.enumArgument(WishBanner.class)
                ).executes(c -> {
                    var level = c.getSource().getLevel();
                    var player = c.getSource().getPlayerOrException();
                    WishEntity.spawnRing(level, player, WishRoller.roll(player, c.getArgument("banner", WishBanner.class), 0, IntegerArgumentType.getInteger(c, "count")));
                    return 0;
                }).then(Commands.argument("rarity", EnumArgument.enumArgument(WishRarity.class)
                ).then(Commands.argument("radiance", BoolArgumentType.bool()
                ).then(Commands.argument("colorful", BoolArgumentType.bool()
                ).executes(c -> {
                    var level = c.getSource().getLevel();
                    var player = c.getSource().getPlayerOrException();
                    var count = IntegerArgumentType.getInteger(c, "count");
                    var banner = c.getArgument("banner", WishBanner.class);
                    var rarity = c.getArgument("rarity", WishRarity.class);
                    var radiance = BoolArgumentType.getBool(c, "radiance");
                    var colorful = BoolArgumentType.getBool(c, "colorful");
                    var result = new ArrayList<WishResult>();
                    for (var i = 0; i < count; i++) result.add(new WishResult(banner, rarity, radiance, colorful));
                    WishEntity.spawnRing(level, player, result);
                    return 0;
                }))))))
        ));
    }
}
