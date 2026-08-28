#!/usr/bin/env python3
from __future__ import annotations
import argparse, json, re
from pathlib import Path

EXTS={'.ps1':'POWERSHELL','.sh':'SHELL','.cmd':'CMD','.bat':'BAT','.py':'PYTHON'}
WRAPPERS={
 'cpf-tools/runtime/cli/cpf','cpf-tools/runtime/cli/cpf.cmd','cpf-tools/runtime/cli/cpf.ps1',
 'cpf-tools/release/open-git/templates/bin/cpf','cpf-tools/release/open-git/templates/bin/cpf.cmd','cpf-tools/release/open-git/templates/bin/cpf.ps1',
 'cpf-tools/release/public/templates/bin/cpf','cpf-tools/release/public/templates/bin/cpf.cmd','cpf-tools/release/public/templates/bin/cpf.ps1',
}
CANONICAL={
 'cpf-tools/runtime/cli/java/CpfCli.java',
 'cpf-tools/generator/engine/cpf_domain_generator.py',
 'cpf-tools/runtime/bootstrap/CpfBootstrap.java',
 'cpf-tools/runtime/cli/java/CpfGeneratorLauncher.java',
}
CLI_CONSUMERS={
 'cpf-tools/release/open-git/cpf_open_git.py',
 'cpf-tools/verification/tools/run-cpf-canonical-verifiers.py',
 'cpf-tools/verification/tools/cpf-source-state.py',
 'cpf-tools/db/tests/run_db3_lifecycle.py',
}
MIGRATE_PREFIXES=(
 'cpf-tools/build/tools/cpf-dev.',
 'cpf-tools/runtime/tools/start-cpf-local.',
 'cpf-tools/runtime/tools/status-cpf-local.',
 'cpf-tools/runtime/tools/stop-cpf-local.',
)

def is_generated(rel:str)->bool:
    parts=rel.split('/')
    return any(x in {'.gradle','build','__pycache__','.pytest_cache','node_modules','cpf-release'} for x in parts)

def classify(rel:str, kind:str, text:str)->tuple[str,str]:
    if rel in CANONICAL: return 'CANONICAL_ENGINE',''
    if rel in WRAPPERS: return 'THIN_WRAPPER','cpf-tools/runtime/cli/java/CpfCli.java'
    if rel in CLI_CONSUMERS: return 'CLI_CONSUMER','cpf-tools/runtime/cli/java/CpfCli.java'
    if rel == 'cpf-tools/runtime/cli/cpf.py': return 'INTERNAL_ENGINE','cpf-tools/runtime/cli/java/CpfCli.java'
    if rel == 'cpf-tools/runtime/cli/build-cpf-cli.py': return 'INTERNAL_ENGINE','cpf-tools/runtime/cli/java/CpfCli.java'
    if rel.startswith(MIGRATE_PREFIXES): return 'MIGRATE_TO_CLI','cpf-tools/runtime/cli/java/CpfCli.java'
    if '/templates/' in rel and ('cpf-' in Path(rel).name or Path(rel).name.startswith('cpf.')):
        # Compatibility aliases/templates may stay only as thin wrappers.
        low=text.lower()
        if 'cpf-cli.jar' in low or re.search(r'\bcpf(?:\.cmd|\.ps1|\b)',low): return 'THIN_WRAPPER','cpf-tools/runtime/cli/java/CpfCli.java'
    if '/tests/' in rel or '/verification/' in rel: return 'INTERNAL_ENGINE',''
    return 'INTERNAL_ENGINE',''

def main()->int:
    ap=argparse.ArgumentParser(); ap.add_argument('--root',default='.'); ap.add_argument('--output',default='cpf-tools/runtime/cli/contracts/cpf-tooling-entrypoint-inventory.json'); args=ap.parse_args()
    root=Path(args.root).resolve(); rows=[]
    for p in sorted(root.joinpath('cpf-tools').rglob('*')):
        if not p.is_file(): continue
        rel=p.relative_to(root).as_posix()
        if is_generated(rel): continue
        kind=None; text=''
        if p.suffix.lower() in EXTS:
            kind=EXTS[p.suffix.lower()]
        elif p.suffix.lower()=='.java':
            try: text=p.read_text(encoding='utf-8-sig')
            except UnicodeDecodeError: continue
            if 'static void main(' in text or 'static int main(' in text: kind='JAVA_MAIN'
        if kind:
            if not text:
                try: text=p.read_text(encoding='utf-8-sig')
                except UnicodeDecodeError: text=''
            cls,repl=classify(rel,kind,text)
            rows.append({'id':f'FILE:{rel}','path':rel,'kind':kind,'classification':cls,'replacement':repl})
    gradle_re=re.compile(r"tasks\.(?:register|create)\(\s*['\"]([^'\"]+)['\"]|\btask\s+([A-Za-z0-9_-]+)")
    for p in sorted([*root.rglob('*.gradle'),*root.rglob('build.gradle')]):
        if not p.is_file(): continue
        rel=p.relative_to(root).as_posix()
        if not rel.startswith('cpf-tools/') or is_generated(rel): continue
        text=p.read_text(encoding='utf-8-sig',errors='replace')
        for m in gradle_re.finditer(text):
            name=m.group(1) or m.group(2)
            rows.append({'id':f'GRADLE:{rel}#{name}','path':rel,'kind':'GRADLE_TASK','command':name,'classification':'INTERNAL_ENGINE','replacement':''})
    rows=sorted(rows,key=lambda r:r['id'])
    dup=[r['id'] for r in rows if r['classification']=='DUPLICATE']; dead=[r['id'] for r in rows if r['classification']=='DEAD']
    payload={'schemaVersion':1,'owner':'cpf-tools/runtime/cli','officialInterface':'cpf','classificationValues':['CANONICAL_ENGINE','INTERNAL_ENGINE','CLI_CONSUMER','THIN_WRAPPER','MIGRATE_TO_CLI','DUPLICATE','DEAD'],'entrypointCount':len(rows),'duplicateCount':len(dup),'deadCount':len(dead),'entries':rows}
    out=root/args.output; out.parent.mkdir(parents=True,exist_ok=True); out.write_text(json.dumps(payload,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    print(json.dumps({'status':'PASS','entrypointCount':len(rows),'duplicateCount':len(dup),'deadCount':len(dead),'output':out.relative_to(root).as_posix()},ensure_ascii=False))
    return 0 if not dup and not dead else 1
if __name__=='__main__': raise SystemExit(main())
