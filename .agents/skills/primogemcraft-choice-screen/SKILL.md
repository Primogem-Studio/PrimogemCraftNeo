---
name: primogemcraft-choice-screen
description: Use whenever the generic card-choice interface is opened, changed, extended, or debugged in this project — picking one reward out of N cards on one player's client, revealing an already-decided reward without a pick (`ChoiceMode.REVEAL` and `CurioReward`), building ChoiceCard options, configuring spin speed and card textures, wiring the selection callback, or adding a new caller of ChoiceRegistry. Covers the server-to-client payload pair, the client interaction contract, the resolution semantics of the callback, and the shared texture and localization contracts.
---

# Generic choice screen

Apply [the project standard](../primogemcraft-standard/SKILL.md) for Ponytail compatibility, verification, and concise replies. Preserve the resolution and client/server contracts even when a shortcut would be shorter. If any option grants or charges fragments, also apply [fragment payment](../primogemcraft-fragment-payment/SKILL.md); automatic inventory grants are not a substitute for that contract.

One server-side call shows a player a row of cards and the server learns how it ended. The card count is whatever the caller passes; "three" survives only in the class name `ChoiceScreen` and the lang keys `gui.primogemcraft.choice.*`. The callers are `WishEntity` (three loot previews), `CurioChoice` (three curios), `EnchantChoice` (enchantment options), `HertaShopOptions` (the shop menu) and `CurioReward` (a non-selective reveal).

Three modes exist, and they differ only in what the screen does with the player's input:

| `ChoiceMode` | Behaviour |
|---|---|
| `SELECT` | The default. The player picks one card, the callback receives that index. |
| `REVEAL` | Nothing is picked: every card is shown and stays until the player dismisses the screen with ESC or a click on the empty space beside the cards. No timeout, no cancel button, no card highlight. See `CurioReward`. |
| `CONFIRM` | `SELECT` whose pick is a request: the screen stays up until the server answers with `ChoiceResultPayload`. A callback that returns `false` refuses that card, so the screen keeps the request and the client marks the card un-clickable instead of closing. See `EventChoice`. |

The framework owns the machinery; the caller supplies nothing else:

| Caller decides | Through |
|---|---|
| Appearance | `title`, `subtitle`, `visual`, `texture`, `textures` |
| How each card spins | `ChoiceCard.spin()` per card, `ChoiceSpin` for the shared style and default |
| What a card wears in the band under its item | `ChoiceCard.icons()` — a texture repeated `count` times |
| One extra layer over the card face | `ChoiceCard.overlay()` — one tile of a sheet, drawn on the front only |
| Whether a card can be clicked at all | `ChoiceCard.enabled()` — a `false` card is drawn dimmed and ignores clicks, numbers and the forced picks |
| The pitch of that card's reveal chime | `ChoiceCard.quality()` — resolved from the item's rarity when left at `AUTO_QUALITY`, and `SILENT_QUALITY` for a card that must not chime |
| Which card means "leave" | `leaveIndex` — the index the forced picks fall back to when nothing is clickable |
| Whether one card is picked | `ChoiceMode` |
| The outcome | the callback — grant the card's `ItemStack` yourself, or pass `null` and let `ChoiceRegistry` place it in the inventory |

| Piece | Path | Side |
|---|---|---|
| `ChoiceRegistry` — the entry point and the pending-request table | `src/main/java/net/per/primogemcraft/choice/ChoiceRegistry.java` | common (server logic) |
| `ChoiceRequest`, `ChoiceCard`, `ChoiceCardSpin`, `ChoiceCardIcons`, `ChoiceCardOverlay`, `ChoiceSpin`, `ChoiceSpinSpeed`, `ChoiceVisual`, `ChoiceMode`, `ChoiceCardTextures`, `ChoiceRoll`, `ChoiceCallback`, `ChoiceSupport` | `src/main/java/net/per/primogemcraft/choice/` | common |
| `CurioReward` — the non-selective curio reveal, the reference `REVEAL` caller | `src/main/java/net/per/primogemcraft/curio/CurioReward.java` | common |
| `EventChoice` — the reference `CONFIRM` caller | `src/main/java/net/per/primogemcraft/event/EventChoice.java` | common |
| `ChoiceOpenPayload`, `ChoiceSelectPayload`, `ChoiceResultPayload` | `src/main/java/net/per/primogemcraft/network/` | common |
| `ChoiceClientHandler`, `ChoiceScreen`, `ChoiceTextScroller`, `NineSlicePlate` | `src/main/java/net/per/primogemcraft/client/gui/` | client only |
| `ChoiceParticles`, `ChoicePreviewRenderer` | `src/main/java/net/per/primogemcraft/client/` | client only |
| `choice_card.png`, `choice_card_back.png`, `choice_card_level.png`, `curio_card.png`, `curio_card_back.png`, `curio_star.png`, `curio_star_fusion.png`, `enchant_card.png`, `enchant_card_back.png` | `src/main/resources/assets/primogemcraft/textures/gui/` | assets |
| `choice_grant.ogg` — the one sound a `REVEAL` screen plays, registered as `PGCSounds.CHOICE_GRANT` | `src/main/resources/assets/primogemcraft/sounds/` | assets |

The whole `choice` package is common code. It must never reference `client.*` classes; the screen is reached only through the payload, which `PGCNetwork` already registers.

## The one call that matters

