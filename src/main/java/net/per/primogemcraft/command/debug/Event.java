package net.per.primogemcraft.command.debug;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.per.primogemcraft.system.event.EventGroup;
import net.per.primogemcraft.system.event.EventQuota;
import net.per.primogemcraft.system.event.EventRegistry;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public class Event {
    private static final String ARGUMENT = "number";
    private static final String RICH = "rich";
    private static final String ALL = "all";
    private static final int MIN_NUMBER = 1;
    private static final double SPACING = 2.5D;

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal(MOD_ID).then(Commands.literal("event").requires(source -> source.hasPermission(2))
                .then(Commands.literal("list").executes(context -> list(context.getSource())))
                .then(Commands.literal("groups").executes(context -> groups(context.getSource())))
                .then(Commands.literal("quota").executes(context -> quota(context.getSource())))
                .then(Commands.literal("info")
                        .then(Commands.argument(ARGUMENT, IntegerArgumentType.integer(MIN_NUMBER)).executes(context -> info(context.getSource(), number(context)))))
                .then(Commands.literal("run")
                        .then(Commands.literal("random").executes(context -> runRandom(context.getSource())))
                        .then(Commands.argument(ARGUMENT, IntegerArgumentType.integer(MIN_NUMBER)).executes(context -> run(context.getSource(), number(context)))))
                .then(Commands.literal("open")
                        .then(Commands.argument(ARGUMENT, IntegerArgumentType.integer(MIN_NUMBER)).executes(context -> open(context.getSource(), number(context)))))
                .then(Commands.literal("summon")
                        .then(Commands.literal(ALL).executes(context -> summonAll(context.getSource())))
                        .then(Commands.literal(RICH)
                                .executes(context -> summonRich(context.getSource()))
                                .then(Commands.argument(ARGUMENT, IntegerArgumentType.integer(MIN_NUMBER)).executes(context -> summon(context.getSource(), number(context)))))
                        .then(Commands.argument(ARGUMENT, IntegerArgumentType.integer(MIN_NUMBER)).executes(context -> summon(context.getSource(), number(context)))))
        ));
    }

    private static int number(CommandContext<CommandSourceStack> context) {
        return IntegerArgumentType.getInteger(context, ARGUMENT);
    }

    private static int list(CommandSourceStack source) {
        var events = EventRegistry.events();
        message(source, "command.primogemcraft.event.list.header", events.size());
        for (var event : events) message(source, "command.primogemcraft.event.event", event.number(), event.title());
        return events.size();
    }

    private static int groups(CommandSourceStack source) {
        var groups = EventRegistry.groups();
        message(source, "command.primogemcraft.event.groups.header", groups.size());
        for (var group : groups) message(source, "command.primogemcraft.event.group", group.number(), group.title(), group.events().size(), group.weight());
        return groups.size();
    }

    private static int quota(CommandSourceStack source) {
        var level = source.getLevel();
        var stored = source.getEntity() instanceof ServerPlayer player ? EventQuota.playerStored(player) : 0;
        message(source, "command.primogemcraft.event.quota", EventQuota.available(level), EventQuota.limit(level), stored);
        return EventQuota.available(level);
    }

    private static int info(CommandSourceStack source, int number) {
        var event = EventRegistry.event(number);
        if (event != null) {
            message(source, "command.primogemcraft.event.info.event", number, event.title());
            message(source, "command.primogemcraft.event.info.description", event.description());
            return 1;
        }
        var group = EventRegistry.group(number);
        if (group == null) return unknown(source, number);
        message(source, "command.primogemcraft.event.info.group", number, group.title(), group.weight());
        message(source, "command.primogemcraft.event.info.cards", cards(group));
        return group.events().size();
    }

    private static int run(CommandSourceStack source, int number) throws CommandSyntaxException {
        var player = source.getPlayerOrException();
        var event = EventRegistry.event(number);
        if (event == null) return EventRegistry.isGroup(number) ? notEvent(source, number) : unknown(source, number);
        if (!EventRegistry.run(player, number)) {
            message(source, "command.primogemcraft.event.run_failed", number);
            return 0;
        }
        message(source, "command.primogemcraft.event.run", number, event.title());
        return 1;
    }

    private static int runRandom(CommandSourceStack source) throws CommandSyntaxException {
        var number = EventRegistry.randomEvent(source.getLevel().random);
        return number == 0 ? unknown(source, number) : run(source, number);
    }

    private static int open(CommandSourceStack source, int number) throws CommandSyntaxException {
        var player = source.getPlayerOrException();
        var group = EventRegistry.group(number);
        if (group == null) return EventRegistry.event(number) == null ? unknown(source, number) : notGroup(source, number);
        message(source, "command.primogemcraft.event.open", number);
        EventRegistry.trigger(player, group);
        return group.events().size();
    }

    private static int summon(CommandSourceStack source, int number) {
        var group = EventRegistry.group(number);
        if (group == null) return EventRegistry.event(number) == null ? unknown(source, number) : notGroup(source, number);
        if (EventRegistry.spawn(source.getLevel(), source.getPosition(), number) == null) {
            message(source, "command.primogemcraft.event.summon_failed", number);
            return 0;
        }
        message(source, "command.primogemcraft.event.summon", number);
        return 1;
    }

    private static int summonRich(CommandSourceStack source) {
        var group = EventRegistry.randomRichGroup(source.getLevel().random);
        if (group == null) return unknown(source, 0);
        if (EventRegistry.spawn(source.getLevel(), source.getPosition(), group.number()) == null) {
            message(source, "command.primogemcraft.event.summon_failed", group.number());
            return 0;
        }
        message(source, "command.primogemcraft.event.summon_rich", group.number(), group.title());
        return 1;
    }

    private static int summonAll(CommandSourceStack source) {
        var level = source.getLevel();
        var origin = source.getPosition();
        var groups = EventRegistry.groups();
        var columns = Mth.ceil(Math.sqrt(groups.size()));
        var rows = Mth.ceil((double) groups.size() / columns);
        var spawned = 0;
        var index = 0;
        for (var group : groups) {
            var x = origin.x + ((index % columns) - (columns - 1) / 2.0D) * SPACING;
            var z = origin.z + ((double) (index / columns) - (rows - 1) / 2.0D) * SPACING;
            if (EventRegistry.spawn(level, new Vec3(x, origin.y, z), group.number()) != null) spawned++;
            index++;
        }
        message(source, "command.primogemcraft.event.summon_all", spawned);
        return spawned;
    }

    private static Component cards(EventGroup group) {
        var content = Component.empty();
        var first = true;
        for (var number : group.events()) {
            var event = EventRegistry.event(number);
            if (event == null) continue;
            if (!first) content.append("§7, §f");
            first = false;
            content.append("§7#").append(Integer.toString(number)).append(" ").append(event.title());
        }
        return content;
    }

    private static int unknown(CommandSourceStack source, int number) {
        message(source, "command.primogemcraft.event.unknown", number);
        return 0;
    }

    private static int notEvent(CommandSourceStack source, int number) {
        message(source, "command.primogemcraft.event.not_event", number);
        return 0;
    }

    private static int notGroup(CommandSourceStack source, int number) {
        message(source, "command.primogemcraft.event.not_group", number);
        return 0;
    }

    private static void message(CommandSourceStack source, String key, Object... arguments) {
        source.sendSuccess(() -> Component.translatable(key, arguments), false);
    }
}
