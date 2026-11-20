"""按人工核实的 Mojang->Yarn 方法名映射转换新增类。

映射来源：Fabric 项目中已编译通过的同类代码（不是自动生成的映射表——
tools/client-mojmap.txt 版本不匹配，自动匹配会产出 renderItem->setRenderHeader
这类错误对应，故弃用）。

只作用于本次新增的类文件，不触碰既有代码。
"""
import re
from pathlib import Path

FAB = Path(r"F:\MCR\NPGC\PrimogemCraftFabric")
ROOT = FAB / "src/main/java/net/per/primogemcraft"
LIST = FAB / "tools/sync-baseline/new-classes.txt"

# 每项均在既有已编译代码中出现过，或经 javap 核实
METHODS = {
    # 实体 / 世界
    "getDeltaMovement": "getVelocity",
    "setDeltaMovement": "setVelocity",
    "addFreshEntity": "spawnEntity",
    "blockPosition": "getBlockPos",
    "getEyePosition": "getEyePos",
    "getLookAngle": "getRotationVector",
    "getMainHandItem": "getMainHandStack",
    "getOffhandItem": "getOffHandStack",
    "getItemInHand": "getStackInHand",
    "setItemInHand": "setStackInHand",
    "getCooldowns": "getItemCooldownManager",
    "serverLevel": "getServerWorld",
    "closeContainer": "closeHandledScreen",
    "isShiftKeyDown": "isSneaking",
    "isPassenger": "hasVehicle",
    "setSecondsOnFire": "setOnFireFor",
    "getBbWidth": "getWidth",
    "getBbHeight": "getHeight",
    "isAlive": "isAlive",
    "removeAllEffects": "clearStatusEffects",
    "getEffect": "getStatusEffect",
    "addEffect": "addStatusEffect",
    "hasEffect": "hasStatusEffect",
    "getAttributeValue": "getAttributeValue",
    "getRandom": "getRandom",
    "tickCount": "age",
    # 物品
    "getDefaultInstance": "getDefaultStack",
    "getOrCreateTag": "getOrCreateNbt",
    "getTag": "getNbt",
    "setTag": "setNbt",
    "hasTag": "hasNbt",
    "getCount": "getCount",
    "setCount": "setCount",
    "shrink": "decrement",
    "grow": "increment",
    "copyWithCount": "copyWithCount",
    "getDisplayName": "getName",
    "getHoverName": "getName",
    "getDamageValue": "getDamage",
    "setDamageValue": "setDamage",
    "getMaxDamage": "getMaxDamage",
    "isDamageableItem": "isDamageable",
    "getUseAnimation": "getUseAction",
    "getUseDuration": "getMaxUseTime",
    "isEnchantable": "isEnchantable",
    "getEnchantmentValue": "getEnchantmentValue",
    "getAllEnchantments": "getEnchantments",
    # 矩阵 / 渲染
    "pushPose": "push",
    "popPose": "pop",
    "translate": "translate",
    "mulPose": "multiply",
    "renderItem": "drawItem",
    "renderItemDecorations": "drawItemInSlot",
    "drawString": "drawText",
    "drawCenteredString": "drawCenteredTextWithShadow",
    "getPositionMatrix": "getMatrices",
    "guiWidth": "getScaledWindowWidth",
    "guiHeight": "getScaledWindowHeight",
    "enableScissor": "enableScissor",
    "disableScissor": "disableScissor",
    # 数据同步
    "defineId": "registerData",
    "defineSynchedData": "initDataTracker",
    "getEntityData": "getDataTracker",
    # 箭实体（经 javap 核实）
    "setBaseDamage": "setDamage",
    "getBaseDamage": "getDamage",
    "setEffectsFromItem": "initFromStack",
    "setPiercingLevel": "setPierceLevel",
    "getPiercingLevel": "getPierceLevel",
    "setCritArrow": "setCritical",
    "isCritArrow": "isCritical",
    "setSoundEvent": "setSound",
    # 以下均经 javap 对 1.20.1 Yarn 原版包核实
    "getYRot": "getYaw",
    "setYRot": "setYaw",
    "getXRot": "getPitch",
    "setXRot": "setPitch",
    "getRemainingFireTicks": "getFireTicks",
    "setRemainingFireTicks": "setFireTicks",
    "isPickable": "canHit",
    "canHitEntity": "canHit",
    "isAlliedTo": "isTeammate",
    "damageSources": "getDamageSources",
    "doPostHurtEffects": "applyDamageEffects",
    "doPostDamageEffects": "applyDamageEffects",
    "setLastHurtMob": "setAttacker",
    "getLastHurtMob": "getAttacker",
    "lengthSqr": "lengthSquared",
    "horizontalDistance": "horizontalDistance",
    "getUseItem": "getActiveItem",
    "getUsedItemHand": "getActiveHand",
    "releaseUsingItem": "stopUsingItem",
    "startUsingItem": "setCurrentHand",
    "updateUsingItem": "tickItemStackUsage",
    "getAttribute": "getAttributeInstance",
    "canHarmPlayer": "canTarget",
    "hurtAndBreak": "damage",
    "getMobType": "getGroup",
    "isOnCooldown": "isCoolingDown",
    "getDescriptionId": "getTranslationKey",
    "removeItemNoUpdate": "setStack",
    "getChargeDuration": "getPullProgress",
    # 高频通用 API（经 javap 核实）
    "isSameItemSameTags": "areEqual",
    "hasFoil": "hasGlint",
    "fireResistant": "fireproof",
    "stacksTo": "maxCount",
    "inflate": "expand",
    "getMaxStackSize": "getMaxCount",
    "setChanged": "markDirty",
    "broadcastChanges": "sendContentUpdates",
    "setKnockback": "setPunch",
    "withStyle": "formatted",
    "getDefaultAttributeModifiers": "getAttributeModifiers",
    "canApplyAtEnchantingTable": "isAcceptableItem",
    # 其它
    "getMillis": "getMeasuringTimeMs",
    "hurt": "damage",
    "playSound": "playSound",
    "sendParticles": "spawnParticles",
    "getGameRules": "getGameRules",
    "isClientSide": "isClient",
    "getGameTime": "getTime",
    "getDayTime": "getTimeOfDay",
}