```java
public static void open(ServerPlayer player, Component title, Component subtitle, ChoiceVisual visual,
                        ResourceLocation texture, ChoiceCardTextures textures, ChoiceSpin spin,
                        List<ChoiceCard> options, ChoiceCallback callback)

public static void open(ServerPlayer player, Component title, Component subtitle, ChoiceVisual visual,
                        ResourceLocation texture, ChoiceCardTextures textures, ChoiceSpin spin,
                        int settleTicks, List<ChoiceCard> options, ChoiceCallback callback)

public static void open(ServerPlayer player, Component title, Component subtitle, ChoiceVisual visual,
                        ResourceLocation texture, ChoiceCardTextures textures, ChoiceSpin spin,
                        int settleTicks, ChoiceMode mode, List<ChoiceCard> options, ChoiceCallback callback)

public static void open(ServerPlayer player, Component title, Component subtitle, ChoiceVisual visual,
                        ResourceLocation texture, ChoiceCardTextures textures, ChoiceSpin spin,
                        int settleTicks, ChoiceMode mode, int leaveIndex, List<ChoiceCard> options,
                        ChoiceCallback callback)
```

The first two are the `SELECT` overloads and forward to the third with `ChoiceMode.SELECT`; the third forwards to the fourth with `ChoiceRequest.NO_LEAVE` (`-1`).

| Parameter | Meaning |
|---|---|
| `player` | Server-side player who sees the screen. The request is keyed by `player.getUUID()`. |
| `title` | Heading shown above the cards, and the heading when a card is being chosen. Already a `Component` from a lang file. |
| `subtitle` | Line under the heading, hidden once a card is selected. |
| `visual` | `ChoiceVisual.ITEM_MODEL` renders each card's `ItemStack` model; `SCENARIO_TEXTURE` renders `texture` instead, the same picture on every card. |
| `texture` | Read only when `visual` is `SCENARIO_TEXTURE`. Pass `ChoiceSupport.BACKGROUND` otherwise (see Traps). |
| `textures` | Card face and card back atlas. Use `ChoiceSupport.DEFAULT_CARDS` unless a screen needs its own art. |
| `spin` | Spin animation. Build it with `ChoiceSupport.spin(...)`. |
| `settleTicks` | How long the chosen card stays on screen before the client answers. The 9-argument overload passes `ChoiceRegistry.DEFAULT_SETTLE_TICKS` (12 ticks, 0.6 s); `ChoiceRegistry.NO_SETTLE_TICKS` (0) makes the screen answer on the next tick, for a screen whose answer immediately opens another one — which is what `HertaShopOptions` does. Negative values are clamped to 0. Ignored by `REVEAL`, which never reaches the settle phase. |
| `mode` | `ChoiceMode.SELECT` (one card is picked), `ChoiceMode.REVEAL` (everything on the cards is kept) or `ChoiceMode.CONFIRM` (the pick is a request the server may refuse). |
| `leaveIndex` | Index of the card that means "leave", or `ChoiceRequest.NO_LEAVE`. It is not a normal card: nothing about the screen treats it specially except that the forced picks fall back to it when no card is clickable. `EventChoice` points it at the shipped `leave` event's card. |
| `options` | The cards. Index order is the order on screen, left to right, and the set a `REVEAL` request grants in full. |
| `callback` | A `ChoiceCallback`: `boolean select(int index)` on the server thread. Returning `false` refuses the pick — the pending entry stays and, for a `CONFIRM` request, the client is told. May be `null`. |

Returns nothing. An empty `options` list sends nothing and creates no pending entry; callers that must react to that check their own list first.

Call it from server code only, with a live `ServerPlayer`. It sends `ChoiceOpenPayload` directly to that one player.

## Non-selective requests

`ChoiceMode.REVEAL` exists so a reward that was already decided can be shown as cards instead of silently appearing in the inventory. The player still sees the whole ceremony — spin, landing, the reveal fade and the particle burst — but there is nothing to pick, and the screen waits for the player instead of timing out:

- No card is ever chosen: `chosen` stays `-1`, so nothing takes the `chosen` column, nothing dims, and the heading keeps the request's `title` instead of a card name. The cards keep their hover lift, their scroll, and their hover tooltip, so the player can inspect what they got.
- **ESC closes it** (as it does for a `SELECT` request), and so does a click that lands on the empty space beside the cards; a click on a card does nothing.
- Both exits run through `onClose()`, which sends `ChoiceSelectPayload` once with `REVEAL_INDEX` (0) before closing the screen. The index carries no meaning: a `REVEAL` caller grants its whole set whatever index arrives, so a dismissal by ESC, by an empty-space click, by a logout or by a superseding request all hand the same batch over exactly once.
- The 30-second expiry, its progress bar and the number keys do not apply at all — `tick()` stops after the spin, and the bottom line is `gui.primogemcraft.choice.reveal` (no `%s`) instead of `gui.primogemcraft.choice.hint`.
- With a `null` callback the registry grants **every** card, not the one at `index`.

`CurioReward.open(player, textures, rewards)` is the reference caller and the reason the mode exists: it shows a set of preset curio stacks and hands all of them over through `Curios.give` when the screen is dismissed. It reuses `CurioChoice`'s card face — the curio star row and level overlay, by way of `CurioChoice.presentation(item)`, which is the same card `CurioChoice` builds minus the quality spin — and always spins one turn at `BRISK` (0.5 s), because this screen has no quality ladder to follow. It takes the card face and back art as an argument so a new source can bring its own, and swallows the resolved index: the set is fixed from the first frame. Its own lang keys are `gui.primogemcraft.curio_reward.title` and `.hint`, so every curio source shares one heading.

Because a second request for the same player would supersede the first screen, `open` never replaces a screen that is still pending: the set goes into a per-player waiting queue instead, and `tick(player)` — called every tick from `CurioEvents.onPlayerTick` — opens the next queued set once `ChoiceRegistry.isPending(player)` is false. That is what stops several curios that trigger on the same tick (the dice that convert the inventory the moment they are picked up) from replacing each other's screen, and it also keeps a reveal from clobbering another caller's screen. `clear(player)` grants every still-waiting set and drops the queue; logout calls it. The callback must not open a screen: it runs while its own entry is still in the pending table, so a nested `open` would cancel that entry and run the same callback again, granting the set twice — the tick is the only place that may open the next set. A set is handed to its screen rather than left in the queue, so it is granted exactly once: by its own screen when it is resolved, or by `clear` when it never reached one.

