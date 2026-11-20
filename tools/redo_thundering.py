"""重做 ThunderingPulseItem 的 Forge→Fabric 转换（此前 PowerShell 批量替换损坏了该文件）。"""
import re
import subprocess
import sys
from pathlib import Path

FAB = Path(r"F:\MCR\NPGC\PrimogemCraftFabric")
TARGET = FAB / "src/main/java/net/per/primogemcraft/item/weapon/ThunderingPulseItem.java"
TOOLS = FAB / "tools"
MOJMAP = TOOLS / "client-mojmap.txt"
TINY = Path.home() / ".gradle/caches/fabric-loom/1.20.1/net.fabricmc.yarn.1_20_1.1.20.1+build.10-v2/mappings.tiny"

# 先把单文件放进独立暂存目录，复用既有流水线，避免影响其它文件
staging = TOOLS / "staging-redo"
rel = "item/weapon/ThunderingPulseItem.java"
dst = staging / rel
dst.parent.mkdir(parents=True, exist_ok=True)
dst.write_text(TARGET.read_text(encoding="utf-8"), encoding="utf-8")

scripts = [
    ["yarn_rename.py", str(MOJMAP), str(TINY), str(staging)],
    ["compat_fix.py", str(staging)],
    ["registry_fix.py", str(staging)],
    ["business_fix.py", str(TINY), str(staging)],
]
for args in scripts:
    r = subprocess.run([sys.executable, str(TOOLS / args[0]), *args[1:]],
                       capture_output=True, text=True, encoding="utf-8", errors="replace")
    tail = (r.stdout or "").strip().splitlines()[-2:]
    print(f"[{args[0]}] rc={r.returncode} " + " | ".join(tail))
    if r.returncode != 0:
        print(r.stderr[-800:])
        raise SystemExit(1)

text = dst.read_text(encoding="utf-8")

# 流水线未覆盖的简单类名与平台机制
simple = {
    "ChatFormatting": "Formatting",
    "SoundSource": "SoundCategory",
    "ServerPlayer": "ServerPlayerEntity",
    "UseAnim": "UseAction",
    "EntityDataAccessor": "TrackedData",
    "SynchedEntityData": "DataTracker",
    "EntityDataSerializers": "TrackedDataHandlerRegistry",
    "Level": "World",
    "Player": "PlayerEntity",
}
for a, b in simple.items():
    text = re.sub(rf"\b{a}\b", b, text)

# Forge 事件 → Fabric API
text = text.replace(
    "import net.minecraftforge.event.TickEvent;\n"
    "import net.minecraftforge.event.server.ServerStoppedEvent;\n"
    "import net.minecraftforge.eventbus.api.SubscribeEvent;\n"
    "import net.minecraftforge.fml.common.Mod;\n",
    "import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;\n"
    "import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;\n")
text = re.sub(r"(?m)^import net\.minecraftforge\.[^\n]*\n", "", text)
text = re.sub(r"(?m)^@Mod\.EventBusSubscriber[^\n]*\n", "", text)
text = text.replace(
    "    @SubscribeEvent\n"
    "    public static void onServerTick(TickEvent.ServerTickEvent event) {\n"
    "        if (event.phase != TickEvent.Phase.END) return;\n",
    "    /** Forge 用 ServerTickEvent(END)；Fabric 的 END_SERVER_TICK 语义等价。 */\n"
    "    public static void registerEvents() {\n"
    "        ServerTickEvents.END_SERVER_TICK.register(server -> onServerTick());\n"
    "        ServerLifecycleEvents.SERVER_STOPPED.register(server -> PENDING_ARROWS.clear());\n"
    "    }\n\n"
    "    private static void onServerTick() {\n")
text = text.replace(
    "    @SubscribeEvent\n"
    "    public static void onServerStopped(ServerStoppedEvent event) {\n"
    "        PENDING_ARROWS.clear();\n"
    "    }\n\n", "")
text = text.replace("import javax.annotation.Nullable;",
                    "import org.jetbrains.annotations.Nullable;")

assert "mmport" not in text and "prmmogemcraft" not in text, "转换结果异常"
assert "net.minecraftforge" not in text, "仍有 Forge 引用"
assert "registerEvents" in text, "事件注册未生成"

TARGET.write_text(text, encoding="utf-8")
print("OK -> ThunderingPulseItem.java")
print("lines:", len(text.splitlines()))