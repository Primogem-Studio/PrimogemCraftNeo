import re
import sys
from pathlib import Path

TINY = Path(sys.argv[1])
ROOT = Path(sys.argv[2])

yarn_by_simple = {}
with TINY.open(encoding='utf-8') as f:
    f.readline()
    for line in f:
        p = line.rstrip('\n').split('\t')
        if p[0] == 'c':
            fq = p[3].replace('/', '.')
            simple = fq.rsplit('.', 1)[-1]
            if '$' in simple:
                continue
            yarn_by_simple.setdefault(simple, set()).add(fq)

replacements = [
    (r'\bAbstractBlock\.Properties\b', 'AbstractBlock.Settings'),
    (r'\bBlockBehaviour\.Properties\b', 'AbstractBlock.Settings'),
    (r'\bItem\.Properties\b', 'Item.Settings'),
    (r'\bBlock\.Properties\b', 'AbstractBlock.Settings'),
    (r'\bProperties\b(?=\s+\w+\s*[,)])', 'Settings'),
    (r'\bResourceLocation\b', 'Identifier'),
    (r'\bFriendlyByteBuf\b', 'PacketByteBuf'),
    (r'\bCompoundTag\b', 'NbtCompound'),
    (r'\bListTag\b', 'NbtList'),
    (r'\bTag\.TAG_COMPOUND\b', 'NbtElement.COMPOUND_TYPE'),
    (r'\bTag\.TAG_STRING\b', 'NbtElement.STRING_TYPE'),
    (r'\bTag\.TAG_INT\b', 'NbtElement.INT_TYPE'),
    (r'\bTag\.TAG_LIST\b', 'NbtElement.LIST_TYPE'),
    (r'\bTag\.TAG_BYTE\b', 'NbtElement.BYTE_TYPE'),
    (r'\bTag\.TAG_LONG\b', 'NbtElement.LONG_TYPE'),
    (r'\bTag\.TAG_FLOAT\b', 'NbtElement.FLOAT_TYPE'),
    (r'\bTag\.TAG_DOUBLE\b', 'NbtElement.DOUBLE_TYPE'),
    (r'\bNonNullList\b', 'DefaultedList'),
    (r'\bPoseStack\b', 'MatrixStack'),
    (r'\bVertexConsumer\b', 'VertexConsumer'),
    (r'com\.mojang\.blaze3d\.vertex\.PoseStack', 'net.minecraft.client.util.math.MatrixStack'),
    (r'com\.mojang\.blaze3d\.vertex\.VertexConsumer', 'net.minecraft.client.render.VertexConsumer'),
    (r'com\.mojang\.math\.Axis\b', 'net.minecraft.util.math.RotationAxis'),
    (r'\bMatrixStack\.Pose\b', 'MatrixStack.Entry'),
    (r'\bTooltipFlag\b', 'TooltipContext'),
    (r'\bRecordItem\b', 'MusicDiscItem'),
    (r'\bTier\b', 'ToolMaterial'),
    (r'getTagElement\(', 'getSubNbt('),
    (r'getOrCreateTagElement\(', 'getOrCreateSubNbt('),
    (r'removeTagKey\(', 'removeSubNbt('),
    (r'getOrCreateTag\(', 'getOrCreateNbt('),
    (r'setHoverName\(', 'setCustomName('),
    (r'getEnchantmentTags\(', 'getEnchantments('),
    (r'getItemEnchantmentLevel\(', 'getLevel('),
    (r'\.canEnchant\(', '.isAcceptableItem('),
    (r'EnchantmentHelper\.getEnchantments\(', 'EnchantmentHelper.get('),
    (r'EnchantmentHelper\.setEnchantments\(', 'EnchantmentHelper.set('),
    (r'addTransientModifier\(', 'addTemporaryModifier('),
    (r'\.getAllKeys\(', '.getKeys('),
    (r'\.getUUID\(', '.getUuid('),
    (r'ItemStack\.of\(', 'ItemStack.fromNbt('),
    (r'consumer\.endVertex\(', 'consumer.next('),
    (r'\.getPersistentData\(', '.primogemcraft$getPersistentData('),
]

mask_re = re.compile(r'"(?:\\.|[^"\\])*"|//.*|/\*.*?\*/', re.DOTALL)
import_re = re.compile(r'^import\s+([\w.]+)\s*;', re.MULTILINE)

def apply_replacements(text):
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

def add_imports(text):
    existing = set(m.group(1) for m in import_re.finditer(text))
    existing_simple = {fq.rsplit('.', 1)[-1] for fq in existing}
    pkg_match = re.search(r'^package\s+([\w.]+)\s*;', text, re.MULTILINE)
    pkg = pkg_match.group(1) if pkg_match else ''
    body = mask_re.sub('', text)
    needed = []
    for simple, fqs in yarn_by_simple.items():
        if len(fqs) != 1:
            continue
        fq = next(iter(fqs))
        if not fq.startswith('net.minecraft.'):
            continue
        if fq in existing or simple in existing_simple:
            continue
        if fq.rsplit('.', 1)[0] == pkg:
            continue
        if not re.search(r'\b' + re.escape(simple) + r'\b', body):
            continue
        needed.append(fq)
    if not needed:
        return text
    needed.sort()
    insert = ''.join(f'import {fq};\n' for fq in needed)
    if pkg_match:
        pos = pkg_match.end()
        return text[:pos] + '\n\n' + insert.rstrip('\n') + text[pos:]
    return insert + text

SKIP_DIRS = {'compat', 'config', 'mixin'}
SKIP_FILES = {'PrimogemCraft.java', 'PGCClient.java'}
files = sorted(ROOT.rglob('*.java'))
changed = 0
for path in files:
    if any(part in SKIP_DIRS for part in path.parts):
        continue
    if path.name in SKIP_FILES:
        continue
    text = path.read_text(encoding='utf-8')
    new = apply_replacements(text)
    if new != text:
        path.write_text(new, encoding='utf-8', newline='\n')
        changed += 1
print(f"files: {len(files)}, changed: {changed}")