## Building cards

```java
public record ChoiceCard(Component title, ItemStack item, Component description, Component footnote,
                         ChoiceCardSpin spin, ChoiceCardIcons icons, ChoiceCardOverlay overlay,
                         boolean textTooltip, boolean itemTooltip, boolean enabled, int quality)
```

Prefer the `ChoiceSupport` factories over the constructor:

| Factory | Result |
|---|---|
| `card(ItemStack item)` | Title, description, and footnote empty; the card shows the item. |
| `card(Component title, ItemStack item, Component description)` | Explicit title and body, empty footnote. |
| `card(Component title, ItemStack item, Component description, Component footnote)` | Full form. |

All of them leave `spin`, `icons`, and `overlay` null, which means "spin the way the screen says", "wear nothing", and "plain face", and every factory produces a clickable card (`enabled` true) whose chime pitch follows the item's rarity (`quality` = `ChoiceCard.AUTO_QUALITY`). Attach per-card presentation with `withSpin(...)` / `withIcons(...)` / `withOverlay(...)` / `withTextTooltip()` / `withItemTooltip()` / `withEnabled(...)` / `withQuality(...)` — they work on any factory result and chain:

```java
ChoiceSupport.card(stack).withSpin(ChoiceCardSpin.of(4, ChoiceSpinSpeed.FASTEST)).withIcons(ChoiceCardIcons.of(texture, 3)).withOverlay(ChoiceCardOverlay.of(levelSheet, 4, 3))
```

Behaviour the screen applies on top:

- `displayTitle()` returns `title` when it has text; otherwise the item's hover name with the item's rarity style. A card with an empty stack and an empty title renders a blank name plate.
- Body text is `description` when it has text, otherwise the item's tooltip lines with the first line removed (the hover name is already on the plate) and lines naming any loaded mod dropped.
- `footnote`, when present, is appended after the body.
- The card body scrolls when it overflows; the scroll wheel works only over the description frame of a card that is not spinning.
- With `ChoiceVisual.ITEM_MODEL` the item's own tooltip is shown on hover — unless the card sets `withTextTooltip()`, which replaces it with the card's own text (name, body, footnote). A body containing `\n` becomes several tooltip lines: the screen splits every line to `max(width / 2, 200)` before rendering, because `renderComponentTooltip` alone would keep the newline inside one line.
- A card that sets both `withTextTooltip()` and `withItemTooltip()` gets a second box beside the text box, holding the whole item's tooltip. That second box is the game's own item tooltip — `renderTooltip(font, stack, …)`, so its lines, wrapping, styles and any tooltip lines other mods add are the real ones, unlike the text box, which the screen renders itself and wraps to `max(width / 2, 200)`. **The screen owns the geometry of both boxes and never lets the game place either one on its own.** It measures the item tooltip by gathering the same components the game will (`ClientHooks.gatherTooltipComponents` with `getTooltipFromItem`, summing `getWidth`/`getHeight`, minus `TOOLTIP_SINGLE_LINE_TRIM` for a one-line tooltip), then picks one of two layouts from those measured sizes:
  - **Side by side**, used whenever `textWidth + TOOLTIP_GAP + itemWidth` fits inside the screen: the text box sits on the left and the item box to its right, the gap being `TOOLTIP_FRAME_PAD * 2 + TOOLTIP_BOX_SPACING`. The pair is slid left as a unit when the cursor is near the right edge — it is never *flipped*, so the two never swap sides and the item box can never land where the text box is. Both share one top edge, chosen so the taller box fits.
  - **Stacked**, used only when the pair cannot fit at all: the text box keeps the cursor-anchored position with the usual screen clamp, and the item box goes `TOOLTIP_GAP` below it, or `TOOLTIP_GAP` above it when below would leave the screen and above would fit. They keep one left edge.
  Either way the item box is still drawn by the game, so the screen translates the pose around that call by the difference between the wanted corner and where `DefaultTooltipPositioner.INSTANCE` would have put it. The gap is `TOOLTIP_FRAME_PAD * 2 + TOOLTIP_BOX_SPACING` because the frame `TooltipRenderUtil` draws reaches one row past `PADDING_TOP`, so counting origins alone would make the two frames overlap. `withItemTooltip()` without `withTextTooltip()` changes nothing: the single box is already the item's tooltip. `EnchantChoice` is the caller that uses it, so a hovering player sees the card's enchantment list and the finished item next to it.
  The layout is computed once per hover frame and both boxes are placed from it, which is what keeps them from overlapping when the item box is long: no box is ever positioned independently of the other.

## Spin configuration

Every card spins on its own timeline and lands on its own. A card's spin is its own data; the screen record only carries the shared style and the default.

```java
public record ChoiceCardSpin(int turns, ChoiceSpinSpeed speed)          // one card
public record ChoiceSpin(int turns, ChoiceSpinSpeed speed, float tilt)  // the whole screen

ChoiceCardSpin.of(int turns, ChoiceSpinSpeed speed)
ChoiceSpin.of(ChoiceSpinSpeed speed)                                    // rarity decides the turns
ChoiceSpin.of(int turns, ChoiceSpinSpeed speed)                         // fixed turns for cards that bring none
ChoiceSpin.of(int turns, ChoiceSpinSpeed speed, float tilt)
```

