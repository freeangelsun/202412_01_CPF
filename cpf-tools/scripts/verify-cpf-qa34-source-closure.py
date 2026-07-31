#!/usr/bin/env python3
from __future__ import annotations
import argparse,csv,hashlib,json,os,re,shutil,subprocess,tempfile
from datetime import datetime,timezone
from pathlib import Path
SOURCE_REQUIREMENTS=['QA34-REQ-001','QA34-REQ-002','QA34-REQ-005','QA34-REQ-006','QA34-REQ-008','QA34-REQ-009','QA34-REQ-011','QA34-REQ-012','QA34-REQ-014','QA34-REQ-016']
GATES=[
 ['python','cpf-tools/scripts/verify-cpf-qa34-build-contract.py','--root','.'],
 ['python','cpf-tools/scripts/verify-cpf-qa34-bff-security.py','--root','.'],
 ['python','cpf-tools/scripts/verify-cpf-qa34-batch-outbound-policy.py','--root','.'],
 ['python','cpf-tools/scripts/verify-cpf-qa34-kafka-ack-contract.py','--root','.'],
 ['python','cpf-tools/scripts/verify-cpf-qa34-network-identity.py','--root','.'],
 ['python','cpf-tools/scripts/verify-cpf-qa34-browser-contract.py','--root','.'],
 ['python','cpf-tools/scripts/verify-cpf-qa34-runtime-contracts.py','--root','.'],
 ['python','cpf-tools/scripts/verify-cpf-db-vendor-static-token-parity.py','--root','.'],
]
SECRET=re.compile(r'(?i)(password|secret|token|authorization|cookie)\s*[:=]\s*\S+')
def now():return datetime.now(timezone.utc).isoformat()
def h(p:Path):return hashlib.sha256(p.read_bytes()).hexdigest()
def git(root:Path,*args):
 r=subprocess.run(['git','-C',str(root),*args],text=True,capture_output=True)
 if r.returncode:raise RuntimeError(r.stderr.strip() or 'git failed')
 return r.stdout.strip()
def impacted(root:Path):
 p=root/'cpf-docs/quality/CPF_20260731_QA34_DEFECT_REGISTER.csv';out=set()
 if p.is_file():
  for row in csv.DictReader(p.open(encoding='utf-8-sig',newline='')):
   for value in (row.get('related_requirements') or '').split(';'):
    value=value.strip()
    if value.startswith('QA33-REQ-'):out.add(value)
 return sorted(out)
def main():
 ap=argparse.ArgumentParser();ap.add_argument('--root',default='.');ap.add_argument('--evidence-output');ap.add_argument('--release',action='store_true');ap.add_argument('--keep-workspace',action='store_true');a=ap.parse_args()
 root=Path(a.root).resolve();sha=git(root,'rev-parse','HEAD')
 if not re.fullmatch(r'[0-9a-f]{40}',sha):raise SystemExit('exact Git SHA required')
 dirty=bool(git(root,'status','--porcelain=v1','--untracked-files=all'));work=Path(tempfile.mkdtemp(prefix='cpf-qa34-source-'));out=Path(a.evidence_output).resolve() if a.evidence_output else work/'CPF_QA34_SOURCE_CLOSURE.sanitized.json';started=now();fail=[];results=[]
 if a.release and dirty:fail.append('release source closure requires clean Working Tree')
 for p in root.rglob('__pycache__'):fail.append(f'repository hygiene: {p.relative_to(root)}')
 for p in root.rglob('*.pyc'):fail.append(f'repository hygiene: {p.relative_to(root)}')
 try:
  for i,cmd in enumerate(GATES,1):
   st=now();r=subprocess.run(cmd,cwd=root,text=True,capture_output=True);so=work/f'{i:02d}.stdout.log';se=work/f'{i:02d}.stderr.log';so.write_text(r.stdout,encoding='utf-8');se.write_text(r.stderr,encoding='utf-8')
   results.append({'name':Path(cmd[1]).stem,'command':' '.join(cmd),'startedAt':st,'finishedAt':now(),'exitCode':r.returncode,'stdoutSha256':h(so),'stderrSha256':h(se)})
   if r.returncode:fail.append(f"{Path(cmd[1]).name}:exit={r.returncode}:{SECRET.sub(r'\1=***',r.stderr.strip())}")
  for app in ['cpf-admin','cpf-biz-admin']:
   for script in sorted((root/app/'frontend/scripts').glob('*.mjs')):
    r=subprocess.run(['node','--check',str(script)],cwd=root,text=True,capture_output=True);results.append({'name':'node-syntax','command':f'node --check {script.relative_to(root)}','startedAt':now(),'finishedAt':now(),'exitCode':r.returncode})
    if r.returncode:fail.append(f'node syntax failed: {script.relative_to(root)}')
  if git(root,'rev-parse','HEAD')!=sha:fail.append('Git SHA changed during source closure')
  final_dirty=bool(git(root,'status','--porcelain=v1','--untracked-files=all'))
  if a.release and final_dirty:fail.append('Source closure changed Working Tree')
  passed=not fail;eligible=bool(a.release and passed and not dirty)
  evidence={'schemaVersion':3,'evidenceId':'QA34-SOURCE-CLOSURE','sourceSha':sha,'resultSha':sha if eligible else None,'branch':git(root,'branch','--show-current'),'sourceDirty':dirty,'command':'python cpf-tools/scripts/verify-cpf-qa34-source-closure.py --root . --release','profile':'QA34_SOURCE_RELEASE' if a.release else 'QA34_SOURCE_DEVELOPMENT','environment':{'os':os.name,'python':os.sys.version.split()[0]},'startedAt':started,'finishedAt':now(),'exitCode':0 if passed else 1,'requirements':SOURCE_REQUIREMENTS,'developmentRequirements':impacted(root),'results':results,'failures':fail,'sanitized':True,'releaseEligible':eligible}
  out.parent.mkdir(parents=True,exist_ok=True);out.write_text(json.dumps(evidence,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
  if fail:raise SystemExit('QA34 source closure failed: '+'; '.join(fail))
  print(f'CPF QA34 source closure: PASS evidence={out}');return 0
 finally:
  if not a.keep_workspace and out.parent!=work:shutil.rmtree(work,ignore_errors=True)
if __name__=='__main__':raise SystemExit(main())
