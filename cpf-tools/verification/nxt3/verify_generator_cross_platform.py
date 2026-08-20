#!/usr/bin/env python3
# CPF Cross-platform Generator 검증은 launcher와 단일 OS-neutral Engine의 동일 계약을 확인합니다.
from __future__ import annotations
import argparse, importlib.util, json, shutil, stat, subprocess, sys, tempfile
from pathlib import Path
from generated_domain_layout import domain_surface_dirs


def run(cmd:list[str], cwd:Path, timeout_seconds:int=6)->dict:
    try:
        cp=subprocess.run(cmd,cwd=cwd,text=True,capture_output=True,timeout=timeout_seconds)
        return {"cmd":cmd,"rc":cp.returncode,"stdout":cp.stdout[-12000:],"stderr":cp.stderr[-12000:]}
    except subprocess.TimeoutExpired as e:
        return {"cmd":cmd,"rc":124,"stdout":(e.stdout or "")[-12000:] if isinstance(e.stdout,str) else "","stderr":((e.stderr or "")[-12000:] if isinstance(e.stderr,str) else "")+f"\nTIMEOUT after {timeout_seconds}s"}
    except Exception as e:
        return {"cmd":cmd,"rc":125,"stdout":"","stderr":repr(e)}


def load_engine(root:Path):
    p=root/'cpf-tools/generator/engine/cpf_domain_generator.py'
    engine_dir=str(p.parent)
    if engine_dir not in sys.path: sys.path.insert(0,engine_dir)
    spec=importlib.util.spec_from_file_location('cpf_domain_generator_cross_gate',p)
    mod=importlib.util.module_from_spec(spec); sys.modules[spec.name]=mod; spec.loader.exec_module(mod)
    return mod


def write_def(path:Path,name:str,code:str,prefix:str,newline:str='\n')->None:
    text=(
        "# Cross-platform Generator 검증용 선언형 입력이며 Vendor/Secret을 직접 저장하지 않습니다.\n"
        f"domain:\n  name: {name}\n  systemCode: {code}\n  packageName: {name}\n"
        f"database:\n  role: CUSTOMER_BUSINESS_DB\n  tablePrefix: {prefix}\n"
        "preset: standard-enterprise\nmodules:\n  online: true\n"
        "features:\n  persistence: mybatis\n  httpClient: true\n  resilience: true\n  cache: none\n"
        "  messaging: none\ngeneration:\n  sampleTransaction: true\n"
    )
    path.parent.mkdir(parents=True,exist_ok=True)
    path.write_text(text.replace('\n',newline),encoding='utf-8',newline='')


def tree_hash(root:Path)->str:
    import hashlib
    h=hashlib.sha256()
    for p in sorted(x for x in root.rglob('*') if x.is_file() and 'build' not in x.parts and '.gradle' not in x.parts):
        h.update(p.relative_to(root).as_posix().encode()); h.update(p.read_bytes())
    return h.hexdigest()

