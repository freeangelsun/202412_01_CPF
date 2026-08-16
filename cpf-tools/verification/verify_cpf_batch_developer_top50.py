#!/usr/bin/env python3
from __future__ import annotations
import argparse,json,re
from pathlib import Path

def main()->int:
    ap=argparse.ArgumentParser();ap.add_argument('--root',default='.');ns=ap.parse_args();root=Path(ns.root).resolve()
    cp=root/'cpf-tools/generator/contracts/cpf-batch-developer-top50.json'; guide=root/'cpf-docs/development/CPF_BATCH_DEVELOPER_TOP_50.md'
    errors=[]
    try: data=json.loads(cp.read_text(encoding='utf-8'))
    except Exception as e: print(f'CPF_BATCH_TOP50=FAIL contract={e}');return 1
    rows=data.get('entries',[])
    if len(rows)!=50: errors.append(f'entry_count={len(rows)}')
    seen=set()
    levels={'golden','capability','advanced'}
    for i,row in enumerate(rows,1):
        if row.get('no')!=i: errors.append(f'ordinal:{i}')
        label=str(row.get('function',''))
        if label in seen: errors.append(f'duplicate:{label}')
        seen.add(label)
        if row.get('usageLevel') not in levels: errors.append(f'usageLevel:{label}')
        rel=str(row.get('source','')).replace('\\','/')
        p=root/rel
        if not p.is_file() or not rel.startswith('cpf-batch/api/src/main/java/'):
            errors.append(f'non_public_api_source:{label}:{rel}');continue
        text=p.read_text(encoding='utf-8',errors='replace')
        cls=p.stem
        if not re.search(rf'public\s+(?:interface|class|record|enum|@interface)\s+{re.escape(cls)}\b',text):
            errors.append(f'not_public_type:{label}:{rel}')
        if '.' in label:
            symbol=label.split('.')[-1].split('(')[0]
            if symbol not in text: errors.append(f'missing_symbol:{label}')
    if not guide.is_file(): errors.append('guide_missing')
    else:
        gt=guide.read_text(encoding='utf-8')
        for row in rows:
            if f"`{row['function']}`" not in gt: errors.append(f'guide_drift:{row["function"]}')
    if errors:
        print('CPF_BATCH_TOP50=FAIL errors='+str(len(errors)))
        for e in errors[:100]: print(e)
        return 1
    print('CPF_BATCH_TOP50=PASS entries=50 publicApiSourceOnly=50')
    return 0
if __name__=='__main__': raise SystemExit(main())
