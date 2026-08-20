#!/usr/bin/env python3
"""Verify CPF evidence semantics; file existence or bulk requirement IDs cannot prove completion."""
from __future__ import annotations
import argparse,csv,hashlib,json,re,sys
from pathlib import Path
SHA_RE=re.compile(r'^[0-9a-f]{40}$');HASH_RE=re.compile(r'^[0-9a-f]{64}$')
class EvidenceError(RuntimeError):pass

def split_paths(value:str)->list[str]:return [part.strip() for part in re.split(r'[;\n]',value or '') if part.strip()]
def load_json(path:Path)->dict:
 if not path.is_file():raise EvidenceError(f'evidence missing: {path}')
 try:value=json.loads(path.read_text(encoding='utf-8-sig'))
 except Exception as exc:raise EvidenceError(f'invalid evidence JSON {path}: {exc}') from exc
 if not isinstance(value,dict):raise EvidenceError(f'evidence must be object: {path}')
 return value

def validate_document(path:Path,data:dict,expected_sha:str|None=None)->set[str]:
 required=['schemaVersion','evidenceId','evidenceType','sourceSha','resultSha','command','startedAt','endedAt','exitCode','sanitized','requirements','scenarios','assertions','artifacts']
 missing=[key for key in required if key not in data]
 if missing:raise EvidenceError(f'{path}: missing fields={missing}')
 if data['evidenceType'] not in ('execution','aggregate'):raise EvidenceError(f'{path}: invalid evidenceType')
 source=result=str(data['sourceSha']),str(data['resultSha'])
 source_sha,result_sha=source
 if not SHA_RE.fullmatch(source_sha) or not SHA_RE.fullmatch(result_sha) or source_sha!=result_sha:raise EvidenceError(f'{path}: exact sourceSha=resultSha required')
 if expected_sha and source_sha!=expected_sha:raise EvidenceError(f'{path}: evidence SHA mismatch expected={expected_sha} actual={source_sha}')
 if not isinstance(data['exitCode'],int) or data['exitCode']!=0:raise EvidenceError(f'{path}: successful evidence requires exitCode=0')
 if data['sanitized'] is not True:raise EvidenceError(f'{path}: sanitized=true required')
 if not str(data['command']).strip():raise EvidenceError(f'{path}: command required')
 requirements=data['requirements']
 if not isinstance(requirements,list) or not requirements or len(set(requirements))!=len(requirements):raise EvidenceError(f'{path}: unique requirements required')
 if data['evidenceType']=='execution' and len(requirements)>5:raise EvidenceError(f'{path}: Bulk-ID evidence forbidden, execution requirements={len(requirements)}')
 if data['evidenceType']=='aggregate':
  children=data.get('childEvidence')
  if not isinstance(children,list) or not children:raise EvidenceError(f'{path}: aggregate requires childEvidence and cannot prove rows directly')
 scenarios=data['scenarios'];assertions=data['assertions'];artifacts=data['artifacts']
 if not isinstance(scenarios,list) or not scenarios:raise EvidenceError(f'{path}: executable scenarios required')
 for scenario in scenarios:
  if not isinstance(scenario,dict) or any(not str(scenario.get(key,'')).strip() for key in ('scenarioId','precondition','action','expectedResult','actualResult')):raise EvidenceError(f'{path}: invalid scenario contract')
 if not isinstance(assertions,list) or not assertions:raise EvidenceError(f'{path}: assertions required')
 for assertion in assertions:
  if not isinstance(assertion,dict) or assertion.get('passed') is not True or not str(assertion.get('name','')).strip():raise EvidenceError(f'{path}: all assertions must explicitly pass')
 if not isinstance(artifacts,list) or not artifacts:raise EvidenceError(f'{path}: artifact hashes required')
 for artifact in artifacts:
  if not isinstance(artifact,dict) or not str(artifact.get('path','')).strip() or not HASH_RE.fullmatch(str(artifact.get('sha256',''))):raise EvidenceError(f'{path}: invalid artifact SHA-256')
 return set(requirements)

def validate_matrix(root:Path,matrix:Path,expected_sha:str|None=None)->tuple[int,int]:
 if not matrix.is_file():raise EvidenceError(f'result matrix missing: {matrix}')
 with matrix.open(encoding='utf-8-sig',newline='') as handle:rows=list(csv.DictReader(handle))
 verified=0;documents={}
 for row_no,row in enumerate(rows,2):
  if row.get('verification_status')!='완료':continue
  verified+=1;requirement=row.get('requirement_id','');paths=split_paths(row.get('evidence_paths',''))
  if not paths:raise EvidenceError(f'{matrix}:{row_no}: verified requirement has no evidence')
  direct=False
  for relative in paths:
   path=(root/relative).resolve()
   if root not in path.parents:raise EvidenceError(f'{matrix}:{row_no}: evidence path escapes repository')
   if path not in documents:documents[path]=(load_json(path),None)
   data,_=documents[path];covered=validate_document(path,data,expected_sha);documents[path]=(data,covered)
   if data['evidenceType']=='execution' and requirement in covered:direct=True
  if not direct:raise EvidenceError(f'{matrix}:{row_no}: no direct execution evidence covers {requirement}')
 return verified,len(documents)

def validate_requirement_master(root:Path,index:Path,expected_sha:str|None=None)->tuple[int,int]:
 if not index.is_file():raise EvidenceError(f'requirement master index missing: {index}')
 with index.open(encoding='utf-8-sig',newline='') as handle:parts=list(csv.DictReader(handle))
 verified=0;documents={}
 for part in parts:
  part_path=root/part['part_path']
  if not part_path.is_file():raise EvidenceError(f'requirement master part missing: {part_path}')
  with part_path.open(encoding='utf-8-sig',newline='') as handle:rows=list(csv.DictReader(handle))
  for row_no,row in enumerate(rows,2):
   if row.get('verification_status')!='완료':continue
   verified+=1;requirement=row.get('requirement_id','');paths=split_paths(row.get('evidence_path',''))
   if not paths:raise EvidenceError(f'{part_path}:{row_no}: verified requirement has no evidence')
   direct=False
   for relative in paths:
    path=(root/relative).resolve()
    if root not in path.parents:raise EvidenceError(f'{part_path}:{row_no}: evidence path escapes repository')
    if path not in documents:documents[path]=(load_json(path),None)
    data,_=documents[path];covered=validate_document(path,data,expected_sha);documents[path]=(data,covered)
    if data['evidenceType']=='execution' and requirement in covered:direct=True
   if not direct:raise EvidenceError(f'{part_path}:{row_no}: no direct execution evidence covers {requirement}')
 return verified,len(documents)

def main()->int:
 parser=argparse.ArgumentParser();parser.add_argument('--root',type=Path,default=Path.cwd());parser.add_argument('--matrix');parser.add_argument('--requirement-master',default='cpf-docs/work/current/CPF_REQUIREMENT_MASTER.csv');parser.add_argument('--expected-sha')
 args=parser.parse_args();root=args.root.resolve()
 verified,documents=(validate_matrix(root,root/args.matrix,args.expected_sha) if args.matrix else validate_requirement_master(root,root/args.requirement_master,args.expected_sha))
 if verified <= 0 or documents <= 0: raise EvidenceError(f'vacuous evidence closure forbidden: verifiedRows={verified} documents={documents}')
 print(f'[PASS] CPF evidence semantics verifiedRows={verified} documents={documents} bulkIdForbidden=true');return 0
if __name__=='__main__':
 try:raise SystemExit(main())
 except EvidenceError as error:print(f'[FAIL] {error}',file=sys.stderr);raise SystemExit(1)
