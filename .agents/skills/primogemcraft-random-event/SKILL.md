---
name: primogemcraft-random-event
description: Use when working on the random-event system in the PrimogemCraftNeo repository — registering a new random event or event group, writing an EventAction, adding a helper to EventContext, changing the drop chance, world or player event quota, event chains, timed combat challenges, the RandomEventEntity that players right-click, its texture or renderer, the event lang keys, or the /primogemcraft event debug commands. Covers the two registries, the numbered-event/group model, the drop-to-choice-to-action lifecycle, the screen contract an event action must respect, and the recurring traps.
---

# Random events

A random event is a piece of gameplay that is offered to one player as a card and only then happens. The world offers it as an entity the player right-clicks; the player picks a card out of a group; the picked card runs an action. Everything an event may do to the world goes through one `EventContext`.

An **event** is a card: a number, a title, a description, an action and an optional condition. A **group** is a hand of cards: a number, a title, a weight and a lazy list of event numbers. Nothing else exists — there is no category, no rarity, no per-event weight, and the condition is the only thing that can make a card un-clickable before it is picked.

| Piece | Path | Side |
|---|---|---|
| `EventRegistry` — the two registries, the weighted roll, run/trigger/spawn/drop | `src/main/java/net/per/primogemcraft/event/EventRegistry.java` | common |
| `RandomEvent`, `EventGroup`, `EventAction`, `EventCondition`, `EventContext` | `src/main/java/net/per/primogemcraft/event/` | common |
| `RandomEvents` — every shipped event and group, and `registerAll()` | `src/main/java/net/per/primogemcraft/event/RandomEvents.java` | common |
| `EventChoice` — turns a group into the card screen | `src/main/java/net/per/primogemcraft/event/EventChoice.java` | common |
| `EventQuota`, `EventChain`, `EventCombat`, `EventLoot` | `src/main/java/net/per/primogemcraft/event/` | common |
| `EventEvents` — the NeoForge hooks | `src/main/java/net/per/primogemcraft/event/EventEvents.java` | common |
| `RandomEventEntity` | `src/main/java/net/per/primogemcraft/entity/misc/RandomEventEntity.java` | common |
| `RandomEventRenderer` | `src/main/java/net/per/primogemcraft/render/entity/RandomEventRenderer.java` | client only |
| `textures/entity/random_event/event1..event7.png` | `src/main/resources/assets/primogemcraft/textures/entity/random_event/` | assets |
| `events/abundance_blight_reward.json` | `src/main/resources/data/primogemcraft/loot_table/events/` | data |
| `/primogemcraft event …` | `src/main/java/net/per/primogemcraft/command/debug/Event.java` | common |

The whole `event` package is common code. Only `RandomEventRenderer` is client-only; the event itself is never a client concern.

## Two registries, one counter

```java
public record RandomEvent(int number, Component title, Component description, EventAction action, EventCondition condition)
public record EventGroup(int number, Component title, ResourceLocation texture, int weight, Supplier<List<Integer>> provider)
```

`EventRegistry` holds `Map<Integer, RandomEvent> EVENTS`, `Map<Integer, EventGroup> GROUPS`, a list of rich-group numbers, and **one** `nextNumber` counter shared by both maps. Registration assigns `nextNumber++`, so an event and a group never share a number, and the numbers depend on registration order alone.

| Method | Meaning |
|---|---|
| `register(EventAction)` / `register(Component, Component, EventAction)` / `register(Component, Component, EventAction, EventCondition)` / `register(RandomEvent)` | Adds an event, returns the numbered copy. |
| `registerGroup(EventGroup)` / `registerGroup(Component, ResourceLocation, int, Supplier)` | Adds an ordinary group. |
| `registerRichGroup(EventGroup)` | Adds a group and also lists it in `RICH_GROUPS`. |
| `event(int)` / `group(int)` | Lookup, `null` when unknown. |
| `events()` / `groups()` | Every registered entry, in registration order. |
| `isGroup(int)` | Whether the number belongs to a group. |
| `randomEvent(RandomSource)` | Uniformly one event number, or `0` when none exist. |
| `weightedGroup(RandomSource)` | One group, probability proportional to `weight`. `null` when every weight is 0 or nothing is registered. |
| `randomRichGroup(RandomSource)` | Uniformly one of the rich groups, falling back to `weightedGroup`. |
| `run(ServerPlayer, int)` | Runs one event and returns its result; `false` on unknown number, `false` action, or a thrown `RuntimeException` (logged as `Random event {} failed`). |
| `trigger(ServerPlayer, EventGroup)` / `trigger(ServerPlayer, int)` | Opens the card screen for that group. No-op for `null` or empty groups. |
| `spawn(ServerLevel, Vec3, int)` | Creates the right-clickable entity for a group number. |
| `drop(ServerLevel, Vec3, ServerPlayer)` | The whole drop attempt: cooldown, quota, weighted group, spawn. |
| `id(String)` / `entityTexture(String)` | `primogemcraft:<path>` and `textures/entity/random_event/<name>.png`. |

