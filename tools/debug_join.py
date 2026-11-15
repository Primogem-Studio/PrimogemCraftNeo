import re
from pathlib import Path

MOJMAP = Path(r'F:\MCR\NPGC\PrimogemCraftFabric\tools\client-mojmap.txt')
TINY = Path(r'C:\Users\Genshin Lumine\.gradle\caches\fabric-loom\1.20.1\net.fabricmc.yarn.1_20_1.1.20.1+build.10-v2\mappings.tiny')

moj_classes = {}
moj_methods = {}
current = None
with MOJMAP.open(encoding='utf-8') as f:
    for line in f:
        line = line.rstrip('\n')
        if not line or line.startswith('#'):
            continue
        if not line.startswith('    '):
            m = re.match(r'(\S+) -> (\S+):', line)
            if m:
                current = m.group(2)
                moj_classes[m.group(1)] = current
        else:
            body = line.strip()
            m = re.match(r'(\S+) (\S+)\((.*)\) -> (\S+)', body)
            if m and current:
                ret = re.sub(r'^\d+:\d+:', '', m.group(1))
                moj_methods.setdefault((current, m.group(4)), []).append((m.group(2), m.group(3), ret))

obf_to_moj = {o: m for m, o in moj_classes.items()}

yarn_methods = {}
current = None
with TINY.open(encoding='utf-8') as f:
    f.readline()
    for line in f:
        parts = line.rstrip('\n').split('\t')
        if parts[0] == 'c':
            current = parts[1]
        elif line.startswith('\tm\t') and current:
            yarn_methods[(current, parts[2], parts[3])] = parts[5]

print('moj_methods keys:', len(moj_methods))
print('yarn_methods keys:', len(yarn_methods))
print('cfz/L in moj:', moj_methods.get(('cfz', 'L')))
print('cfz/()I/L in yarn:', yarn_methods.get(('cfz', '()I', 'L')))

target = ('cfz', '()I', 'L')
print('target in yarn_methods:', target in yarn_methods)

count_iter = 0
count_cfz = 0
for k in yarn_methods:
    count_iter += 1
    if k[0] == 'cfz':
        count_cfz += 1
        if count_cfz <= 5:
            print('cfz entry repr:', repr(k))
print('iterated:', count_iter, 'cfz entries:', count_cfz)
print('target repr:', repr(target))
for k in yarn_methods:
    if k[0] == 'cfz' and k[2] == 'L':
        print('MATCH k[2]==L:', repr(k))
        break