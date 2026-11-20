"""恢复并重做 WishArrowEntity 的转换（此前 PowerShell 批量替换损坏了该文件）。"""
import re
import subprocess
import sys
from pathlib import Path

FAB = Path(r"F:\MCR\NPGC\PrimogemCraftFabric")
FORGE = Path(r"F:\MCR\NPGC\PrimogemCraftForge")
REL = "entity/misc/WishArrowEntity.java"
SRC = FORGE / "src/main/java/net/per/primogemcraft" / REL
TARGET = FAB / "src/main/java/net/per/primogemcraft" / REL
TOOLS = FAB / "tools"
MOJMAP = TOOLS / "client-mojmap.txt"
TINY = Path.home() / ".gradle/caches/fabric-loom/1.20.1/net.fabricmc.yarn.1_20_1.1.20.1+build.10-v2/mappings.tiny"

staging = TOOLS / "staging-redo2"
dst = staging / REL
dst.parent.mkdir(parents=True, exist_ok=True)
dst.write_text(SRC.read_text(encoding="utf-8"), encoding="utf-8")

for args in [
    ["yarn_rename.py", str(MOJMAP), str(TINY), str(staging)],
    ["compat_fix.py", str(staging)],
    ["registry_fix.py", str(staging)],
    ["business_fix.py", str(TINY), str(staging)],
]:
    r = subprocess.run([sys.executable, str(TOOLS / args[0]), *args[1:]],
                       capture_output=True, text=True, encoding="utf-8", errors="replace")
    print(f"[{args[0]}] rc={r.returncode}")
    if r.returncode != 0:
        print(r.stderr[-800:])
        raise SystemExit(1)

text = dst.read_text(encoding="utf-8")

# 实体基类 Arrow -> ArrowEntity（不可误伤物品类 ArrowItem）
text = re.sub(r"\bextends Arrow\b", "extends ArrowEntity", text)
text = text.replace("import net.minecraft.entity.projectile.Arrow;",
                    "import net.minecraft.entity.projectile.ArrowEntity;")
for a, b in {
    "ChatFormatting": "Formatting",
    "SoundSource": "SoundCategory",
    "EntityDataAccessor": "TrackedData",
    "SynchedEntityData": "DataTracker",
    "EntityDataSerializers": "TrackedDataHandlerRegistry",
    "Level": "World",
    "Player": "PlayerEntity",
}.items():
    text = re.sub(rf"\b{a}\b", b, text)
# 修正被上一条连带改名的既有 Yarn 名
text = text.replace("ServerPlayerEntityEntity", "ServerPlayerEntity")
text = text.replace("PlayerEntityEntity", "PlayerEntity")
text = text.replace("ServerWorldWorld", "ServerWorld")
text = text.replace("import javax.annotation.Nullable;",
                    "import org.jetbrains.annotations.Nullable;")
text = re.sub(r"(?m)^import net\.minecraftforge\.[^\n]*\n", "", text)
text = re.sub(r"(?m)^\s*@SubscribeEvent\s*\n", "", text)
text = re.sub(r"(?m)^@Mod\.EventBusSubscriber[^\n]*\n", "", text)

assert text.startswith("package net.per.primogemcraft.entity.misc;"), "package 行异常"
assert "packagx" not in text and "primogxmcraft" not in text, "仍有损坏"
assert "extends ArrowEntity" in text, "基类未修正"

TARGET.write_text(text, encoding="utf-8")
print("OK -> WishArrowEntity.java, lines:", len(text.splitlines()))