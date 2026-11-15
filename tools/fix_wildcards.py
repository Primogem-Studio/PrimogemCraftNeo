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

yarn_by_obf = {}
with TINY.open(encoding='utf-8') as f:
    f.readline()
    for line in f:
        p = line.rstrip('\n').split('\t')
        if p[0] == 'c':
            yarn_by_obf[p[1]] = p[3].replace('/', '.')

moj_pkg_to_yarn = {}
for moj, obf in moj_classes.items():
    if obf in yarn_by_obf:
        mp = moj.rsplit('.', 1)[0]
        yp = yarn_by_obf[obf].rsplit('.', 1)[0]
        moj_pkg_to_yarn.setdefault(mp, {}).setdefault(yp, 0)
        moj_pkg_to_yarn[mp][yp] += 1

wild_re = re.compile(r'^import\s+(net\.minecraft(?:\.[A-Za-z_][A-Za-z0-9_]*)+)\.\*\s*;', re.MULTILINE)

stats = {'fixed': 0, 'ambiguous': 0}

def fix(text):
    def sub(m):
        pkg = m.group(1)
        if pkg in moj_pkg_to_yarn:
            candidates = moj_pkg_to_yarn[pkg]
            best = max(candidates, key=candidates.get)
            if len(candidates) > 1:
                stats['ambiguous'] += 1
            stats['fixed'] += 1
            return f'import {best}.*;'
        return m.group(0)
    return wild_re.sub(sub, text)

SKIP_DIRS = {'compat', 'config', 'mixin'}
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
print(f"files changed: {changed}, wildcards fixed: {stats['fixed']}, ambiguous: {stats['ambiguous']}")