import re
import sys
from pathlib import Path

ROOT = Path(sys.argv[1])

replacements = [
    (r'\bStairBlock\b', 'StairsBlock'),
    (r'\bIronBarsBlock\b', 'PaneBlock'),
    (r'\bForgeSpawnEggItem\b', 'SpawnEggItem'),
    (r'\bResourceLocation\b', 'Identifier'),
    (r'\bServerPlayer\b', 'ServerPlayerEntity'),
    (r'\bServerLevel\b', 'ServerWorld'),
    (r'\bCompoundTag\b', 'NbtCompound'),
    (r'\bListTag\b', 'NbtList'),
    (r'\bVec3\b', 'Vec3d'),
    (r'\bInteractionHand\b', 'Hand'),
    (r'\bInteractionResultHolder\b', 'TypedActionResult'),
    (r'\bInteractionResult\b', 'ActionResult'),
    (r'\bUseOnContext\b', 'ItemUsageContext'),
    (r'\bCommandSourceStack\b', 'ServerCommandSource'),
    (r'\bRandomSource\b', 'Random'),
    (r'\bBlockGetter\b', 'BlockView'),
    (r'\bAbstractContainerMenu\b', 'ScreenHandler'),
    (r'\bCreativeModeTabs\b', 'ItemGroups'),
    (r'\bCreativeModeTab\b', 'ItemGroup'),
    (r'\bMobEffectInstance\b', 'StatusEffectInstance'),
    (r'\bMobEffects\b', 'StatusEffects'),
    (r'\bMobEffect\b', 'StatusEffect'),
    (r'\bFriendlyByteBuf\b', 'PacketByteBuf'),
    (r'\bNonNullList\b', 'DefaultedList'),
    (r'\bPoseStack\b', 'MatrixStack'),
    (r'\bTooltipFlag\b', 'TooltipContext'),
    (r'\bRecordItem\b', 'MusicDiscItem'),
    (r'\bTier\b', 'ToolMaterial'),
    (r'AbstractBlock\.Settings\.of\(', 'AbstractBlock.Settings.create('),
    (r'BlockBehaviour\.Settings\.of\(', 'AbstractBlock.Settings.create('),
    (r'\.requiresCorrectToolForDrops\(', '.requiresTool('),
    (r'\.noOcclusion\(', '.nonOpaque('),
    (r'\.pushReaction\(', '.pistonBehavior('),
    (r'\.noLootTable\(', '.dropsNothing('),
    (r'\.ignitedByLava\(', '.burnable('),
    (r'\.isRedstoneConductor\(', '.solidBlock('),
    (r'\.defaultBlockState\(', '.getDefaultState('),
    (r'\.getOrCreateTag\(', '.getOrCreateNbt('),
    (r'\.getUUID\(', '.getUuid('),
    (r'\.getAllKeys\(', '.getKeys('),
    (r'\.isClientSide\(', '.isClient('),
    (r'\.getMaxStackSize\(', '.getMaxCount('),
    (r'\.getHoverName\(', '.getName('),
    (r'\.getUseItem\(', '.getActiveItem('),
    (r'\.getUseDuration\(', '.getMaxUseTime('),
    (r'\.getMainHandItem\(', '.getMainHandStack('),
    (r'\.getOffhandItem\(', '.getOffHandStack('),
    (r'\.setItemInHand\(', '.setStackInHand('),
    (r'\.getArmorValue\(', '.getArmor('),
    (r'\.addEffect\(', '.addStatusEffect('),
    (r'\.removeEffect\(', '.removeStatusEffect('),
    (r'\.hasEffect\(', '.hasStatusEffect('),
    (r'\.getActiveEffects\(', '.getStatusEffects('),
    (r'\.getPlayerList\(', '.getPlayerManager('),
    (r'\.sendSystemMessage\(', '.sendMessage('),
    (r'\.displayClientMessage\(', '.sendMessage('),
    (r'\.getGameTime\(', '.getTime('),
    (r'\.getDayTime\(', '.getTimeOfDay('),
    (r'\.setBlock\(', '.setBlockState('),
    (r'\.destroyBlock\(', '.breakBlock('),
    (r'\.hasChunkAt\(', '.isChunkLoaded('),
    (r'\.getCachedState\(', '.getBlockState('),
    (r'\.triangle\(', '.nextTriangular('),
]

mask_re = re.compile(r'"(?:\\.|[^"\\])*"|//.*|/\*.*?\*/', re.DOTALL)

def fix(text):
    spans = [(m.start(), m.end()) for m in mask_re.finditer(text)]
    def in_mask(pos):
        for s, e in spans:
            if s <= pos < e:
                return True
            if s > pos:
                break
        return False
    for pattern, repl in replacements:
        if pattern == repl:
            continue
        out = []
        pos = 0
        for m in re.finditer(pattern, text):
            if in_mask(m.start()):
                continue
            out.append(text[pos:m.start()])
            out.append(repl)
            pos = m.end()
        out.append(text[pos:])
        text = ''.join(out)
        spans = [(m.start(), m.end()) for m in mask_re.finditer(text)]
    return text

files = sorted(ROOT.rglob('*.java'))
changed = 0
for path in files:
    text = path.read_text(encoding='utf-8')
    new = fix(text)
    if new != text:
        path.write_text(new, encoding='utf-8', newline='\n')
        changed += 1
print(f"files: {len(files)}, changed: {changed}")