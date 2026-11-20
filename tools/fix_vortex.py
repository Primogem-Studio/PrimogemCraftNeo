"""精修 SkywardHarpVortexEntity：NBT 读写与实体基类成员名（参照既有 DendroCoreEntity）。"""
import re
from pathlib import Path

P = Path(r"F:\MCR\NPGC\PrimogemCraftFabric\src\main\java\net\per\primogemcraft\entity\misc\SkywardHarpVortexEntity.java")
t = P.read_text(encoding="utf-8")
o = t

pairs = [
    ("readAdditionalSaveData", "readCustomDataFromNbt"),
    ("addAdditionalSaveData", "writeCustomDataToNbt"),
    ("defineSynchedData", "initDataTracker"),
    ("noPhysics", "noClip"),
    ("entityData.define(", "dataTracker.startTracking("),
    ("entityData", "dataTracker"),
    ("tickCount", "age"),
    ("level()", "getWorld()"),
    ("vortex.setPos(center)", "vortex.setPos(center.x, center.y, center.z)"),
    ("getWorld().getEntities(this,", "getWorld().getOtherEntities(this,"),
]
for a, b in pairs:
    t = t.replace(a, b)

assert t.splitlines()[0] == "package net.per.primogemcraft.entity.misc;"
if t != o:
    P.write_text(t, encoding="utf-8")
    print("PATCHED SkywardHarpVortexEntity.java")
else:
    print("UNCHANGED")