- `ChoiceCardSpin.ticks()` is that card's animation length: `speed.durationTicks(turns)`. The screen runs until its longest card is done, and each card reveals its content the moment it lands.
- A card's own spin wins. `ChoiceCard.spin()` non-null means that card uses it, whatever the screen was given — this is how a caller with its own quality ladder (curio grade, tier, whatever) maps quality to turns and pace without touching the framework.
- A card with `spin == null` falls back to the screen through `ChoiceSpin.cardSpin(item)`: the screen's `turns` (0 means "let rarity decide": `COMMON` 1, `UNCOMMON` 2, `RARE` 3, `EPIC` 4) plus the screen's `speed`. `ChoiceSupport.spin(...)` forwards to `ChoiceSpin.of`.
- `turns` is clamped to 1..24. `tilt` is the lean the cards do while slowing down, clamped to 0..45 degrees.
- `ChoiceSpinSpeed` is the pace, and duration comes from a per-turn-count table, not from a rate: `durationMillis(turns)` indexes `{1,2,3,4}` turns as `SLOW {1260, 1800, 2520, 3240}`, `MEDIUM {950, 1350, 1890, 2430}`, `FAST {700, 1000, 1400, 1800}`, `BRISK {500, 680, 880, 1120}`, `FASTEST {470, 670, 940, 1210}` milliseconds. The constants are declared in that order and the payload carries the ordinal, so a new tier belongs in the position its one-turn duration deserves — `BRISK` sits between `FAST` and `FASTEST` because 500 ms is between their 700 ms and 470 ms. `durationTicks(turns)` divides by 50, so one turn at `BRISK` is 10 ticks / 0.5 s. The `millisecondsPerTurn` constructor argument is stored but never read — changing it changes nothing.
- The spin blocks all input: the client ignores clicks and keys until the longest card lands.

## The card's icon row

```java
public record ChoiceCardIcons(ResourceLocation texture, int count)
```

A card's own band of repeated icons, drawn centered between the item model and the description frame — below the visual, above the text. It exists so a caller can mark a card with its own rank, tier or anything else countable, using its own art.

- `count` is clamped to 1..6. `null` icons draw nothing.
- The row is centered horizontally on the card, `count` tiles of 32 units with a 6-unit gap, and the texture is sampled as a 16x16 tile (`ICON_TILE`), so the shipped placeholders are exactly 16x16.
- It appears with the card's content, fading in on the same `reveal` value: the value is 0 until the card has landed and reaches 1 one `REVEAL_SECONDS` later, and a card the player did not pick never shows it.
- The vertical band is derived, not fixed: `ICON_UNITS_Y = (ICON_UNITS_TOP + TEXT_UNITS_TOP - ICON_UNITS_SIZE) / 2`, so moving the visual or the description frame moves the icons with it.

## The face overlay

```java
public record ChoiceCardOverlay(ResourceLocation sheet, int columns, int column)
```

One extra layer painted straight over the card face — a level, a frame, a sheen. It is a horizontal strip like every other card sheet, so a 4-column sheet gives four variants, one per column.

`ChoiceSupport.level(column)` hands out the shared project level layer: `choice_card_level.png`, 5 columns of 16x32 — white, blue, gold and red for the four graded tiers, the fifth a blood red meant for negative entries. The column meanings belong to the caller that knows what its tiers are; the framework only knows the sheet.

- Exactly one tile is drawn: `column` is clamped to `0..columns-1`, and only one layer, never stacked.
- It uses the card's own `tileWidth`/`tileHeight` and draws into the same `CARD_UNITS_W` x `CARD_UNITS_H` area as the face, so the sheet must agree with the card sheet on tile size. `columns` is the sheet's own column count, used only for UV normalisation.
- It is painted as part of the card's content and fades in on the same `reveal` value, so it never shows while the card is still turning and never on a card the player did not pick. It is drawn first, under the name plate, the item, the icon row and the text.
- Semi-transparent sheets work, but only because `renderCardOverlay` brackets the blit with `RenderSystem.enableBlend()` / `disableBlend()`. Every `blit(ResourceLocation, …)` overload routes to the untinted `GuiGraphics.innerBlit`, which never touches the blend state, so an alpha sheet renders as an opaque slab unless the caller enables blending itself. Vanilla does the same in `BossHealthOverlay.drawBar` and `Gui.renderTextureOverlay`; the ambient blend function is already the default `SRC_ALPHA, ONE_MINUS_SRC_ALPHA` that `RenderType`'s transparency states restore. The icon row and the card edge fade are wrapped the same way, and any new translucent draw here must be too.

## Visuals and card textures

```java
public record ChoiceCardTextures(ResourceLocation sheet, int columns, int tileWidth, int tileHeight,
                                 int idle, int hovered, int chosen, int dimmed, ResourceLocation backSheet)
```

`idle`, `hovered`, `chosen`, `dimmed` are column indices in `sheet`. `ChoiceSupport.DEFAULT_CARDS` is the project art: sheet `textures/gui/choice_card.png`, 4 columns of 16x32 tiles, columns 0..3 in that order, back `textures/gui/choice_card_back.png`. `ChoiceSupport.CURIO_CARDS` is the curio set: `curio_card.png` / `curio_card_back.png`, same layout, currently a renamed copy of the default art. `ChoiceSupport.ENCHANT_CARDS` is the enchant set: `enchant_card.png` / `enchant_card_back.png`, a renamed copy of the curio art.

The sheet contract, as the screen binds it:

- The sheet is a **single horizontal strip**: `columns * tileWidth` wide and `tileHeight` tall. Only row 0 is ever sampled, so vertical atlas rows are unreachable. The shipped `choice_card.png` is 64x32, `choice_card_back.png` is 16x32.
- `columns`, `tileWidth`, and `tileHeight` are clamped to at least 1 by the record.
- The back tile is drawn with the **same** `tileWidth`/`tileHeight` as the face tiles, so a custom sheet and its back sheet must agree on tile size.

To point at a texture of your own use `ChoiceSupport.texture("textures/gui/…")`; it fills in `MOD_ID` for you. Never build a `ResourceLocation` from a hardcoded namespace.

## The reveal chime

Each card that is worth one plays `SoundEvents.AMETHYST_BLOCK_HIT` — the amethyst mining tick — the moment its spin lands, which is the same instant as its particle burst, when its content starts fading in — so a row of cards with different spin lengths chimes in sequence.

