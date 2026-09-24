---
name: primogemcraft-standard
description: Use for any change inside the PrimogemCraftNeo repository — Java sources, registries, items/blocks, mixins, lang files, assets, data, or Gradle configuration — and whenever a task has to follow this project's coding, naming, localization, or verification rules.
---

# PrimogemCraftNeo project standard

Binding rules for every change in this repository. Follow them without being asked. Where they disagree with general Minecraft-mod habits, these win.

## What this project is

A from-scratch remake of the PrimogemCraft mod: a Genshin Impact × Honkai: Star Rail fan project for Minecraft. Its content is derived from two HoYo IPs, so names are not free-form authoring — they are looked up in the terminology dictionary. Load the `primogemcraft-term-lookup` skill before naming anything that comes from either game.

| Fact | Value |
|---|---|
| Minecraft | 1.21.1 |
| Loader | NeoForge 21.1.250 |
| Java | 21, toolchain pinned in `build.gradle` |
| Build plugin | ModDevGradle `net.neoforged.moddev` 2.0.146 |
| Mappings | Parchment `2024.11.17` for 1.21.1 |
| Mod id | `primogemcraft` |
| Display name | PrimogemCraftNeo |
| Root package | `net.per.primogemcraft` |
| License | MIT |
| Authors | PepperMR, hackermdch |

`gradle.properties` owns `mod_version`, `minecraft_version`, `neo_version`, and the Parchment versions. Never hardcode any of them in source or in `neoforge.mods.toml` — that file is a template under `src/main/templates` and is expanded by the `generateModMetadata` task.

Current layout: `PrimogemCraft.java` (the `@Mod` entry point), `registry/PGCItems.java`, `mixin/MixinPlugin.java`, `assets/primogemcraft/lang/*.json`, `primogemcraft.mixins.json`. Treat the existing files as the style reference; the tree is small on purpose and grows one feature at a time.

## Off-limits

- `build/`, `run/`, `.gradle/` are outputs and runtime state. Never edit, never commit, never treat as source.
- `src/generated/resources/` is produced by the data run. Never hand-edit it; when generated content is needed, write the data provider and run data generation.
- Never bump `minecraft_version`, `neo_version`, `mod_version`, or the Parchment versions unless the user asks for that upgrade.
- `build.gradle` currently declares no dependencies and only `mavenLocal()` as a repository. Do not add a dependency, repository, or plugin on your own initiative; propose it and wait.
- Registry ids, lang keys, and advancement ids are a compatibility surface (world saves, resource packs, other code). Rename one only when asked, and update every reference in the same change.
- Do not re-root or reorganize packages. `net.per.primogemcraft` stays the root.
- Do not delete or rewrite files you were not asked to touch, and never `git push`, force-push, or rewrite history.

## Code rules

1. **Write no comments.** The single exception is documentation required by a public API — Javadoc on a public class or method that other code consumes. Inline explanations, section banners, and comments restating the code are not allowed. No commented-out code, no `TODO`/`FIXME` left behind: if something is unfinished, say it in the reply instead.
2. Keep code clean and minimal. Four spaces, one class per file, no unused imports, no wildcard imports, no dead or unreachable code, no leftover debug prints.
3. Identifiers are English and meaningful. Never name anything with pinyin or pinyin initials.
4. Use `PrimogemCraft.MOD_ID` (static import where the file already does) instead of a `"primogemcraft"` literal, and `PrimogemCraft.LOGGER` for logging.
5. Registries: one class per registry domain in `net.per.primogemcraft.registry`, named `PGC` + domain (`PGCItems`, `PGCBlocks`, `PGCEffects`). A single `DeferredRegister` field named `REGISTRY`, registered from the mod constructor only — never from a static initializer or a second entry point.
6. Registry ids are lowercase `snake_case`, derived from the entry's official English name (`Dull Blade` → `dull_blade`, `Triangular Drum-roll Device` → `triangular_drum_roll_device`). The namespace comes from `MOD_ID`; never build a `ResourceLocation` from a hardcoded namespace string.
7. Lang keys follow the vanilla hierarchy and equal the registry id: `item.primogemcraft.<id>`, `block.primogemcraft.<id>`, `effect.primogemcraft.<id>`, and so on.
8. No player-visible literal string in Java. Every display name, tooltip, and message comes from a lang file. Java keeps only ids and log text.
9. Keep the client/server split honest: common code must never reference client-only classes, and client-only behavior belongs behind a `Dist` check or its own client-side class. Verify a change still loads on a dedicated server when it touches anything shared.
10. Match the formatting and structure of the neighbouring code rather than imposing a personal style.
11. New content belongs in `src/main/resources/assets/primogemcraft/` (models, textures, blockstates) and `.../data/primogemcraft/` for data. Recipes, tags, models, and loot tables should be produced by data generation once a provider exists, not hand-written.
12. Unless necessary—for example, when unboxing a wrapper class for a primitive type—all variables should be declared using the keyword `var`. Any existing variables found not declared with `var` should all be replaced with `var` declarations.
13. Unless necessary, do not explicitly use the `this` keyword to call methods or access fields.
14. **Mixin is a last resort, and it needs consent.** Reach for a NeoForge event, an existing extension point, a registry hook, a data file, or a delegating class first. Touch `mixin/` only when nothing in the public API can express the change, and only after the user has approved that specific mixin: state why a mixin is unavoidable in the reply and wait, never add one on your own initiative.

