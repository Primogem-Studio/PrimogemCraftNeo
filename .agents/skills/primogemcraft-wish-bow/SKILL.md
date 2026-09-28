---
name: primogemcraft-wish-bow
description: Register, extend, or debug PrimogemCraftNeo wish bows (祈愿弓), including firing cadence, refinement skills, arrow effects, animated held sprites, first/third-person transforms, inventory icons, and wish loot registration. Use for WishWeaponBowItem subclasses and their shared rendering or projectile flow.
---

# Wish bows

Apply [the project standard](../primogemcraft-standard/SKILL.md). This skill describes the existing bow pipeline; it does not make Thundering Pulse's stats, rarity, dimensions, or abilities mandatory for other bows. Paths in code spans are relative to the repository root. Read only the implementation areas involved in the request.

## Start from the working pipeline

| Concern | Source |
|---|---|
| Bow lifecycle, ammunition, damage and weapon integration | [WishWeaponBowItem.java](../../../src/main/java/net/per/primogemcraft/system/weapon/WishWeaponBowItem.java) |
| A bow with separate left-click and sneak-use skills | [ThunderingPulseItem.java](../../../src/main/java/net/per/primogemcraft/item/weapon/ThunderingPulseItem.java) |
| Tick intervals and animation frames | [BowAttackCycle.java](../../../src/main/java/net/per/primogemcraft/system/weapon/BowAttackCycle.java) |
| Shared refinement passives | [BowRefinement.java](../../../src/main/java/net/per/primogemcraft/system/weapon/BowRefinement.java) |
| Homing, target filtering, impact and piercing | [WishArrowEntity.java](../../../src/main/java/net/per/primogemcraft/entity/misc/WishArrowEntity.java) |
| Automatic client extension registration and draw pose | [WishBowClient.java](../../../src/main/java/net/per/primogemcraft/client/WishBowClient.java) |
| Frame selection, texture loading and reload cache | [WishBowRenderer.java](../../../src/main/java/net/per/primogemcraft/client/WishBowRenderer.java) |
| Extruded pixel mesh, UVs and face normals | [BowSpriteMesh.java](../../../src/main/java/net/per/primogemcraft/client/BowSpriteMesh.java) |
| Separate held and inventory models | [thundering_pulse.json](../../../src/main/resources/assets/primogemcraft/models/item/thundering_pulse.json) |
| Existing four hand transforms | [example_wish_bow.json](../../../src/main/resources/assets/primogemcraft/models/item/example_wish_bow.json) |

Use `WishWeaponBowItem` directly for a bow needing only cadence, textures, sound and passives. Subclass it for actual custom skills. Do not copy its firing loop into each bow or replace it with vanilla release-to-shoot logic: the existing design fires repeatedly while right-click is held, and `releaseUsing` intentionally does not fire.

`BowAttackCycle(minimumCooldown, maximumCooldown, frameCount)` takes ticks, not arrows per second. At 20 TPS, the rate bounds are `20 / maximumCooldown` and `20 / minimumCooldown`. For example, 2–4 arrows/second uses `new BowAttackCycle(5, 10, 5)` for five animation frames. A random interval is sampled for each shot; those bounds are not a promise of exactly N arrows in every wall-clock second.

The server sets `PGCDataComponents.BOW_DRAW_DURATION` and `BOW_SHOT_TIME`; the client derives the frame and draw pose from them. Keep `onStopUsing` cleanup and `super` behavior when overriding lifecycle methods. Do not add a second client firing timer.

## Complete registration

For a new id, inspect the analogous entries and cover these independent surfaces:

| Surface | Location / requirement |
|---|---|
| Item | `registry/PGCItems.java`; `fiveStarWeapon(id, factory)` currently applies `Rarity.EPIC` |
| Creative listing | `registry/PGCCreativeTabs.java` |
| Weapon type | `src/main/resources/data/primogemcraft/tags/item/weapon/bow.json` |
| Star classification | Appropriate weapon star tag; five-star bows also enter `weapon/five_star.json` |
| Wish acquisition | Requested loot table, such as `data/primogemcraft/loot_table/wish/gold.json`; choose weight deliberately |
| Name and skills | Both `assets/primogemcraft/lang/zh_cn.json` and `en_us.json` |
| Model and textures | `assets/primogemcraft/models/item/<id>.json`, held frame sheet and static icon |
| Custom left-click | Client input subscriber, dedicated payload, and `network/PGCNetwork.java` handler, if needed |