The pitch is that card's quality, and it runs **downhill**: `ChoiceCard.revealPitch()` is `1.0 + 0.15 * (MAX_QUALITY - revealQuality())`, so 3 (the best) → 1.0 and 2 → 1.15. A lesser card sounds higher, and the best card sounds like an ordinary note. `ChoiceCard.revealQuality()` is the card's own number clamped to `0..3`: `ChoiceCard.AUTO_QUALITY` (`-1`) makes the screen read the item's vanilla rarity (`COMMON`..`EPIC` = 0..3), and a caller with its own ladder passes it with `withQuality(...)` — `CurioChoice` passes the curio tier (B/A/S, +1 for fusion) and `EnchantChoice` the enchant grade column. The volume is fixed at 0.7.

Only the top two rungs chime: `chimes(index)` requires `revealQuality()` to reach `CHIME_MIN_QUALITY` (2), so 3 and 2 sound and 1 and 0 are silent. The silent half is everything below HIGH — a `COMMON` or `UNCOMMON` item, a B- or A-grade curio, a `LOW` or `MEDIUM` enchant and an empty `ItemStack` — which is what keeps the text-only event cards silent without the framework having to know about them.

A caller that wants a screen with no chime at all does not need a new flag: it grades its cards at the lowest rung with `ChoiceCard.SILENT_QUALITY` (0) instead of passing its own grade, and the threshold above does the rest. That is what the Herta shop's service menu does — its four cards describe what the shop sells, not a reward, so they never chime, while the screens those cards open (buying a curio, picking a code, enchanting) are ordinary callers and chime normally. Silence costs the cards nothing else, because a card's stars, level layer and spin come from `withIcons` / `withOverlay` / `withSpin` and are never read from `quality`.

When every card shares one spin length *and* one quality, they land on the same tick, so the screen plays the chime **once** instead of stacking the identical sound `size` times. A row that differs in either dimension chimes per card, each on its own landing.

Do not read the chime as a hint about `ChoiceCard.enabled()`: an un-clickable card is graded and chimes like any other.

## The grant sound

A `REVEAL` request never plays the chime, whatever its cards are worth: it plays the project's own `PGCSounds.CHOICE_GRANT` (`sounds/choice_grant.ogg`, subtitle `subtitles.primogemcraft.choice_grant`) **once** per screen, on the first card that lands, at pitch 1.0 and the default volume. `grantPlayed` is the latch, so a row whose cards land on different ticks still sounds exactly one note, and a fresh request — a new screen instance — resets it. The file itself is a placeholder waiting to be replaced; the registration, the subtitle key in both lang files and the `sounds.json` entry are already in place.

## Rolling candidate options

`ChoiceRoll` is a `@FunctionalInterface` returning `List<ItemStack>`, used with the `ChoiceSupport` helpers:

| Method | Purpose |
|---|---|
| `distinct(List<ItemStack> rolled, ChoiceRoll source, int count, int attempts)` | Keeps distinct non-empty stacks, re-rolling `attempts` times until `count` are collected. |
| `limit(List<ChoiceCard> cards, int count)` | Copies at most `count` cards from the front. |
| `limitItems(List<ItemStack> stacks, int count)` | Same for raw stacks. |

`WishEntity` uses `distinct` to fill its three preview slots and falls back to a flat consolation prize when it cannot reach the count. Callers decide that fallback; the interface has none.

## Client interaction contract

Anything a caller's design depends on:

