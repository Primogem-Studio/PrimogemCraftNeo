"""精修 WishArrowEntity 的剩余 Yarn 对应（全部经 javap 核实）。"""
import re
from pathlib import Path

P = Path(r"F:\MCR\NPGC\PrimogemCraftFabric\src\main\java\net\per\primogemcraft\entity\misc\WishArrowEntity.java")
t = P.read_text(encoding="utf-8")
o = t

# 裸调用（本类继承自 Entity/ProjectileEntity，无接收者前缀）
bare = {
    "level()": "getWorld()",
    "position()": "getPos()",
    "tickCount": "age",
    "hasImpulse": "velocityDirty",
    "entityData": "dataTracker",
    "pickup": "pickupType",
    "yRotO": "prevYaw",
    "xRotO": "prevPitch",
    "yRot": "getYaw()",
    "xRot": "getPitch()",
}
for a, b in bare.items():
    if a.endswith("()"):
        t = re.sub(r"(?<![.\w])" + re.escape(a), b, t)
    elif a in ("yRot", "xRot"):
        # 字段形式 this.yRot / 裸 yRot 需转为方法调用
        t = re.sub(r"(?<![.\w])" + a + r"\b(?!\s*\()", b, t)
        t = re.sub(r"\bthis\." + a + r"\b(?!\s*\()", "this." + b, t)
    else:
        t = re.sub(r"(?<![.\w])" + a + r"\b", b, t)
        t = re.sub(r"\bthis\." + a + r"\b", "this." + b, t)

pairs = [
    # 射线上下文枚举
    ("RaycastContext.Block.", "RaycastContext.ShapeType."),
    ("RaycastContext.Fluid.", "RaycastContext.FluidHandling."),
    ("level().clip(", "getWorld().raycast("),
    # 命中结果
    ("hit.getLocation()", "hit.getPos()"),
    # 归属与附魔
    ("tame.isOwnedBy(", "tame.isOwner("),
    ("EnchantmentHelper.getDamageBonus(", "EnchantmentHelper.getAttackDamage("),
    ("EnchantmentHelper.applyDamageEffects(", "EnchantmentHelper.onTargetDamaged("),
    # Vec3d 字段访问 -> 方法
    (".scale(", ".multiply("),
]
for a, b in pairs:
    t = t.replace(a, b)

# PersistentProjectileEntity 需要显式导入（PickupPermission 限定名用到）
if "PersistentProjectileEntity.PickupPermission" in t and \
        "import net.minecraft.entity.projectile.PersistentProjectileEntity;" not in t:
    t = t.replace("import net.minecraft.entity.projectile.ArrowEntity;",
                  "import net.minecraft.entity.projectile.ArrowEntity;\n"
                  "import net.minecraft.entity.projectile.PersistentProjectileEntity;")

assert t.splitlines()[0] == "package net.per.primogemcraft.entity.misc;"
if t != o:
    P.write_text(t, encoding="utf-8")
    print("PATCHED WishArrowEntity.java")
else:
    print("UNCHANGED")