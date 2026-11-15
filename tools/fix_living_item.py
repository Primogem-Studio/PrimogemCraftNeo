import re
from pathlib import Path

p = Path(r'F:\MCR\NPGC\PrimogemCraftFabric\src\main\java\net\per\primogemcraft\entity\mob\LivingItemEntity.java')
t = p.read_text(encoding='utf-8')
repl = [
    (r'\bPlayer\b(?!Entity|Inventory|Abilities|Manager)', 'PlayerEntity'),
    (r'\bVec3\b(?!d)', 'Vec3d'),
    (r'\bCompoundTag\b', 'NbtCompound'),
    (r'\bMob\b(?!Entity|Type|Effect)', 'MobEntity'),
    (r'\bAttributeModifier\b', 'EntityAttributeModifier'),
    (r'\bAttributeInstance\b', 'EntityAttributeInstance'),
    (r'\bAttribute\b(?!s|Instance|Modifier)', 'EntityAttribute'),
    (r'\bAABB\b', 'Box'),
    (r'\bFlyingNavigation\b', 'BirdNavigation'),
    (r'\bServerLevel\b', 'ServerWorld'),
    (r'\bLevel\b(?!Renderer|Reader)', 'World'),
]
mask = re.compile(r'"(?:\\.|[^"\\])*"|//.*|/\*.*?\*/', re.DOTALL)
spans = [(m.start(), m.end()) for m in mask.finditer(t)]

def in_mask(pos):
    for s, e in spans:
        if s <= pos < e:
            return True
        if s > pos:
            break
    return False

for pat, rep in repl:
    out = []
    pos = 0
    for m in re.finditer(pat, t):
        if in_mask(m.start()):
            continue
        prev = t[m.start()-1] if m.start() > 0 else ''
        if prev == '.':
            continue
        out.append(t[pos:m.start()])
        out.append(rep)
        pos = m.end()
    out.append(t[pos:])
    t = ''.join(out)
    spans = [(m.start(), m.end()) for m in mask.finditer(t)]

p.write_text(t, encoding='utf-8', newline='\n')
print('done')