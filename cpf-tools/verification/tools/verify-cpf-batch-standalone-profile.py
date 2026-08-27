#!/usr/bin/env python3
from __future__ import annotations
import argparse, json, re, shutil, subprocess, sys
from pathlib import Path
try:
    import yaml
except Exception:
    yaml=None
ROLES={
 'control-plane':('CONTROL_PLANE',8180), 'scheduler':('SCHEDULER',8181),
 'worker':('WORKER',8182), 'center-cut':('CENTER_CUT_RUNNER',8183), 'agent':('AGENT',8184),
}
def flatten(x,p=''):
    if isinstance(x,dict):
        for k,v in x.items(): yield from flatten(v, f'{p}.{k}' if p else str(k))
    else: yield p,x
def main():
    ap=argparse.ArgumentParser();ap.add_argument('--root',default='.');a=ap.parse_args();root=Path(a.root).resolve();find=[];checks=[]
    for role,(runtime_role,port) in ROLES.items():
        base=root/'cpf-batch'/role
        for shell in ('run.ps1','stop.ps1','run.sh','stop.sh'):
            p=base/'bin'/shell; ok=p.is_file() and p.stat().st_size>100;checks.append((f'{role}:shell:{shell}',ok));
            if not ok: find.append(f'MISSING_SHELL:{role}:{shell}')
            elif shell.startswith('run'):
                t=p.read_text(encoding='utf-8')
                for token in ('dev','test','prod','instance','port','UTF-8','readiness','pid'):
                    if token.lower() not in t.lower(): find.append(f'SHELL_CONTRACT_MISSING:{role}:{shell}:{token}')
        for profile in ('dev','test','prod'):
            p=base/'src/main/resources'/f'application-{profile}.yml'; ok=p.is_file(); checks.append((f'{role}:profile:{profile}',ok))
            if not ok: find.append(f'MISSING_PROFILE:{role}:{profile}'); continue
            text=p.read_text(encoding='utf-8')
            if yaml:
                try: data=yaml.safe_load(text) or {}
                except Exception as e: find.append(f'YAML_INVALID:{role}:{profile}:{e}'); continue
                vals=list(flatten(data))
            else: vals=[]
            if runtime_role not in text: find.append(f'ROLE_MISMATCH:{role}:{profile}')
            if profile=='prod':
                for key,val in vals:
                    sval='' if val is None else str(val)
                    if re.search(r'(^|//)(localhost|127\.0\.0\.1)(:|/|$)',sval,re.I): find.append(f'PROD_LOCALHOST:{role}:{key}')
                    lk=key.lower()
                    if any(w in lk for w in ('password','secret','token','key')) and sval and not sval.startswith('${') and sval.lower() not in ('true','false','need'):
                        find.append(f'PROD_LITERAL_SECRET:{role}:{key}')
                # non-control roles require explicit control-plane endpoint in prod
                if role!='control-plane' and '${CPF_BATCH_CONTROL_BASE_URL}' not in text: find.append(f'PROD_CONTROL_URL_NOT_REQUIRED:{role}')
    # Canonical Batch Shells are checked-in product source. If a real Git working tree is
    # available, fail closed when the repository ignore rules would hide any shell from git add.
    if (root/'.git').exists() and shutil.which('git'):
        for role in ROLES:
            for shell in ('run.ps1','stop.ps1','run.sh','stop.sh'):
                rel=f'cpf-batch/{role}/bin/{shell}'
                proc=subprocess.run(['git','-C',str(root),'check-ignore','--quiet','--no-index',rel],
                                    stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
                if proc.returncode == 0:
                    find.append(f'GIT_IGNORED_SHELL:{rel}')
                elif proc.returncode not in (1,):
                    find.append(f'GIT_IGNORE_CHECK_FAILED:{rel}:rc={proc.returncode}')
    # no profile should silently alias local as prod
    for p in root.glob('cpf-batch/*/src/main/resources/application-prod.yml'):
        if '${SPRING_PROFILES_ACTIVE:local}' in p.read_text(encoding='utf-8'): find.append(f'PROD_LOCAL_PROFILE_FALLBACK:{p.relative_to(root)}')
    result={'status':'PASS' if not find else 'FAIL','roles':len(ROLES),'shellFiles':20,'physicalProfiles':15,'checks':len(checks),'findings':find}
    print(json.dumps(result,ensure_ascii=False,indent=2)); return 0 if not find else 1
if __name__=='__main__': raise SystemExit(main())