def main()->int:
    ap=argparse.ArgumentParser(); ap.add_argument('--root',type=Path,required=True); ap.add_argument('--evidence',type=Path)
    ns=ap.parse_args(); root=ns.root.resolve(); cli=root/'cpf-tools/runtime/cli/cpf'; py=root/'cpf-tools/runtime/cli/cpf.py'; engine=load_engine(root)
    launcher=[str(cli)] if cli.is_file() and bool(cli.stat().st_mode & stat.S_IXUSR) else ['sh',str(cli)]
    checks=[]
    def add(name,status,detail=''):
        checks.append({'name':name,'status':status,'detail':detail}); print(f'[{status}] {name}',flush=True)
    def check(name,ok,detail=''): add(name,'PASS' if ok else 'FAIL',detail)

    # Launcher는 실제 실행하고 Windows는 이 환경에서 물리 실행 대신 thin-wrapper 계약을 검증한다.
    check('posix-launcher-runnable',cli.is_file() and (bool(cli.stat().st_mode & stat.S_IXUSR) or shutil.which('sh') is not None),{'path':str(cli),'executableBit':bool(cli.stat().st_mode & stat.S_IXUSR) if cli.is_file() else False,'launcher':launcher})
    bat=root/'cpf-tools/runtime/cli/cpf.bat'; check('windows-launcher-present',bat.is_file(),str(bat))
    bat_text=bat.read_text(encoding='utf-8-sig') if bat.is_file() else ''
    sh_text=cli.read_text(encoding='utf-8-sig') if cli.is_file() else ''
    check('windows-launcher-thin','cpf-tools\\runtime\\cli\\cpf.py' in bat_text and 'create-domain.ps1' not in bat_text,bat_text)
    check('linux-launcher-thin','cpf-tools/runtime/cli/cpf.py' in sh_text and 'pwsh' not in sh_text,sh_text)
    for name,args in [('linux-cli-help',['--help']),('linux-cli-version',['--version'])]:
        rr=run([*launcher,*args],root); add(name,'PASS' if rr['rc']==0 else 'FAIL',rr)
    invalid=run([*launcher,'domain','generate','--file','__missing__.yaml'],root); check('invalid-input-exit-code',invalid['rc']==2,invalid)
    generic=run([*launcher,'verify','generator'],root); add('genericity-source-scan','PASS' if generic['rc']==0 else 'FAIL',generic)

    # 두 공식 Root는 한 번의 Public CLI verify-all로 검증하여 중복 Python startup을 제거한다.
    for name in ('cpf-member','cpf-external'):
        logical=name.removeprefix('cpf-'); definition=root/'cpf-tools/generator/definitions'/logical/'cpf-domain.yaml'; check(f'{name}-definition',definition.is_file(),str(definition)); check(f'{name}-customer-metadata-zero',not (root/name/'.cpf').exists(),str(root/name/'.cpf'))
    all_verify=run([*launcher,'verify','all'],root,10)
    check('retained-member-external-verify-all',all_verify['rc']==0 and '"status": "PASS"' in all_verify['stdout'],all_verify)
    for name in ('cpf-member','cpf-external'):
        d=root/name; dirs=domain_surface_dirs(d)
        logical=name.removeprefix('cpf-')
        definition=root/'cpf-tools/generator/definitions'/logical/'cpf-domain.yaml'
        dd=engine.validate_definition(engine.load_yaml_subset(definition))
        expected={'online'} | ({'batch'} if dd.batch else set())
        check(f'{name}-minimal-ia',dirs==expected,{'expected':sorted(expected),'actual':sorted(dirs)})
        check(f'{name}-no-readme-verification-db',not any((d/x).exists() for x in ['README.md','verification','db']),str(d))
    for name in ('cpf-member','cpf-external'):
        try:
            logical=name.removeprefix('cpf-'); dr=engine.diff(root,root/'cpf-tools/generator/definitions'/logical/'cpf-domain.yaml',root/name)
            check(f'{name}-idempotent-diff',dr.get('clean') is True,dr)
        except Exception as e: add(f'{name}-idempotent-diff','FAIL',repr(e))

    temp_parent=root/'build/domain-generator/verification'; temp_parent.mkdir(parents=True,exist_ok=True)
    with tempfile.TemporaryDirectory(prefix='CPF 한글 Path With Spaces ', dir=temp_parent) as td:
        troot=Path(td)
        definition=troot/'crlf spec'/'ledger.yaml'; write_def(definition,'ledger','LDG','LDG','\r\n')
        out=troot/'cpf-ledger'
        gr=run([*launcher,'domain','generate','--file',str(definition),'--output',str(out)],root,10)
        add('third-domain-fresh-generate-path-space-utf8-crlf','PASS' if gr['rc']==0 else 'FAIL',gr)
        dbs=[]
        for vendor in ('oracle','postgresql','mariadb'):
            rr=run([*launcher,'db','render','--file',str(definition),'--vendor',vendor,'--output',str(troot/'db render'/vendor)],root,8); dbs.append(rr)
        add('third-domain-db3-render','PASS' if all(x['rc']==0 for x in dbs) else 'FAIL',dbs)

        # add가 retained roots를 훼손하지 않는지 실제 hash로 확인한다.
        member_hash=tree_hash(root/'cpf-member')
        ext_hash=tree_hash(root/'cpf-external')
        adddef=troot/'add.yaml'; write_def(adddef,'orders','ORD','ORD'); addout=troot/'cpf-orders'
        ar=run([*launcher,'domain','add','--file',str(adddef),'--output',str(addout)],root,10)
        intact=tree_hash(root/'cpf-member')==member_hash and tree_hash(root/'cpf-external')==ext_hash
        add('add-domain-does-not-damage-retained-roots','PASS' if ar['rc']==0 and intact else 'FAIL',{'add':ar,'retainedIntact':intact})

        # 사용자 수정 영역 보호는 Public CLI regenerate에서 fail-closed를 확인한다.
        mod=out/'online/src/main/java/ledger/online/ledger/service/SampleTransactionPolicy.java'
        if mod.is_file():
            original=mod.read_text(encoding='utf-8'); mod.write_text(original+'\n// 사용자 수정 보호 검증\n',encoding='utf-8',newline='\n')
            rr=run([*launcher,'domain','regenerate','ledger','--file',str(definition),'--output',str(out)],root,8)
            preserved=rr['rc']==2 and '사용자 변경' in rr['stderr'] and '사용자 수정 보호 검증' in mod.read_text(encoding='utf-8')
            add('user-owned-modification-protection','PASS' if preserved else 'FAIL',rr)
            mod.write_text(original,encoding='utf-8',newline='\n')
        else: add('user-owned-modification-protection','FAIL','target file missing')

        # Lifecycle은 같은 Canonical Engine을 한 프로세스에서 수행한다. Shell별 별도 Engine은 사용하지 않는다.
        lifedef=troot/'lifecycle.yaml'; write_def(lifedef,'lifecycle','LFC','LFC'); life=root/'build/domain-generator/lifecycle-tests/cpf-lifecycle'
        if life.exists(): shutil.rmtree(life)
        try:
            gen=engine.generate(root,lifedef,life)
            rem=engine.remove_owned(root,lifedef,life,apply=True)
            restore=engine.generate(root,lifedef,life)
            clean=engine.diff(root,lifedef,life)
            ok=gen.get('status')=='GENERATED' and rem.get('status')=='REMOVED' and restore.get('status')=='GENERATED' and clean.get('clean') is True and not (life/'.cpf').exists()
            add('remove-restore-isolated-lifecycle','PASS' if ok else 'FAIL',{'generate':gen.get('status'),'remove':rem.get('status'),'restore':restore.get('status'),'diffClean':clean.get('clean')})
        except Exception as e: add('remove-restore-isolated-lifecycle','FAIL',repr(e))
        finally:
            if life.exists(): shutil.rmtree(life)

        # generate-all Public CLI를 실제 실행한다.
        defs=troot/'definitions'; write_def(defs/'alpha'/'cpf-domain.yaml','alpha','ALP','ALP'); write_def(defs/'beta'/'cpf-domain.yaml','beta','BET','BET')
        outs=troot/'all outputs'; allr=run([*launcher,'domain','generate-all','--definitions-root',str(defs),'--output-root',str(outs)],root,12)
        allok=allr['rc']==0 and (outs/'cpf-alpha/online').is_dir() and (outs/'cpf-beta/online').is_dir() and not (outs/'cpf-alpha/.cpf').exists() and not (outs/'cpf-beta/.cpf').exists()
        add('generate-all-two-domains','PASS' if allok else 'FAIL',allr)

    # CLI surface 자체에 lifecycle command가 누락되지 않았는지 Source 계약도 검증한다.
    py_text=py.read_text(encoding='utf-8-sig') if py.is_file() else ''
    for command in ('generate','add','dry-run','diff','regenerate','upgrade','remove','restore','generate-all'):
        check('cli-surface-'+command,command in py_text,command)

    text_files=[p for p in [cli,py,root/'cpf-tools/generator/definitions/member/cpf-domain.yaml',root/'cpf-tools/generator/definitions/external/cpf-domain.yaml'] if p.is_file()]
    lf_ok=True; utf_ok=True
    for p in text_files:
        raw=p.read_bytes(); lf_ok &= b'\r\n' not in raw
        try: raw.decode('utf-8')
        except UnicodeDecodeError: utf_ok=False
    check('generated-lf-normalized',lf_ok,','.join(str(x) for x in text_files)); check('utf8-decode',utf_ok)

    java=run(['java','-version'],root); gradle=shutil.which('gradle'); pwsh=shutil.which('pwsh') or shutil.which('powershell')
    add('windows-launcher-execution','UNVERIFIED','Windows cmd runtime is unavailable in this Linux container.')
    add('gradle-generated-compile-test','UNVERIFIED' if not gradle else 'NOT_RUN','gradle executable unavailable' if not gradle else gradle)
    version_text=(java['stdout']+java['stderr']); add('java25-runtime','PASS' if ('version "25' in version_text or 'version "26' in version_text) else 'UNVERIFIED',java)
    add('powershell-wrapper-execution','UNVERIFIED' if not pwsh else 'NOT_RUN','PowerShell runtime unavailable' if not pwsh else pwsh)

    failed=[x for x in checks if x['status']=='FAIL']; unverified=[x for x in checks if x['status'] in {'UNVERIFIED','NOT_RUN'}]
    result={'gate':'NXT3_GENERATOR_CROSS_PLATFORM','staticStatus':'PASS' if not failed else 'FAIL','overallStatus':'UNVERIFIED' if not failed and unverified else ('FAIL' if failed else 'PASS'),'checks':checks,'failedCount':len(failed),'unverifiedCount':len(unverified)}
    if ns.evidence:
        ev=ns.evidence if ns.evidence.is_absolute() else root/ns.evidence; ev.parent.mkdir(parents=True,exist_ok=True); ev.write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    print(json.dumps(result,ensure_ascii=False,indent=2)); return 1 if failed else 0
if __name__=='__main__': raise SystemExit(main())
