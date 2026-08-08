#!/usr/bin/env python3
from pathlib import Path
import sys
root=Path(sys.argv[1] if len(sys.argv)>1 else '.').resolve()
core=root/'cpf-core/src/main/java'
forbidden=('com.cpf.core.mapper.','org.apache.ibatis.','org.mybatis.')
violations=[]
for p in core.rglob('*.java'):
    text=p.read_text(encoding='utf-8',errors='replace')
    for token in forbidden:
        if token in text:
            violations.append(f'{p.relative_to(root)} -> {token}')
if violations:
    print('FAIL core persistence boundary')
    print('\n'.join(violations))
    raise SystemExit(1)
print('PASS core persistence boundary: no MyBatis/mapper implementation imports')
