"""精修剩余 API（全部经 javap 核实真实签名）。"""
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


patch("system/living/LivingItemUsePlayer.java", [
    ("result.getObject()", "result.getValue()"),
    ("CrossbowItem.getPullProgress(stack)", "CrossbowItem.getPullTime(stack)"),
    ("stack.getItem().useOnRelease(stack)", "stack.getItem().isUsedOnRelease(stack)"),
])

patch("recipe/TenPullWishCoreRecipe.java", [
    ("result.getOrCreateNbt().merge(firstTag)", "result.getOrCreateNbt().copyFrom(firstTag)"),
])

patch("client/WishBowRenderer.java", [
    ("MinecraftClient.getInstance().level", "MinecraftClient.getInstance().world"),
    ("ItemRenderer.getFoilBufferDirect(", "ItemRenderer.getDirectItemGlintConsumer("),
    ("poses.last()", "poses.peek()"),
])

patch("system/wish/WishCoreTransfer.java", [
    (".getMaxCount(remainder)", ".getMaxItemCount(remainder)"),
    ("menu.stillValid(player)", "menu.canUse(player)"),
    (".set(items.get(index))", ".setStack(items.get(index))"),
    ("menu.createResult()", "menu.updateResult()"),
])

patch("entity/misc/WishArrowEntity.java", [
    # getEntitiesByClass 需要 Predicate 参数
    ("getWorld().getEntitiesByClass(LivingEntity.class, getBoundingBox().expand(targetRange))",
     "getWorld().getEntitiesByClass(LivingEntity.class, getBoundingBox().expand(targetRange), e -> true)"),
    # setAttacker 需要 LivingEntity；victim 是 Entity，加类型判定
    ("if (owner instanceof LivingEntity living) living.setAttacker(victim);",
     "if (owner instanceof LivingEntity living && victim instanceof LivingEntity target) living.setAttacker(target);"),
])