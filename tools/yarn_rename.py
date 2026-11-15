import re
import sys
from pathlib import Path

MOJMAP = Path(sys.argv[1])
TINY = Path(sys.argv[2])
ROOT = Path(sys.argv[3])

moj_classes = {}
with MOJMAP.open(encoding='utf-8') as f:
    for line in f:
        line = line.rstrip('\n')
        if not line or line.startswith('#') or line.startswith('    '):
            continue
        m = re.match(r'(\S+) -> (\S+):', line)
        if m:
            moj_classes[m.group(1)] = m.group(2)

yarn_classes = {}
with TINY.open(encoding='utf-8') as f:
    f.readline()
    for line in f:
        parts = line.rstrip('\n').split('\t')
        if parts[0] == 'c':
            yarn_classes[parts[1]] = parts[3]

classes = {}
for moj, obf in moj_classes.items():
    if obf in yarn_classes:
        classes[moj] = yarn_classes[obf]

simple = {}
for moj, yarn in classes.items():
    ms = moj.rsplit('.', 1)[-1]
    ys = yarn.rsplit('/', 1)[-1]
    if ms != ys:
        simple.setdefault(ms, set()).add(ys)

fq_re = re.compile(r'\bnet\.minecraft(?:\.[A-Za-z_][A-Za-z0-9_]*)+\b')
ident_re = re.compile(r'\b[A-Za-z_][A-Za-z0-9_]*\b')

stats = {'fq': 0, 'simple': 0, 'unknown': set()}

def rename(text):
    def fq_sub(m):
        full = m.group(0)
        named = classes.get(full)
        if named:
            stats['fq'] += 1
            return named.replace('/', '.')
        stats['unknown'].add(full)
        return full
    text = fq_re.sub(fq_sub, text)
    return text

files = sorted(ROOT.rglob('*.java'))
for path in files:
    text = path.read_text(encoding='utf-8')
    text = rename(text)
    path.write_text(text, encoding='utf-8', newline='\n')

print(f"files: {len(files)}")
print(f"fq renames: {stats['fq']}, simple renames: {stats['simple']}")
print(f"joined classes: {len(classes)}, simple rename map: {len(simple)}")
print("unmapped net.minecraft references:")
for u in sorted(stats['unknown']):
    print("  " + u)