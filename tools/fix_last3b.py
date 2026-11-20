"""修完最后 3 处（经 javap 核实：Entry.getNormalMatrix / VertexConsumer.overlay / next）。"""
from pathlib import Path

ROOT = Path(r"F:\MCR\NPGC\PrimogemCraftFabric\src\main\java\net\per\primogemcraft")


def patch(rel, pairs):
    p = ROOT / rel
    t = p.read_text(encoding="utf-8")
    o = t
    for a, b in pairs:
        t = t.replace(a, b)
    assert t.splitlines()[0].startswith("package net.per.primogemcraft."), rel
    if t != o:
        p.write_text(t, encoding="utf-8")
        print(f"PATCHED {rel}")
    else:
        print(f"UNCHANGED {rel}")


patch("client/WishBowRenderer.java", [
    ("pose.normal()", "pose.getNormalMatrix()"),
    (".endVertex();", ".next();"),
])

patch("render/entity/WishArrowRenderer.java", [
    (".overlayCoords(", ".overlay("),
    (".endVertex();", ".next();"),
])

patch("collab/rei/WishCoreTransferHandler.java", [
    ("castValue().is(", "castValue().isOf("),
])