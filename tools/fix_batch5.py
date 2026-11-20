"""精修剩余渲染/容器/杂项 API（全部经 javap 或既有已编译代码核实）。"""
import re
from pathlib import Path

ROOT = Path(r"F:\MCR\NPGC\PrimogemCraftFabric\src\main\java\net\per\primogemcraft")

# 通用替换：作用于本次新增的所有类
COMMON = [
    ("graphics.blit(", "graphics.drawTexture("),
    ("LightTexture.MAX_LIGHT_COORDINATE", "LightmapTextureManager.MAX_LIGHT_COORDINATE"),
    ("LightTexture.", "LightmapTextureManager."),
    ("OverlayTexture.NO_OVERLAY", "OverlayTexture.DEFAULT_UV"),
    ("RenderLayer.entityTranslucentEmissive(", "RenderLayer.getEntityTranslucent("),
    ("RenderLayer.entityCutout(", "RenderLayer.getEntityCutout("),
    ("poseStack.last()", "poseStack.peek()"),
    (".containerId", ".syncId"),
    (".setDefaultPickupDelay()", ".resetPickupDelay()"),
    (".getPixelRGBA(", ".getColor("),
    (".getEntityModels()", ".getEntityModelLoader()"),
    ("Minecraft.getInstance().entityRenderDispatcher",
     "MinecraftClient.getInstance().getEntityRenderDispatcher()"),
    (".getPlayerByUUID(", ".getPlayerByUuid("),
    ("SoundEvents.PLAYER_ATTACK_SWEEP", "SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP"),
]

REL_LIST = Path(r"F:\MCR\NPGC\PrimogemCraftFabric\tools\sync-baseline\new-classes.txt")


def apply_common():
    rels = [l.strip() for l in REL_LIST.read_text(encoding="utf-8").splitlines() if l.strip()]
    n = 0
    for rel in rels:
        p = ROOT / rel
        if not p.exists():
            continue
        t = p.read_text(encoding="utf-8")
        o = t
        for a, b in COMMON:
            t = t.replace(a, b)
        # Vec3d 链式 scale/cross
        t = re.sub(r"\b(right|forward|offset|push|movement|vec|delta)\.scale\(", r"\1.multiply(", t)
        t = re.sub(r"\b(forward|right|up)\.cross\(", r"\1.crossProduct(", t)
        # 裸 position()/level()
        t = re.sub(r"(?<![.\w])position\(\)", "getPos()", t)
        t = re.sub(r"(?<![.\w])level\(\)", "getWorld()", t)
        if t != o:
            assert t.splitlines()[0].startswith("package net.per.primogemcraft."), rel
            p.write_text(t, encoding="utf-8")
            n += 1
    print(f"common patched: {n}")


def patch(rel, pairs):
    p = ROOT / rel
    t = p.read_text(encoding="utf-8")
    o = t
    for a, b in pairs:
        t = t.replace(a, b)
    if t != o:
        p.write_text(t, encoding="utf-8")
        print(f"PATCHED {rel}")


if __name__ == "__main__":
    apply_common()
    # 铁砧等级消耗改用访问器（levelCost 私有无 setter）
    patch("system/wish/WishCoreAnvil.java", [
        ("menu.setLevelCost(LEVEL_COST);",
         "((net.per.primogemcraft.mixin.AnvilScreenHandlerAccessor) menu)"
         ".primogemcraft$getLevelCostProperty().set(LEVEL_COST);"),
        ("menu.getLevelCost()", "menu.getLevelCost()"),
    ])