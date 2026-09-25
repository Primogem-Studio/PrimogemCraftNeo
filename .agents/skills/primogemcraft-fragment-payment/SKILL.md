---
name: primogemcraft-fragment-payment
description: Use whenever PrimogemCraftNeo code consumes, grants, counts, prices, or destroys Cosmic Fragments (宇宙碎片) for a player — event costs and rewards, shop prices, command costs, curio conversions, loot rewards, new payment or reward paths — or when touching the Otherworld Bankbook (异世界存折), its stored amount, its auto-pay or auto-pickup mode, its chat reports, or any "not enough fragments" check. Every custom path in this mod that consumes or grants fragments must go through OtherworldBankbook, so stored fragments can pay and granted fragments can be banked, and every movement reports the amount and what is left.
---

# Cosmic Fragment payment

Apply [the project standard](../primogemcraft-standard/SKILL.md) for Ponytail compatibility, verification, and concise replies. The bankbook contract is part of the minimum working solution; reuse its entry points rather than adding a shorter inventory-only path.

**The rule, binding, in both directions:**

1. Every custom method in this mod that **consumes** Cosmic Fragments for a player counts and pays through `OtherworldBankbook`, never through `PlayerItems` alone. A path that calls `PlayerItems.count`/`PlayerItems.take` on `PGCItems.COSMIC_FRAGMENT` is incomplete — it ignores the fragments the player has stored in the Otherworld Bankbook, which the item's own tooltip promises to spend "only when paying at events or shops". In the words it was asked for: 所有的本mod的自定义方法实现消耗宇宙碎片都应当兼容异世界存折。
2. Every custom method that **grants** Cosmic Fragments hands them over through `OtherworldBankbook.give`, never through `Curios.give`/`PlayerItems.give`. A grant that writes straight into the inventory ignores the auto-pickup mode the player set for exactly this, and leaves the fragments outside the wallet the next payment will be drawn from. In the words it was asked for: 所有的自定义赋予宇宙碎片都应当兼容存折。
3. Every movement through the bankbook **reports itself** in chat — how much was paid, how much was banked, and how much is left. `payFragments`, `storeFragments` and the manual deposit each carry their own line; no call site adds one. See *Reporting* below.

This is a player-facing contract, not a convenience. For 4096 fragments the bankbook is the only practical wallet; a path that cannot see it silently costs the player the whole stock they set aside for exactly this. The two directions are one promise — fragments in, fragments out, all through the bankbook.

## Framework map

| Piece | Path | Role |
|---|---|---|
| `OtherworldBankbook` | `src/main/java/net/per/primogemcraft/item/misc/OtherworldBankbook.java` | The one payment API. Counting, paying, storing, withdrawing, finding the bankbooks a player carries. |
| `OtherworldBankbookItem` | `.../item/misc/OtherworldBankbookItem.java` | The item: right-click cycles the mode, sneak-left-click deposits, sneak-right-click withdraws one stack. |
| `OtherworldBankbookEvents` | `.../item/misc/OtherworldBankbookEvents.java` | `ItemEntityPickupEvent.Pre` — absorbs a picked-up fragment stack into bankbooks that have auto-pickup. |
| `PGCDataComponents.BANKBOOK_FRAGMENTS` / `BANKBOOK_MODE` | `src/main/java/net/per/primogemcraft/registry/PGCDataComponents.java` | The two components: stored amount and mode. |
| `PlayerItems` | `src/main/java/net/per/primogemcraft/util/PlayerItems.java` | Inventory-only `count`/`take`/`give`. Correct for other items and for a path that must not bank (a withdrawal), wrong for a fragment payment or grant. |
| `CuriosIntegration.equipped` / `CuriosBridge` | `src/main/java/net/per/primogemcraft/system/curio/compat/` | Lets the bankbook sit in the accessory slot and be found there. |
| Paths that already comply — payment | `system/shop/HertaShop.java`, `system/event/EventContext.java` | The reference implementations for a cost. |
| Paths that already comply — grant | `system/event/EventContext.java`, `system/curio/CurioContext.java`, `system/curio/CurioReward.java`, `item/misc/RandomEventErrorCodeItem.java`, `item/curio/RupertEmpireMechanicalGearItem.java` | Every one of them hands its rewards to `OtherworldBankbook.give`. |

