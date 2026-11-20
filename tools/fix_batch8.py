"""修完最后一批缺口（全部经 javap 或既有已编译代码核实）。"""
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


# 配方序列化器：Yarn 用 read/read/write
patch("recipe/TenPullWishCoreRecipe.java", [
    ("public TenPullWishCoreRecipe fromJson(", "public TenPullWishCoreRecipe read("),
    ("public TenPullWishCoreRecipe fromNetwork(", "public TenPullWishCoreRecipe read("),
    ("public void toNetwork(", "public void write("),
])

# 顶点提交：Yarn 用 getPositionMatrix / texture / light
patch("client/WishBowRenderer.java", [
    ("pose.getMatrices()", "pose.getPositionMatrix()"),
])
patch("render/entity/WishArrowRenderer.java", [
    ("pose.getMatrices()", "pose.getPositionMatrix()"),
    (".uv2(LightmapTextureManager.FULL_BRIGHT)", ".light(LightmapTextureManager.MAX_LIGHT_COORDINATE)"),
    (".uv2(", ".light("),
])

# 发射：Yarn ProjectileEntity 用 setVelocity
patch("system/weapon/WishWeaponBowItem.java", [
    ("projectile.shoot(", "projectile.setVelocity("),
])
patch("item/weapon/ThunderingPulseItem.java", [
    ("fireArrow.shoot(", "fireArrow.setVelocity("),
    ("player.getRotationVector().scale(", "player.getRotationVector().multiply("),
])

# 苍翠猎弓：Yarn Item 用 postHit
patch("item/weapon/TheViridescentHuntItem.java", [
    ("public boolean hurtEnemy(", "public boolean postHit("),
    ("super.hurtEnemy(", "super.postHit("),
])

# 假玩家：背包移除与弹药获取
patch("system/living/LivingItemUsePlayer.java", [
    ("getInventory().setStack(index)", "getInventory().removeStack(index)"),
    ("owner.getProjectile(weapon)",
     "net.minecraft.item.RangedWeaponItem.getHeldProjectile(owner, weapon.getItem() instanceof net.minecraft.item.RangedWeaponItem ranged "
     "? ranged.getProjectiles() : net.minecraft.item.RangedWeaponItem.BOW_PROJECTILES)"),
])

# 箭实体：实体查询需 Predicate；三元表达式不能含 void
patch("entity/misc/WishArrowEntity.java", [
    ("getWorld().getEntitiesByClass(LivingEntity.class, victim.getBoundingBox().expand(2.0D))",
     "getWorld().getEntitiesByClass(LivingEntity.class, victim.getBoundingBox().expand(2.0D), e -> true)"),
])

# JEI 入口值判定：Yarn ItemStack 用 isOf
patch("collab/jei/WishCoreTransferHandler.java", [], [
    (r"castValue\(\)\.is\(", "castValue().isOf("),
])

# Forge 专有手持变换扩展在 Fabric 无对应机制，去掉该调用
patch("client/WishBowClient.java", [], [
    (r"(?m)^[ \t]*WishBowClientExtensions\.clearMeshes\(\);[ \t]*\n",
     "                        // Forge 的 IClientItemExtensions 手持变换在 Fabric 无对应机制\n"),
])