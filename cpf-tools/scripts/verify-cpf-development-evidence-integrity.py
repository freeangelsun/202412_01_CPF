#!/usr/bin/env python3
"""Fail-closed validation for development evidence, finding revalidation, and package manifests."""
from __future__ import annotations
import argparse,csv,hashlib,json,re,subprocess,sys
from pathlib import Path

SHA_RE=re.compile(r'^[0-9a-f]{40}$')
HASH_RE=re.compile(r'^[0-9a-f]{64}$')
PLACEHOLDERS=('TODO','TBD','<HEAD>','<SHA>','YOUR_','REPLACE_ME','추후','나중에 실행','미정')
class GateError(RuntimeError): pass

def _sha(path:Path)->str:
 h=hashlib.sha256()
 with path.open('rb') as f:
  for b in iter(lambda:f.read(1024*1024),b''):h.update(b)
 return h.hexdigest()

def _rows(path:Path):
 if not path.is_file(): raise GateError(f'missing csv: {path}')
 with path.open(encoding='utf-8-sig',newline='') as f:
  r=csv.DictReader(f); return list(r.fieldnames or []),[{k:(v or '').strip() for k,v in x.items()} for x in r]

def _head(root:Path)->str:
 p=subprocess.run(['git','-C',str(root),'rev-parse','HEAD'],text=True,capture_output=True)
 if p.returncode: raise GateError('git HEAD unavailable: '+p.stderr.strip())
 return p.stdout.strip()

def _safe(root:Path, rel:str)->Path:
 if not rel or rel.startswith(('/', '\\')) or '..' in Path(rel).parts: raise GateError(f'unsafe evidence path: {rel!r}')
 p=(root/rel).resolve()
 if p!=root and root not in p.parents: raise GateError(f'evidence escapes root: {rel}')
 return p

