"""精修箭实体与假玩家剩余项（全部经 javap 核实真实签名）。"""
import re
from pathlib import Path

ROOT = Path(r"F:\MCR\NPGC\PrimogemCraftFabric\src\main\java\net\per\primogemcraft")


def patch(rel, pairs, regexes=()):
    p = ROOT / rel
    t = p.read_text(encoding="utf-8")
    o = t
    for a, b in pairs:
        t = t.replace(a, b)
    for pat, rep in regexes:
        t = re.sub(pat, rep, t)
    assert t.splitlines()[0].startswith("package net.per.primogemcraft."), rel
    if t != o:
        p.write_text(t, encoding="utf-8")
        print(f"PATCHED {rel}")
    else:
        print(f"UNCHANGED {rel}")


patch("entity/misc/WishArrowEntity.java", [
    ("dataTracker.define(", "dataTracker.startTracking("),
    (".distanceToSqr(", ".squaredDistanceTo("),
    ("getVelocity().yRot(", "getVelocity().rotateY("),
    ("getWorld().clip(", "getWorld().raycast("),
    ("getWorld().getEntitiesOfClass(", "getWorld().getEntitiesByClass("),
    ("living.push(", "living.addVelocity("),
    ("super.onHitBlock(", "super.onBlockHit("),
    ("protected void onHitBlock(", "protected void onBlockHit("),
])

patch("system/living/LivingItemUsePlayer.java", [
    ("direction.normalize().scale(", "direction.normalize().multiply("),
    ("setPos(position().add(offset))", "setPos(getPos().add(offset))"),
    ("getAttributes().assignValues(", "getAttributes().setFrom("),
    ("stack.use(level(), this, Hand.MAIN_HAND)", "stack.use(getWorld(), this, Hand.MAIN_HAND)"),
    ("useItem = stack;", "activeItemStack = stack;"),
    ("stack.useOnRelease()", "stack.getItem().useOnRelease(stack)"),
], [
    # Forge 专有的“能否继续使用”钩子：Fabric 无对应，改为物品一致性判断
    (r"if \(!ForgeHooks\.canContinueUsing\(getActiveItem\(\), stack\)\)",
     "if (!ItemStack.areEqual(getActiveItem(), stack))"),
    (r"(?m)^import net\.minecraftforge\.[^\n]*\n", ""),
])