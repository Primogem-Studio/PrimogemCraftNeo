---
name: primogemcraft-standard
description: Use for any change inside the PrimogemCraftNeo repository — Java sources, registries, items/blocks, mixins, lang files, assets, data, or Gradle configuration — and whenever a task has to follow this project's coding, naming, localization, or verification rules.
---

# PrimogemCraftNeo project standard

Binding rules for every change in this repository. Follow them without being asked. Where they disagree with general Minecraft-mod habits, these win.

## Working with Ponytail

Keep the implementation and the reply short; keep the required behavior complete. Ponytail chooses the simplest implementation that satisfies the user's request and the applicable project contracts. It never removes payment compatibility, server validation, localization, interaction states, or necessary verification. Do not substitute a reduced feature and ask the user to request the rest again.

- Reuse the project's existing entry points before considering standard-library or platform alternatives. A shorter bypass of `OtherworldBankbook`, `ChoiceRegistry`, `GuiAtlas`, or `NineSliceButton` is not a working simplification.
- Read the affected flow and callers, then change only what is needed to complete the request. A shared root-cause fix and its required callers are in scope. Converting encountered local declarations to `var` is the explicit project-wide style exception below; other unrelated cleanup is not in scope. Shortest code means least unnecessary complexity, not compressed formatting.
- Project contracts and verification requirements constrain Ponytail's shortcuts. A domain skill supplies details within its domain; it does not require unrelated work. If two project rules genuinely disagree, inspect the implementation and state the discrepancy; do not silently weaken a contract.
- Keep implementation comments subject to the code rules below. Report a material limitation briefly in the reply; do not add `ponytail:` comments.

Load only the domain skills involved in the task, including a linked skill when its contract is crossed:

| Task touches | Skill |
|---|---|
| Fragment counts, costs, grants, destruction, or bankbooks | [primogemcraft-fragment-payment](../primogemcraft-fragment-payment/SKILL.md) |
| Card selection, reward reveals, callbacks, or payloads | [primogemcraft-choice-screen](../primogemcraft-choice-screen/SKILL.md) |
| Random events, conditions, groups, quotas, or challenges | [primogemcraft-random-event](../primogemcraft-random-event/SKILL.md) |
| Container panels, slots, scrolling, or their textures | [primogemcraft-gui-atlas](../primogemcraft-gui-atlas/SKILL.md) |
| Button appearance, states, or hit testing | [primogemcraft-nine-slice-button](../primogemcraft-nine-slice-button/SKILL.md) |

## Environment portability

- These skills work as plain Markdown instructions on Windows, Linux, and macOS, in an editor, terminal agent, or CI workspace. Read them directly when no skill loader is available. Ponytail is optional: the concise implementation and reply rules above remain in force without a global installation.
- Locate the repository root from the checkout (`git rev-parse --show-toplevel` when Git is available, otherwise locate `build.gradle` and `src/`). Run project commands there. Markdown links resolve from the containing file; source and command paths resolve from the repository root. Search for the named class if a documented source path has moved; do not recreate an obsolete package.
- Detect the available shell and tools. Prefer `rg`, with the editor search, `git grep`, or native filesystem search as fallbacks. Adapt quoting, executable suffixes, and path separators to that shell; do not assume PowerShell, a drive letter, a username, or a globally installed agent tool.
- Read current versions and dependencies from `gradle.properties`, `build.gradle`, and `gradle/wrapper/gradle-wrapper.properties`; descriptive version tables are orientation, not permission to override the checkout. Never change versions just to fit the current machine.
- Optional lookup skills and validators are conveniences, not prerequisites. If absent, inspect their source evidence or validate the Markdown/frontmatter and local links directly. If network access, a JDK, build dependencies, or a display is unavailable, continue independent work and state exactly which verification or lookup remains unavailable. Do not claim execution on an operating system that was not tested.

## What this project is

