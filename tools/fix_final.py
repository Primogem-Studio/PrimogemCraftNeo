"""修完最后 7 处（全部经 javap 核实真实签名）。"""
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


# 顶点属性：Yarn 用 overlay/light/normal
patch("client/WishBowRenderer.java", [
    (".overlayCoords(", ".overlay("),
    (".uv2(", ".light("),
    (".getNormalMatrix(", ".normal("),
])
patch("render/entity/WishArrowRenderer.java", [
    (".uv(u, v)", ".texture(u, v)"),
])

# 渲染器贴图方法名
patch("render/entity/SkywardHarpVortexRenderer.java", [
    ("public Identifier getTextureLocation(", "public Identifier getTexture("),
])

# 背包插入与掉落
patch("system/living/LivingItemUsePlayer.java", [
    ("owner.getInventory().add(remainder)", "owner.getInventory().insertStack(remainder)"),
    ("owner.drop(remainder, false)", "owner.dropItem(remainder, false)"),
], [
    # 1.20.1 PlayerEntity 无 getProjectile 覆写点
    (r"    @Override\n    public ItemStack getProjectile\(ItemStack weapon\)",
     "    /** 1.20.1 无此覆写点，保留为本类自有方法。 */\n"
     "    public ItemStack getProjectile(ItemStack weapon)"),
])

# 三元表达式两分支类型不一致（extraHit 返回 void）：改为 if/else
patch("entity/misc/WishArrowEntity.java", [
    ("            var hurt = victim instanceof LivingEntity living\n"
     "                    ? WeaponDamage.extraHit(living, source, finalDamage) : victim.damage(source, finalDamage);",
     "            boolean hurt;\n"
     "            if (victim instanceof LivingEntity living) {\n"
     "                WeaponDamage.extraHit(living, source, finalDamage);\n"
     "                hurt = true;\n"
     "            } else {\n"
     "                hurt = victim.damage(source, finalDamage);\n"
     "            }"),
])

# JEI 入口值判定
patch("collab/jei/WishCoreTransferHandler.java", [
    ("castValue().is(", "castValue().isOf("),
])