Every read path calls a private `load()` that runs `RandomEvents.registerAll()` once. Registration is **lazy on first access**, not static-init and not a mod-constructor call, so `EventRegistry.register` may safely be called from anywhere in the mod as long as it happens before the first read — and `load()` is re-entrancy safe because it sets the flag before registering.

The `of`/`dynamic` factories all pass `number = 0`, which is the value `register` treats as "unassigned". `register` throws `IllegalArgumentException` only when a record already carries a non-zero number, which is why the factories exist.

```java
EventGroup.of(RandomEvent...)                                      // weight 1, no texture, empty title
EventGroup.of(Component title, RandomEvent...)
EventGroup.of(Component title, int weight, RandomEvent...)
EventGroup.of(Component title, ResourceLocation texture, RandomEvent...)
EventGroup.of(Component title, ResourceLocation texture, int weight, RandomEvent...)
EventGroup.dynamic(Component title, int weight, Supplier<List<Integer>> provider)
EventGroup.dynamic(Component title, ResourceLocation texture, int weight, Supplier<List<Integer>> provider)
```

The `number`-based factories read `RandomEvent::number` through a `Supplier`, so the group evaluates the numbers only when `events()` or `isEmpty()` is called. That makes a group safe to build before its events exist, and it is what lets `GroupRandomTriple` and the combat reward groups roll their cards freshly every time the screen opens.

`weight` is clamped to at least 1 in the record's compact constructor, so a weight of 0 still fires. `textureOrDefault()` returns `EventRegistry.DEFAULT_TEXTURE` (`event1`) when the group has no texture.

`EventAction` is a `@FunctionalInterface`:

```java
public interface EventAction {
    boolean run(EventContext context);
}
```

`EventCondition` is its side-effect-free twin, and the only thing that decides whether a card can be clicked at all. It answers twice: `met` decides the card, and `unmet` names the requirements the player is missing, which is what the screen prints under `条件不足`:

```java
public interface EventCondition {
    boolean met(EventContext context);

    List<Component> unmet(EventContext context);

    static EventCondition all(EventCondition... conditions);
    static EventCondition fragments(int amount);
    static EventCondition health(double ratio);
    static EventCondition enchantTargets();
    static EventCondition curios(TagKey<Item> tag);
    static EventCondition chain();
}
```

- Conditions come from those factories, never from a bare lambda — a lambda cannot carry the label its unmet line needs. Each factory's label is one lang key, `gui.primogemcraft.event.requirement.<fragments|health|enchant_targets|curios|chain>`, and `all(...)` concatenates the labels of every part that fails, in order.
- A factory is a faithful reading half of one `EventContext` check: `fragments(amount)` is `hasFragments(amount)`, `health(ratio)` is `hasHealth(ratio)`, `enchantTargets()` is `hasEnchantTargets()`, `curios(tag)` is `hasCurios(tag)`, `chain()` is `chainReady()`. Add a factory beside the check it mirrors, never a condition that is merely similar to it.
- `RandomEvent.condition()` is `null` for an event that is always available, and `RandomEvent.conditional()` is the null check. `RandomEvent.available(context)` is `condition == null || condition.met(context)`, and `RandomEvent.unmet(context)` is `List.of()` when there is no condition.
- `RandomEvent.forced()` is the second flag, and it only matters to the screen: a forced event is one the player must take, so its group never gets the appended `leave` card. `RandomEvents` sets it through `registerForced(...)` for the three events that hand out a negative curio outright — `curio/negative`, `curio/clock` (咕咕钟) and `curio/negative_cf` (失败处方).
- A condition must be **pure**: it reads, it never pays, never prints and never consumes. A gate expressed as a condition therefore uses the `chain()` factory (over `chainReady`), not `gate(name, ticks)` — see the action API below.
- A condition is not the action's own check; it is a copy of it. Keep the two in step: `enchantHealthLow` pays `health(0.2)` and is conditioned on `health(0.2)`, `lotteryPayShort` pays `fragments(20)` and is conditioned on `fragments(20)`. A card whose condition is unmet is un-clickable, and the player never spends anything on it.
- A condition cannot describe everything an action may refuse — a mob that will not spawn, a screen that will not open. Those failures are caught after the pick by the refusal path (see the lifecycle below), not by the condition.