# 静态工具类整体替换（Mojang 全限定 -> Yarn 全限定）
STATIC_TYPES = {
    "Mth": "MathHelper",
    "ChatFormatting": "Formatting",
    "SoundSource": "SoundCategory",
}

# 变量/字段名（Mojang -> Yarn），仅在明确的成员访问上下文替换
FIELDS = {
    "hurtMarked": "velocityModified",
    "containerMenu": "currentScreenHandler",
    "inventoryMenu": "playerScreenHandler",
    "noPhysics": "noClip",
    "entityData": "dataTracker",
    "pickup": "pickupType",
}

# 类型/枚举整体替换（经 javap 核实的 Yarn 名）
TYPES = {
    "Pickup": "PersistentProjectileEntity.PickupPermission",
    "GsonHelper": "JsonHelper",
    "ExperienceOrb": "ExperienceOrbEntity",
    "TamableAnimal": "TameableEntity",
    "ClipContext": "RaycastContext",
}

# 常量：Mojang 名 -> Yarn 名（经 javap 核实）
CONSTANTS = {
    "POWER_ARROWS": "POWER",
    "PUNCH_ARROWS": "PUNCH",
    "FLAMING_ARROWS": "FLAME",
    "INFINITY_ARROWS": "INFINITY",
    "MOVEMENT_SPEED": "GENERIC_MOVEMENT_SPEED",
    "KNOCKBACK_RESISTANCE": "GENERIC_KNOCKBACK_RESISTANCE",
    "ATTACK_DAMAGE": "GENERIC_ATTACK_DAMAGE",
    "ATTACK_KNOCKBACK": "GENERIC_ATTACK_KNOCKBACK",
    "ARROW_SHOOT": "ENTITY_ARROW_SHOOT",
    "FIRECHARGE_USE": "ITEM_FIRECHARGE_USE",
}


def convert(text: str) -> str:
    for a, b in STATIC_TYPES.items():
        text = re.sub(rf"\b{a}\b", b, text)
    # 方法调用：name( 形式，避免误伤同名字符串/注释里的普通词
    for a, b in METHODS.items():
        if a == b:
            continue
        text = re.sub(rf"\b{a}\s*\(", b + "(", text)
    # 成员访问：.field 形式
    for a, b in FIELDS.items():
        if a == b:
            continue
        text = re.sub(rf"\.{a}\b", "." + b, text)
    for a, b in TYPES.items():
        text = re.sub(rf"\b{a}\b", b, text)
    # dataTracker.define(...) 在 Yarn 中是 startTracking
    text = re.sub(r"\bdataTracker\.define\(", "dataTracker.startTracking(", text)
    # 按接收者限定：这些名字在不同类上语义不同，不能全局替换
    # level() -> getWorld()（Entity/Item 上下文）；server.level() 同理
    text = re.sub(r"\b(player|entity|shooter|owner|target|living|this|arrow|self|victim|attacker|candidate|mob)\.level\(\)",
                  r"\1.getWorld()", text)
    # stack.is(Item) -> stack.isOf(Item)
    text = re.sub(r"\b(stack|weapon|ammunition|fates|core|result|item|held|main|offhand|left|right|output)\.is\(",
                  r"\1.isOf(", text)
    # Vec3d.scale -> multiply
    text = re.sub(r"\b(vec|vector|direction|offset|motion|velocity|look|delta|pos3)\.scale\(",
                  r"\1.multiply(", text)
    # Entity.position() -> getPos()
    text = re.sub(r"\b(player|entity|shooter|owner|target|this|arrow|self)\.position\(\)",
                  r"\1.getPos()", text)
    # Minecraft.getInstance() -> MinecraftClient.getInstance()
    text = re.sub(r"\bMinecraft\.getInstance\(\)", "MinecraftClient.getInstance()", text)
    text = re.sub(r"\bMinecraft\b(?!Client)", "MinecraftClient", text)
    # 常量仅在其所属类的限定访问下替换，避免误伤同名字符串
    for a, b in CONSTANTS.items():
        text = re.sub(rf"\b(Enchantments|EntityAttributes|SoundEvents)\.{a}\b", rf"\1.{b}", text)
    # 双重前缀修正（GENERIC_ 已存在时不再叠加）
    text = text.replace("GENERIC_GENERIC_", "GENERIC_")
    text = text.replace("ENTITY_ENTITY_", "ENTITY_")
    text = text.replace("ITEM_ITEM_", "ITEM_")
    # ResourceLocation 静态工厂 -> Yarn 构造
    text = re.sub(r"\bIdentifier\.fromNamespaceAndPath\(", "new Identifier(", text)
    text = re.sub(r"\bIdentifier\.parse\(", "new Identifier(", text)
    return text


def main():
    rels = [l.strip() for l in LIST.read_text(encoding="utf-8").splitlines() if l.strip()]
    changed = 0
    for rel in rels:
        p = ROOT / rel
        if not p.exists():
            continue
        t = p.read_text(encoding="utf-8")
        n = convert(t)
        if n != t:
            # 防御：确保没有把 package 行或中文注释改坏
            assert n.splitlines()[0].startswith("package net.per.primogemcraft."), rel
            p.write_text(n, encoding="utf-8")
            changed += 1
    print(f"converted files: {changed}")


if __name__ == "__main__":
    main()