Java paths above are under `src/main/java/net/per/primogemcraft/`; resource paths without `src/main/resources/` are under that directory. Follow the standard's data-generation rule if a relevant provider exists.

Name formatting such as `§d`, Minecraft rarity, star tags and loot membership are separate concerns. Setting one does not register the others. New subclasses already receive the renderer through `WishBowClient`'s registry scan; do not duplicate a renderer registration for each item. Reuse the registered `WISH_ARROW` entity for compatible arrows.

Match the name's color code in both lang files to the item's base vanilla `Rarity` (`DataComponents.RARITY`). Vanilla mappings are `§f` / `#FFFFFF` → `COMMON`, `§e` / `#FFFF55` → `UNCOMMON`, `§b` / `#55FFFF` → `RARE`, and `§d` / `#FF55FF` → `EPIC`. Do not use a lang color alone to imitate a quality that rarity-aware UI cannot see. Check the actual default components as well as the displayed name; enchantments can promote the effective rarity of a stack. For The Viridescent Hunt, the purple name `§d` requires base `Rarity.EPIC`, while its four-star classification and purple wish membership remain independent. Do not add a four-star bow to the five-star tag merely to obtain purple text.

Use `WeaponDescription.of(action, text, values...)` for skill descriptions, following existing `item.primogemcraft.<id>.description.<text>` keys. Action labels use `weapon.primogemcraft.action.<action>`. A literal percent in a translated template must be `%%`. Keep these established weapon descriptions instead of creating a parallel tooltip renderer.

Player-facing bow descriptions use generic damage wording: Chinese `造成伤害` (with the requested amount or ATK ratio), English `deal DMG`. Do not write `风元素伤害`, `雷元素伤害`, other `X元素伤害`, or their English equivalents in these descriptions. This is a localization rule; it does not change the damage source, elemental reactions, or gameplay implementation.

## Vanilla quality and dismantling compatibility

Preserve `WishWeaponBowItem` / `WishWeapon` integration and the existing recovery path when registering a bow. Verify these independently of name color and wish loot rarity:

- `weapon/bow.json` includes the item, and the root `weapon.json` includes that bow tag. The shared [weapon recovery recipe](../../../src/main/resources/data/primogemcraft/recipe/weapon_recovery.json) accepts `#primogemcraft:weapon` plus `specially_treated_fine_ore`; a bow omitted from the tag cannot use that recipe.
- [WishWeapon.isFiveStar](../../../src/main/java/net/per/primogemcraft/system/weapon/WishWeapon.java) reads only `weapon/five_star`. [WeaponRecoveryRecipe](../../../src/main/java/net/per/primogemcraft/recipe/WeaponRecoveryRecipe.java) stores `WeaponState` and that classification in `WEAPON_RECOVERY`. Neither name color nor vanilla `Rarity.EPIC` makes a weapon five-star. Keep applicable non-five-star entries in `weapon/low`; that tag is not the recovery recipe's input tag.
- [TreatedFineOreItem](../../../src/main/java/net/per/primogemcraft/item/misc/TreatedFineOreItem.java) redeems the stored recovery through [WeaponEnhancement.refund](../../../src/main/java/net/per/primogemcraft/system/weapon/WeaponEnhancement.java). Reuse its XP/ore refunds and refinement provenance: non-five-star duplicate layers return two Masterless Starglitter each, superimposer layers return Custom Superimposers; five-star refinement layers return Custom Superimposers and the weapon also returns one Lucent Afterglow. Temporary refinements do not become refundable permanent layers. Do not create a bow-specific dismantling formula.

When changing quality or star classification, check an ordinary stack and an enhanced/refined stack through the recovery recipe and redemption. Source/tag inspection confirms routing only; report runtime recovery as untested unless exercised in game.

