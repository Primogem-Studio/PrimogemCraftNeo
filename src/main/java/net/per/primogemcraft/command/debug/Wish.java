package net.per.primogemcraft.command.debug;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.CommandManager;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.per.primogemcraft.entity.misc.WishEntity;
import net.per.primogemcraft.system.wish.WishBanner;
import net.per.primogemcraft.system.wish.WishRarity;
import net.per.primogemcraft.system.wish.WishResult;
import net.per.primogemcraft.system.wish.WishRoller;

import java.util.ArrayList;

public class Wish {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("primogemcraft").then(
                CommandManager.literal("wish").requires(c -> c.hasPermissionLevel(2)
                ).then(CommandManager.argument("count", IntegerArgumentType.integer(1)
                ).then(CommandManager.argument("banner", com.mojang.brigadier.arguments.StringArgumentType.word()
                ).executes(c -> {
                    var world = c.getSource().getWorld();
                    var player = c.getSource().getPlayerOrThrow();
                    WishEntity.spawnRing(world, player, WishRoller.roll(player, c.getArgument("banner", WishBanner.class), 0, IntegerArgumentType.getInteger(c, "count")));
                    return 0;
                }).then(CommandManager.argument("rarity", com.mojang.brigadier.arguments.StringArgumentType.word()
                ).then(CommandManager.argument("radiance", BoolArgumentType.bool()
                ).then(CommandManager.argument("colorful", BoolArgumentType.bool()
                ).executes(c -> {
                    var world = c.getSource().getWorld();
                    var player = c.getSource().getPlayerOrThrow();
                    var count = IntegerArgumentType.getInteger(c, "count");
                    var banner = c.getArgument("banner", WishBanner.class);
                    var rarity = c.getArgument("rarity", WishRarity.class);
                    var radiance = BoolArgumentType.getBool(c, "radiance");
                    var colorful = BoolArgumentType.getBool(c, "colorful");
                    var result = new ArrayList<WishResult>();
                    for (var i = 0; i < count; i++) result.add(new WishResult(banner, rarity, radiance, colorful));
                    WishEntity.spawnRing(world, player, result);
                    return 0;
                }))))))
        ));
    }
}
