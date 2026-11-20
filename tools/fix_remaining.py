"""修正剩余映射遗漏：渲染器基类、假玩家基类、JEI 菜单类型、动画方法名。"""
import re
from pathlib import Path

ROOT = Path(r"F:\MCR\NPGC\PrimogemCraftFabric\src\main\java\net\per\primogemcraft")


def patch(rel, pairs, regexes=(), must_absent=()):
    p = ROOT / rel
    t = p.read_text(encoding="utf-8")
    o = t
    for a, b in pairs:
        t = t.replace(a, b)
    for pat, rep in regexes:
        t = re.sub(pat, rep, t)
    for bad in must_absent:
        assert bad not in t, f"{rel}: still contains {bad}"
    if t != o:
        p.write_text(t, encoding="utf-8")
        print(f"PATCHED {rel}")
    else:
        print(f"UNCHANGED {rel}")


# 物品内建渲染器：Forge BlockEntityWithoutLevelRenderer -> Fabric BuiltinModelItemRenderer
# renderByItem -> render，ItemDisplayContext -> ModelTransformationMode
patch("client/WishBowRenderer.java", [
    ("extends BlockEntityWithoutLevelRenderer", "extends BuiltinModelItemRenderer"),
    ("public void renderByItem(", "public void render("),
    ("ItemDisplayContext", "ModelTransformationMode"),
], must_absent=("BlockEntityWithoutLevelRenderer", "ItemDisplayContext"))

# 活体物品持物用的假玩家：Forge FakePlayer 无 Fabric 对应，改用 ServerPlayerEntity
patch("system/living/LivingItemUsePlayer.java", [
    ("extends FakePlayer", "extends net.minecraft.server.network.ServerPlayerEntity"),
    ("stack.getUseAnimation() == UseAnim.BOW", "stack.getUseAction() == UseAction.BOW"),
], must_absent=("FakePlayer", "UseAnim"))

# JEI 转移处理器：MenuType -> ScreenHandlerType
patch("collab/jei/WishCoreTransferHandler.java", [
    ("MenuType<AnvilScreenHandler>", "ScreenHandlerType<AnvilScreenHandler>"),
    ("MenuType.ANVIL", "ScreenHandlerType.ANVIL"),
], must_absent=("MenuType<", "MenuType.ANVIL"))