## Skill inputs, refinement and arrows

- Left-click follows `client/ThunderingPulseInput.java` and `network/ThunderingPulsePayload.java`: a client-only `InteractionKeyMappingTriggered` subscriber checks `isAttack()`, cancellation, screen and main-hand item, then sends the dedicated serverbound payload. Decide whether vanilla attacking should be canceled. Never infer left-click from a swing.
- Server handlers recheck the held item, living/non-spectator state, menu eligibility and skill state. Keep right-click in `Item.use`; for a sneak-use override, delegate ordinary use to `super.use` so firing still works. `consume(stack)` avoids an unnecessary swing.
- Use `WeaponEnhancement.refinementOf(player, stack)`, including tooltip values. Permanent refinement currently reaches 5, with up to 3 extra levels. Check the formula at ranks 1, 5 and the current effective maximum; do not silently clamp all effects at 5. Minimum cooldowns and duration endpoints come from the request, not the reference bow.
- `PGCTimer` is an in-memory entity timer. Choose whether an ability belongs to the player or the particular stack, and whether it should survive replacement entities or restarts. Copying a timer name gives all copies of that weapon on one player shared state; it does not create persistent stack state.
- The base bow supplies ordinary arrows when the offhand is not ammunition. Offhand arrows or fireworks enter the existing projectile/ammo pipeline. `createProjectile` may return a firework or another projectile; check its type before adding `WishArrowEntity` effects. Shooter and owner may differ for `LivingItemUsePlayer`; preserve owner attribution and deliberately choose whose ability state is consulted.
- Elemental hits use `ElementDamage.of(...)`; consecutive extra hits can use `WeaponDamage.extraHit(...)` to handle hurt immunity. Vanilla fire damage and mod Pyro damage are distinct requests. Do not replace one with merely igniting the target, or assume every ammunition type receives arrow-only effects.

## Piercing: establish semantics before changing the comparison

Read the current `WishArrowEntity` together with the current mapped `AbstractArrow.tick`, `canHitEntity` and `onHitEntity` when changing piercing. The custom entity owns its damage and visited-target set instead of calling the vanilla hit implementation. The inherited tick loop uses `getPierceLevel()` to decide whether to search for another impact in the same tick.

Distinguish **total targets hit** from **additional targets after the first**. Vanilla piercing level N permits N+1 targets; this project's custom discard comparison may use a total-target budget instead. At the time this skill was written, the custom check was `piercedTargets.size() >= Math.max(1, getPierceLevel())`. Do not replace it with `>` or add/subtract 1 without confirming the requested meaning and testing a line of targets. The user confirmed the current Thundering Pulse piercing worked; the later repair was visual only.

Preserve already-hit exclusion in collision and homing so arrows cannot repeatedly damage or turn back toward the same target. Keep friendly/owner filtering and block collision. A custom unsynchronized piercing field exists only on the server; if changing client collision behavior or synchronizing it, check that the client also records visited targets before enabling repeated collision searches. A compile pass does not validate target counts or these loops.

## Textures and model perspectives

The held texture is a vertical strip of N square frames: width S, height S*N, with N equal to `cycle().frameCount()`. A sheet's total height does not change its native per-frame resolution. Keep the static inventory icon independent of the held animation and preserve the requested dimensions.

The default inventory item icon is **16×16**, showing a recognizable part of the bow body. For a larger held frame, take an exact native-pixel crop around the grip and characteristic body detail; do not shrink the whole bow into the icon or use the entire 32×32 frame as its inventory texture. Choose the crop for that artwork, without resampling. A 16×16 icon does not change a 32×32 held animation or its model scale. If the user already cropped or edited the icon, preserve that file and do not recrop or regenerate it. An explicit request for another icon size takes precedence.

The default model size corresponds to a 16×16 texture. `BowSpriteMesh.build` scales geometry uniformly by `S / 16` around `(0.5, 0.5, 0.5)`, including thickness: 32×32 is twice the baseline size, 64×64 is four times. UVs still span one frame. Do not normalize larger textures back to the same physical size or multiply the same resolution scale again in JSON or the renderer. Keep the grip near the frame center and stationary across frames so scaling preserves the hand anchor.