## The action API

`EventContext` is the only interface an action should need. It is built by `EventContext.of(player, event)` and captures `player.serverLevel()` and `player.getRandom()` once.

| Member | Behaviour |
|---|---|
| `player()`, `level()`, `event()`, `random()` | The context's subjects. |
| `chance(double p)` | `random.nextDouble() < p`. |
| `range(int min, int max)` | `Mth.nextInt`, inclusive. |
| `gate(String name, int ticks)` | Consumes `PGCTimer`'s named timer: `true` and re-arms when it was done, `false` otherwise. A general-purpose cooldown for actions. |
| `ready(String name)` | The same check without consuming or re-arming: whether that timer is done. This is what a condition calls. |
| `chainReady()` | `ready` on the `event/chain` gate, for the condition of `randomEvents`. |
| `prompt(Component)` / `prompt(Component, boolean actionBar)` | Sends a client message. Always returns `true`. |
| `give(ItemStack)` | `OtherworldBankbook.give` — a fragment stack goes into the player's auto-pickup bankbooks and only the remainder reaches the inventory; any other stack is handed over like `Curios.give`, dropping what does not fit on the ground. Always `true`. |
| `deny()` | Prints `message.primogemcraft.event.deny` (rate-limited by the `event/deny` gate, 100 ticks) and returns `false`. **The only correct way to say "cannot afford this".** |
| `fragments(int amount)` | Requires and consumes `PGCItems.COSMIC_FRAGMENT`; `deny()` when short. Pays through `OtherworldBankbook.payFragments`, so a fragment stack stored in the Otherworld Bankbook pays too — see the `primogemcraft-fragment-payment` skill before touching either half. |
| `hasFragments(int amount)` | The same count without consuming — the reading half of `fragments`, and the condition half of every fragment-cost event. It is `OtherworldBankbook.canPayFragments`, not an inventory count. |
| `hasCurios(TagKey<Item> tag)` | Whether that item tag resolves to at least one entry, i.e. whether `curios(tag, …)` has anything to roll. |
| `hasEnchantTargets()` | Whether the player owns anything `EnchantChoice` can enchant — an enchantable curio from the inventory, or a piece of armor. Behind the `enchantTargets()` condition, and the reason a player with nothing to enchant sees those cards as `条件不足` instead of opening an empty screen. |
| `hasHealth(double ratio)` | Whether current health is at least `maxHealth * ratio`. |
| `health(double ratio)` | Same check, then hurts with generic damage for `maxHealth * ratio`. Can kill. |
| `enchant(EnchantGrade)` | Opens `EnchantChoice.open(player, grade)`. |
| `enchant(EnchantGrade, int fragments)` | Charges the fragments first, then opens. |
| `curios(TagKey<Item> tag, int count)` | Rolls up to `count` distinct curios from a `c` item tag (20 attempts) and opens `CurioChoice` titled with the event's own title, hinting with `gui.primogemcraft.event.hint`, granting through `Curios.give`. `deny()` when the tag yields nothing. |
| `lootTable(ResourceLocation table, int count)` | Rolls a loot table through `EventLoot.roll`, dedups with `ChoiceSupport.distinct`, and offers the results under `gui.primogemcraft.event.loot`. |
| `choose(List<ItemStack> options, Component title)` | A plain `ChoiceRegistry` pick over up to N stacks: `ITEM_MODEL`, `ChoiceSupport.DEFAULT_CARDS`, `FASTEST` spin, one turn. |
| `group(EventGroup)` / `group(int groupNumber)` | Opens another card screen. `deny()` for `null` or empty. |
| `worldQuota(int)` | Adds to the world's available storage, overflowing into the player's own storage when the world is already at its limit, then reports `message.primogemcraft.event.quota.world`. |
| `playerQuota(int)` | Adds to the player's own storage and reports `.quota.player`. |
| `worldLimit(int)` | Changes the world limit, reports `.quota.limit` to the player, and broadcasts `.quota.plentiful` / `.quota.scarce` to everyone. |
| `randomEvents(int count)` | Gate `event/chain` over 20 ticks, roll `count` event numbers, hand them to `EventChain.events`. |

Two composition rules follow from the `boolean` return:

