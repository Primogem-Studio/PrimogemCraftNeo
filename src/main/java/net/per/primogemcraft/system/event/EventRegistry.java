package net.per.primogemcraft.system.event;

import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.math.Vec3d;
import net.per.primogemcraft.PrimogemCraft;
import net.per.primogemcraft.config.PGCConfig;
import net.per.primogemcraft.entity.misc.RandomEventEntity;
import net.per.primogemcraft.registry.PGCEntities;
import net.per.primogemcraft.util.PGCTimer;

import java.util.*;
import java.util.function.Supplier;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public final class EventRegistry {
    public static final Identifier DEFAULT_TEXTURE = entityTexture("event1");

    private static final String DROP_GATE = "event/drop";
    private static final int NO_NUMBER = 0;

    private static final Map<Integer, RandomEvent> EVENTS = new LinkedHashMap<>();
    private static final Map<Integer, EventGroup> GROUPS = new LinkedHashMap<>();
    private static final List<Integer> RICH_GROUPS = new ArrayList<>();
    private static int nextNumber = 1;
    private static boolean loaded;

    private EventRegistry() {
    }

    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }

    public static Identifier entityTexture(String name) {
        return id("textures/entity/random_event/" + name + ".png");
    }

    public static RandomEvent register(EventAction action) {
        return register(RandomEvent.of(action));
    }

    public static RandomEvent register(Text title, Text description, EventAction action) {
        return register(RandomEvent.of(title, description, action));
    }

    public static RandomEvent register(Text title, Text description, EventAction action, EventCondition condition) {
        return register(RandomEvent.of(title, description, action, condition));
    }

    public static RandomEvent register(Text title, Text description, EventAction action, EventCondition condition, boolean forced) {
        return register(RandomEvent.of(title, description, action, condition, forced));
    }

    public static RandomEvent register(RandomEvent event) {
        load();
        if (event.number() != NO_NUMBER) throw new IllegalArgumentException("Random event " + event.number() + " is already registered");
        var numbered = new RandomEvent(nextNumber++, event.title(), event.description(), event.action(), event.condition(), event.forced());
        EVENTS.put(numbered.number(), numbered);
        return numbered;
    }

    public static EventGroup registerGroup(EventGroup group) {
        load();
        if (group.number() != NO_NUMBER) throw new IllegalArgumentException("Event group " + group.number() + " is already registered");
        var numbered = new EventGroup(nextNumber++, group.title(), group.texture(), group.weight(), group.provider());
        GROUPS.put(numbered.number(), numbered);
        return numbered;
    }

    public static EventGroup registerGroup(Text title, Identifier texture, int weight, Supplier<List<Integer>> provider) {
        return registerGroup(EventGroup.dynamic(title, texture, weight, provider));
    }

    public static EventGroup registerRichGroup(EventGroup group) {
        var registered = registerGroup(group);
        RICH_GROUPS.add(registered.number());
        return registered;
    }

    public static RandomEvent event(int number) {
        load();
        return EVENTS.get(number);
    }

    public static EventGroup group(int number) {
        load();
        return GROUPS.get(number);
    }

    public static Collection<RandomEvent> events() {
        load();
        return EVENTS.values();
    }

    public static Collection<EventGroup> groups() {
        load();
        return GROUPS.values();
    }

    public static boolean isGroup(int number) {
        load();
        return GROUPS.containsKey(number);
    }

    public static int randomEvent(Random random) {
        load();
        var numbers = new ArrayList<>(EVENTS.keySet());
        return numbers.isEmpty() ? NO_NUMBER : numbers.get(random.nextInt(numbers.size()));
    }

    public static EventGroup weightedGroup(Random random) {
        load();
        var all = new ArrayList<>(GROUPS.values());
        var total = 0;
        for (var group : all) total += group.weight();
        if (total <= 0) return null;
        var roll = MathHelper.nextInt(random, 1, total);
        var current = 0;
        for (var group : all) {
            current += group.weight();
            if (roll <= current) return group;
        }
        return all.get(all.size() - 1);
    }

    public static EventGroup randomRichGroup(Random random) {
        load();
        if (RICH_GROUPS.isEmpty()) return weightedGroup(random);
        return GROUPS.get(RICH_GROUPS.get(random.nextInt(RICH_GROUPS.size())));
    }

    public static boolean run(ServerPlayerEntity player, int number) {
        var event = event(number);
        if (event == null) return false;
        try {
            return event.action().run(EventContext.of(player, event));
        } catch (RuntimeException exception) {
            PrimogemCraft.LOGGER.error("Random event {} failed", number, exception);
            return false;
        }
    }

    public static void trigger(ServerPlayerEntity player, EventGroup group) {
        if (group == null || group.isEmpty()) return;
        EventChoice.open(player, group);
    }

    public static void trigger(ServerPlayerEntity player, int number) {
        trigger(player, group(number));
    }

    public static RandomEventEntity spawn(ServerWorld level, Vec3d position, int groupNumber) {
        var group = group(groupNumber);
        if (group == null || group.isEmpty()) return null;
        var entity = PGCEntities.RANDOM_EVENT.get().create(level);
        if (entity == null) return null;
        entity.setGroup(groupNumber);
        entity.refreshPositionAndAngles(position.getX(), position.getY(), position.getZ(), level.random.nextFloat() * 360.0F, 0.0F);
        level.spawnEntity(entity);
        return entity;
    }

    public static RandomEventEntity drop(ServerWorld level, Vec3d position, ServerPlayerEntity player) {
        var cooldown = PGCConfig.EVENT_DROP_COOLDOWN.get();
        if (!PGCTimer.isDone(player, DROP_GATE)) return null;
        PGCTimer.set(player, DROP_GATE, cooldown);
        if (!EventQuota.roll(level, player)) return null;
        var group = weightedGroup(level.random);
        return group == null ? null : spawn(level, position, group.number());
    }

    private static void load() {
        if (loaded) return;
        loaded = true;
        RandomEvents.registerAll();
    }
}
