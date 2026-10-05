---
name: primogemcraft-spell-primitive
description: Register or extend Genshin Craft (原的世界) spell primitives (基元) in PrimogemCraftNeo, including PrimitiveType, forged-component JSON, aspects, stats, localization, acquisition, and blueprint integration. Also use for closely related spell decorators and triggers; ordinary elemental weapon damage does not require this skill.
---

# Genshin Craft spell primitives

Apply [the project standard](../primogemcraft-standard/SKILL.md). This is an optional-mod integration, not PrimogemCraft's ordinary elemental weapon system. Paths below are relative to the repository root unless stated otherwise.

## Evidence and version boundary

The API and schemas below were inspected in the Genshin Craft **3.2.1** JAR, its embedded JSON and guidebook, and decompiled classes. They were not verified by an in-game run. Read [build.gradle](../../../build.gradle) for the current dependency before implementing; recheck changed APIs against the resolved version rather than upgrading the dependency to fit this skill.

Find the dependency through the current checkout's Gradle resolution or `GRADLE_USER_HOME` (otherwise the current user's `.gradle`). If sources are unavailable, inspect the JAR with `jar`, `javap`, and an available Java decompiler. Extract/decompile into a temporary directory, never into project sources or generated outputs. Do not copy machine-specific cache paths or decompiled variable names into code.

Useful upstream evidence, all under `net.hackermdch.genshincraft`:

| Concern | Classes / JAR resources |
|---|---|
| Registry ownership and built-ins | `GenshinCraft`, `misc.CommonEvents`, `spell.custom.SpellRegistries` |
| Type and data codecs | `spell.PrimitiveType`, `PrimitiveInstance`, `PrimitiveDefinition`, `ForgedComponentDefinition` |
| Minimal instant effect / configurable projectile | `spell.custom.PullPrimitive`, `BulletPrimitive` |
| Attributes and runtime | `spell.Stats`, `AspectDefinition`, `EffectSpec`, `SpellRuntimeIR`, `SpellPipeline`, `SpellRuntimeContext`, `entity.misc.SpellStub` |
| Item identity, manufacture and use | `item.misc.ForgedComponent`, `Blueprint`, `SpellRune`, `block.multi.ElementalForgeCoreEntity` |
| Real data examples | `data/genshincraft/genshincraft/forged_component/{pyro_bullet,explosion,pull,fan,repeat,on_hit_entity}.json` |
| Player workflow | `data/genshincraft/genshin_guidebook/{blueprint_design,elemental_forge}.ggb` |

## Choose the smallest registration path

There are two distinct registries:

- **Behavior types:** static registry `PrimitiveType.REGISTRY`, id `genshincraft:spell_primitive_type`. Java registers codecs and implementations here.
- **Playable components:** synchronized data-pack registry `ForgedComponentDefinition.REGISTRY_KEY`, id `genshincraft:forged_component`. JSON registers selectable, named components here.

Genshin Craft owns both registries. Add entries; do not recreate either registry or call `NewRegistryEvent`/`DataPackRegistryEvent.NewRegistry` to register them again.

Reuse a behavior for parameter-only variants. Built-in references are `genshincraft:bullet`, `genshincraft:explosion`, and `genshincraft:pull`. In 3.2.1, `bullet` already accepts `physical`, `quantum`, `imaginary`, `pyro`, `hydro`, `electro`, `cryo`, `dendro`, `anemo`, and `geo` damage values. A new damage/color/stat variant does not need a new Java type. Inspect each type's codec: the bullet's `damage` option is not automatically available on explosion or pull.

Use a decorator for a reusable change to existing effects, such as projectile steering; use a primitive for a new base effect. Inspect `DecoratorType`, `DecoratorInstance`, and the nearest built-in when that is the requested feature. Triggers have their own `TriggerType.REGISTRY`; registering a trigger entry alone does not make the runtime emit its event.

## Register the playable JSON entry

For component id `primogemcraft:quantum_bullet`, the resource path is:

`src/main/resources/data/primogemcraft/genshincraft/forged_component/quantum_bullet.json`

The extra **`genshincraft/` directory is required** by the namespaced registry path. Do not shorten this to `data/primogemcraft/forged_component/`. Follow the standard's data-generation rule when a relevant provider exists.

This illustrative entry reuses the existing bullet behavior; its numbers and art are examples, not mandatory balance or presentation:

```json
{
  "type": "primitive",
  "reference": "genshincraft:bullet",
  "texture": "genshincraft:item/spell/bullet",
  "name_color": "8066CC",
  "tint": "8066CC",
  "aspects": [
    "common:damageable",
    "common:sizeable",
    "common:countable",
    "common:movable",
    "common:destroyable"
  ],
  "stats": {
    "common:damage": 12.0,
    "common:count": 1.0,
    "common:lifetime": 100.0
  },
  "particle": {
    "options": { "type": "minecraft:end_rod" },
    "color": "8066CC"
  },
  "damage": "quantum",
  "cost": 5.6
}
```

- `type` chooses the component category (`primitive`, `decorator`, `trigger`); **`reference` chooses its behavior**. The primitive's codec fields are flattened alongside these fields, not nested inside a made-up `config` object.
- For primitives, `aspects` is required; `stats` is optional. `texture` and `cost` belong to the shared component definition. In 3.2.1, `cost` must be in **[1, 99]**; `name_color` is optional and `tint` defaults to no tint.
- `texture` is a resource location without `textures/` or `.png`: `primogemcraft:item/spell/example` resolves to `assets/primogemcraft/textures/item/spell/example.png`.
- Name lookup is **`forged_component.name.primogemcraft.quantum_bullet`**, not `item.primogemcraft.quantum_bullet`. Add the actual component name in both `zh_cn.json` and `en_us.json`. This key follows the upstream renderer's contract.

## Add Java only for a new behavior

1. Implement `PrimitiveInstance<YourPrimitive>` in the optional integration area. Implement `init(SpellRuntimeContext, SpellStub, EffectSpec)` and `type()`; override `onHitEntity`, `onHitBlock`, or `onExpire` only when needed. Read the current signatures before writing overrides.
2. Provide one stable `PrimitiveType<YourPrimitive>` whose `codec()` returns a `MapCodec<YourPrimitive>`. Use `MapCodec.unit(...)` for a parameterless behavior, following `PullPrimitive`; use `RecordCodecBuilder.mapCodec(...)` for configurable fields, following `BulletPrimitive`. `type()` must return the registered type, not a fresh instance.
3. Add a project registry class such as `registry/PGCSpellPrimitiveTypes.java` with a single field `REGISTRY`, initialized with `DeferredRegister.create(PrimitiveType.REGISTRY, PrimogemCraft.MOD_ID)`. Register the type under this mod's id, not through `GenshinCraft.SPELL_PRIMITIVE_TYPES`, which uses the upstream namespace.
4. Attach that deferred register to the mod event bus exactly once during mod construction, through the optional integration guard. Then add the playable JSON with `reference: "primogemcraft:<your_type_id>"`. A registered behavior without a JSON component is not a selectable base effect.

Follow the existing guard in [GenshinCraftIntegration.java](../../../src/main/java/net/per/primogemcraft/collab/genshincraft/GenshinCraftIntegration.java) and inspect [GenshinCraftBridge.java](../../../src/main/java/net/per/primogemcraft/collab/genshincraft/GenshinCraftBridge.java). Keep upstream class references and new registry initialization behind the loaded-mod boundary. Do not make `genshincraft` required or add unguarded automatic subscribers just to initialize this feature. A `compileOnly` dependency does not put the mod on the development runtime classpath. Check optional-mod resource behavior as well as Java class loading; do not assume a Java guard suppresses recipes or loot entries referencing absent upstream items.

Treat decoded primitive instances as shared definitions. Keep per-cast mutable state on the runtime, spell stub, or an appropriate per-execution behavior, not on the registered type or decoded instance. `SpellRuntimeContext` requires a server level; apply damage, healing, inventory changes, and spawning on the server. Use the existing runtime's entity and trigger flow instead of a second projectile/tick system when it expresses the effect.