- **Chain with `&&`.** `context.health(0.2D) && context.enchant(EnchantGrade.LOW)` means "pay, then act, and stop if the payment failed". Every shipped multi-step action is written this way.
- **`false` means "nothing happened", and the screen is the only thing that reads it.** `EventChoice` turns it into a refusal that keeps the card screen open; `EventChain` and every other caller ignore it. Do not write an action whose `false` is ambiguous between "acted but quietly" and "did nothing at all".

`EventContext.choose` and `EventContext.curios` grant the result themselves in the `ChoiceRegistry` callback; `lootTable` and `choose` are thin wrappers over it. A caller that wants different art or a different grant writes its own `ChoiceRegistry.open` call — see the `primogemcraft-choice-screen` skill for that contract.

## The shipped content

`RandomEvents.registerAll()` is the single body that registers everything, in this order: single events first (leave, enchant, curio, combat, trash, fragments, quota, supply, chain, code, lottery), then `registerGroups()` for the hands. That order is what decides the numbering.

```java
private static RandomEvent register(String path, EventAction action) {
    return EventRegistry.register(text(path, "title"), text(path, "description"), action);
}

private static RandomEvent register(String path, EventAction action, EventCondition condition) {
    return EventRegistry.register(text(path, "title"), text(path, "description"), action, condition);
}

private static RandomEvent registerForced(String path, EventAction action, EventCondition condition) {
    return EventRegistry.register(text(path, "title"), text(path, "description"), action, condition, true);
}

private static Component text(String path, String suffix) {
    return Component.translatable("event.primogemcraft." + path.replace('/', '.') + "." + suffix);
}
```

So a path is also the lang-key fragment: `"combat/zombies_reward"` becomes `event.primogemcraft.combat.zombies_reward.title` and `.description`, and the same helper builds `.line`, `.name` and `.group` keys for the actions that need them.

Groups are registered with `EventRegistry.registerGroup(...)` and `EventRegistry.registerRichGroup(...)`, titled with `text(GROUP_…, "title")` and given one of `EVENT_3_TEXTURE` … `EVENT_7_TEXTURE` or nothing.

Conditions are declared in the same call, after the action, through the `EventCondition` factories and only for the shipped events that can actually fail: every `enchant(...)` event (`enchantTargets`, plus its own price), every `curios` roll (`curios`), `chain/random_ten` (`chain`) and the two lottery payments. `leave` and everything that only gives, prompts or spawns has none. A group whose events are **all** conditional is a group whose every card can be un-clickable at once, so `EventChoice` appends the `leave` card to it — unless one of those events is **forced**, which is what `registerForced` marks: see below.

## The lifecycle

```
LivingDeathEvent (non-player killed by a ServerPlayer)
    └─ EventEvents.onLivingDeath
         ├─ EventCombat.killed(killer, dead)      // loot drops for the killer, challenge progress
         └─ EventRegistry.drop(level, pos, killer)
              ├─ PGCTimer gate "event/drop" over EVENT_DROP_COOLDOWN   (per player)
              ├─ EventQuota.roll(level, player)                        (refill, chance, storage)
              ├─ weightedGroup(level.random)
              └─ spawn(level, pos, groupNumber)  →  RandomEventEntity

player right-clicks the entity
    └─ RandomEventEntity.interact
         ├─ entity discarded
         └─ EventRegistry.trigger(player, group)
              └─ EventChoice.open  →  ChoiceRegistry screen (CONFIRM)

player picks a clickable card (or presses ESC, or lets the timer run out — both
pick only among the clickable cards, then the "leave" card, then -1 = gave up)
    └─ ChoiceSelectPayload  →  ChoiceRegistry.respond
         └─ EventChoice's callback runs EventRegistry.run(player, number)
              ├─ true  → ChoiceResultPayload(accepted) → the screen closes
              └─ false → ChoiceResultPayload(refused)  → the screen stays,
                         that card turns un-clickable and the player tries another
```

The four hooks live in `EventEvents`, one `@EventBusSubscriber` for the package: `LivingDeathEvent` for the drop, `ServerTickEvent.Post` for `EventCombat.tick` and `EventChain.tick`, and `PlayerLoggedOutEvent` for `EventChain.clear`.

`EventChoice.open` builds one card per event number in the group, all sharing the group's title as the screen heading, the `event_question.png` icon as `ChoiceVisual.SCENARIO_TEXTURE`, and `ChoiceSupport.EVENT_CARDS`; each card's text is the event's title/description with `withTextTooltip()`. The item on the card is empty, so the text is the only content and those cards stay silent (a card with an item chimes on landing).

