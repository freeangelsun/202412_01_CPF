#!/usr/bin/env python3
"""CPF 최종 검증을 first-error-stop 없이 끝까지 실행하고 단일 결과를 남긴다."""
from __future__ import annotations

import sys as _cpf_sys

# CPF 표준 인코딩은 UTF-8 이다. 호출자의 콘솔 코드페이지(Windows cp949 등)에 좌우되면
# 한글 출력이 깨져 진단 메시지를 읽을 수 없다. 진입점이 스스로 출력 스트림을 고정한다.
for _cpf_stream in (_cpf_sys.stdout, _cpf_sys.stderr):
    try:
        _cpf_stream.reconfigure(encoding='utf-8')
    except (AttributeError, ValueError):
        pass
import argparse,json,os,platform,shutil,subprocess,sys,time
from pathlib import Path

SUPPORTED_GENERATED_DB_VENDORS=('mariadb','postgresql','oracle')

class Collector:
 def __init__(self,root:Path,evidence:Path): self.root=root; self.evidence=evidence; self.rows=[]; evidence.mkdir(parents=True,exist_ok=True)
 def run(self,name,cmd,timeout=600,optional=False):
  started=time.time(); log=self.evidence/(name.replace('/','_')+'.log')
  try:
   p=subprocess.run(cmd,cwd=self.root,text=True,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,timeout=timeout,env={**os.environ, 'PYTHONDONTWRITEBYTECODE':'1'})
   out=p.stdout or ''; rc=p.returncode
   environmental = (
       (name == 'git_diff_check' and not (self.root / '.git').exists())
       or ('UnknownHostException' in out and 'services.gradle.org' in out)
       or ('Could not download' in out and 'gradle' in out.lower())
   )
   status='PASS' if rc==0 else ('UNVERIFIED' if optional or environmental else 'FAIL')
  except FileNotFoundError as e: out=str(e); rc=127; status='UNVERIFIED' if optional else 'FAIL'
  except subprocess.TimeoutExpired as e: out=(e.stdout or '')+(e.stderr or '')+f'\nTIMEOUT={timeout}s'; rc=124; status='UNVERIFIED' if optional else 'FAIL'
  except Exception as e: out=repr(e); rc=125; status='UNVERIFIED' if optional else 'FAIL'
  log.write_text(out,encoding='utf-8',errors='replace'); self.rows.append({'name':name,'status':status,'rc':rc,'seconds':round(time.time()-started,2),'log':log.relative_to(self.root).as_posix()}); print(f'[{status}] {name} rc={rc}')
 def summary(self):
  counts={k:sum(1 for x in self.rows if x['status']==k) for k in ['PASS','FAIL','UNVERIFIED']}; status='FAIL' if counts['FAIL'] else ('UNVERIFIED' if counts['UNVERIFIED'] else 'PASS'); result={'status':status,'counts':counts,'checks':self.rows}; (self.evidence/'VERIFY_ALL_RESULT.json').write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n',encoding='utf-8'); return result

