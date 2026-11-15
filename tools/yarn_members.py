import re
import sys
from pathlib import Path

MOJMAP = Path(sys.argv[1])
TINY = Path(sys.argv[2])
ROOT = Path(sys.argv[3])

moj_classes = {}
moj_methods = {}
moj_fields = {}
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
                continue
            m = re.match(r'(\S+) (\S+) -> (\S+)', body)
            if m and current:
                moj_fields[(current, m.group(3))] = m.group(2)

obf_to_moj = {o: m for m, o in moj_classes.items()}

yarn_methods = {}
yarn_fields = {}
current = None
with TINY.open(encoding='utf-8') as f:
    f.readline()
    for line in f:
        parts = line.rstrip('\n').split('\t')
        if parts[0] == 'c':
            current = parts[1]
        elif line.startswith('\tf\t') and current:
            yarn_fields[(current, parts[2])] = parts[5]
        elif line.startswith('\tm\t') and current:
            yarn_methods[(current, parts[2], parts[3])] = parts[5]

def moj_type_to_obf(t):
    arr = 0
    while t.endswith('[]'):
        arr += 1
        t = t[:-2]
    prim = {'int': 'I', 'long': 'J', 'float': 'F', 'double': 'D', 'boolean': 'Z',
            'byte': 'B', 'short': 'S', 'char': 'C', 'void': 'V'}
    if t in prim:
        base = prim[t]
    else:
        obf = moj_classes.get(t)
        if obf is None:
            return None
        base = 'L' + obf + ';'
    return '[' * arr + base

def moj_desc_to_obf(params, ret):
    r = moj_type_to_obf(ret)
    if r is None:
        return None
    out = '('
    if params:
        for p in params.split(','):
            t = moj_type_to_obf(p.strip())
            if t is None:
                return None
            out += t
    return out + ')' + r

methods = {}
for (cls_obf, desc, obfname), yarnname in yarn_methods.items():
    mojcls = obf_to_moj.get(cls_obf)
    if not mojcls:
        continue
    for (mojname, params, ret) in moj_methods.get((cls_obf, obfname), []):
        if moj_desc_to_obf(params, ret) == desc:
            methods.setdefault((mojcls, mojname), set()).add(yarnname)

fields = {}
for (cls_obf, obfname), yarnname in yarn_fields.items():
    mojname = moj_fields.get((cls_obf, obfname))
    mojcls = obf_to_moj.get(cls_obf)
    if mojname and mojcls:
        fields.setdefault((mojcls, mojname), set()).add(yarnname)

moj_to_yarn = {}
for moj, obf in moj_classes.items():
    pass
yarn_class_by_obf = {}
current = None
with TINY.open(encoding='utf-8') as f:
    f.readline()
    for line in f:
        parts = line.rstrip('\n').split('\t')
        if parts[0] == 'c':
            yarn_class_by_obf[parts[1]] = parts[3].replace('/', '.')
for moj, obf in moj_classes.items():
    if obf in yarn_class_by_obf:
        moj_to_yarn[moj] = yarn_class_by_obf[obf]

method_names = {}
for (cls, mojname), opts in methods.items():
    changed = {o for o in opts if o != mojname}
    ycls = moj_to_yarn.get(cls)
    if changed and ycls:
        method_names.setdefault(mojname, {})[ycls] = changed
field_names = {}
for (cls, mojname), opts in fields.items():
    changed = {o for o in opts if o != mojname}
    ycls = moj_to_yarn.get(cls)
    if changed and ycls:
        field_names.setdefault(mojname, {})[ycls] = changed

mask_re = re.compile(r'"(?:\\.|[^"\\])*"|//.*|/\*.*?\*/', re.DOTALL)
import_re = re.compile(r'import\s+(net\.minecraft(?:\.[A-Za-z_][A-Za-z0-9_]*)+)\s*;')
ident_re = re.compile(r'\b([A-Za-z_][A-Za-z0-9_]*)\b')

stats = {'method': 0, 'field': 0}

def masked(text):
    spans = []
    for m in mask_re.finditer(text):
        spans.append((m.start(), m.end()))
    return spans

def in_mask(pos, spans):
    for s, e in spans:
        if s <= pos < e:
            return True
        if s > pos:
            break
    return False

decl_re = re.compile(r'\b(?:public|private|protected|static|final|abstract|synchronized|native|void|int|long|float|double|boolean|byte|short|char|var)\s+(?:[A-Za-z_][A-Za-z0-9_<>,.\[\]\s]*\s+)?([A-Za-z_][A-Za-z0-9_]*)\s*\(')

def rename_file(text):
    imports = set()
    for m in import_re.finditer(text):
        imports.add(m.group(1))
    if not imports:
        return text
    spans = masked(text)
    decls = set()
    for m in decl_re.finditer(text):
        if not in_mask(m.start(1), spans):
            decls.add(m.start(1))
    out = []
    pos = 0
    for m in ident_re.finditer(text):
        name = m.group(1)
        start = m.start(1)
        if in_mask(start, spans):
            continue
        if start in decls:
            continue
        prev = text[start - 1] if start > 0 else ''
        if prev != '.':
            continue
        end = m.end(1)
        nxt = text[end] if end < len(text) else ''
        is_call = nxt == '('
        table = method_names if is_call else field_names
        if name not in table:
            continue
        per_class = table[name]
        opts = set()
        for cls in imports:
            if cls in per_class:
                opts |= per_class[cls]
        if len(opts) == 1:
            out.append(text[pos:start])
            out.append(next(iter(opts)))
            pos = end
            stats['method' if is_call else 'field'] += 1
    out.append(text[pos:])
    return ''.join(out)

files = sorted(ROOT.rglob('*.java'))
for path in files:
    text = path.read_text(encoding='utf-8')
    new = rename_file(text)
    if new != text:
        path.write_text(new, encoding='utf-8', newline='\n')

print(f"files: {len(files)}")
print(f"method renames: {stats['method']}, field renames: {stats['field']}")
print(f"method map: {len(method_names)}, field map: {len(field_names)}")