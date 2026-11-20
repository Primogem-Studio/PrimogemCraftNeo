"""修正弓基类覆写方法签名（经 javap 核实 BowItem/RangedWeaponItem 真实成员）。"""
from pathlib import Path

P = Path(r"F:\MCR\NPGC\PrimogemCraftFabric\src\main\java\net\per\primogemcraft\system\weapon\WishWeaponBowItem.java")
t = P.read_text(encoding="utf-8")
o = t

pairs = [
    # BowItem.onStoppedUsing 对应 Mojang releaseUsing
    ("public void releaseUsing(ItemStack stack, World level, LivingEntity entity, int remainingTicks)",
     "public void onStoppedUsing(ItemStack stack, World level, LivingEntity entity, int remainingTicks)"),
    # RangedWeaponItem.getEnchantability 对应 Mojang getEnchantmentValue
    ("public int getEnchantmentValue()", "public int getEnchantability()"),
    # RangedWeaponItem.getProjectiles 对应 Mojang getAllSupportedProjectiles
    ("public Predicate<ItemStack> getAllSupportedProjectiles()",
     "public Predicate<ItemStack> getProjectiles()"),
]
for a, b in pairs:
    t = t.replace(a, b)

# customArrow 在 1.20.1 BowItem 中不存在（1.21 才有），去掉 @Override
t = t.replace(
    "    @Override\n    public PersistentProjectileEntity customArrow(PersistentProjectileEntity arrow) {",
    "    /** 1.20.1 的 BowItem 无此钩子，保留为本类自有方法。 */\n"
    "    public PersistentProjectileEntity customArrow(PersistentProjectileEntity arrow) {")

assert t.splitlines()[0] == "package net.per.primogemcraft.system.weapon;"
if t != o:
    P.write_text(t, encoding="utf-8")
    print("PATCHED WishWeaponBowItem.java")
else:
    print("UNCHANGED")