Both numbers are on screen for debugging: the subtitle is `gui.primogemcraft.event.group_id` (the group number, then the wrapped `gui.primogemcraft.event.hint`), and every card carries `gui.primogemcraft.event.card_id` as its `withBadge(...)`, drawn at the bottom of the card face by `ChoiceScreen`. A group built inline and never passed to `registerGroup` — the combat-reward and lottery groups — has number `0`, so `Event #0`/`事件组 #0` there means "not in the registry", not a real entry.

Three things make the event screen different from every other `ChoiceRegistry` caller:

- It asks each event for its **availability** at open time and marks the card `withEnabled(event.available(context))` plus `withUnmet(event.unmet(context))`, so a card whose condition the player does not meet is drawn dimmed, cannot be clicked, numbered into, or picked by ESC or the timer, and its tooltip lists one line per unmet requirement under `gui.primogemcraft.choice.unavailable`. Nothing is spent on it. A card the server refuses after the pick has no unmet lines and falls back to the single header line.
- It runs in `ChoiceMode.CONFIRM`, so the pick is a request: the callback returns `EventRegistry.run(player, numbers.get(index))`, and a `false` there keeps the screen and the request alive instead of closing on an event that did nothing.
- When **every** event in the group is conditional and none of them is forced — no card is guaranteed clickable, and the group is allowed to offer a way out — it appends one more card for the shipped `leave` event (`RandomEvents.leaveEvent()`), adds its number to the same index list, and passes its index as `leaveIndex`. That card is the way out the forced picks fall back to. A group that already contains `leave`, that has at least one unconditional event, or whose events are forced, gets nothing appended.
- A **forced** group is the exception: the three negative-curio groups (`group/curio/negative`, `group/curio/clock`, `group/curio/negative_cf`) exist to make the player take a bad curio, so no `leave` card is offered there. If every card of such a group is un-clickable anyway, the forced picks fall through to `leaveIndex` — which is `NO_LEAVE` — and the client answers with `-1` instead of an index, which the server reads as "gave up" and closes the screen.

## Quota

`EventQuota` is a `SavedData` named `primogemcraft_event_quota` on the overworld's data storage, so it is per-world and survives restarts. It has two integers, `limit` and `available`, and the player's own storage is a `PlayerFlags` counter at `primogemcraft:event/quota`.

`roll(level, player)` is the drop gate, in this order:

1. `refill(level, player)` — gate `event/refill` for `EVENT_RECOVERY_TICKS`. If the player's own storage is 0, it gets +1; otherwise the world gets +1 available. So recovery favours a player who is empty.
2. `level.random.nextDouble() >= EVENT_DROP_CHANCE / 100` → no drop. The config is a percentage 0..100.
3. Player storage > 0 → decrement it and allow.
4. World `available <= 0` → no drop. Otherwise decrement and allow.

`addAvailable` clamps to `[0, limit]`; `addLimit` takes `Math.max(0, …)` and lowers `available` to fit. The `quota/*/up` and `quota/*/down` events are the only in-game way to move these numbers, and `EventContext.worldQuota` is the only place the overflow into player storage happens.

## Chains

`EventChain` is a static `Map<UUID, Deque<Step>>`. `Step` is a `(boolean group, int number)` pair, so `EventChain.events` runs the numbers as events and `EventChain.groups` opens them as groups.

`tick` walks every queued chain each server tick and calls `advance`, which **polls at most one step and only when `ChoiceRegistry.isPending(player)` is false**. A step that opens a choice therefore suspends the whole chain until the player answers. A player who logs out loses the queue.

The shipped `chain/random_ten` event is the only producer: `context.randomEvents(10)`.

## Combat

`EventCombat` is the library every combat event action uses, and it is the only place mob spawning, marking and challenge bookkeeping live.

| Method | Behaviour |
|---|---|
| `summon(context, type, count, modifier)` | Spawns without marking and without a challenge. |
| `guard(context, type, count, modifier)` | Spawns, marks every mob as the player's targeted guard, no challenge. |
| `challenge(context, type, count, required, modifier, completion)` | Guard-spawns and registers a timed challenge that runs `completion` when enough targets die. |
| `loot(context, type, count, table)` | Spawns, and registers a loot table to be offered to whoever kills each mob. |
| `killed(killer, dead)` | Called from `LivingDeathEvent`. Offers any pending loot, then advances the killer's challenge. |
| `tick(server)` | Expires challenges past `EVENT_CHALLENGE_TICKS`, printing `message.primogemcraft.event.challenge_timeout` on the action bar. |