## Separate left-click and right-click actions

- All future left-click and right-click features must have separate input paths. Never infer a left click from `onEntitySwing`: right-click success and other animations can also swing the hand and accidentally spend items or grant rewards.
- Follow `client/WishMaterialValues.java`: handle left clicks in a `Dist.CLIENT` subscriber for `InputEvent.InteractionKeyMappingTriggered`, gated by `event.isAttack()`, no open screen, a present player, and the expected main-hand item. Keep right-click behavior in `Item.use` or the appropriate use/interact hook. Respect canceled input events and decide explicitly whether the custom action should also allow the vanilla attack.
- Client-only display actions may stay on the client. Inventory, rewards, and persistent state changes must use a dedicated serverbound payload registered in `network/PGCNetwork.java`. Recheck the current held item, player eligibility, and item state on the server; never trust client-supplied reward amounts or item state. See `client/LuckySpecialTicketInput.java`, `network/LuckySpecialTicketCashOutPayload.java`, and `item/misc/LuckySpecialTicketItem.java` for a complete example.
- When a right-click action needs no swing, use `InteractionResultHolder.consume(stack)` as `WishCoreItem.use` does. Suppressing the swing alone is not a replacement for separate input paths. Do not use timing flags or cooldowns to guess which button caused a swing.
- Review air, block, and entity targets; main hand versus offhand; a single item versus a stack; and repeated clicks. Verify that right-click never executes left-click behavior, left-click never executes right-click behavior, and each accepted action mutates state only once. Distinguish source/build checks from in-game checks that were actually run.

## Naming standard

Names are the part of this project most likely to be wrong, so they get their own rule.

- Every id, class, field, and method name is proper English, spelled out. Abbreviations are allowed only when the abbreviation itself is the established term (`hp`, `xp`, `id`, `nbt`, `tick`).
- Pinyin and pinyin initials — `mmolagao`, `cebunzhunxia`, `anqqh`, `dbmlk` — are legacy artifacts of the original mod. They are read-only evidence: useful to locate old content, never a naming standard, never copied into new code or new lang keys.
- The dictionary's `语言键` column records those legacy keys. Use it to identify what an entry was, then name the new thing from the official English name.
- One concept, one name: a term spelled one way in `en_us.json` must be spelled the same way in every other file, in ids, and in replies.

## Localization rules

- Every new content entry needs both `assets/primogemcraft/lang/zh_cn.json` and `en_us.json`. An entry in one file without the other is a bug.
- Both files are UTF-8 **without** BOM, 2-space indented, valid JSON, ending in a newline.
- The Chinese value is the Chinese name; the English value is the official HoYo English name when one exists. Look it up — never translate a Chinese name from memory into English, and never invent an "official" translation.
- Tooltip text is one lang key per displayed line, numbered `.tooltip.0`, `.tooltip.1`, … Never put a `\n` inside a value. When the text comes from the original project, its line count, line order, and color codes are fixed — see the `primogemcraft-text-migration` skill.
- Removing or renaming a key that other entries or code still reference is a break. Grep both lang files for the key before you change it.

## Workflow

- Read before writing. Search the repository for the closest existing thing first; this codebase is small, so there is rarely a reason to guess.
- Make the smallest change that satisfies the request. Do not reformat, refactor, rename, or "improve" unrelated code in the same edit.
- Keep each edit reviewable and its intent stated in one sentence.
- Prefer editing the real source over creating parallel copies, and prefer one correct implementation over a variant behind a flag.
- If a request conflicts with a rule here, say which rule and why before proceeding — or ask, if the conflict is not obviously yours to resolve.

## Verification

This checkout has **no Gradle wrapper scripts**: `gradlew`, `gradlew.bat`, and `gradle/wrapper/gradle-wrapper.jar` are all absent, and `gradle` is not on the PATH. Only `gradle/wrapper/gradle-wrapper.properties` remains, pinned to Gradle 9.7.1, and that distribution is already cached locally. Compile with the cached distribution:

```powershell
$gradle = Get-ChildItem "$env:USERPROFILE\.gradle\wrapper\dists\gradle-9.7.1-bin" -Recurse -Filter gradle.bat | Select-Object -First 1 -ExpandProperty FullName
& $gradle compileJava --console=plain
```

- `compileJava` is the check to run after a substantive change. `build` is the full check before calling work finished.
- `runClient`, `runServer`, `runData`, and `gameTestServer` open a game or write generated files and take minutes. Run them only when the user asks.
- Never claim a build passed without having run it. If verification was impossible — no toolchain, no network, a task that cannot finish in the session — say exactly that and what remains unverified.
- After a failed command, read the error before changing code. Do not retry the same command hoping for a different result.

## Communication

- Talk to the user in Chinese. Code, identifiers, lang keys, comments, and commit messages stay in English.
- Short and concrete: what changed, which files, what it means, what is left. Do not pad the answer or restate the request.
- Reference files by their exact relative path so they resolve in the editor.
- Commit only when asked to. State the intended message and wait.
- Report uncertainty as uncertainty. A guessed name, an unverified build, or an unread file must be described as such rather than presented as fact.
