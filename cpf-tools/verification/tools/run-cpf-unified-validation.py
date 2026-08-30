#!/usr/bin/env python3
"""Cross-platform INTERNAL `cpf dev full-validation` engine.

This is the canonical portable orchestrator. Platform-specific runtime/DB clients remain
owned by their engines, but this coordinator never requires PowerShell on Linux or Bash
on Windows. Missing mandatory physical prerequisites fail closed.
"""
from __future__ import annotations
import argparse, json, os, re, shutil, subprocess, sys, time
from pathlib import Path


def run(label:str, cmd:list[str], root:Path, env:dict[str,str], timeout:int=3600)->dict:
    started=time.time(); print(f'[CPF][UNIFIED-VALIDATION] START {label}',flush=True)
    try:
        cp=subprocess.run(cmd,cwd=root,env=env,text=True,encoding='utf-8',errors='replace',stdout=subprocess.PIPE,stderr=subprocess.STDOUT,timeout=timeout)
        out=cp.stdout[-20000:]
        if out: print(out,end='' if out.endswith('\n') else '\n')
        status='PASS' if cp.returncode==0 else 'FAIL'
        print(f'[CPF][UNIFIED-VALIDATION] {status} {label} rc={cp.returncode} elapsed={time.time()-started:.1f}s',flush=True)
        return {'stage':label,'status':status,'exitCode':cp.returncode,'elapsedSeconds':round(time.time()-started,3),'outputTail':out}
    except subprocess.TimeoutExpired as exc:
        out=exc.stdout or ''
        if isinstance(out,bytes): out=out.decode('utf-8','replace')
        print(f'[CPF][UNIFIED-VALIDATION] FAIL {label} rc=124 timeout={timeout}s',flush=True)
        return {'stage':label,'status':'FAIL','exitCode':124,'elapsedSeconds':round(time.time()-started,3),'outputTail':out[-20000:]}


def java25()->bool:
    try:
        cp=subprocess.run(['java','-version'],text=True,encoding='utf-8',errors='replace',stdout=subprocess.PIPE,stderr=subprocess.STDOUT)
        return cp.returncode==0 and bool(re.search(r'version\s+"25(?:\.|\")',cp.stdout))
    except OSError: return False


def main()->int:
    ap=argparse.ArgumentParser(); ap.add_argument('--root',default='.'); ap.add_argument('--output'); ap.add_argument('--skip-db3-runtime',action='store_true'); ap.add_argument('--skip-runtime',action='store_true'); ap.add_argument('--skip-open-git',action='store_true'); args=ap.parse_args()
    root=Path(args.root).resolve(); env={**os.environ,'PYTHONDONTWRITEBYTECODE':'1','PYTHONUTF8':'1','PYTHONIOENCODING':'utf-8','JAVA_TOOL_OPTIONS':(os.environ.get('JAVA_TOOL_OPTIONS','')+' -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -Dstderr.encoding=UTF-8').strip()}
    if not java25():
        print('CPF_UNIFIED_VALIDATION=FAIL reason=Java25-required',file=sys.stderr); return 69
    py=sys.executable; gradle=str(root/('gradlew.bat' if os.name=='nt' else 'gradlew')); cli=str(root/'cpf-tools/runtime/cli'/('cpf.cmd' if os.name=='nt' else 'cpf'))
    stages=[]
    stages.append(run('source-identity',[py,str(root/'cpf-tools/verification/tools/cpf-source-state.py'),'--root',str(root),'--scope','source'],root,env,300))
    stages.append(run('tooling-inventory',[py,str(root/'cpf-tools/runtime/cli/tools/generate-cpf-tooling-entrypoint-inventory.py'),'--root',str(root)],root,env,300))
    stages.append(run('canonical-verifiers',[py,str(root/'cpf-tools/verification/tools/run-cpf-canonical-verifiers.py'),'--root',str(root),'--child-timeout','120'],root,env,3600))
    stages.append(run('gradle-build',[gradle,'-PcpfResourceProfile=local','--no-daemon','--no-parallel','--continue','cpfBuildAll'],root,env,3600))
    stages.append(run('gradle-test',[gradle,'-PcpfResourceProfile=local','--no-daemon','--no-parallel','--continue','cpfTestAll'],root,env,3600))
    if not args.skip_db3_runtime:
        stages.append(run('db3-runtime',[py,str(root/'cpf-tools/db/tests/run_db3_lifecycle.py'),'--root',str(root)],root,env,3600))
    if not args.skip_runtime:
        stages.append(run('runtime-start',[cli,'run'],root,env,3600))
        if stages[-1]['status']=='PASS':
            stages.append(run('runtime-status',[cli,'status'],root,env,120))
            stages.append(run('runtime-stop',[cli,'stop'],root,env,300))
    if not args.skip_open_git:
        stages.append(run('open-git-check',[cli,'release','open-git','check','--profile','binary'],root,env,1800))
    failed=[x for x in stages if x['status']!='PASS']
    payload={'schemaVersion':1,'status':'PASS' if not failed else 'FAIL','stages':stages,'failedStages':[x['stage'] for x in failed]}
    out=Path(args.output).resolve() if args.output else root/'build/cpf-validation/unified-validation.json'; out.parent.mkdir(parents=True,exist_ok=True); out.write_text(json.dumps(payload,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    print(f'CPF_UNIFIED_VALIDATION={payload["status"]} stages={len(stages)} failed={len(failed)} evidence={out}')
    return 0 if not failed else 1
if __name__=='__main__': raise SystemExit(main())
