#!/usr/bin/env python3
from __future__ import annotations
import argparse,csv,re
from pathlib import Path

SCRIPT_EXT={'.ps1','.sh','.cmd','.bat','.py'}
EXCLUDED={'.git','.gradle','.idea','.pytest_cache','__pycache__','node_modules','cpf-release','dist','out','target'}
OFFICIAL_WRAPPERS={
 'cpf-tools/runtime/cli/cpf','cpf-tools/runtime/cli/cpf.cmd','cpf-tools/runtime/cli/cpf.ps1',
 'cpf-tools/release/open-git/templates/bin/cpf','cpf-tools/release/open-git/templates/bin/cpf.cmd','cpf-tools/release/open-git/templates/bin/cpf.ps1',
 'cpf-tools/release/public/templates/bin/cpf','cpf-tools/release/public/templates/bin/cpf.cmd','cpf-tools/release/public/templates/bin/cpf.ps1',
}
CANONICAL_JAVA={
 'cpf-tools/runtime/cli/java/CpfCli.java':'Unified CLI implementation',
 'cpf-tools/runtime/bootstrap/CpfBootstrap.java':'Local bootstrap/runtime engine',
 'cpf-tools/runtime/cli/java/CpfGeneratorLauncher.java':'Generator distribution consumer',
}

def excluded(p:Path)->bool:
    normalized='/' + p.as_posix().lstrip('/')
    if '/cpf-docs/work/evidence/' in normalized:
        return True
    return any(x in EXCLUDED for x in p.parts) or ('build' in p.parts and '/cpf-tools/build/' not in normalized)

def owner(rel:str)->str:
    parts=rel.split('/')
    if rel.startswith('cpf-tools/'): return '/'.join(parts[:3]) if len(parts)>=3 else 'cpf-tools'
    if rel.startswith('cpf-batch/'): return '/'.join(parts[:2])
    if rel.startswith('cpf-'): return parts[0]
    return 'root'

def classify(rel:str,kind:str,symbol:str='')->tuple[str,str,str]:
    if rel=='cpf-tools/runtime/cli/java/CpfCli.java': return 'CANONICAL_ENGINE','cpf','Exactly-one official CLI implementation'
    if rel in OFFICIAL_WRAPPERS: return 'THIN_WRAPPER','cpf-tools/runtime/cli/java/CpfCli.java','OS launcher only'
    if rel=='cpf-tools/runtime/cli/cpf.py': return 'INTERNAL_ENGINE','cpf-tools/generator/engine + internal verification','Legacy low-level Python tooling engine; not official CLI'
    if rel=='cpf-tools/runtime/bootstrap/CpfBootstrap.java': return 'CANONICAL_ENGINE','cpf bootstrap/run/stop/reset','Bootstrap engine remains canonical owner'
    if rel=='cpf-tools/runtime/cli/java/CpfGeneratorLauncher.java': return 'CLI_CONSUMER','canonical generator distribution','CLI collaborator; generator engine remains separate owner'
    if rel.startswith('cpf-tools/generator/engine/'): return 'CANONICAL_ENGINE','cpf domain-new/domain-sync','Canonical Generator Engine'
    if rel.startswith('cpf-tools/db/') and kind in {'SCRIPT','JAVA_MAIN'}: return 'CANONICAL_ENGINE','cpf bootstrap / future cpf db namespace','Canonical DB tooling engine; CLI consumer does not copy it'
    if rel.startswith('cpf-tools/release/open-git/') and rel.endswith('cpf_open_git.py'): return 'CANONICAL_ENGINE','cpf release open-git','Canonical Open Git release engine'
    if rel.startswith('cpf-tools/verification/') or rel.startswith('cpf-tools/testing/'):
        return 'INTERNAL_ENGINE','cpf verify/dev full-validation','Verification/QA engine; Internal capability only'
    if kind=='GRADLE_TASK': return 'INTERNAL_ENGINE','cpf build/test/dev/publish as cataloged','Low-level Gradle engine/task'
    if rel.startswith('cpf-tools/') or rel.startswith('cpf-batch/'):
        return 'INTERNAL_ENGINE','cpf command catalog or low-level automation','Retained engine/debug/automation entrypoint'
    return 'INTERNAL_ENGINE','owner-specific runtime/automation','Non-official entrypoint'