Marking gives `GLOWING` and `FIRE_RESISTANCE` for `EVENT_CHALLENGE_TICKS` and sets the mob's target. Spawn positions come from `spawnPosition`: a ring of radius 10 around the player, angle and `sqrt`-ramped distance from `level.random`, then a vertical scan of ±8 blocks for a spot with two air blocks above a solid one, falling back to the player's own position.

Kill progress is counted per killer: `EventCombat.CHALLENGES` holds **one** challenge per player UUID, keyed by `Set<UUID>` of targets. Loot drops are keyed per mob UUID in `LOOT_DROPS` and removed the moment the mob dies.

## The entity and its renderer

`RandomEventEntity` is a plain `Entity` (not a `Mob`), registered as `PGCEntities.RANDOM_EVENT` / `primogemcraft:random_event` under `MobCategory.MISC`. It carries only one piece of state, the group number, in a synced `EntityDataAccessor<Integer>`. On the server it discards itself once `tickCount` passes `PGCConfig.EVENT_ENTITY_LIFETIME`, and it falls with a simple gravity/friction/bounce integrator that keeps the velocity it is given.

`isPickable()` is true with a pick radius of 1.5, `isAttackable()` and `isInvulnerableTo()` make it indestructible, and `ignoreExplosion()` makes it explosion-proof. `interact` discards the entity **before** triggering the group, on the server side only, and returns `SUCCESS` on both sides.

`RandomEventRenderer` ignores the vanilla item/entity pipeline and draws a hand-built two-sided panel with four rim quads, full bright, tinted white, at a scale of ~2.4×3.0, bobbing and spinning on `tickCount + partialTick` with a per-entity `bobOffs` phase. It has no shadow. `getTextureLocation` is `entity.group().textureOrDefault()`.

## Lang key contract

Every player-visible string is in `assets/primogemcraft/lang/zh_cn.json` and `en_us.json`. An entry in one file without the other is a bug.

| Key pattern | Use |
|---|---|
| `event.primogemcraft.<path with '.' instead of '/'>.title` | An event's card title. |
| `event.primogemcraft.<…>.description` | An event's card body. Newlines inside the value are the multi-line text the card renders. |
| `event.primogemcraft.<…>.line`, `.line_alt`, `.name`, `.group` | Extra text some actions print or use as a title. |
| `event.primogemcraft.group.<…>.title` | A group's heading, which is the screen title. |
| `gui.primogemcraft.event.hint` | Subtitle of every event card screen and of the picks an event opens (warns that closing it picks at random). It takes no argument — the event screen prepends `group_id` to it. |
| `gui.primogemcraft.event.group_id` | The event screen's `事件组 #%s`, prefixed to the hint. |
| `gui.primogemcraft.event.card_id` | A card's `事件 #%s`, the badge `EventChoice` draws at the bottom of the card face. |
| `gui.primogemcraft.event.requirement.<fragments\|health\|enchant_targets\|curios\|chain>` | One unmet requirement, as listed under `gui.primogemcraft.choice.unavailable`. |
| `gui.primogemcraft.event.loot` | Title of the reward pick opened by `EventContext.lootTable`. |
| `message.primogemcraft.event.deny` | `deny()`'s message. |
| `message.primogemcraft.event.target_killed` | Action-bar line on each challenge kill. |
| `message.primogemcraft.event.challenge_timeout` | Action-bar line when a challenge expires. |
| `message.primogemcraft.event.quota.*` | `world`, `player`, `overflow`, `limit`, `plentiful`, `scarce`. |
| `command.primogemcraft.event.*` | Every debug-command reply. |
| `entity.primogemcraft.random_event` | The entity's display name. |
| `config.primogemcraft.category.event`, `primogemcraft.configuration.event_*` (+ `.tooltip`) | The config screen. |

## Config

Six values in `PGCConfig`, under the `config.primogemcraft.category.event` category:

| Constant | Spec key | Default | Range |
|---|---|---|---|
| `EVENT_DROP_CHANCE` | `event_drop_chance` | 1 (%) | 0..100 |
| `EVENT_WORLD_LIMIT` | `event_world_limit` | 5 | 0..1000 |
| `EVENT_RECOVERY_TICKS` | `event_recovery_ticks` | 6000 | 20..240000 |
| `EVENT_ENTITY_LIFETIME` | `event_entity_lifetime` | 6000 | 100..240000 |
| `EVENT_DROP_COOLDOWN` | `event_drop_cooldown` | 20 | 0..1200 |
| `EVENT_CHALLENGE_TICKS` | `event_challenge_ticks` | 3600 | 100..240000 |