A from-scratch remake of the PrimogemCraft mod: a Genshin Impact × Honkai: Star Rail fan project for Minecraft. Its content is derived from two HoYo IPs, so names are not free-form authoring. Use `primogemcraft-term-lookup` when available; otherwise inspect the project's terminology resources and existing translations, then verify any new official term from an authoritative source. Do not invent an official name or block unrelated work because an optional lookup skill is missing.

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
- Read `build.gradle` for the current dependencies and repositories; reuse what is already installed. Do not add a dependency, repository, or plugin on your own initiative; propose it and wait unless the user has already authorized that addition.
- Registry ids, lang keys, and advancement ids are a compatibility surface (world saves, resource packs, other code). Rename one only when asked, and update every reference in the same change.
- Do not re-root or reorganize packages. `net.per.primogemcraft` stays the root.
- Change files required by the requested behavior, including shared implementations and their callers; do not delete or rewrite unrelated files. Never `git push`, force-push, or rewrite history.

## Code rules

1. **Write no comments.** The single exception is documentation required by a public API — Javadoc on a public class or method that other code consumes. Inline explanations, section banners, and comments restating the code are not allowed. No commented-out code, no `TODO`/`FIXME` left behind: if something is unfinished, say it in the reply instead.
2. Keep code clean and minimal. Four spaces, one class per file, no unused imports, no wildcard imports, no dead or unreachable code, no leftover debug prints.
3. Identifiers are English and meaningful. Never name anything with pinyin or pinyin initials.
4. Use `PrimogemCraft.MOD_ID` (static import where the file already does) instead of a `"primogemcraft"` literal, and `PrimogemCraft.LOGGER` for logging.
5. Registries: one class per registry domain in `net.per.primogemcraft.registry`, named `PGC` + domain (`PGCItems`, `PGCBlocks`, `PGCEffects`). A single `DeferredRegister` field named `REGISTRY`, registered from the mod constructor only — never from a static initializer or a second entry point.
6. Registry ids are lowercase `snake_case`, derived from the entry's official English name (`Dull Blade` → `dull_blade`, `Triangular Drum-roll Device` → `triangular_drum_roll_device`). The namespace comes from `MOD_ID`; never build a `ResourceLocation` from a hardcoded namespace string.
7. Lang keys follow the vanilla hierarchy and equal the registry id: `item.primogemcraft.<id>`, `block.primogemcraft.<id>`, `effect.primogemcraft.<id>`, and so on.
8. No player-visible literal string in Java. Every display name, tooltip, and message comes from a lang file. Java keeps only ids and log text.
9. Keep the client/server split honest: common code must never reference client-only classes, and client-only behavior belongs behind a `Dist` check or its own client-side class. Review shared changes for dedicated-server class loading; an actual server launch follows the verification rules below. Do not describe a source review as a successful runtime check.
10. Match the formatting and structure of the neighbouring code rather than imposing a personal style.
11. New content belongs in `src/main/resources/assets/primogemcraft/` (models, textures, blockstates) and `.../data/primogemcraft/` for data. Recipes, tags, models, and loot tables should be produced by data generation once a provider exists, not hand-written.
12. **Use `var` for all eligible local variable declarations throughout the project**, including loop variables and local try-with-resources declarations. Convert every eligible existing declaration encountered while reading or editing for a task, not only declarations whose logic changes. This conversion is an explicit exception to the smallest-diff and no-unrelated-cleanup rules; do not launch a separate repository-wide migration unless requested. Preserve behavior: inference must not alter overload selection, numeric precision, unboxing, generic types, or later assignments. Keep explicit types only where Java forbids `var` (such as fields, method parameters/return types, and catch parameters) or a declaration cannot preserve its semantics with straightforward inference (such as target-typed lambdas, a bare `null` initializer, or deliberate widening). Do not add casts, dummy initializers, or redesign control flow solely to force `var`.
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
- Item tooltip text is one lang key per displayed line, numbered `.tooltip.0`, `.tooltip.1`, … Do not embed `\n` in those values. Event/card descriptions may contain newlines when their renderer explicitly supports them. Preserve the original line count, order, and color codes when migrating text; use `primogemcraft-text-migration` when available, otherwise compare directly with the source text.
- Removing or renaming a key that other entries or code still reference is a break. Grep both lang files for the key before you change it.

## Workflow

