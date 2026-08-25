#!/usr/bin/env python3
"""Validate the split canonical requirement master and the current developer projection.

This gate deliberately does not mutate or reinterpret QA/Codex status.  It proves that the
REQUIREMENT_STATUS.csv is the Current Canonical Requirement development ledger.
Only 개발GPT_* columns are owned here; QA/Codex status is never synthesized by this gate.
"""
from __future__ import annotations
import argparse,csv,hashlib,json,sys
from pathlib import Path

class GateError(RuntimeError): pass

def sha256(p:Path)->str:
 h=hashlib.sha256()
 with p.open('rb') as f:
  for b in iter(lambda:f.read(1024*1024),b''):h.update(b)
 return h.hexdigest()

def rows(p:Path):
 with p.open(encoding='utf-8-sig',newline='') as f:
  r=csv.DictReader(f);return list(r.fieldnames or []),list(r)

def verify(root:Path)->dict:
 master=root/'cpf-docs/work/current/CPF_REQUIREMENT_MASTER.csv'
 projection=root/'cpf-docs/work/REQUIREMENT_STATUS.csv'
 if not master.is_file() or not projection.is_file(): raise GateError('requirement master/projection missing')
 mf,mrows=rows(master)
 req={'dataset_kind','logical_record_count','part_sequence','part_path','part_record_count','size_bytes','sha256','consumer_rule'}
 if req-set(mf): raise GateError(f'master index missing columns: {sorted(req-set(mf))}')
 if not mrows: raise GateError('master index empty')
 if any(r['dataset_kind']!='split_csv_logical_master_index' for r in mrows): raise GateError('master dataset_kind drift')
 logical={int(r['logical_record_count']) for r in mrows}
 if len(logical)!=1: raise GateError('logical_record_count inconsistent across index')
 logical_count=logical.pop()
 if logical_count!=30605: raise GateError(f'canonical logical count drift: {logical_count}')
 if len(mrows)!=19: raise GateError(f'canonical part count drift: {len(mrows)}')
 total=0; seen=set(); first_header=None
 for expected_seq,r in enumerate(mrows,1):
  if int(r['part_sequence'])!=expected_seq: raise GateError('part_sequence is not contiguous')
  rel=r['part_path'].replace('\\','/'); p=root/rel
  if not p.is_file(): raise GateError(f'part missing: {rel}')
  if p.stat().st_size!=int(r['size_bytes']): raise GateError(f'part size mismatch: {rel}')
  if sha256(p)!=r['sha256'].lower(): raise GateError(f'part hash mismatch: {rel}')
  pf,pr=rows(p)
  if first_header is None:first_header=pf
  elif pf!=first_header: raise GateError(f'part header mismatch: {rel}')
  if len(pr)!=int(r['part_record_count']): raise GateError(f'part record count mismatch: {rel}')
  for item in pr:
   rid=(item.get('requirement_id') or '').strip()
   if not rid or rid in seen: raise GateError(f'missing/duplicate requirement id: {rid!r}')
   seen.add(rid)
  total+=len(pr)
 if total!=logical_count or len(seen)!=logical_count: raise GateError(f'logical assembly mismatch expected={logical_count} actual={total}/{len(seen)}')
 pf,prows=rows(projection)
 required_projection={'exact_id','개발GPT_수행상태','개발GPT_개발상태','개발GPT_검증상태','개발GPT_전체상태','개발GPT_자체검수','개발GPT_검증내용','개발GPT_환경','개발GPT_Evidence','baseline_source_zip_sha256'}
 if required_projection-set(pf): raise GateError(f'developer projection columns missing: {sorted(required_projection-set(pf))}')
 forbidden=[c for c in pf if c.startswith('QA_') or c.startswith('Codex_')]
 if forbidden: raise GateError(f'developer projection must not own QA/Codex status columns: {forbidden}')
 if any(not (r.get('exact_id') or '').strip() for r in prows): raise GateError('developer ledger has blank exact_id')
 canonical_doc=root/'cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md'
 import re
 catalog_ids=[]
 for line in canonical_doc.read_text(encoding='utf-8-sig').splitlines():
  match=re.match(r'^\| `([A-Z0-9-]+)` \|',line)
  if match: catalog_ids.append(match.group(1))
 ledger_ids=[(r.get('exact_id') or '').strip() for r in prows]
 if not catalog_ids or len(catalog_ids)!=len(set(catalog_ids)): raise GateError(f'canonical catalog count/duplicate drift: {len(catalog_ids)}/{len(set(catalog_ids))}')
 if len(prows)!=len(catalog_ids): raise GateError(f'canonical developer ledger count drift: ledger={len(prows)} catalog={len(catalog_ids)}')
 if ledger_ids!=catalog_ids: raise GateError('REQUIREMENT_STATUS exact_id order/set differs from Current Canonical catalog')
 return {'status':'PASS','canonicalParts':len(mrows),'canonicalLogicalRequirements':logical_count,'canonicalDeveloperLedgerRows':len(prows),'ledgerRole':f'CANONICAL_{len(catalog_ids)}_DEVELOPMENT_STATUS_ONLY'}

def main()->int:
 ap=argparse.ArgumentParser();ap.add_argument('--root',default='.');ap.add_argument('--json-output');ns=ap.parse_args()
 try: result=verify(Path(ns.root).resolve()); rc=0
 except Exception as exc: result={'status':'FAIL','message':str(exc)};rc=1
 if ns.json_output:
  p=Path(ns.json_output); p=p if p.is_absolute() else Path(ns.root)/p; p.parent.mkdir(parents=True,exist_ok=True); p.write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
 print(json.dumps(result,ensure_ascii=False));return rc
if __name__=='__main__':raise SystemExit(main())