- The screen is not a pause screen, and it does not take the movement keys away from the player. Two vanilla facts work against that and both have to be answered: opening any screen runs `KeyMapping.releaseAll()` and `KeyboardHandler` only feeds a `KeyMapping` while no screen is open, so `tick()` re-holds `keyUp`, `keyDown`, `keyLeft`, `keyRight`, `keyJump`, `keyShift` and `keySprint` whenever they are physically down (read with `InputConstants.isKeyDown`, the same call vanilla's `KeyMapping.setAll()` makes); and NeoForge's `Options` puts those seven bindings in `KeyConflictContext.IN_GAME`, whose `isActive()` is `screen == null`, so `KeyMapping.isDown()` reports false in any GUI however the raw state was set — `init()` therefore moves them to `KeyConflictContext.UNIVERSAL` and `removed()` restores each binding's own context. `tick()` only ever sets a binding down and never up, and skips a binding that is already down, so a `ToggleKeyMapping` is never flipped twice and a key the player really released does not stay held. The mouse stays released: the player keeps walking, sprinting, sneaking and jumping through the whole screen, but cannot look around, attack or use. Every other key, every click and the wheel belong to the screen.
- ESC closes the screen in all three modes, at any moment — including while the cards are still spinning — and there is no cancel button. Closing with no pick still answers the request once: a `SELECT` request sends the card the player had already picked when the settle had not elapsed yet, otherwise a random **clickable** card index from the player's own `RandomSource`; a `REVEAL` request sends `REVEAL_INDEX`. A close that does not go through `onClose()` — a superseding request, a logout — sends nothing, because the server resolved that request already. A `CONFIRM` request never closes on ESC: the pick is sent and the screen waits for the verdict, and ESC pressed while that verdict is in flight does nothing at all.
- Selection is a click anywhere on a card or the number key `1`..`N` matching the position, with `N = request.size()`. Number keys and clicks are ignored until the longest card has landed, or after a choice. A `REVEAL` request ignores both — a click on the empty space beside the cards dismisses it, a click on a card does nothing, and number keys do nothing at any time.
- An **un-clickable** card — one the request marked `enabled = false`, or one a `CONFIRM` server refused — draws the `dimmed` face column, takes no hover lift, and ignores clicks and number keys, but keeps its text scroll and its hover tooltip so its body stays readable. Its hover box gains one red line, `gui.primogemcraft.choice.unavailable` (`条件不足`), above the card's own name; a card that would otherwise show the game's plain item tooltip is switched to the screen's own text box so that line has somewhere to go. The forced picks skip it.
- Each card reveals its name plate and content only once its own spin has landed, fading in over `REVEAL_SECONDS` (0.35s) after that — never during the rotation, where the content would sweep in with the turning face. The clock therefore runs `REVEAL_SECONDS` past the longest landing.
- On expiry the client selects the **last** card, `request.size() - 1`, unless that card is un-clickable, in which case it takes the forced-pick fallback. The timer is client-side only, counted down from `expireTime` and drawn as a bar scaled by `ChoiceRegistry.DEFAULT_DURATION`; a `REVEAL` request neither draws the bar nor reaches the expiry path.
- A click on a card or the matching number key plays the vanilla button click — `SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F)`, the same call `AbstractButton.playDownSound` makes. The timeout selection does not play it, because nobody clicked, and neither does the `REVEAL` dismissal by ESC; the `REVEAL` empty-space click does, so the player hears that the click registered.
- After a choice the client holds the chosen card for the request's `settleTicks` (the chosen card gets the `chosen` column, the rest dim, and the heading becomes the card's title), then sends `ChoiceSelectPayload`. A settle of 0 answers on the next tick, so nothing of that hold is visible. A `SELECT` request closes right there; a `CONFIRM` request does not — it waits for `ChoiceResultPayload(requestId, index, accepted)`, which either closes the screen or un-chooses the card, marks that index un-clickable and hands the screen back to the player. That wait is capped at 100 client ticks, so a verdict that never arrives (a stale request id, a superseded request) still closes the screen instead of trapping the player in it.
- The **forced picks** are the two ways out that are not a click: the ESC answer and the expiry answer. Both take only cards that are clickable, and when none is, they fall back to `request.leaveIndex()`. With no `leaveIndex` either — every card un-clickable and no "leave" card in the request — the client sends `ChoiceRegistry.NO_ANSWER` (`-1`) rather than an index, and the server resolves that as "the player gave up": the callback runs with `-1` and a `CONFIRM` screen is told to close. A screen can therefore never dead-end on a row of cards nobody can pick.
- Re-sending a request while the screen is open replaces it with a fresh screen; the older request was already resolved server-side by then (see the supersede row below).
- Cards are laid out automatically for any count in one row; width shrinks as the count grows.

## Server-side resolution semantics

| Situation | Result |
|---|---|
| `respond(player, id, index)` with the id matching the stored pending request, and a callback that returns `true` (or is `null`) | Entry is removed; a `CONFIRM` request also gets `ChoiceResultPayload(id, index, true)`. |
| The callback returns `false` | A `CONFIRM` request keeps its pending entry and sends `ChoiceResultPayload(id, index, false)`, so the screen stays and can answer again. Any other mode removes the entry anyway (a `SELECT` screen is already gone and has nowhere to put a refusal). |
| `respond` with an id that does not match (stale or forged packet) | Silently ignored; the pending entry stays. |
| `index` negative | Treated as "the player gave up": the entry is removed, the callback runs with `NO_ANSWER`, and a `CONFIRM` request is sent `ChoiceResultPayload(…, true)` so its screen closes. This is what keeps a screen with nothing clickable and no "leave" card escapable. |
| `index` at or above `size` | Clamped to `0`, so the first card is chosen. |
| `mode` is `REVEAL` and the callback is `null` | Every card's `ItemStack` is copied into the player's inventory, whatever `index` says. |
| Callback is `null` | The chosen card's `ItemStack` is copied into the player's inventory instead of calling anything. |
| Callback throws | Caught and logged as `Choice callback failed`; the generation continues. |
| `cancel(uuid, index)` | Removes the pending entry **without checking the id** and runs the callback as `respond` would. `WishEntity` passes `-1` as "gave up". A `null` callback makes `cancel` a no-op. |
| Player logs out | `ChoiceRegistry` cancels with index `NO_ANSWER` (`-1`) on `PlayerLoggedOutEvent`. |
| `open` while a request for that player is still pending | The pending one is resolved first with `cancel(uuid, NO_ANSWER)`, then the new request takes its place — so a superseded request runs its callback (with `-1`) instead of vanishing, and a `REVEAL` caller still hands over what it had already decided. |
| Player opens another screen without answering | Nothing happens server-side: no packet arrives, the pending entry stays until something calls `respond`/`cancel`. Give the feature its own timeout. |

`ChoiceRegistry` never runs the callback on expiry — the 600-tick limit is a client animation, not a server guarantee. A caller that must not wait forever, like `WishEntity`, keeps its own give-up path. Every callback must tolerate `-1`, because `cancel` reaches it with that value on logout and on supersede.

## Worked example

`WishEntity.openChoice(ServerLevel)` is the reference call site for a screen that lets the framework pick each card's turns:

```java
waitingForChoice = true;
choiceAnswered = false;
ChoiceRegistry.open(player, Component.translatable("wish.primogemcraft.capturing_radiance"), Component.translatable("wish.primogemcraft.capturing_radiance.choice_hint"), ChoiceVisual.ITEM_MODEL, ChoiceSupport.BACKGROUND, ChoiceSupport.DEFAULT_CARDS, ChoiceSupport.spin(ChoiceSpinSpeed.FAST), cards(), index -> {
    selectedChoice = index;
    choiceAnswered = true;
    waitingForChoice = false;
});
```

The callback only records the index; the reward is granted later from the entity's own `previews` list, and `-1` means no choice was made. That is the reason the entity calls `ChoiceRegistry.cancel(owner, NO_CHOICE)` in both `giveUp` and its own cleanup path.

`CurioChoice.open(...)` is the reference call site for a screen that decides each card's spin and stars itself — its own quality ladder, its own art, its own outcome:

