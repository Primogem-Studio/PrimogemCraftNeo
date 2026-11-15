import re
from pathlib import Path

p = Path(r'F:\MCR\NPGC\PrimogemCraftFabric\src\main\java\net\per\primogemcraft\entity\mob\LivingItemEntity.java')
t = p.read_text(encoding='utf-8')

pairs = [
    ('Player', 'PlayerEntity'),
    ('Vec3', 'Vec3d'),
    ('CompoundTag', 'NbtCompound'),
    ('AABB', 'Box'),
    ('AttributeInstance', 'EntityAttributeInstance'),
    ('AttributeModifier', 'EntityAttributeModifier'),
    ('Attributes', 'EntityAttributes'),
    ('Attribute', 'EntityAttribute'),
    ('Mob', 'MobEntity'),
    ('ServerLevel', 'ServerWorld'),
    ('Level', 'World'),
    ('Mth', 'MathHelper'),
]

for old, new in pairs:
    pattern = r'(?<![.\w])' + old + r'(?!\w)'
    t = re.sub(pattern, new, t)

t = t.replace('PlayerEntityEntity', 'PlayerEntity')
t = t.replace('Vec3dd', 'Vec3d')
t = t.replace('EntityEntityAttribute', 'EntityAttribute')
t = t.replace('MobEntityEntity', 'MobEntity')
t = t.replace('WorldWorld', 'World')
t = t.replace('ServerWorldWorld', 'ServerWorld')

p.write_text(t, encoding='utf-8', newline='\n')
print('done')