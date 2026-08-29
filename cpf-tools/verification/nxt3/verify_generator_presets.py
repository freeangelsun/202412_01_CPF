#!/usr/bin/env python3
# CPF 개발/검증 Source이며 최신 Requirement와 실패 누적 검증 계약을 따릅니다.
from __future__ import annotations
import argparse, json, subprocess, tempfile, sys
from pathlib import Path

def run(cmd,cwd):
    cp=subprocess.run(cmd,cwd=cwd,text=True,capture_output=True)
    return {'cmd':cmd,'rc':cp.returncode,'stdout':cp.stdout,'stderr':cp.stderr}

def definition(name,code,prefix,preset,features,sample,batch=False):
    lines=[
      'domain:',f'  name: {name}',f'  systemCode: {code}',f'  packageName: {name}',
      'database:','  role: CUSTOMER_BUSINESS_DB',f'  tablePrefix: {prefix}',
      f'preset: {preset}','modules:','  online: true',f'  batch: {str(batch).lower()}',
    ]
    if features is not None:
      lines += ['features:']+[f'  {k}: {str(v).lower() if isinstance(v,bool) else v}' for k,v in features.items()]
    lines += ['generation:',f'  sampleTransaction: {str(sample).lower()}']
    return '\n'.join(lines)+'\n'

