import re
import sys
from pathlib import Path

ROOT = Path(sys.argv[1])

replacements = [
    (r'\bCompoundTag\b', 'NbtCompound'),
    (r'\bListTag\b', 'NbtList'),
    (r'\bTag\.TAG_COMPOUND\b', 'NbtElement.COMPOUND_TYPE'),
    (r'\bTag\.TAG_STRING\b', 'NbtElement.STRING_TYPE'),
    (r'\bTag\.TAG_INT\b', 'NbtElement.INT_TYPE'),
    (r'\bTag\.TAG_LIST\b', 'NbtElement.LIST_TYPE'),
    (r'\bOptional<Tag>\b', 'Optional<NbtElement>'),
    (r'\bDynamicOps<Tag>\b', 'DynamicOps<NbtElement>'),
    (r'\(Tag tag\)', '(NbtElement tag)'),
    (r'\bResourceLocation\b', 'Identifier'),
    (r'\bFriendlyByteBuf\b', 'PacketByteBuf'),
    (r'(?<![\w])Component\b(?!\w)', 'Text'),
    (r'\bPoseStack\b', 'MatrixStack'),
    (r'\bRecordItem\b', 'MusicDiscItem'),
    (r'\bTooltipFlag\b', 'TooltipContext'),
    (r'\bNonNullList\b', 'DefaultedList'),
    (r'(?<![\w])Inventory\b(?!\w)', 'PlayerInventory'),
    (r'\bTier\b', 'ToolMaterial'),
    (r'(?<![\w])Attribute\b(?!\w)', 'EntityAttribute'),
    (r'(?<![\w])AttributeModifier\b(?!\w)', 'EntityAttributeModifier'),
    (r'(?<![\w])AttributeInstance\b(?!\w)', 'EntityAttributeInstance'),
    (r'(?<![\w])Attributes\b(?!\w)', 'EntityAttributes'),
    (r'(?<![\w])Holder\b(?!\w)', 'RegistryEntry'),
    (r'\bHolderOwner\b', 'RegistryEntryOwner'),
    (r'\bResourceKey\b', 'RegistryKey'),
    (r'getTagElement\(', 'getSubNbt('),
    (r'getOrCreateTagElement\(', 'getOrCreateSubNbt('),
    (r'removeTagKey\(', 'removeSubNbt('),
    (r'getOrCreateTag\(', 'getOrCreateNbt('),
    (r'\.getTag\(', '.getNbt('),
    (r'setHoverName\(', 'setCustomName('),
    (r'getEnchantmentTags\(', 'getEnchantments('),
    (r'writeComponent\(', 'writeText('),
    (r'readComponent\b', 'readText'),
    (r'writeResourceLocation\b', 'writeIdentifier'),
    (r'readResourceLocation\b', 'readIdentifier'),
    (r'writeUUID\b', 'writeUuid'),
    (r'readUUID\b', 'readUuid'),
    (r'writeItem\b', 'writeItemStack'),
    (r'readItem\b', 'readItemStack'),
    (r'writeUtf\b', 'writeString'),
    (r'readUtf\b', 'readString'),
    (r'getItemEnchantmentLevel\(', 'getLevel('),
    (r'\.canEnchant\(', '.isAcceptableItem('),
    (r'EnchantmentHelper\.getEnchantments\(', 'EnchantmentHelper.get('),
    (r'EnchantmentHelper\.setEnchantments\(', 'EnchantmentHelper.set('),
    (r'addTransientModifier\(', 'addTemporaryModifier('),
    (r'entity\.getAttribute\(', 'entity.getAttributeInstance('),
    (r'appendHoverText\(', 'appendTooltip('),
    (r'Item\.Properties\b', 'Item.Settings'),
    (r'AbstractBlock\.Settings\.of\(', 'AbstractBlock.Settings.create('),
    (r'BlockBehaviour\.Properties\b', 'AbstractBlock.Settings'),
    (r'\.getAllKeys\(', '.getKeys('),
    (r'\.getUUID\(', '.getUuid('),
    (r'inventory\.items\b', 'inventory.main'),
    (r'inventory\.offhand\b', 'inventory.offHand'),
    (r'EntityAttributes\.ATTACK_DAMAGE\b', 'EntityAttributes.GENERIC_ATTACK_DAMAGE'),
    (r'EntityAttributes\.ATTACK_SPEED\b', 'EntityAttributes.GENERIC_ATTACK_SPEED'),
    (r'stack\.save\(', 'stack.writeNbt('),
    (r'ItemStack\.of\(', 'ItemStack.fromNbt('),
    (r'consumer\.endVertex\(', 'consumer.next('),
    (r'stack\.last\(', 'stack.peek('),
    (r'\.pose\(', '.getPositionMatrix('),
    (r'\.normal\(', '.getNormalMatrix('),
    (r'PoseStack\.Pose\b', 'MatrixStack.Entry'),
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