import sys
from pathlib import Path

TINY = Path(sys.argv[1])
want_cls = set(sys.argv[2:])
cur = None
with TINY.open(encoding='utf-8') as f:
    f.readline()
    for line in f:
        p = line.rstrip('\n').split('\t')
        if p[0] == 'c':
            cur = p[3]
            if cur in want_cls:
                print('CLASS', cur)
        elif line.startswith('\tm\t') and cur in want_cls:
            print('  M', cur.split('/')[-1], p[5], p[2])
        elif line.startswith('\tf\t') and cur in want_cls:
            print('  F', cur.split('/')[-1], p[5])