def verify(root:Path, review_dir:Path, expected_sha:str|None, source_head:str|None, expected_requirements:int, expected_findings:int):
 if expected_sha and not SHA_RE.fullmatch(expected_sha): raise GateError('expected SHA format invalid')
 actual_head=source_head or _head(root)
 if not SHA_RE.fullmatch(actual_head): raise GateError('source HEAD format invalid')
 if expected_sha and actual_head!=expected_sha: raise GateError(f'HEAD mismatch expected={expected_sha} actual={actual_head}')
 review=(root/review_dir).resolve() if not review_dir.is_absolute() else review_dir.resolve()
 if review!=root and root not in review.parents: raise GateError('review directory escapes root')
 required=['PACKAGE_MANIFEST.json','QA_FINDING_REVALIDATION.csv','REQUIREMENT_STATUS.csv','TEST_AND_EVIDENCE.md','CHANGE_MANIFEST.csv']
 for n in required:
  if not (review/n).is_file(): raise GateError(f'missing review artifact: {n}')
 manifest=json.loads((review/'PACKAGE_MANIFEST.json').read_text(encoding='utf-8'))
 manifest_head=manifest.get('sourceHead') or manifest.get('baselineSha') or manifest.get('basis_sha')
 if manifest_head!=actual_head: raise GateError(f'package manifest source HEAD mismatch {manifest_head!r} != {actual_head}')
 files=manifest.get('files')
 if not isinstance(files,list) or not files: raise GateError('package manifest files must be a non-empty list')
 seen=set();verified=0
 for item in files:
  if not isinstance(item,dict): raise GateError('package manifest file item must be object')
  rel=str(item.get('path','')).replace('\\','/')
  if rel in seen: raise GateError(f'duplicate manifest path: {rel}')
  seen.add(rel);p=_safe(root,rel)
  if not p.is_file(): raise GateError(f'manifest file missing: {rel}')
  size=item.get('sizeBytes',item.get('size_bytes'))
  digest=item.get('sha256')
  if int(size)!=p.stat().st_size: raise GateError(f'manifest size mismatch: {rel}')
  if not isinstance(digest,str) or not HASH_RE.fullmatch(digest) or digest!=_sha(p): raise GateError(f'manifest hash mismatch: {rel}')
  verified+=1
 fields,findings=_rows(review/'QA_FINDING_REVALIDATION.csv')
 mandatory={'finding_id','개발GPT_상태','source_head','positive_exit_code','negative_exit_code','regression_exit_code','evidence_paths','execution_command'}
 if mandatory-set(fields): raise GateError(f'finding ledger missing columns: {sorted(mandatory-set(fields))}')
 if len(findings)!=expected_findings: raise GateError(f'finding count mismatch expected={expected_findings} actual={len(findings)}')
 ids=set();complete=0;incomplete=0;evidence_refs=0
 for row in findings:
  fid=row['finding_id']
  if not fid or fid in ids: raise GateError(f'missing/duplicate finding id: {fid}')
  ids.add(fid)
  state=row['개발GPT_상태']
  if state not in {'완료','미완료'}: raise GateError(f'{fid}: invalid developer state {state!r}')
  if row['source_head']!=actual_head: raise GateError(f'{fid}: stale source head')
  command=row['execution_command']
  if not command or any(token.lower() in command.lower() for token in PLACEHOLDERS): raise GateError(f'{fid}: non-reproducible command')
  refs=[x.strip() for x in re.split(r'[;\n]',row['evidence_paths']) if x.strip()]
  if not refs: raise GateError(f'{fid}: evidence missing')
  for rel in refs:
   if not _safe(root,rel).is_file(): raise GateError(f'{fid}: referenced evidence missing: {rel}')
   evidence_refs+=1
  if state=='완료':
   for key in ('positive_exit_code','negative_exit_code','regression_exit_code'):
    if row[key] != '0': raise GateError(f'{fid}: completed finding lacks successful {key}')
   complete+=1
  else:
   if not row.get('미완료사유','').strip(): raise GateError(f'{fid}: incomplete reason missing')
   incomplete+=1
 req_fields,reqs=_rows(review/'REQUIREMENT_STATUS.csv')
 if len(reqs)!=expected_requirements: raise GateError(f'requirement count mismatch expected={expected_requirements} actual={len(reqs)}')
 if 'requirement_id' not in req_fields: raise GateError('requirement_id column missing')
 if len({r['requirement_id'] for r in reqs})!=len(reqs): raise GateError('requirement IDs missing/duplicate')
 for p in review.glob('*'):
  if p.is_file() and p.suffix.lower() in {'.md','.csv','.json','.txt'}:
   text=p.read_text(encoding='utf-8-sig',errors='replace')
   for token in PLACEHOLDERS:
    if token.lower() in text.lower(): raise GateError(f'placeholder token {token!r} in {p.name}')
 return {'status':'PASS','verifiedAgainstSha':actual_head,'manifestFiles':verified,'findings':{'total':len(findings),'complete':complete,'incomplete':incomplete,'evidenceReferences':evidence_refs},'requirements':len(reqs)}

def main()->int:
 a=argparse.ArgumentParser();a.add_argument('--root',default='.');a.add_argument('--review-dir',required=True);a.add_argument('--expected-sha');a.add_argument('--source-head');a.add_argument('--expected-requirements',type=int,default=10558);a.add_argument('--expected-findings',type=int,default=25);a.add_argument('--json-output');ns=a.parse_args()
 root=Path(ns.root).resolve()
 try:r=verify(root,Path(ns.review_dir),ns.expected_sha,ns.source_head,ns.expected_requirements,ns.expected_findings);code=0
 except Exception as e:r={'status':'FAIL','message':str(e)};code=1
 if ns.json_output:
  p=Path(ns.json_output);p=p if p.is_absolute() else root/p;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(r,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
 print(json.dumps(r,ensure_ascii=False));return code
if __name__=='__main__':raise SystemExit(main())
