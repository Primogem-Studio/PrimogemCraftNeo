"""从 mojmap + yarn tiny 提取方法名 Mojang->Yarn 对应，供批量转换使用。"""
import collections
import json
import pathlib
import re

FAB = pathlib.Path(r"F:\MCR\NPGC\PrimogemCraftFabric")
MOJ = FAB / "tools/client-mojmap.txt"
TINY = pathlib.Path.home() / ".gradle/caches/fabric-loom/1.20.1/net.fabricmc.yarn.1_20_1.1.20.1+build.10-v2/mappings.tiny"

# tiny v2: 'c\t<obf>\t<intermediary>\t<named>' / '\tm\t<desc>\t<obf>\t<intermediary>\t<named>'
# key = (owner_obf, method_obf) -> named
tiny_m = {}
owner = None
for line in TINY.read_text(encoding="utf-8", errors="replace").splitlines():
    if line.startswith("c\t"):
        owner = line.split("\t")[1]
    elif line.startswith("\tm\t") and owner:
        p = line.split("\t")
        if len(p) >= 6:
            tiny_m[(owner, p[3])] = p[5]

print("tiny method entries:", len(tiny_m))

# mojmap: 'full.Class -> obf:' 然后 '    ret name(args) -> obfName'
pairs = collections.defaultdict(collections.Counter)
cls_obf = None
for line in MOJ.read_text(encoding="utf-8", errors="replace").splitlines():
    if not line.startswith("    "):
        m = re.match(r"^([\w.$]+) -> (\w+):", line)
        if m:
            cls_obf = m.group(2)
        continue
    if cls_obf is None:
        continue
    m = re.search(r"\b(\w+)\([^)]*\) -> (\w+)$", line)
    if not m:
        continue
    mojang, obf = m.group(1), m.group(2)
    yarn = tiny_m.get((cls_obf, obf))
    if yarn and yarn != mojang and not yarn.startswith("method_"):
        pairs[mojang][yarn] += 1

# obf 方法名在不同类间大量复用，全局投票会产出错误对应
# （如 renderItem->setRenderHeader）。这里只保留“唯一候选”的名字，
# 多候选一律丢弃，交由人工按 owner 类确认，避免批量误改。
result = {}
ambiguous = {}
for k, v in pairs.items():
    if len(v) == 1:
        result[k] = next(iter(v))
    else:
        ambiguous[k] = dict(v)
out = FAB / "tools/sync-baseline/method-map.json"
out.write_text(json.dumps(result, indent=1, ensure_ascii=False), encoding="utf-8")
print("unambiguous method names:", len(result))
print("ambiguous (need manual owner check):", len(ambiguous))
(FAB / "tools/sync-baseline/method-ambiguous.json").write_text(
    json.dumps(ambiguous, indent=1, ensure_ascii=False), encoding="utf-8")

targets = ["fromNamespaceAndPath", "getDefaultInstance", "getDeltaMovement", "defineId",
           "renderItem", "setSecondsOnFire", "getMainHandItem", "getItemInHand",
           "setItemInHand", "addFreshEntity", "getLookAngle", "blockPosition",
           "getEyePosition", "isSameItemSameTags", "getCooldowns", "serverLevel",
           "closeContainer", "isShiftKeyDown", "pushPose", "popPose", "fireResistant",
           "inflate", "canApplyAtEnchantingTable", "doPostHurtEffects", "getBaseDamage",
           "isPassenger", "level", "position", "scale", "is", "shrink", "blit",
           "drawString", "width", "last", "list", "getItem", "getLocation", "uv"]
print("=== 目标方法对应 ===")
for t in targets:
    print(f"{t:28} -> {result.get(t, '(未解析)')}")