## Aspects, defaults, and lifecycle traps

| Aspect | Stats initialized from registered defaults |
|---|---|
| `common:damageable` | `common:damage` |
| `common:sizeable` | `common:size` |
| `common:countable` | `common:count` |
| `common:movable` | `common:speed` |
| `common:destroyable` | `common:lifetime`, `common:destroy_on_hit` |

`EffectSpec` initializes `common:repeat`, then the selected aspects' stats, then applies explicit JSON `stats`. A stat having an upstream default does **not** mean it exists on every effect. In particular, a non-countable instant effect still needs a valid `common:count` (normally 1); explosion and pull explicitly supply it. Inspect the built-in's lifetime and disposal behavior too. `SpellRuntimeIR` rejects non-integer counts and counts outside 1–1024.

Declare only capabilities that the effect actually implements. Decorators' `require_aspects` must be satisfied; consume modified values through `EffectSpec.getStat(...)` rather than hardcoding them. New attributes can use the mod-bus `Stats.RegisterEvent` for defaults and, when needed, a registered `AspectDefinition`; first check whether existing stats suffice.

Instant effects can finish in `init`; sustained effects need an appropriate lifecycle. `PrimitiveInstance` has no general tick override: inspect `SpellBehavior`, `EffectSpec.addBehavior`, and `SpellStub` for sustained work. Check when behaviors attach and how removal triggers lifecycle events; do not assume `discard()` and normal expiry are interchangeable.

Test quantity, repeat, and triggered follow-ups together. The 1024 bound is per fork, not a whole-cast budget. Limit custom sustained work according to the requested effect; do not silently remove existing composition features.

## Finish the player-facing integration

All definitions share `GenshinItems.forged_component`. To construct a specific component stack on the server, resolve its holder from the level's `ForgedComponentDefinition.REGISTRY_KEY` registry and set `GenshinComponents.FORGED_COMPONENT` on that item. Do not register one Minecraft item per definition or hand-build obsolete NBT. For recipes/loot, inspect the current data-component codec and existing project serialization before choosing the output syntax.

Registration does not establish a survival acquisition route. When adding playable content, wire the requested recipe, reward, or loot path to a correctly component-bearing stack. Check creative visibility if requested; do not assume a new JSON entry supplies items to the player. Load payment, random-event, or choice-screen skills only if the chosen acquisition path crosses those systems.

The gameplay loop to preserve is **design table → write blueprint → matching physical components plus liquid primogems in elemental forge → spell rune → right-click cast**. Main sequences have no trigger; other sequences attach to an earlier sequence and need a trigger. Built-ins emit hit-entity, hit-block, and lifecycle-end events. In 3.2.1, child operations start in order in the same pipeline invocation; they do not wait for the previous projectile to expire. Use an explicit trigger for impact/expiry follow-ups.

Component cost affects both forging fluid and the resulting rune's cooldown; it is not just a shop price. Inspect `Blueprint.fluidCost` for the actual aggregation, rather than assuming all costs add. Baseline runes have 256 durability and consume one per normal cast; preserve this existing flow unless the request changes it.

## Verify the requested change

- Check resource path, codec field names, behavior id, aspect ids, translations, texture resolution, and acquisition stack identity against the current dependency. Parse JSON, but do not treat JSON syntax alone as codec validation.
- For implementation changes, compile/build using the project standard. Review both loaded and absent upstream-mod paths. Do not claim runtime compatibility from compilation.
- When a game run is requested, check selection in the design table, blueprint write, forge requirements and output, rune casting, modifier behavior, trigger positioning, repeated casts, and survival acquisition. For custom behavior include the relevant target/owner and lifecycle cases. An optional-mod feature also needs an absent-mod launch check before claiming that compatibility was tested.
- For edits to this skill only, validate frontmatter and links and run `git diff --check`; no game build is required. State which implementation or gameplay checks remain unrun.
