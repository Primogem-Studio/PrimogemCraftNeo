"""精修覆写方法名与向量运算（全部经 javap 核实）。"""
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


patch("system/weapon/WishWeaponBowItem.java", [
    ("public int getDefaultProjectileRange()", "public int getRange()"),
    ("public void onUseTick(", "public void usageTick("),
    ("up.scale(", "up.multiply("),
], [
    # Forge 专有的重装备动画钩子，1.20.1 Item 无此方法
    (r"    @Override\n    public boolean shouldCauseReequipAnimation\(",
     "    /** Forge 专有钩子，1.20.1 无对应覆写点，保留为本类自有方法。 */\n"
     "    public boolean shouldCauseReequipAnimation("),
])

patch("entity/misc/WishArrowEntity.java", [
    ("protected ItemStack getPickupItem()", "protected ItemStack asItemStack()"),
    ("protected void onHitEntity(", "protected void onEntityHit("),
    ("direction.dot(", "direction.dotProduct("),
    ("applyDamageEffects(living)", "applyDamageEffects(living, this)"),
])

patch("entity/misc/SkywardHarpVortexEntity.java", [
    ("target.getVelocity().scale(", "target.getVelocity().multiply("),
], [
    # 1.20.1 Entity 无 getBoundingBoxForCulling 覆写点
    (r"    @Override\n    public Box getBoundingBoxForCulling\(\)",
     "    /** 1.20.1 无此覆写点，保留为本类自有方法。 */\n"
     "    public Box getBoundingBoxForCulling()"),
])

patch("client/WishBowRenderer.java", [
    (".uv(", ".texture("),
    ("public void onResourceManagerReload(ResourceManager resources)",
     "public void reload(ResourceManager resources)"),
])

patch("client/WeaponTutorialRenderer.java", [
    ("public void renderImage(TextRenderer font, int x, int y, DrawContext graphics)",
     "public void drawItems(TextRenderer font, int x, int y, DrawContext graphics)"),
])