def py(root:Path,rel:str,*args): return [sys.executable,str(root/rel),*map(str,args)]
def main(argv=None):
 ap=argparse.ArgumentParser(); ap.add_argument('--root',required=True); ap.add_argument('--evidence-dir',default='cpf-docs/work/evidence/current/verify-all'); a=ap.parse_args(argv); root=Path(a.root).resolve(); ev=root/a.evidence_dir; c=Collector(root,ev)
 # 저비용/독립 Gate는 선행 실패와 관계없이 모두 실행한다.
 c.run('git_diff_check',['git','diff','--check'],120)
 c.run('generator_gate',py(root,'cpf-tools/verification/nxt3/cpf_nxt3_generator_gate.py','--root',root),180)
 c.run('generated_domain_minimal_ia',py(root,'cpf-tools/verification/nxt3/verify_generated_customer_domain_minimal_ia.py','--root',root),180)
 c.run('layout_gate',py(root,'cpf-tools/verification/nxt3/cpf_nxt3_layout_gate.py','--root',root),180)
 c.run('garbage_sweep_gate',py(root,'cpf-tools/verification/nxt3/verify_nxt3_repository_garbage.py','--root',root),180)
 c.run('hygiene_gate',py(root,'cpf-tools/verification/nxt3/verify_nxt3_hygiene.py','--root',root),180)
 c.run('query_boundary_gate',py(root,'cpf-tools/verification/nxt3/cpf_nxt3_query_boundary_gate.py','--root',root),300)
 c.run('korean_comment_gate',py(root,'cpf-tools/verification/nxt3/verify_nxt3_korean_comment.py','--root',root),180)
 c.run('adm_dangerous_approval_boundary',py(root,'cpf-tools/verification/tools/verify-cpf-adm-dangerous-action-approval-boundary.py'),180)
 c.run('fixed_length_closure',py(root,'cpf-tools/verification/tools/verify-cpf-fixed-length-closure.py'),180)
 c.run('python_compile',[sys.executable,'-c',"import ast,pathlib; roots=[pathlib.Path('cpf-tools/verification/nxt3'),pathlib.Path('cpf-tools/generator/engine')]; files=[p for r in roots for p in r.rglob('*.py')]; [ast.parse(p.read_text(encoding='utf-8-sig'), filename=str(p)) for p in files]; print(f'PYTHON_SYNTAX=PASS files={len(files)}')"],180)
 # Root Gradle은 각 task를 독립 실행하여 첫 실패가 다음 Gate를 막지 않게 한다.
 gradle=root/('gradlew.bat' if os.name=='nt' else 'gradlew')
 if gradle.exists():
  executable=str(gradle) if os.name=='nt' else './gradlew'
  base=[executable,'--no-daemon']
  for task,to in [('help',300),('qualityGate',900),('aggregateQualityBuild',1200),('publicationGate',900),('qa34IntegrationTest',1200)]:
   c.run('gradle_'+task,base+[task],to,optional=False)
  # Root의 projects는 Generated Build가 settings에 실제 mount되는지만 검증합니다.
  # Root `test`는 Included Build의 하위 project test를 실행하지 않으므로 PASS 근거로 쓰지 않습니다.
  c.run('gradle_generated_projects',base+['-PcpfIncludeGeneratedDomains=true','projects'],600)
  # 각 Generated Build를 Product Source composite consumer로 직접 실행해야 CPF Artifact substitution,
  # 선택 Vendor Mapper overlay, Generated Online test가 모두 실제 task graph에 포함됩니다.
  generated_roots=sorted(
      definition.parent for definition in root.glob('cpf-*/gradle.properties')
      if definition.parent.is_dir() and 'cpf.domain.contractVersion=' in definition.read_text(encoding='utf-8-sig')
      and 'cpf.domain.generationMode=generated' in definition.read_text(encoding='utf-8-sig')
  )
  for vendor in SUPPORTED_GENERATED_DB_VENDORS:
   for generated_root in generated_roots:
    capabilities=[name for name in ('online','batch') if (generated_root/name/'build.gradle').is_file()]
    if not capabilities:
     c.run(f'gradle_generated_{generated_root.name}_{vendor}', ['__cpf_generated_capability_missing__'],10)
     continue
    generated_cmd=[executable,'-p',str(generated_root),'--no-daemon',f'-PcpfProductCompositeRoot={root}',
                   f'-PcpfDbVendor={vendor}','--max-workers=1',*[f':{name}:check' for name in capabilities]]
    c.run(f'gradle_generated_{generated_root.name}_{vendor}',generated_cmd,1800)
 else:
  c.run('gradle_wrapper_missing',['__cpf_missing_gradle_wrapper__'],10,optional=False)
 # 외부 Live 환경은 미구성 시 성공으로 위장하지 않고 UNVERIFIED로 남긴다.
 live=os.environ.get('CPF_RUN_EXTERNAL_LIVE','').lower() in {'1','true','yes'}
 if live:
  candidates=[('db3_live','cpf-tools/verification/run-db3-live.py'),('redis_valkey_live','cpf-tools/verification/run-cache-live.py')]
  for name,rel in candidates:
   if (root/rel).exists(): c.run(name,py(root,rel,'--root',root),1200,optional=True)
   else: c.run(name,['__cpf_live_harness_missing__'],10,optional=True)
 else:
  for name in ['db3_live','redis_valkey_live','multi_instance_reconcile','process_kill_recovery']:
   c.run(name,['__cpf_external_runtime_not_requested__'],10,optional=True)
 result=c.summary(); print(json.dumps(result,ensure_ascii=False,indent=2)); return 2 if result['status']=='FAIL' else 0
if __name__=='__main__': raise SystemExit(main())
