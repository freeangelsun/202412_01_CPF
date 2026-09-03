#!/usr/bin/env python3
"""Validate split requirement master integrity and Current Registry -> Current Status projection."""
from __future__ import annotations

import sys as _cpf_sys

# CPF 표준 인코딩은 UTF-8 이다. 호출자의 콘솔 코드페이지(Windows cp949 등)에 좌우되면
# 한글 출력이 깨져 진단 메시지를 읽을 수 없다. 진입점이 스스로 출력 스트림을 고정한다.
for _cpf_stream in (_cpf_sys.stdout, _cpf_sys.stderr):
    try:
        _cpf_stream.reconfigure(encoding='utf-8')
    except (AttributeError, ValueError):
        pass
import argparse,csv,hashlib,json
from pathlib import Path

class GateError(RuntimeError): pass

def sha256(p:Path)->str:
 h=hashlib.sha256();
 with p.open('rb') as f:
  for b in iter(lambda:f.read(1024*1024),b''): h.update(b)
 return h.hexdigest()

def rows(p:Path):
 with p.open(encoding='utf-8-sig',newline='') as f:
  r=csv.DictReader(f); return list(r.fieldnames or []),list(r)

def verify(root:Path)->dict:
 master=root/'cpf-docs/governance/development-harness/current/CPF_REQUIREMENT_MASTER.csv'
 registry=root/'cpf-docs/governance/development-harness/current/CURRENT_WORK_ITEM_REGISTRY.csv'
 projection=root/'cpf-docs/governance/development-harness/current/CURRENT_DEVELOPMENT_STATUS.csv'
 if not master.is_file() or not registry.is_file() or not projection.is_file(): raise GateError('requirement master/Current Registry/Current Status projection missing')
 mf,mrows=rows(master); req={'dataset_kind','logical_record_count','part_sequence','part_path','part_record_count','size_bytes','sha256','consumer_rule'}
 if req-set(mf): raise GateError(f'master index missing columns: {sorted(req-set(mf))}')
 if not mrows: raise GateError('master index empty')
 logical={int(r['logical_record_count']) for r in mrows}
 if len(logical)!=1: raise GateError('logical_record_count inconsistent across index')
 logical_count=logical.pop(); total=0; first_header=None
 for expected_seq,r in enumerate(mrows,1):
  if int(r['part_sequence'])!=expected_seq: raise GateError('part_sequence is not contiguous')
  p=root/r['part_path'].replace('\\','/')
  if not p.is_file() or p.stat().st_size!=int(r['size_bytes']) or sha256(p)!=r['sha256'].lower(): raise GateError(f'part integrity mismatch: {r["part_path"]}')
  pf,pr=rows(p)
  if first_header is None:first_header=pf
  elif pf!=first_header: raise GateError(f'part header mismatch: {r["part_path"]}')
  if len(pr)!=int(r['part_record_count']): raise GateError(f'part record count mismatch: {r["part_path"]}')
  total+=len(pr)
 if total!=logical_count: raise GateError(f'logical assembly mismatch expected={logical_count} actual={total}')
 rf,rrows=rows(registry); pf,prows=rows(projection)
 registry_required={'work_item_id','source_requirement_ids','priority','work_package','developer_status','verification_status','overall_status','source_identity','closure_rule'}
 projection_required={'work_item_id','source_requirement_ids','priority','work_package','development_status','verification_status','runtime_status','overall_status','source_identity','devgpt_status','independent_reviewer_status','qa_status','current_action','closure_rule'}
 if registry_required-set(rf): raise GateError(f'Current Registry columns missing: {sorted(registry_required-set(rf))}')
 if projection_required-set(pf): raise GateError(f'Current Status projection columns missing: {sorted(projection_required-set(pf))}')
 # WP-R16.01/02 등록으로 411 -> 413, 2026-09-03 사용자 Steering 3건(WP-R17.01 Shell 조립성 /
 # WP-R17.02 운영자 선택 마스킹 / WP-R17.03 운영자 구성 로그 항목) 등록으로 413 -> 416 이 되었다.
 if len(rrows)!=417 or len(prows)!=417: raise GateError(f'Current projection row count drift registry={len(rrows)} projection={len(prows)} expected=417')
 rids=[(r.get('work_item_id') or '').strip() for r in rrows]; pids=[(r.get('work_item_id') or '').strip() for r in prows]
 if len(set(rids))!=len(rids) or len(set(pids))!=len(pids) or rids!=pids: raise GateError('Current Status work_item_id order/set differs from Current Registry')
 pmap={r['work_item_id']:r for r in prows}
 mism=[]
 for r in rrows:
  p=pmap[r['work_item_id']]
  pairs=(('source_requirement_ids','source_requirement_ids'),('priority','priority'),('work_package','work_package'),('developer_status','development_status'),('verification_status','verification_status'),('overall_status','overall_status'),('source_identity','source_identity'),('closure_rule','closure_rule'))
  for rc,pc in pairs:
   if (r.get(rc) or '').strip()!=(p.get(pc) or '').strip(): mism.append(f'{r["work_item_id"]}:{rc}->{pc}')
 if mism: raise GateError('Current Status projection drift: '+','.join(mism[:30]))
 return {'status':'PASS','canonicalParts':len(mrows),'canonicalLogicalRequirements':logical_count,'currentRegistryRows':len(rrows),'currentStatusRows':len(prows),'projection':'CURRENT_WORK_ITEM_REGISTRY_TO_CURRENT_DEVELOPMENT_STATUS'}

def main()->int:
 ap=argparse.ArgumentParser(); ap.add_argument('--root',default='.'); ap.add_argument('--json-output'); ns=ap.parse_args()
 try: result=verify(Path(ns.root).resolve()); rc=0
 except Exception as exc: result={'status':'FAIL','message':str(exc)}; rc=1
 if ns.json_output:
  p=Path(ns.json_output); p=p if p.is_absolute() else Path(ns.root)/p; p.parent.mkdir(parents=True,exist_ok=True); p.write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
 print(json.dumps(result,ensure_ascii=False)); return rc
if __name__=='__main__': raise SystemExit(main())