- Read before writing. Search the repository for the closest existing thing first; this codebase is small, so there is rarely a reason to guess.
- Make the smallest change that satisfies the request and the all-eligible-locals `var` rule. That required conversion is the only standing cleanup exception; do not otherwise reformat, refactor, rename, or "improve" unrelated code in the same edit.
- Keep each edit reviewable and its intent stated in one sentence.
- Prefer editing the real source over creating parallel copies, and prefer one correct implementation over a variant behind a flag.
- An explicit user instruction overrides a project preference. Mention a material exception briefly and proceed within the authorized scope; do not ask for the same authorization again. Ask only when a necessary decision cannot be inferred or authorization is still missing.

## Verification

Resolve the build launcher in the current environment rather than assuming a wrapper, a cached distribution, or a Gradle installation exists:

1. Read the required Gradle version from `gradle/wrapper/gradle-wrapper.properties` and the Java toolchain from `build.gradle`. Check the available JDK and `JAVA_HOME`; do not overwrite the user's global configuration.
2. Prefer a complete repository wrapper: `gradlew.bat` with its wrapper JAR/properties on Windows, or `gradlew` with those files on Linux/macOS. A properties file alone is not a runnable wrapper. On POSIX shells a non-executable wrapper can be invoked with `sh ./gradlew`.
3. Without a complete wrapper, use an installed Gradle matching the pinned distribution (check `gradle --version`), or locate that exact distribution under the current `GRADLE_USER_HOME` cache. When unset, resolve the current user's `.gradle` directory through the environment. Use `bin/gradle.bat` on Windows or `bin/gradle` on Linux/macOS; do not select an arbitrary cached version or copy a previous machine's path.
4. If none is available, report the required version and missing launcher. Use an environment-supported provisioning route only when available and authorized; do not rewrite the wrapper, upgrade Gradle, or assume network access to hide the missing prerequisite.

With a complete wrapper, examples from the repository root are:

| Shell | Compile | Finish implementation changes |
|---|---|---|
| PowerShell | `.\gradlew.bat compileJava --console=plain` | `.\gradlew.bat build --console=plain` |
| Windows cmd | `gradlew.bat compileJava --console=plain` | `gradlew.bat build --console=plain` |
| Linux/macOS POSIX shell | `sh ./gradlew compileJava --console=plain` | `sh ./gradlew build --console=plain` |

For an installed or cached launcher, pass the same task arguments to its resolved executable. Quote paths with spaces; PowerShell needs `&` before a quoted executable path. No example implies that this checkout currently has wrapper scripts.

- After substantive Java changes, run `compileJava`; run `build` before finishing changes to code, packaged resources, or build configuration. Once checks pass, repeat only after relevant changes or new evidence of a problem.
- For skill/documentation-only edits, check the changed text, relative links, skill frontmatter where applicable, and `git diff --check`; no game build is required. Domain skills' compile instructions apply to implementation changes, not edits to their prose.
- For non-trivial logic, leave the smallest meaningful runnable regression check using Java and existing project facilities where feasible. Reuse a check that already covers the behavior. Do not add Python mirrors of Java logic, production self-tests, or a testing dependency merely to satisfy a generic Ponytail example. Compilation alone does not verify behavior. If a meaningful check requires the game, record a concrete reproduction and expected outcome, and report whether it was run.
- `runClient`, `runServer`, `runData`, and `gameTestServer` open a game or write generated files and take minutes. Run them only when the user asks.
- Never claim a build passed without having run it. If verification was impossible — no toolchain, no network, a task that cannot finish in the session — say exactly that and what remains unverified.
- After a failed command, read the error before changing code. Do not retry the same command hoping for a different result.

## Communication

- Talk to the user in Chinese. Code, identifiers, lang keys, comments, and commit messages stay in English.
- Default to one short Chinese paragraph or up to three short bullets: outcome, verification, and a material limitation or next action only if one exists. Link the main changed file when useful. Do not paste code already edited, narrate routine steps, list every touched file, or reproduce skill checklists unless asked. A requested report or explanation gets the detail it needs; never omit a failure or material risk merely to fit the default length.
- Use clickable file paths supported by the current editor.
- Commit only when asked to. Existing explicit authorization is sufficient; otherwise leave changes uncommitted.
- Report uncertainty as uncertainty. A guessed name, an unverified build, or an unread file must be described as such rather than presented as fact.
