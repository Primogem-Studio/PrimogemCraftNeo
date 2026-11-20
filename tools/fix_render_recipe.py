"""精修渲染器与配方类（全部经 javap 核实真实签名）。"""
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


# 提示框组件：Yarn 的 getWidth 需要 TextRenderer 参数
patch("client/WeaponTutorialRenderer.java", [
    ("font.width(", "font.getWidth("),
])

# 箭渲染器：旋转轴与上一刻角度字段
patch("render/entity/WishArrowRenderer.java", [
    ("Axis.YP", "RotationAxis.POSITIVE_Y"),
    ("Axis.ZP", "RotationAxis.POSITIVE_Z"),
    ("Axis.XP", "RotationAxis.POSITIVE_X"),
    ("arrow.yRotO", "arrow.prevYaw"),
    ("arrow.xRotO", "arrow.prevPitch"),
    ("getTextureId(", "getTexture("),
], [
    (r"(?m)^import com\.mojang\.math\.Axis;\n", ""),
])

# 十连配方：Yarn 的 Ingredient/DefaultedList 工厂名
patch("recipe/TenPullWishCoreRecipe.java", [
    ("CraftingBookCategory", "CraftingRecipeCategory"),
    ("Ingredient.of(", "Ingredient.ofItems("),
    ("DefaultedList.<Ingredient>withSize(", "DefaultedList.<Ingredient>ofSize("),
    ("super.assemble(input, registries)", "super.craft(input, registries)"),
    ("public ItemStack assemble(", "public ItemStack craft("),
])