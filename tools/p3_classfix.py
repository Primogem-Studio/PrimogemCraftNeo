import re
import sys
from pathlib import Path

ROOT = Path(sys.argv[1])

replacements = [
    (r'\bServerPlayer\b(?!Entity)', 'ServerPlayerEntity'),
    (r'\bPlayer\b(?!Entity|Inventory|Abilities|Manager)', 'PlayerEntity'),
    (r'\bServerLevel\b(?!)', 'ServerWorld'),
    (r'\bLevel\b(?!Renderer|Reader)', 'World'),
    (r'\bLevelReader\b', 'WorldView'),
    (r'\bVec3\b(?!d)', 'Vec3d'),
    (r'\bAABB\b', 'Box'),
    (r'\bCompoundTag\b', 'NbtCompound'),
    (r'\bListTag\b', 'NbtList'),
    (r'\bResourceLocation\b', 'Identifier'),
    (r'\bFriendlyByteBuf\b', 'PacketByteBuf'),
    (r'\bAttributeModifier\b', 'EntityAttributeModifier'),
    (r'\bAttributeInstance\b', 'EntityAttributeInstance'),
    (r'\bAttribute\b(?!s|Instance|Modifier)', 'EntityAttribute'),
    (r'\bAttributes\b', 'EntityAttributes'),
    (r'\bInteractionHand\b', 'Hand'),
    (r'\bInteractionResultHolder\b', 'TypedActionResult'),
    (r'\bInteractionResult\b(?!Holder)', 'ActionResult'),
    (r'\bPathfinderMob\b', 'PathAwareEntity'),
    (r'\bPathNavigation\b', 'EntityNavigation'),
    (r'\bDiggerItem\b', 'MiningToolItem'),
    (r'\bMob\b(?!Entity|Type|Effect)', 'MobEntity'),
    (r'\bMirror\b', 'BlockMirror'),
    (r'\bRotation\b', 'BlockRotation'),
    (r'\bEnchantmentInstance\b', 'EnchantmentLevelEntry'),
    (r'\bEntityBlock\b', 'BlockEntityProvider'),
    (r'\bUseOnContext\b', 'ItemUsageContext'),
    (r'\bCommandSourceStack\b', 'ServerCommandSource'),
    (r'\bRandomSource\b', 'Random'),
    (r'\bBlockGetter\b', 'BlockView'),
    (r'\bAbstractContainerMenu\b', 'ScreenHandler'),
    (r'\bMobEffectInstance\b', 'StatusEffectInstance'),
    (r'\bMobEffects\b', 'StatusEffects'),
    (r'\bMobEffect\b(?!Instance|s)', 'StatusEffect'),
    (r'\bNonNullList\b', 'DefaultedList'),
    (r'\bPoseStack\b', 'MatrixStack'),
    (r'\bTooltipFlag\b', 'TooltipContext'),
    (r'\bRecordItem\b', 'MusicDiscItem'),
    (r'\bTier\b', 'ToolMaterial'),
    (r'\bFoodProperties\b', 'FoodComponent'),
    (r'\bBlockBehaviour\b', 'AbstractBlock'),
    (r'\bSoundType\b', 'SoundGroup'),
    (r'\bComponent\b(?!Serialization)', 'Text'),
    (r'\bMutableComponent\b', 'MutableText'),
    (r'\bChatFormatting\b', 'Formatting'),
    (r'\bResourceKey\b', 'RegistryKey'),
    (r'\bHolder\b(?!Owner)', 'RegistryEntry'),
    (r'\bBuiltInRegistries\b', 'Registries'),
    (r'\bCreativeModeTab\b', 'ItemGroup'),
    (r'\bGameType\b', 'GameMode'),
    (r'\bSoundSource\b', 'SoundCategory'),
    (r'\bHumanoidArm\b', 'Arm'),
    (r'\bMth\b', 'MathHelper'),
    (r'\bUUIDUtil\b', 'Uuids'),
    (r'\bMobEffectUtil\b', 'StatusEffectUtil'),
    (r'\bPotionUtil\b', 'PotionUtil'),
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
        out = []
        pos = 0
        for m in re.finditer(pattern, text):
            if in_mask(m.start()):
                continue
            prev = text[m.start()-1] if m.start() > 0 else ''
            if prev == '.':
                continue
            out.append(text[pos:m.start()])
            out.append(repl)
            pos = m.end()
        out.append(text[pos:])
        text = ''.join(out)
        spans = [(m.start(), m.end()) for m in mask_re.finditer(text)]
    return text

SKIP_DIRS = {'compat', 'config', 'mixin', 'collab', 'render', 'client'}
files = sorted(ROOT.rglob('*.java'))
changed = 0
for path in files:
    if any(part in SKIP_DIRS for part in path.parts):
        continue
    text = path.read_text(encoding='utf-8')
    new = fix(text)
    if new != text:
        path.write_text(new, encoding='utf-8', newline='\n')
        changed += 1
print(f"files: {len(files)}, changed: {changed}")