#!/usr/bin/env python3
"""Verify Current Development Harness work-item progress from the single Current Registry."""
from __future__ import annotations

import sys as _cpf_sys

# CPF 표준 인코딩은 UTF-8 이다. 호출자의 콘솔 코드페이지(Windows cp949 등)에 좌우되면
# 한글 출력이 깨져 진단 메시지를 읽을 수 없다. 진입점이 스스로 출력 스트림을 고정한다.
for _cpf_stream in (_cpf_sys.stdout, _cpf_sys.stderr):
    try:
        _cpf_stream.reconfigure(encoding='utf-8')
    except (AttributeError, ValueError):
        pass
import argparse,csv,json,re
from collections import Counter
from pathlib import Path

REQUIRED=("work_item_id","developer_status","verification_status","overall_status","source_identity","item_role")
DEV_STATES={"완료","미완료","부분 구현","미구현","실패","재확인 필요","해당 없음","SOURCE_FIXED","VERIFICATION_PENDING","BLOCKED_EXTERNAL","NOT_EXECUTED","UNKNOWN"}
VERIFY_STATES={"완료","미완료","미검증","실패","재확인 필요","해당 없음","VERIFICATION_PENDING","BLOCKED_EXTERNAL","NOT_EXECUTED","UNKNOWN"}
# item_role 은 열거값이다. 정본이 늘어나면 여기 추가한다(작업 현황 수치가 아니라 계약이다).
ITEM_ROLES={'ROOT_CAUSE_EXECUTION','TRACKING'}

OVERALL_STATES={"완료","부분 구현","미구현","미검증","실패","재확인 필요","해당 없음","VERIFICATION_PENDING","BLOCKED_EXTERNAL","NOT_EXECUTED","UNKNOWN"}

def split(value:str)->set:
    return {x.strip() for x in (value or '').split(',') if x.strip()}

def load(path:Path):
    if not path.is_file(): raise ValueError(f"registry missing: {path}")
    with path.open(encoding='utf-8-sig',newline='') as f:
        reader=csv.DictReader(f); rows=list(reader); fields=tuple(reader.fieldnames or ())
    missing=[c for c in REQUIRED if c not in fields]
    if missing: raise ValueError("unsupported Current Registry schema; missing="+",".join(missing))
    if not rows: raise ValueError("empty Current Registry")
    ids=[(r.get('work_item_id') or '').strip() for r in rows]
    if any(not x for x in ids): raise ValueError('blank work_item_id')
    dup=sorted(k for k,v in Counter(ids).items() if v>1)
    if dup: raise ValueError('duplicate work_item_id='+','.join(dup))
    failures=[]
    for r in rows:
        wid=r['work_item_id']
        for col,allowed in (("developer_status",DEV_STATES),("verification_status",VERIFY_STATES),("overall_status",OVERALL_STATES)):
            value=(r.get(col) or '').strip()
            if value not in allowed: failures.append(f"{wid}:{col}={value!r}")
        sid=(r.get('source_identity') or '').strip().lower()
        if not re.fullmatch(r'[0-9a-f]{64}',sid): failures.append(f"{wid}:invalid_source_identity={sid!r}")
        if (r.get('overall_status') or '').strip()=='완료' and (r.get('verification_status') or '').strip()!='완료':
            failures.append(f"{wid}:overall_complete_without_verification_complete")
    if failures: raise ValueError('; '.join(failures[:50]))
    return rows

def main()->int:
    ap=argparse.ArgumentParser(); ap.add_argument('--root',default='.'); ap.add_argument('--ledger'); ap.add_argument('--csv'); ap.add_argument('--expected-canonical',type=int); ap.add_argument('--json-output'); ns=ap.parse_args()
    root=Path(ns.root).resolve(); raw=ns.ledger or ns.csv or 'cpf-docs/governance/development-harness/current/CURRENT_WORK_ITEM_REGISTRY.csv'; path=Path(raw); path=path if path.is_absolute() else root/path
    try: rows=load(path)
    except Exception as e:
        print('REQUIREMENT_PROGRESS_GATE=FAIL'); print('REQUIREMENT_PROGRESS_ERROR='+str(e)); return 1
    expected=ns.expected_canonical
    if expected is None:
        # 작업 현황 row 수는 제품 계약상 고정 cardinality 가 아니라 진행에 따라 늘어나는 값이다.
        # snapshot 숫자를 코드에 복제하면 (1) WP 를 하나 추가할 때마다 사람이 상수를 올려야 하고,
        # (2) 그 습관이 "숫자만 올려 PASS 시키는" 경로가 되어 실제 정합성 검사를 대체해 버린다.
        # 따라서 현재 Registry 자체를 Source of Truth 로 쓰고, 대신 의미적 불변조건을 검사한다.
        execution=[r for r in rows if (r.get('item_role') or '').strip()=='ROOT_CAUSE_EXECUTION']
        if not execution:
            print('REQUIREMENT_PROGRESS_GATE=FAIL\nREQUIREMENT_PROGRESS_ERROR=no_root_cause_execution_work_item'); return 1
        roles=Counter((r.get('item_role') or '').strip() for r in rows)
        unknown=sorted(k for k in roles if k not in ITEM_ROLES)
        if unknown:
            print(f'REQUIREMENT_PROGRESS_GATE=FAIL\nREQUIREMENT_PROGRESS_ERROR=invalid_item_role={unknown}'); return 1
        # Root Cause Execution 은 실행 주체가 자기 자신을 가리켜야 추적이 끊기지 않는다.
        orphan=sorted(r['work_item_id'] for r in execution
                      if r['work_item_id'] not in split(r.get('execution_wp_ids') or ''))
        if orphan:
            print(f'REQUIREMENT_PROGRESS_GATE=FAIL\nREQUIREMENT_PROGRESS_ERROR=execution_wp_self_reference_missing={orphan[:20]}'); return 1
        expected=len(rows)
    if len(rows)!=expected:
        print(f'REQUIREMENT_PROGRESS_GATE=FAIL\nREQUIREMENT_PROGRESS_ERROR=current_registry_count={len(rows)} expected={expected}'); return 1
    result={'schema':'CPF_CURRENT_WORK_ITEM_REGISTRY_V1','rows':len(rows),'overall':dict(Counter((r.get('overall_status') or '').strip() for r in rows)),'development':dict(Counter((r.get('developer_status') or '').strip() for r in rows)),'verification':dict(Counter((r.get('verification_status') or '').strip() for r in rows))}
    print(json.dumps(result,ensure_ascii=False,sort_keys=True))
    if ns.json_output:
        out=Path(ns.json_output); out=out if out.is_absolute() else root/out; out.parent.mkdir(parents=True,exist_ok=True); out.write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    print('REQUIREMENT_PROGRESS_GATE=PASS'); return 0
if __name__=='__main__': raise SystemExit(main())
