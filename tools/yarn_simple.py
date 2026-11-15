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

moj_to_yarn = {}
for moj, obf in moj_classes.items():
    if obf in yarn_by_obf:
        moj_to_yarn[moj] = yarn_by_obf[obf]
yarn_to_moj = {v: k for k, v in moj_to_yarn.items()}

mask_re = re.compile(r'"(?:\\.|[^"\\])*"|//.*|/\*.*?\*/', re.DOTALL)
import_re = re.compile(r'^import\s+(static\s+)?([\w.]+)\s*;', re.MULTILINE)
ident_re = re.compile(r'\b([A-Za-z_][A-Za-z0-9_]*)\b')
decl_re = re.compile(r'\b(?:class|interface|enum|record)\s+([A-Za-z_][A-Za-z0-9_]*)')

stats = {'renames': 0, 'files': 0}

def process(text):
    imports = list(import_re.finditer(text))
    rename = {}
    protected = set()
    for m in imports:
        fq = m.group(2)
        simple = fq.rsplit('.', 1)[-1]
        if fq.startswith('net.minecraft.') and fq in yarn_to_moj:
            moj = yarn_to_moj[fq]
            old_simple = moj.rsplit('.', 1)[-1]
            if old_simple != simple:
                rename[old_simple] = simple
        else:
            protected.add(simple)
    for m in decl_re.finditer(text):
        protected.add(m.group(1))
    for old in list(rename):
        if old in protected:
            del rename[old]
    if not rename:
        return text
    spans = [(m.start(), m.end()) for m in mask_re.finditer(text)]
    import_spans = [(m.start(), m.end()) for m in imports]
    def skipped(pos):
        for s, e in spans:
            if s <= pos < e:
                return True
            if s > pos:
                break
        for s, e in import_spans:
            if s <= pos < e:
                return True
            if s > pos:
                break
        return False
    out = []
    pos = 0
    for m in ident_re.finditer(text):
        name = m.group(1)
        start = m.start(1)
        if name not in rename or skipped(start):
            continue
        prev = text[start - 1] if start > 0 else ''
        if prev == '.':
            continue
        out.append(text[pos:start])
        out.append(rename[name])
        pos = m.end(1)
        stats['renames'] += 1
    out.append(text[pos:])
    return ''.join(out)

SKIP_DIRS = {'compat', 'config', 'mixin'}
SKIP_FILES = {'PrimogemCraft.java', 'PGCClient.java'}
files = sorted(ROOT.rglob('*.java'))
for path in files:
    if any(part in SKIP_DIRS for part in path.parts):
        continue
    if path.name in SKIP_FILES:
        continue
    text = path.read_text(encoding='utf-8')
    new = process(text)
    if new != text:
        path.write_text(new, encoding='utf-8', newline='\n')
        stats['files'] += 1
print(f"files changed: {stats['files']}, simple renames: {stats['renames']}")