Resolution scaling must work with both left/right first-person and third-person transforms, including the active draw pose; never implement it only in the first-person hook. Preserve `separate_transforms` and the generated static icon for GUI, ground and fixed contexts, so their size and color remain independent of the held sheet. Adjust hand placement only when the actual artwork needs it, and distinguish static transform inspection from an in-game visual check.

Draw directly on the target grid with transparent background and crisp opaque pixels; preview by nearest-neighbor enlargement. Keep the grip and overall orientation consistent across draw frames. `WishBowRenderer` selects frames from the shot timer; a time-looping `.mcmeta` is not a substitute for draw animation. Its raw texture path includes `textures/` and `.png`; model texture references omit both.

Start new bow models from the **current** `thundering_pulse.json` structure, replacing its texture id:

| Model field | Purpose |
|---|---|
| `loader: neoforge:separate_transforms` | Select held versus static models by display context |
| Top-level `gui_light: front` | Inventory lighting; prevents the flat icon being shaded like a block |
| `base.parent: primogemcraft:item/example_wish_bow` | Inherits `builtin/entity` and the existing first/third-person transforms for both hands |
| Root and base `textures.particle` | The new bow's static icon, not the example bow's particle |
| `perspectives.gui`, `.fixed`, `.ground` | Each uses `minecraft:item/generated` with the new static icon as `textures.layer0` |

The base renderer still obtains the animated sheet from the new Java bow item's `texture()`; inheriting the example model does not select its bow texture. Reuse those transforms as a starting point, adjusting for materially different art geometry when necessary.

Do not register a new bow with only `parent: builtin/entity` and a particle texture. That omits hand transforms and caused the abnormal first/third-person display. A particle texture is not an inventory icon declaration. Feeding every perspective through the custom entity renderer without front GUI lighting caused the reported dark/off-color slot display; use a generated static icon instead of brightening the PNG to compensate.

Debug appearance in order: selected perspective and parent chain, light mode, texture/frame dimensions, mesh UVs/normals, then artwork. Hand transforms and the active draw pose compose; do not duplicate both in Java and JSON. Check left and right hands independently. Preserve mesh cache clearing on resource reload. Pure asset changes can be inspected with F3+T; Java changes need the updated mod and a restart.

The Viridescent Hunt wind-eye visual target is the shared Skyward Harp vortex at the smaller render scale, fixed **3-block** attraction radius and **75% opacity** (visible alpha, approximately `191/255`). Keep this setting specific to the wind eye; do not change Skyward Harp's own vortex. A skill-only request records this target without authorizing an implementation or texture edit.

## Verification

- For skill-only edits, validate Markdown/frontmatter, source links and `git diff --check`; no game build is necessary.
- For implementation/resource changes, follow the standard's build checks with the launcher resolved from the current environment. Parse changed JSON, resolve model/texture references, check frame count and PNG dimensions, and compare the static icon's intended colors with the resting frame. Confirm the default icon is a 16×16 native crop, name color matches base vanilla rarity, bow descriptions use generic damage wording in both languages, and recovery tags/classification follow the shared dismantling rules.
- Reuse the runnable Java checks in `tests/BowAttackCycleTest.java`, `BowRefinementTest.java`, `BowSpriteMeshTest.java` and, when relevant, `ThunderingPulseStatsTest.java`. Root `tests/` is not the Gradle test source set: `build` may report `test NO-SOURCE`. Compile these checks with their referenced pure Java sources into a temporary directory and run them explicitly; do not leave `.class` files in `tests/`.
- For gameplay changes, record a reproduction with lined-up targets, exact expected hit count, normal versus empowered arrows, repeated inputs, and a block stopping the arrow. Include ordinary arrows and any other ammunition affected by the change.
- For visuals, check hotbar and inventory colors, dropped item and item frame, both hand preferences in first/third person, idle versus drawing, animation reset after switching items, and resource reload. Follow the standard's conditions for launching the game. Report which checks actually ran; a successful build or static model check is not an in-game rendering test.