def main():
    ap=argparse.ArgumentParser(); ap.add_argument('--root',type=Path,required=True); ap.add_argument('--evidence',type=Path); ns=ap.parse_args(); root=ns.root.resolve(); cli_py=root/'cpf-tools/runtime/cli/cpf.py'; cli=[sys.executable,str(cli_py),'--root',str(root)]
    cases=[
      ('mini','MIN','MI','minimal',{},False,False),
      ('stdapi','STA','ST','standard-enterprise',{'persistence':'mybatis','httpClient':True,'resilience':True},True,True),
      ('fullx','FUL','FU','full-enterprise',None,True,False),
      ('customx','CUS','CU','custom',{'persistence':'none','httpClient':False,'resilience':False,'cache':'none','messaging':'none'},False,False),
    ]
    checks=[]
    verify_root=root/'cpf-docs/governance/development-harness/evidence/platform/current/generated/domain-generator/verification'; verify_root.mkdir(parents=True,exist_ok=True)
    with tempfile.TemporaryDirectory(prefix='preset-matrix-',dir=str(verify_root)) as td:
      t=Path(td)
      for name,code,prefix,preset,features,sample,batch in cases:
        f=t/f'{name}.yaml'; out=t/f'cpf-{name}'; f.write_text(definition(name,code,prefix,preset,features,sample,batch),encoding='utf-8',newline='\n')
        r=run(cli+['domain','generate','--file',str(f),'--output',str(out)],root)
        failures=[]
        ok=r['rc']==0
        if not ok:
          failures.append(f'generate rc={r["rc"]}')
        if ok:
          vr=run(cli+['verify','domain','--file',str(f),'--output',str(out)],root)
          if vr['rc']!=0: failures.append(f'domain verify rc={vr["rc"]}')
          files=[p.relative_to(out).as_posix() for p in out.rglob('*') if p.is_file()]
          sample_files=[x for x in files if 'SampleTransaction' in x or '_sample_transaction.sql' in x]
          if sample and not sample_files: failures.append('sampleTransaction requested but sample source missing')
          if not sample and sample_files: failures.append(f'sampleTransaction disabled but sample source exists: {sample_files[:3]}')
          if not (out/'online').is_dir(): failures.append('online module missing')
          batch_exists=(out/'batch').is_dir() and any(p.is_file() for p in (out/'batch').rglob('*'))
          if batch_exists != batch: failures.append(f'batch selection mismatch expected={batch} actual={batch_exists}')
          forbidden_roots=[x for x in ('domain','jobpack') if (out/x).exists()]
          if forbidden_roots: failures.append(f'legacy roots present: {forbidden_roots}')
          forbidden_misc=[x for x in ['README.md','verification',f'{name}-api',f'{name}-common',f'{name}-batch'] if (out/x).exists()]
          if forbidden_misc: failures.append(f'forbidden generated surface present: {forbidden_misc}')
          persistence = (features or {}).get('persistence') if features is not None else None
          expects_db = persistence not in {None, 'none'} or preset in {'standard-enterprise','full-enterprise'}
          if (out/'db').exists(): failures.append('Generated Domain root db/ is forbidden; DB3 is owned by canonical renderer')
          try:
            generated_payload=json.loads(r['stdout']); renderer=generated_payload.get('verify',{}).get('db3Renderer')
          except Exception:
            renderer=None
          expected_renderer='EXTERNAL_CANONICAL_RENDERER' if expects_db else 'NOT_APPLICABLE'
          if renderer != expected_renderer: failures.append(f'db renderer mismatch expected={expected_renderer} actual={renderer}')
          if preset in {'minimal','custom'} and not sample:
            app=(out/'online/src/main/resources/application.yml').read_text(encoding='utf-8')
            if 'datasource:' in app or 'mybatis:' in app: failures.append('minimal/custom application.yml contains persistence config')
          dr=run(cli+['domain','dry-run','--file',str(f),'--output',str(out)],root)
          if dr['rc']!=0:
            failures.append(f'dry-run rc={dr["rc"]}')
          else:
            try:
              payload=json.loads(dr['stdout'])
            except json.JSONDecodeError:
              failures.append('dry-run stdout is not JSON'); payload={}
            summary=payload.get('selectionSummary',{})
            bc=summary.get('batchCapability',{})
            if bc.get('generatedByDomainGenerator') is not True or bool(bc.get('selected')) != batch:
              failures.append(f'batchCapability mismatch expected={batch} actual={bc}')
            if summary.get('internalArtifactsDirectlyExposed')!=[]:
              failures.append(f'internal artifacts exposed: {summary.get("internalArtifactsDirectlyExposed")}')
            if not summary.get('publicArtifacts'): failures.append('publicArtifacts selection empty')
          ok=not failures
        detail=dict(r); detail['stdout']=detail['stdout'][-5000:]; detail['stderr']=detail['stderr'][-5000:]; detail['assertionFailures']=failures
        checks.append({'preset':preset,'name':name,'status':'PASS' if ok else 'FAIL','detail':detail})
      # Invalid standard-enterprise must fail-fast instead of silently downgrading the Golden Path.
      bad=t/'bad.yaml'; bad.write_text(definition('badstd','BAD','BA','standard-enterprise',{'persistence':'none','httpClient':False,'resilience':False},True),encoding='utf-8',newline='\n')
      rr=run(cli+['domain','generate','--file',str(bad),'--output',str(t/'cpf-badstd')],root)
      checks.append({'preset':'standard-enterprise-invalid','name':'fail-fast','status':'PASS' if rr['rc']!=0 else 'FAIL','detail':rr})
      # minimal 이름과 실제 Surface가 어긋나는 optional capability override는 fail-closed 한다.
      bad_min=t/'bad-minimal.yaml'; bad_min.write_text(definition('badmin','BMI','BM','minimal',{'persistence':'mybatis','httpClient':True,'resilience':True},True),encoding='utf-8',newline='\n')
      rm=run(cli+['domain','generate','--file',str(bad_min),'--output',str(t/'cpf-badmin')],root)
      minimal_fail=rm['rc']!=0 and 'preset=minimal' in (rm['stderr']+rm['stdout']) and '수정 경로=$.features' in (rm['stderr']+rm['stdout'])
      checks.append({'preset':'minimal-invalid','name':'true-minimal-fail-closed','status':'PASS' if minimal_fail else 'FAIL','detail':rm})
    failed=[x for x in checks if x['status']=='FAIL']; result={'gate':'NXT3_GENERATOR_PRESETS','status':'PASS' if not failed else 'FAIL','failedCount':len(failed),'checks':checks}
    if ns.evidence:
      ev=ns.evidence if ns.evidence.is_absolute() else root/ns.evidence; ev.parent.mkdir(parents=True,exist_ok=True); ev.write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    print(json.dumps(result,ensure_ascii=False,indent=2)); return 1 if failed else 0
if __name__=='__main__': raise SystemExit(main())
