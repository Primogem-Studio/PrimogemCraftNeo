"""精修弓基类与活体物品假玩家的剩余 Yarn 对应（全部经 javap 核实真实签名）。"""
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
    # 双重前缀（此前批量替换叠加所致）
    ("PersistentProjectileEntity.PersistentProjectileEntity.PickupPermission",
     "PersistentProjectileEntity.PickupPermission"),
    # 玩家背包选中槽 / 创造模式
    ("getInventory().selected", "getInventory().selectedSlot"),
    ("getAbilities().instabuild", "getAbilities().creativeMode"),
    # 弩/弓弹药谓词：Yarn 常量名为 BOW_PROJECTILES
    ("return ARROW_OR_FIREWORK;", "return BOW_PROJECTILES;"),
    # 装备损坏广播
    ("entity.broadcastBreakEvent(hand)", "entity.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND)"),
    # Vec3d
    ("forward.cross(", "forward.crossProduct("),
    ("forward.scale(", "forward.multiply("),
    # 附魔台可用性：Yarn 的 Item 只有 isEnchantable(ItemStack)
    ("    public boolean isAcceptableItem(ItemStack stack, Enchantment enchantment) {\n"
     "        return super.isAcceptableItem(stack, enchantment)\n"
     "                || Items.BOW.isAcceptableItem(stack, enchantment);\n"
     "    }",
     "    public boolean isEnchantable(ItemStack stack) {\n"
     "        return super.isEnchantable(stack) || Items.BOW.isEnchantable(stack);\n"
     "    }"),
], [
    # Forge 箭矢离弦事件：Fabric 无对应钩子，去除该判定行
    (r"(?m)^[ \t]*if \(ForgeEventFactory\.onArrowLoose\([^\n]*\n", ""),
    (r"(?m)^import net\.minecraftforge\.[^\n]*\n", ""),
])

patch("system/living/LivingItemUsePlayer.java", [
    ("tickCount", "age"),
    ("direction.horizontalDistance()", "direction.horizontalLength()"),
    ("this.level()", "this.getWorld()"),
    ("this.position()", "this.getPos()"),
    (".items", ".main"),
], [
    (r"(?m)^import net\.minecraftforge\.[^\n]*\n", ""),
])