`EVENT_CHALLENGE_TICKS` is used both as the challenge deadline and as the duration of the marker effects, so changing it changes both.

## Debug commands

All of `/primogemcraft event …` requires permission level 2.

| Command | Effect |
|---|---|
| `list` | Every event with its number and title. |
| `groups` | Every group with its number, title, card count and weight. |
| `quota` | World `available`/`limit` and the caller's own stored count. |
| `info <number>` | An event's title and description, or a group's title, weight and resolved card list. |
| `run random` | Runs a uniformly random event on the caller. |
| `run <number>` | Runs one event. Refuses a group number. |
| `open <number>` | Opens a group's card screen. Refuses an event number. |
| `summon <number>` | Spawns the entity for a group number at the caller's position. |
| `summon rich` / `summon rich <number>` | Spawns a uniformly random rich group, or one named rich group. |
| `summon all` | Lays out one entity per group in a square grid of 2.5-block spacing. |

## Adding an event

1. Add a `private static final String PATH = "…"` constant next to the others in `RandomEvents`, using the same slash-grouped shape as its neighbours.
2. Add the matching `private static RandomEvent` field, or register it without keeping a field if no group needs to name it.
3. Register it inside `registerAll()` with a lambda over `EventContext`. Write the whole action as one `&&` chain: costs first, effects last.
4. If the action can return `false` — a price it cannot pay, a gate, a tag with nothing in it, nothing enchantable in the inventory — pass an `EventCondition` built from its factories (`EventCondition.all(EventCondition.fragments(20), EventCondition.enchantTargets())`), each part a pure copy of one check the action makes. Leave it out only when the action has no way to fail, and add a factory beside `EventContext` when the check you need has none.
5. Add both lang keys, `event.primogemcraft.<path>.title` and `.description`, to `zh_cn.json` and `en_us.json`.
6. Put it in at least one group in `registerGroups()` — an event that belongs to no group can only be reached by `run <number>`.
7. If it needs a new capability, add one method to `EventContext` rather than reaching for `player`/`level` in the action; that is what keeps every action a one-liner, and a reading variant of it (`ready` beside `gate`, `hasFragments` beside `fragments`) is what its condition needs.
8. If it is a combat event, express it through `EventCombat` rather than spawning entities directly.
9. If it hands out something outright bad and is meant to be unavoidable, register it with `registerForced` instead of `register`, so its group offers no `leave` card.

## Adding a group

1. Add the constant and the `EventGroup` (ordinary or rich) in `registerGroups()`.
2. Give it a title key `event.primogemcraft.group.<path>.title` in both lang files. A group left with `Component.empty()` shows a blank screen heading.
3. Pick a texture with `EventRegistry.entityTexture("event<n>")`, or accept the `event1` default.
4. Decide the weight. It only matters for `weightedGroup`, i.e. the natural drop path; `summon rich` ignores it and rolls uniformly over the rich groups.
5. Use `registerRichGroup` only when the group should be reachable by `summon rich` as well as by the weighted roll.

## Traps

