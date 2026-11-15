import re
import sys
from pathlib import Path

ROOT = Path(sys.argv[1])

pairs = [
    ('ServerPlayer', 'ServerPlayerEntity'),
    ('Player', 'PlayerEntity'),
    ('EnchantmentInstance', 'EnchantmentLevelEntry'),
    ('ListTag', 'NbtList'),
    ('CompoundTag', 'NbtCompound'),
    ('ResourceLocation', 'Identifier'),
    ('AttributeModifier', 'EntityAttributeModifier'),
    ('AttributeInstance', 'EntityAttributeInstance'),
    ('Attributes', 'EntityAttributes'),
    ('Attribute', 'EntityAttribute'),
    ('Vec3', 'Vec3d'),
    ('AABB', 'Box'),
    ('Mob', 'MobEntity'),
    ('ServerLevel', 'ServerWorld'),
    ('Level', 'World'),
    ('Mth', 'MathHelper'),
    ('FriendlyByteBuf', 'PacketByteBuf'),
    ('InteractionHand', 'Hand'),
    ('InteractionResult', 'ActionResult'),
    ('UseOnContext', 'ItemUsageContext'),
    ('BlockMirror', 'BlockMirror'),
    ('BlockRotation', 'BlockRotation'),
    ('Mirror', 'BlockMirror'),
    ('Rotation', 'BlockRotation'),
    ('TooltipFlag', 'TooltipContext'),
    ('Component', 'Text'),
    ('MutableComponent', 'MutableText'),
    ('NonNullList', 'DefaultedList'),
    ('ResourceKey', 'RegistryKey'),
    ('Holder', 'RegistryEntry'),
    ('BuiltInRegistries', 'Registries'),
    ('MobEffectInstance', 'StatusEffectInstance'),
    ('MobEffects', 'StatusEffects'),
    ('MobEffect', 'StatusEffect'),
    ('SoundSource', 'SoundCategory'),
    ('GameType', 'GameMode'),
    ('UUIDUtil', 'Uuids'),
    ('PotionUtil', 'PotionUtil'),
    ('FoodProperties', 'FoodComponent'),
    ('BlockBehaviour', 'AbstractBlock'),
    ('SoundType', 'SoundGroup'),
    ('PathfinderMob', 'PathAwareEntity'),
    ('PathNavigation', 'EntityNavigation'),
    ('DiggerItem', 'MiningToolItem'),
    ('EntityBlock', 'BlockEntityProvider'),
    ('CommandSourceStack', 'ServerCommandSource'),
    ('RandomSource', 'Random'),
    ('BlockGetter', 'BlockView'),
    ('AbstractContainerMenu', 'ScreenHandler'),
    ('LevelReader', 'WorldView'),
    ('HumanoidArm', 'Arm'),
    ('ChatFormatting', 'Formatting'),
    ('CreativeModeTab', 'ItemGroup'),
    ('Tier', 'ToolMaterial'),
    ('RecordItem', 'MusicDiscItem'),
    ('PoseStack', 'MatrixStack'),
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
    for old, new in pairs:
        if old == new:
            continue
        pattern = r'(?<![.\w])' + old + r'(?!\w)'
        out = []
        pos = 0
        for m in re.finditer(pattern, text):
            if in_mask(m.start()):
                continue
            out.append(text[pos:m.start()])
            out.append(new)
            pos = m.end()
        out.append(text[pos:])
        text = ''.join(out)
        spans = [(m.start(), m.end()) for m in mask_re.finditer(text)]
    text = text.replace('PlayerEntityEntity', 'PlayerEntity')
    text = text.replace('ServerPlayerEntityEntity', 'ServerPlayerEntity')
    text = text.replace('EntityEntityAttribute', 'EntityAttribute')
    text = text.replace('MobEntityEntity', 'MobEntity')
    text = text.replace('WorldWorld', 'World')
    text = text.replace('Vec3dd', 'Vec3d')
    text = text.replace('BlockBlockMirror', 'BlockMirror')
    text = text.replace('BlockBlockRotation', 'BlockRotation')
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