"""修完最后三处：NativeImage 包路径、JSON 重载基类、Projectile 类型名。"""
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
    if t != o:
        p.write_text(t, encoding="utf-8")
        print(f"PATCHED {rel}")
    else:
        print(f"UNCHANGED {rel}")


patch("client/WishBowRenderer.java", [
    ("import com.mojang.blaze3d.platform.NativeImage;",
     "import net.minecraft.client.texture.NativeImage;"),
])

# JSON 重载监听：Fabric 侧还需实现 IdentifiableResourceReloadListener（参照 WishDrops）
patch("collab/teyvatdelight/CurrencyExchange.java", [
    ("extends SimpleJsonResourceReloadListener",
     "extends JsonDataLoader implements net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener"),
])

# Projectile 作为类型名时应为 ProjectileEntity（变量名 projectile 不受影响，大小写敏感）
patch("item/weapon/ThunderingPulseItem.java", [], [
    (r"\bProjectile\b", "ProjectileEntity"),
])