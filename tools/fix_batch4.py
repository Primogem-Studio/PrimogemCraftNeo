"""精修渲染器、配方、箭实体与假玩家剩余项（经 javap 核实）。"""
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


patch("render/entity/WishArrowRenderer.java", [
    ("getTextureLocation(", "getTexture("),
    ("poseStack.last()", "poseStack.peek()"),
    ("RenderLayer.entityTranslucentEmissive(", "RenderLayer.getEntityTranslucent("),
    ("RenderLayer.eyes(", "RenderLayer.getEyes("),
])

patch("recipe/TenPullWishCoreRecipe.java", [
    ("input.getItem(", "input.getStack("),
    ("input.getContainerSize()", "input.size()"),
])

patch("entity/misc/WishArrowEntity.java", [
    ("arrow.setPos(getPos())", "arrow.setPos(getX(), getY(), getZ())"),
    ("arrow.shoot(", "arrow.setVelocity("),
])

patch("system/living/LivingItemUsePlayer.java", [
    # Yarn 的 ServerPlayerEntity 构造器需要 server 参数
    ('super(level, new GameProfile(entity.getUuid(), "[LivingItem]"));',
     'super(level.getServer(), level, new GameProfile(entity.getUuid(), "[LivingItem]"));'),
    ("getEyeHeight()", "getStandingEyeHeight()"),
    ("setPos(getPos().add(offset))",
     "setPos(getPos().add(offset).x, getPos().add(offset).y, getPos().add(offset).z)"),
])