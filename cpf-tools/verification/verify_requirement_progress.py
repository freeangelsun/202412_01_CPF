#!/usr/bin/env python3
"""Verify the current CPF Developer-GPT requirement ledger without stale stage columns."""
from __future__ import annotations
import argparse,csv,json,sys
from collections import Counter
from pathlib import Path

CURRENT_REQUIRED=("exact_id","requirement","개발GPT_수행상태","개발GPT_개발상태","개발GPT_검증상태","개발GPT_전체상태","개발GPT_자체검수","개발GPT_검증내용","개발GPT_환경","개발GPT_Evidence","baseline_source_zip_sha256")
ROLE_STATES={"완료","미완료","재개발 요청","재검수 요청","해당 없음","미검증"}
DEV_STATES={"완료","미완료","부분 구현","미구현","실패","재확인 필요","해당 없음"}
VERIFY_STATES={"완료","미완료","미검증","실패","재확인 필요","해당 없음"}
OVERALL_STATES={"완료","부분 구현","미구현","미검증","실패","재확인 필요","해당 없음"}

def load(path:Path):
    if not path.is_file(): raise ValueError(f"ledger missing: {path}")
    with path.open(encoding='utf-8-sig',newline='') as f:
        reader=csv.DictReader(f); rows=list(reader); fields=tuple(reader.fieldnames or ())
    missing=[c for c in CURRENT_REQUIRED if c not in fields]
    if missing: raise ValueError("unsupported ledger schema; missing="+",".join(missing))
    if not rows: raise ValueError("empty ledger")
    ids=[(r.get('exact_id') or '').strip() for r in rows]
    dup=sorted(k for k,v in Counter(ids).items() if k and v>1)
    if any(not x for x in ids): raise ValueError('blank exact_id')
    if dup: raise ValueError('duplicate exact_id='+','.join(dup))
    failures=[]
    for r in rows:
        rid=r['exact_id']
        checks=(("개발GPT_수행상태",ROLE_STATES),("개발GPT_개발상태",DEV_STATES),("개발GPT_검증상태",VERIFY_STATES),("개발GPT_전체상태",OVERALL_STATES))
        for col,allowed in checks:
            value=(r.get(col) or '').strip()
            if value not in allowed: failures.append(f"{rid}:{col}={value!r}")
        if (r.get('개발GPT_전체상태') or '').strip()=='완료' and (r.get('개발GPT_검증상태') or '').strip()!='완료':
            failures.append(f"{rid}:overall_complete_without_verification_complete")
    if failures: raise ValueError('; '.join(failures[:50]))
    return rows

def main()->int:
    ap=argparse.ArgumentParser()
    ap.add_argument('--root',default='.')
    ap.add_argument('--ledger'); ap.add_argument('--csv')
    ap.add_argument('--expected-canonical',type=int,default=205)
    ap.add_argument('--json-output')
    ns=ap.parse_args()
    root=Path(ns.root).resolve(); raw=ns.ledger or ns.csv or 'cpf-docs/work/REQUIREMENT_STATUS.csv'; path=Path(raw); path=path if path.is_absolute() else root/path
    try: rows=load(path)
    except Exception as e:
        print('REQUIREMENT_PROGRESS_GATE=FAIL'); print('REQUIREMENT_PROGRESS_ERROR='+str(e)); return 1
    if len(rows)!=ns.expected_canonical:
        print(f'REQUIREMENT_PROGRESS_GATE=FAIL\nREQUIREMENT_PROGRESS_ERROR=canonical_count={len(rows)} expected={ns.expected_canonical}'); return 1
    status=Counter((r.get('개발GPT_전체상태') or '').strip() for r in rows)
    dev=Counter((r.get('개발GPT_개발상태') or '').strip() for r in rows)
    verify=Counter((r.get('개발GPT_검증상태') or '').strip() for r in rows)
    result={'schema':'CPF_REQUIREMENT_LEDGER_V2','rows':len(rows),'overall':dict(status),'development':dict(dev),'verification':dict(verify)}
    print(json.dumps(result,ensure_ascii=False,sort_keys=True))
    if ns.json_output:
        out=Path(ns.json_output); out=out if out.is_absolute() else root/out; out.parent.mkdir(parents=True,exist_ok=True); out.write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    print('REQUIREMENT_PROGRESS_GATE=PASS'); return 0
if __name__=='__main__': raise SystemExit(main())