```java
private static final List<ChoiceCardSpin> LADDER = List.of(
        ChoiceCardSpin.of(1, ChoiceSpinSpeed.SLOW),
        ChoiceCardSpin.of(2, ChoiceSpinSpeed.MEDIUM),
        ChoiceCardSpin.of(3, ChoiceSpinSpeed.FAST),
        ChoiceCardSpin.of(4, ChoiceSpinSpeed.FASTEST));

var cards = new ArrayList<ChoiceCard>();
for (var option : options) {
    var form = Curios.formOf(option);
    var grade = CurioGrade.of(option);
    var tier = tierOf(form, grade);
    cards.add(ChoiceSupport.card(option).withSpin(ladder(tier)).withIcons(stars(form, grade)).withOverlay(level(form, grade, tier)));
}
ChoiceRegistry.open(player, Component.translatable(TITLE_KEY), Component.translatable(HINT_KEY), ChoiceVisual.ITEM_MODEL, ChoiceSupport.BACKGROUND, ChoiceSupport.CURIO_CARDS, ChoiceSupport.spin(LADDER.get(BASE_TIER)), cards, index -> Curios.give(player, options.get(index)));
```

Its tier comes from the curio's grade tag, not from the item's rarity; its stars are B/A/S → 1/2/3 tiles of `curio_star.png`, or of `curio_star_fusion.png` when the curio is a fusion form; its face layer is `ChoiceSupport.level(tier)`, or column 4 of the level sheet for a negative curio, which has no grade and so gets no stars; and the outcome is granted by the caller instead of by the registry. That is the whole shape of a variant screen: art, per-card spin, per-card icons, per-card overlay, outcome.

`CurioReward.open(player, textures, rewards)` is the reference call site for a non-selective reveal, and the shape every curio source that hands out curios uses:

```java
ChoiceRegistry.open(player, Component.translatable(TITLE_KEY), Component.translatable(HINT_KEY), ChoiceVisual.ITEM_MODEL, ChoiceSupport.BACKGROUND, reveal.textures(), ChoiceSupport.spin(SPIN), ChoiceRegistry.NO_SETTLE_TICKS, ChoiceMode.REVEAL, cards, index -> {
    grant(player, reveal.rewards());
    return true;
});
```