## The API

| Method | Behaviour |
|---|---|
| `isBankbook(ItemStack)` | Whether a stack is `PGCItems.OTHERWORLD_BANKBOOK`. |
| `stored(ItemStack)` / `mode(ItemStack)` / `room(ItemStack)` | Stored amount, mode, and how much more fits. |
| `pays(ItemStack)` / `picksUp(ItemStack)` | Whether the mode includes auto-pay / auto-pickup. |
| `store(stack, amount)` / `withdraw(stack, amount)` | Move fragments in or out of one bankbook; both return the amount actually moved. |
| `held(ServerPlayer)` | Every bankbook the player has: inventory, offhand, and equipped curios. |
| `deposit(player, bankbook)` | Moves as many carried fragments into that bankbook as fit. |
| `canPayFragments(player, amount)` | **The reading half.** `true` when the inventory alone covers `amount`, or when the bankbooks that have auto-pay cover the rest. |
| `payFragments(player, amount)` | **The paying half.** Takes from the inventory first, then withdraws from each auto-pay bankbook in turn, and returns whether the whole amount was covered. |
| `isFragment(ItemStack)` | Whether a stack is `PGCItems.COSMIC_FRAGMENT`. |
| `storeFragments(player, amount)` | Stores an amount into the auto-pickup bankbooks, reports what was banked, and returns how much was banked. |
| `totalStored(player)` | The sum of `stored` over every bankbook the player carries, whatever its mode. |
| `give(player, stack)` | **The granting half.** A fragment stack goes into the auto-pickup bankbooks, and only the part that does not fit reaches the inventory; any other stack goes straight to the inventory exactly like `Curios.give`. |
| `absorbFragments(player, dropped)` | Auto-pickup: stores from a dropped fragment stack into auto-pickup bankbooks and shrinks it by what was accepted. |

Mode values are `MODE_OFF`, `MODE_PAY`, `MODE_PICKUP`, `MODE_BOTH`; a bankbook that is not in an auto-pay mode never pays, whatever it stores, and one that is not in an auto-pickup mode never banks a grant.

## The contract

A payment is always the same two calls, in this order, on the same `ServerPlayer`:

```java
if (!OtherworldBankbook.canPayFragments(player, cost)) return deny();
return OtherworldBankbook.payFragments(player, cost);
```

The two halves must move together. Whatever the player is shown as the cost, whatever condition dims the card or the button, and whatever actually charges must all answer the same question: `canPayFragments`. Widening only the check makes a card look clickable and then fail; widening only the payment charges a player who was told they could not afford it.

`EventContext` is the reference for a cost paid inside an event — the check and the payment are one pair, and the `EventCondition.fragments(n)` label that the screen prints under `条件不足` mirrors the same number:

```java
public boolean hasFragments(int amount) {
    return OtherworldBankbook.canPayFragments(player, amount);
}

public boolean fragments(int amount) {
    if (!hasFragments(amount)) return deny();
    return OtherworldBankbook.payFragments(player, amount);
}
```

`HertaShop` is the reference for a shop: `unaffordableFragments` reports the shortfall to the player, `chargeFragments` pays, and `EnchantCost.of(price, payer -> canPayFragments(...), payer -> payFragments(...))` carries the same pair into the enchant-price screen. Note that its generic `unaffordable`/`charge` pair is for other items and still uses `PlayerItems` — that is correct, and only the fragment pair goes through the bankbook.

Ordering rules that follow:

- Pay **after** the step that can still fail. `EventContext.enchant(grade, fragments)` opens the enchant screen first and only then pays, so a screen that does not open costs nothing.
- An `&&` chain has no rollback. Check affordability and other prerequisites before mutation, and coordinate fallible effects and payment through the existing helper on the server thread. Keep payment after the fallible screen-opening step as `enchant` does; do not generalize this into granting rewards before an unchecked payment. If either half can still fail after the other commits, handle that failure explicitly rather than rearranging the chain.
- Never combine a bankbook payment with a separate `PlayerItems.take` of the same fragments. One cost is paid once, in one call.

## The grant contract

A grant of fragments is one call:

```java
OtherworldBankbook.give(player, new ItemStack(PGCItems.COSMIC_FRAGMENT.get(), amount));
```

