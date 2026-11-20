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
    header = f.readline().rstrip('\n').split('\t')
    # tiny v2 头形如 tiny\t2\t0\t<ns1>\t<ns2>...
    namespaces = header[3:]
    if 'named' not in namespaces:
        raise SystemExit(f"mappings.tiny has no 'named' namespace: {namespaces}")
    named_col = 1 + namespaces.index('named')
    # mojmap 的 obf 名对应 official 命名空间；若表里没有 official，
    # 则第一列即 intermediary，无法与 mojmap 对接，需换用含 official 的表。
    if 'official' not in namespaces:
        raise SystemExit(
            f"mappings.tiny lacks 'official' namespace ({namespaces}); "
            "mojmap obf names cannot be joined. Use the loom yarn mappings.tiny "
            "which contains official/intermediary/named.")
    obf_col = 1 + namespaces.index('official')
    for line in f:
        parts = line.rstrip('\n').split('\t')
        if parts and parts[0] == 'c' and len(parts) > max(obf_col, named_col):
            yarn_classes[parts[obf_col]] = parts[named_col]

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