- **One counter for events and groups.** Inserting a new event in the middle of `registerAll()` renumbers every later event *and* every later group. Nothing persisted depends on those numbers (the entity's group number is not saved), but the numbers the debug command prints shift, so re-check any hand-written `/primogemcraft event run <n>`.
- **`RandomEventEntity` does not persist its group.** `readAdditionalSaveData` and `addAdditionalSaveData` are both empty, so after a chunk reload the entity's synced number is 0, `group()` returns `null`, and the entity is inert and renders the default texture. An event entity is a session-lifetime object.
- **Registration runs on the client too.** `RandomEventRenderer.getTextureLocation` calls `entity.group()`, which calls `EventRegistry.group`, which calls `load()`. The first frame with an event entity in view therefore executes `RandomEvents.registerAll()` on the client. Keep the registration body free of server-only state — no level, no player, no world data.
- **A weight of 0 still fires.** `EventGroup`'s compact constructor clamps to `Math.max(1, weight)`. To keep a group out of the natural drop pool, do not register it as a weighted group at all, or accept that every registered group is reachable.
- **`deny()` is rate-limited, and it is the only "no".** It shares one 100-tick gate per player, so a player who fails two different events in five seconds sees only the first message. Returning `false` silently instead of calling `deny()` gives the player no feedback; calling `deny()` outside a payment check gives misleading ones.
- **A `&&` chain does not refund.** A paid step that runs before the step which can fail still costs the player when that step fails. `context.enchant(grade, fragments)` is written the safe way — it checks `hasEnchantTargets` and the fragments, opens the screen, and only then takes the payment — but an event that pays with its own `context.fragments(...)` in front of another step, the way `enchant/health_special` does, has no such protection. Order the chain so paid steps are last, or make the paid step the one whose failure ends the event.
- **The enchant screen only opens when something in the inventory can really be enchanted.** `EnchantChoice.hasTargets` and its pool are one filter, and it is `stack.isEnchantable() && stack.getEnchantmentValue() > 0`: a damageable item whose enchantment value is 0 — a shield, an elytra, shears, flint and steel — is `isEnchantable()` yet can never receive an enchantment, so counting it would make an enchant card look clickable and then open nothing at all. `hasEnchantTargets` is exactly the check that keeps `enchant/fragments_*` from charging fragments for a screen that never appears.
- **`health(ratio)` can kill.** It is unresisted generic damage for a fraction of max health, so `0.95` is lethal to a player already hurt. The `hasHealth` pre-check is what makes the shipped `enchant/health_special` survivable.
- **A forced pick on the event screen only takes cards that are clickable, and `leave` is the way out.** ESC and the 30-second timer skip every card whose condition is unmet or that the server has refused, and fall back to the group's `leave` card when none is left. Adding a conditional event to a group that has no unconditional member therefore also adds a `leave` card the player did not ask for — that is the design, not a stray entry, unless that event is registered with `registerForced`. `gui.primogemcraft.event.hint` is the in-game warning for this.
- **A condition that lies about its action is worse than no condition.** The card is dimmed on the strength of `condition.met`, but the action is what actually charges: a condition that says "available" for an action that then `deny()`s leaves the player with a card that looks clickable and does nothing, and a condition that says "unavailable" for an action that would have worked hides a working event. Keep each condition a faithful reading half of the action's first check — and keep its factory's label honest too, because that label is the line the player reads under `条件不足`.
- **An event that returns `false` keeps the event screen open.** The refusal goes back to the client, that card turns un-clickable, and the request stays pending — so anything that waits on `ChoiceRegistry.isPending` (a chain, another caller) waits until the player answers again. A combat event whose spawn fails on every card leaves the player on a screen with nothing to click unless the group carries a `leave` card; that is why conditional groups get one appended.
- **A chain runs one step per tick and pauses on every screen.** `EventChain.advance` returns early while a choice is pending, so the shipped ten-event chain needs ten screens answered before the queue drains. Ten rapid screens is the design, not a bug.
- **One challenge per player.** `EventCombat.CHALLENGES` is keyed by the killer's UUID and overwritten, so starting a second combat event before the first finishes silently drops the first completion. A `guard` or `summon` spawn registers no challenge at all, so killing those mobs advances nothing.
- **Loot is offered per mob, to the killer only.** `EventCombat.LOOT_DROPS` is removed when the mob dies, and the loot screen opens for whoever landed the kill, which need not be the player the event was offered to.
- **Killing an event mob can drop another event.** `EventEvents.onLivingDeath` calls `EventRegistry.drop` for every non-player death by a player, including the mobs the previous event spawned. The per-player `event/drop` cooldown is the only brake on that recursion.
- **The drop path uses `level.random`, not the player's RNG.** `EventQuota.roll` and `weightedGroup` both read the level's `RandomSource`, so the same player killing two mobs in the same tick draws from one shared stream.
- **`EventRegistry.drop` returns the entity or `null`, and `null` covers four different reasons** — cooldown, failed roll, no weighted group, or an empty group. It reports nothing; do not read a `null` as a specific failure.
- **Lazy group providers are re-evaluated per read.** `EventGroup.events()` calls `provider.get()` every time, and `RandomEvents.randomEvent()` (the no-arg helper used by the dynamic groups) rolls from an unseeded `RandomSource.create()`. A dynamic group therefore shows different cards each time its screen opens, and an `info` command afterwards will not match what the player was just shown.

## Verification

Compile after any change here:

```powershell
$gradle = Get-ChildItem "$env:USERPROFILE\.gradle\wrapper\dists\gradle-9.7.1-bin" -Recurse -Filter gradle.bat | Select-Object -First 1 -ExpandProperty FullName
& $gradle compileJava --console=plain
```

Compilation proves nothing about the drop rate, the card screen, the challenge timing or the renderer. `/primogemcraft event list`, `info`, `run` and `summon all` are the manual checks and need a running game (`runClient` or `runServer`); say plainly that they were not run unless they were.