`give` banks whatever the auto-pickup bankbooks accept and hands only the remainder to the inventory, so a reward is never silently swallowed and never lands outside the wallet the next payment draws from. A non-fragment stack is passed to `Curios.give` unchanged, which is what makes it safe at a choke point that carries mixed rewards — `EventContext.give`, `CurioContext.give`, `CurioReward.grant` and `RandomEventErrorCodeItem` all route every reward through it for exactly that reason.

Three rules follow:

- **Do not grant fragments with `Curios.give` or `PlayerItems.give`.** Those miss the auto-pickup mode and leave the fragments in the inventory, where the next payment still has to reach them by hand.
- **Withdrawal is the one grant that must not be banked.** `OtherworldBankbookItem.withdraw` hands fragments back with `Curios.give` on purpose: routing it through `give` would let the very bankbook the player just drew from swallow them again in `MODE_BOTH`.
- **A reward that lands in the world needs nothing.** `CurioLoot.spawn`, the wish loot tables, the jar loot tables and any other `ItemEntity` are absorbed by `OtherworldBankbookEvents` when the player picks them up, so data-driven fragment drops are already covered.

## Reporting

Every movement of fragments through the bankbook announces itself in the player's chat, and the announcement is part of the method that moves them — never a call site's job:

| Movement | Where it is printed | Key | Arguments |
|---|---|---|---|
| Paid from a bankbook (`payFragments`) | `report` inside `payFragments` | `message.primogemcraft.otherworld_bankbook.paid` | amount from the bankbook, total left |
| Banked by `storeFragments` — a grant or an auto-pickup | `report` inside `storeFragments` | `message.primogemcraft.otherworld_bankbook.received` | amount banked, total left |
| Manual deposit (sneak-left-click) | `OtherworldBankbookItem.deposit` | `message.primogemcraft.otherworld_bankbook.deposited` | amount deposited, total left |

`report` stays silent when nothing moved, which is what keeps an inventory-only payment or a grant with no bankbook from spamming. It prints on chat (`displayClientMessage(..., false)`), while the item's own mode and withdrawal messages stay on the action bar.

The manual deposit is the one path that does not pass through `storeFragments` — it fills one named bankbook through `store` — so it carries its own message. Do not "unify" it by routing it through `storeFragments`: that would ignore the specific bankbook the player clicked and print a second line on top of the first.

A withdrawal is the one movement with no remainder line: `OtherworldBankbookItem.withdraw` prints the taken amount on the action bar and leaves the rest to the tooltip the player is already holding. It is neither a payment nor a bank, so it is not routed through `report`. If that is ever changed, change it there and not inside `withdraw`.

## Adding a fragment cost or reward

1. **Cost.** Write the check and the payment as one pair — `canPayFragments` for whatever dims the card, shows the price or gates the button, `payFragments` to charge. Never `PlayerItems.count`/`take`.
2. **Reward.** Hand the stack over with `OtherworldBankbook.give`, never `Curios.give`/`PlayerItems.give`. A reward that enters the world as an `ItemEntity` instead needs nothing.
3. **Event costs.** Put the check behind an `EventCondition.fragments(n)` factory, so the line the player reads under `条件不足` names the same number the payment charges. Keep the action and the condition in step.
4. **Price text.** The number the player reads is a lang value in both `zh_cn.json` and `en_us.json`, and it must match the number in the code.
5. **Messages.** Add none. `payFragments` and `storeFragments` report on their own; a call site printing its own confirmation is a second line.
6. **Verify.** `compileJava`, then the in-game checks below — the automatic ones go through both modes, because a mode is a player setting and a path that ignores it looks correct while the bankbook is empty.

## Where `PlayerItems` is still in use

These read or destroy fragments without the bankbook. They are the open follow-ups of this rule, not endorsements — a change to any of them must decide explicitly whether the bankbook takes part and say so:

| Path | What it does today |
|---|---|
| `item/curio/CavitySystemModelItem.java` | Charges carried fragments for a buff, and drains every carried fragment on the non-sneak use. |
| `item/curio/TypicalGeniusSocietyGossipItem.java` | Converts carried fragments into experience. |
| `item/curio/RupertEmpireMechanicalGearItem.java` | Counts carried fragments as a gate and purges the whole carried stock past a threshold; its fragment gift already goes through `OtherworldBankbook.give`. |
| `item/curio/RobeOfTheBeautyItem.java` | Only reads the carried count, to scale damage. No consumption. |

