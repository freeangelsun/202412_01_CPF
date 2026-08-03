#!/usr/bin/env python3
"""Fail-closed requirement result/role/evidence traceability gate."""
from __future__ import annotations
import argparse,csv,json,re,subprocess,sys
from pathlib import Path
SHA=re.compile(r'^[0-9a-f]{40}$');ROLE={'완료','미완료','재개발 요청','재검수 요청','해당 없음',''};OVERALL={'부분 구현','미구현','미검증','실패','재확인 필요',''}
MANDATORY=('requirement_id','development_status','verification_status','개발GPT_수행여부','개발GPT_상태','개발GPT_수행내용','개발GPT_미완료사유','개발GPT_실행및검증','개발GPT_필요환경및권한','개발GPT_evidence')
class GateError(RuntimeError):pass
def git(root,*a):
 p=subprocess.run(['git','-C',str(root),*a],capture_output=True,text=True)
 if p.returncode:raise GateError(f"git {' '.join(a)} failed: {p.stderr.strip()}")
 return p.stdout.strip()
def read(p):
 with p.open(encoding='utf-8-sig',newline='') as f:r=csv.DictReader(f);return list(r.fieldnames or []),list(r)
def verify(root:Path,result_matrix:Path,expected_sha=None,require_clean=False,release=False):
 head=git(root,'rev-parse','HEAD');status=git(root,'status','--porcelain')
 if expected_sha and head!=expected_sha:raise GateError(f'HEAD mismatch expected={expected_sha} actual={head}')
 if require_clean and status:raise GateError('working tree is not clean')
 if not result_matrix.is_file():raise GateError(f'active result matrix missing: {result_matrix}')
 fields,rows=read(result_matrix)
 if not rows:raise GateError('active result matrix is empty')
 miss=[x for x in MANDATORY if x not in fields]
 if miss:raise GateError(f'mandatory columns missing: {miss}')
 findings=[];ids=set();verified=0
 for n,row in enumerate(rows,2):
  rid=(row.get('requirement_id') or '').strip()
  if not rid:findings.append(f'row {n}: blank requirement_id');continue
  if rid in ids:findings.append(f'row {n}: duplicate requirement_id={rid}')
  ids.add(rid)
  if row.get('development_status','').strip() not in OVERALL:findings.append(f'{rid}: invalid development_status')
  if row.get('verification_status','').strip() not in OVERALL:findings.append(f'{rid}: invalid verification_status')
  if row.get('개발GPT_상태','').strip() not in ROLE:findings.append(f'{rid}: invalid 개발GPT_상태')
  # QA pass is the only condition under which an overall 완료 is legal.
  qa_pass=(row.get('QA_상태') or row.get('qa_status') or '').strip()=='통과'
  if (row.get('development_status','').strip()=='완료' or row.get('verification_status','').strip()=='완료') and not qa_pass:findings.append(f'{rid}: overall 완료 before QA 통과')
  ev=(row.get('개발GPT_evidence') or '').strip();cmd=(row.get('개발GPT_실행및검증') or '').strip()
  if row.get('개발GPT_상태','').strip()=='완료':
   if not ev or not cmd:findings.append(f'{rid}: completed role lacks evidence/command')
   sha=(row.get('verifiedAgainstSha') or row.get('exact_sha') or '').strip()
   if sha!=head:findings.append(f'{rid}: exact SHA mismatch expected={head} actual={sha or "blank"}')
   else:verified+=1
  if re.search(r'<[^>]+>|TODO|TBD|나중에|환경 없음$',cmd,flags=re.I):findings.append(f'{rid}: placeholder/non-reproducible command')
 if release and any((r.get('개발GPT_상태') or '').strip()!='완료' for r in rows):findings.append('release mode requires every development role row complete')
 result={'status':'PASS' if not findings else 'FAIL','verifiedAgainstSha':head,'workingTreeClean':not bool(status),'matrix':result_matrix.relative_to(root).as_posix() if root in result_matrix.parents else str(result_matrix),'rowCount':len(rows),'uniqueRequirementCount':len(ids),'exactShaVerifiedRows':verified,'findings':findings}
 if findings:raise GateError(json.dumps(result,ensure_ascii=False,indent=2))
 return result

def main():
 p=argparse.ArgumentParser();p.add_argument('--root',default='.');p.add_argument('--result-matrix',required=True);p.add_argument('--expected-sha');p.add_argument('--require-clean',action='store_true');p.add_argument('--release',action='store_true');p.add_argument('--json-output');a=p.parse_args();root=Path(a.root).resolve();rm=Path(a.result_matrix);rm=rm if rm.is_absolute() else root/rm
 try:r=verify(root,rm,a.expected_sha,a.require_clean,a.release);code=0
 except Exception as e:
  try:r=json.loads(str(e))
  except:r={'status':'FAIL','message':str(e)}
  code=1
 if a.json_output:
  o=Path(a.json_output);o=o if o.is_absolute() else root/o;o.parent.mkdir(parents=True,exist_ok=True);o.write_text(json.dumps(r,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
 print(json.dumps(r,ensure_ascii=False));return code
if __name__=='__main__':raise SystemExit(main())