def rows(root:Path):
    out=[]
    for p in root.rglob('*'):
        if not p.is_file() or excluded(p): continue
        rel=p.relative_to(root).as_posix()
        if p.suffix.lower() in SCRIPT_EXT:
            c,repl,note=classify(rel,'SCRIPT')
            out.append({'entry_id':f'SCRIPT:{rel}','kind':'SCRIPT','path':rel,'symbol':'','classification':c,'canonical_replacement':repl,'owner':owner(rel),'public_surface':'YES' if c in {'THIN_WRAPPER','CLI_CONSUMER'} and ('release/open-git/templates' in rel or 'release/public/templates' in rel) else 'NO','notes':note})
        elif p.suffix=='.java':
            text=p.read_text(encoding='utf-8',errors='ignore')
            if 'public static void main' in text:
                c,repl,note=classify(rel,'JAVA_MAIN')
                out.append({'entry_id':f'JAVA_MAIN:{rel}','kind':'JAVA_MAIN','path':rel,'symbol':'main','classification':c,'canonical_replacement':repl,'owner':owner(rel),'public_surface':'NO','notes':note})
        elif p.suffix in {'.gradle','.kts'} and p.name.endswith(('.gradle','.gradle.kts')):
            text=p.read_text(encoding='utf-8',errors='ignore')
            names=[]
            names += [m.group(1) for m in re.finditer(r"tasks\.(?:register|create)\s*\(\s*['\"]([^'\"]+)",text)]
            names += [m.group(1) for m in re.finditer(r"(?m)^\s*task\s+([A-Za-z0-9_-]+)\b",text)]
            for name in sorted(set(names)):
                c,repl,note=classify(rel,'GRADLE_TASK',name)
                out.append({'entry_id':f'GRADLE_TASK:{rel}#{name}','kind':'GRADLE_TASK','path':rel,'symbol':name,'classification':c,'canonical_replacement':repl,'owner':owner(rel),'public_surface':'NO','notes':note})
    return sorted(out,key=lambda r:r['entry_id'])

def main():
    ap=argparse.ArgumentParser(); ap.add_argument('--root',type=Path,default=Path.cwd()); ap.add_argument('--output',type=Path); ap.add_argument('--check',action='store_true'); ns=ap.parse_args()
    root=ns.root.resolve(); target=(ns.output or root/'cpf-tools/runtime/cli/contracts/cpf-tool-entrypoint-inventory.csv').resolve(); data=rows(root)
    fields=['entry_id','kind','path','symbol','classification','canonical_replacement','owner','public_surface','notes']
    if ns.check:
        if not target.is_file(): raise SystemExit('CPF_TOOL_ENTRYPOINT_INVENTORY=FAIL missing catalog')
        with target.open(encoding='utf-8-sig',newline='') as f: current=list(csv.DictReader(f))
        if current!=data:
            cur={r['entry_id']:r for r in current}; exp={r['entry_id']:r for r in data}
            print('CPF_TOOL_ENTRYPOINT_INVENTORY=FAIL')
            print('missing=',sorted(set(exp)-set(cur))[:20]); print('stale=',sorted(set(cur)-set(exp))[:20]); print('changed=',sorted(k for k in set(cur)&set(exp) if cur[k]!=exp[k])[:20])
            raise SystemExit(1)
    else:
        target.parent.mkdir(parents=True,exist_ok=True)
        with target.open('w',encoding='utf-8-sig',newline='') as f:
            w=csv.DictWriter(f,fieldnames=fields); w.writeheader(); w.writerows(data)
    counts={}
    for r in data: counts[r['classification']]=counts.get(r['classification'],0)+1
    print(f'CPF_TOOL_ENTRYPOINT_INVENTORY=PASS entries={len(data)} counts={counts}')
if __name__=='__main__': main()