## Traps

- **Stored is not spendable.** `stored(stack) > 0` says nothing about a payment; only `canPayFragments` knows the mode. A check written against `stored` or against `held` sums will charge a player whose bankbook is in `MODE_OFF`.
- **`held` is inventory, offhand and equipped curios.** A bankbook in a chest, a shulker box, an ender chest, or another player's hands does not exist for payment. Do not widen `held` to "everywhere" to make a test pass.
- **Several bankbooks add up.** `payFragments` withdraws from them in `held` order until the amount is covered, so the paid amount can come from two items at once. Never compute a cost from one bankbook's `stored`, and never assume a single `withdraw` covers it.
- **`WITHDRAWAL` is the item's sneak-right-click cap, not a payment cap.** Paying is limited by `stored` and by the amount asked for.
- **Auto-pickup covers two routes and no more.** A fragment stack that lands in the world is absorbed by the `ItemEntityPickupEvent.Pre` hook, and a stack handed over by mod code is banked by `OtherworldBankbook.give`. A stack that reaches the inventory any other way — a bare `Curios.give`, a loot table that adds to the inventory itself, another mod's helper — is seen by neither.
- **A grant follows the mode and must not force its way in.** A bankbook in `MODE_OFF` or `MODE_PAY` leaves a granted stack in the inventory, which is the player's setting working as intended. Never write `BANKBOOK_FRAGMENTS` directly, and never clear the inventory count to "top up" a bankbook.
- **The unbanked remainder is a feature.** `give` puts whatever the bankbook cannot hold — a full 4096, or no bankbook at all — into the inventory and drops the rest on the ground like `Curios.give` does. A reward must never disappear because the wallet was full.
- **A grant and a payment are two different modes.** Auto-pickup and auto-pay are independent bits of the same field; `MODE_PAY` alone banks nothing and `MODE_PICKUP` alone pays nothing. Test both when either is touched.
- **Do not add a second payment API.** No capability reads, no NBT poking, no "find the bankbook" helper beside `held`. `OtherworldBankbook` is the single entry point, and a parallel implementation is exactly how the two halves drift apart.
- **Do not make the bankbook a curio.** It fits the accessory slot through the `CuriosBridge` predicate; registering it as a `CurioItem` would give it a form, a durability and triggers it is not supposed to have.
- **A fragment cost is a lang number, not a literal.** The cost the player reads comes from a lang value in both `zh_cn.json` and `en_us.json`; the number in the code and the number in the text must match, because the tooltip is the only place the player can see the price.
- **Never announce a bankbook movement twice.** `payFragments` and `storeFragments` already report; a call site that prints its own "存折已支付" line on top of that is the bug. Change the report inside the bankbook, not beside it.
- **The reported remainder is the wallet total, not one item's.** `totalStored` sums every bankbook the player carries, including `MODE_OFF` ones, because the player is reading "how much do I still have", not "which item paid". A per-bankbook number would be wrong the moment a payment spans two bankbooks.

## Verification

For implementation changes, run `compileJava` and the required completion checks using the current environment's launcher resolved by [the project verification rules](../primogemcraft-standard/SKILL.md#verification).

Compilation proves nothing about payment or grant behaviour. The manual checks need a running game:

- Deposit fragments into a bankbook, set it to auto-pay, and confirm the event card or shop entry can be bought with the bankbook alone; then set it to `MODE_OFF` and confirm the same purchase is refused with `条件不足`.
- Set the bankbook to auto-pickup, clear the inventory of fragments, and confirm a granted reward — the `fragments/*` event is the easiest — lands in the bankbook rather than the inventory, and that the chat line names the banked amount and the total left; then set it to `MODE_OFF` and confirm the same reward lands in the inventory with no line.
- Pay from a bankbook and confirm one chat line names the amount the bankbook paid and the total left, with no second line from the shop or the event.
- With the bankbook in `MODE_BOTH`, sneak-right-click a withdrawal and confirm the fragments reach the inventory instead of being taken straight back.

Say plainly that these were not run unless they were.
