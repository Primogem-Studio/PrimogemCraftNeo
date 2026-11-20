"""精修许愿核心转移与活体物品编队（经 javap 核实）。"""
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


patch("system/wish/WishCoreTransfer.java", [
    ("menu.getCarried()", "menu.getCursorStack()"),
    ("slot.index", "slot.getIndex()"),
    ("slot.getItem()", "slot.getStack()"),
    (".getMaxCount(target)", ".getMaxItemCount(target)"),
], [
    (r"\bcandidate\.is\(", "candidate.isOf("),
])

# Vec3d.scale -> multiply（编队向量运算）
patch("system/living/LivingItemFormation.java", [], [
    (r"\b(right|forward|offset|vec)\.scale\(", r"\1.multiply("),
])