Everything about the request is ordinary — curio art, `ITEM_MODEL` visuals, the curio star row and level overlay — and the only structural differences from `CurioChoice` are `ChoiceMode.REVEAL`, a card face without a per-card spin (`CurioChoice.presentation(item)`), one screen-wide `SPIN` of `ChoiceCardSpin.of(1, ChoiceSpinSpeed.BRISK)` (0.5 s flat, whatever the curio's grade), and a callback that ignores its argument. The reward list was rolled before the screen opened (`Curios.convert` returns its rolled curios instead of granting them; `CurioContext.reward(stack)` folds the jar-break `production` multiplicity into the stack count so one card reads `×N`), and one `Curios.give` per entry happens when the screen is dismissed, however it is dismissed. The whole screen is driven from a per-player queue and the tick that drains it, so `CurioReward` is not a plain one-call wrapper — see the queue notes above for why the callback must not open the next screen itself.

`EnchantChoice.open(player, options, callback)` in `net.per.primogemcraft.enchantment` is the reference call site for a screen whose caller describes each option instead of each card: an `EnchantOption` carries a preview item (`EnchantOption.of(preview, …)` stores copies of it), a level, an `EnchantGrade` preset and an `EnchantCost`. **That preview is the binding result, so it has to be rolled before the screen opens**: the card body lists the enchantments the preview carries — `EnchantmentHelper.getEnchantmentsForCrafting` plus `Enchantment.getFullname`, one per line — and the caller applies exactly those on selection. `CurioEnchanting.tablePoolResult(player, target, level)` rolls the table pool into a stack without touching the target, and `CurioEnchanting.applyResult(target, result)` copies that stack's enchantments onto the target. The grade picks the face layer (`ChoiceSupport.level(grade.column())`, columns 0..3 = LOW/MEDIUM/HIGH/SPECIAL, the same white/blue/gold/red ladder the curio screen uses) and the spin ladder; the grade name is the card's title and the cost description is its footnote. The cost is resolved by the screen, not by the caller: on selection it tests `cost.payable()`, prints `message.primogemcraft.enchant_choice.unavailable` and stops when the player cannot pay, and only then charges `cost.pay()` and runs the callback. That callback is a `Predicate<EnchantOption>` — it returns whether the enchant actually landed, and the screen plays the enchanting table sound (`SoundEvents.ENCHANTMENT_TABLE_USE` on `SoundSource.PLAYERS`) only when it did, so a caller that bails out with a message stays silent. `EnchantGrade.rollLevel(random)` is how a caller turns the grade preset into a concrete level. `/primogemcraft enchant choose [grade] [fixed]` in `command/debug/Enchant.java` is the test entry point; `fixed` only swaps the preview icon between the enchanted target and a book carrying the same roll.

## Checklist for a new caller

1. Build the options server-side as `ChoiceCard`s, at least one, and decide the fallback when there are too few.
2. Give the title, subtitle, and any card text their own lang keys in both `zh_cn.json` and `en_us.json`; keep literals out of Java.
3. Call `open` on the server thread with the owning `ServerPlayer`, passing a callback, and pick the mode. A callback that can refuse a pick must be paired with `ChoiceMode.CONFIRM`, because a `SELECT` screen is already closed by the time it runs.
4. Record the index in the callback and do the granting there, or in the tick path that waits for it; do not both auto-grant and grant again. A `REVEAL` caller grants the whole set in the callback regardless of the index it receives, and every callback must be able to return `true` for an index it ignores.
5. Add a timeout or give-up path of your own for a player who never answers.
6. Decide what each card does with `ChoiceCard.withSpin(...)` and what happens on selection in the callback. The payloads, the screen, and the network handlers already carry both, so a new variant screen needs no framework change.

## Localization keys

These already exist in both lang files. A new caller supplies `title` and `subtitle` from its own keys; the rest is the screen's own text.

| Key | Use |
|---|---|
| `gui.primogemcraft.choice.hint` | Shown after the spin of a `SELECT` request; takes the option count as `%s` (`1~3` in the text). |
| `gui.primogemcraft.choice.unavailable` | The red `条件不足` line the hover box of an un-clickable card carries above its name; takes no `%s`. |
| `gui.primogemcraft.choice.reveal` | Shown after the spin of a `REVEAL` request; names the two ways to dismiss it, so it takes no `%s`. |
| `gui.primogemcraft.choice.rotation` | Shown during the spin; takes the turns and the speed name of the card that spins longest. |
| `gui.primogemcraft.choice.speed.slow`, `.medium`, `.fast`, `.brisk`, `.fastest` | Speed names, resolved from `ChoiceSpinSpeed.translationKey()` — the key is `…speed.` + the lower-cased constant name, so a new tier needs a new key in both files. |
| `gui.primogemcraft.curio_reward.title`, `.hint` | `CurioReward`'s heading and subtitle, shared by every curio source that reveals its loot. |

## Traps

- **The records and the payload must change together.** `ChoiceOpenPayload.encode`/`decode` write `ChoiceRequest` field by field, including every field of `ChoiceCardTextures`, `ChoiceSpin`, the request's `settleTicks`, the request's `mode` (`VAR_INT` ordinal, after `expireTime`), the request's `leaveIndex` (last, after `mode`), both fields of each card's `ChoiceCardSpin` (`turns` 0 means "no per-card spin"), the optional `ChoiceCardIcons` and `ChoiceCardOverlay` (a boolean for each, then their fields), and the card's `enabled` boolean and `quality` (`VAR_INT`, `AUTO_QUALITY` = `-1`) after its two tooltip flags. Add a field to one record and forget the codec and the client silently reads a corrupt request. `ChoiceSelectPayload` carries only `requestId` and `index`; `ChoiceResultPayload` carries `requestId`, `index` and `accepted`, and only a `CONFIRM` request ever sends it. `PGCNetwork.PROTOCOL_VERSION` is bumped whenever this layout changes.
- **`ChoiceSupport.BACKGROUND` points at `textures/gui/choice_background.png`, which does not exist in the repository.** Nothing breaks today only because the sole caller passes `ChoiceVisual.ITEM_MODEL`, which never reads the texture argument, and the screen overrides `renderBackground` to draw nothing. Do not switch a caller to `SCENARIO_TEXTURE` with that constant expecting a picture.
- **The callback replaces the automatic grant.** With a callback the screen's item is not placed in the inventory; with `null` it is. Pick one.
- **ESC is an answer, not a cancel.** A `SELECT` screen always resolves: a player who presses ESC while the cards are still spinning is handed a random card, and the callback runs exactly as if they had clicked it. A caller whose outcome is not free — a charge, a consumption, something irreversible — pays for a card nobody looked at. `EnchantChoice` is the only caller with a cost today, and its cost is `EnchantCost.free()`. `enabled = false` is the answer to this for a caller that can tell in advance: the forced picks skip un-clickable cards, so a card that would be wasted (an event whose condition is unmet) stays out of the lottery.
- **A refusal needs `CONFIRM`, and `CONFIRM` needs a verdict.** Only a `CONFIRM` screen survives its own answer, and only the server can end it — if `respond` never runs for that request id (stale id, a superseding request), the client falls back to its 100-tick cap and closes. Never return `false` from a `SELECT` callback: nothing consumes the refusal and the pending entry is dropped anyway.
- **The chime is per card, so the pitch ladder is per caller.** Leaving `quality` at `AUTO_QUALITY` reads the *item's* rarity; a caller whose cards are graded by something else (curio tier, enchant grade) must pass that grade, or two cards of the same grade chime at different pitches depending on the preview item.
- **A `KeyMapping` cannot be faked down while a screen is open.** NeoForge's `Options` gives the vanilla in-game bindings `KeyConflictContext.IN_GAME`, and `IN_GAME.isActive()` is `Minecraft.screen == null`, so `isDown()` is false in every GUI no matter how the raw state was set — `setDown(true)` looks like it works and reads back as false on the very next line. Keeping an in-game binding alive under a screen means moving it to an active context for that time and restoring it afterwards; writing the raw state alone changes nothing.
- **`cancel` ignores the request id but still requires the player's UUID**, so a stale cancel can fire the callback for a request the caller has already forgotten. `WishEntity` guards against this by only setting `selectedChoice` through the callback.
- **The pending table is keyed by UUID, not by request id.** A second `open` for the same player while one is pending resolves the first one with `NO_ANSWER` before installing the second, so a `REVEAL` caller never loses what it had already rolled; a `SELECT` caller whose screen was replaced sees its callback fire with `-1`.
- **A `REVEAL` request is not a choice, so do not read `index` in its callback.** The index the client sends is `0`, and `cancel` reaches the same callback with `-1`; a caller that means "grant everything shown" has to grant its own set on every resolution.
- **A screen-level `turns` of 0 hands the turn count to the item's rarity.** A caller with its own quality notion must set the spin per card, or every card without one silently falls back to the vanilla rarity ladder.
- **Card count changes the width, not the count of columns in the atlas.** The atlas `columns` is how many face states exist (idle/hovered/chosen/dimmed), not how many cards are shown.

## Verification

For implementation changes, run `compileJava` and the required completion checks using the current environment's launcher resolved by [the project verification rules](../primogemcraft-standard/SKILL.md#verification).

Compilation proves nothing about the animation, the texture atlas, or the timer. Say plainly that the screen itself was not run unless the caller was launched with `runClient`.
