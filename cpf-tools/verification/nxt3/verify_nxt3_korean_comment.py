#!/usr/bin/env python3
"""NXT3-KOREAN-COMMENT-001: 신규/변경 Source의 한국어 설명 계약 Gate."""
from __future__ import annotations
import argparse,csv,json,re,subprocess
from dataclasses import dataclass,asdict
from pathlib import Path
from typing import Iterable

HANGUL=re.compile(r'[가-힣]')
TYPE_DECL=re.compile(r'\b(class|interface|enum|@interface|record)\s+[A-Za-z_][A-Za-z0-9_]*')
METHOD_DECL=re.compile(r'^\s*(public|protected)\s+(?:static\s+)?(?:final\s+)?(?:<[^>]+>\s+)?[\w<>?,.\[\] ]+\s+([A-Za-z_][A-Za-z0-9_]*)\s*\(')
CTOR_DECL=re.compile(r'^\s*(public|protected)\s+([A-Z][A-Za-z0-9_]*)\s*\(')
FIELD_DECL=re.compile(r'^\s*(?:private|protected|public)\s+(?:final\s+)?[\w<>?,.\[\] ]+\s+[A-Za-z_][A-Za-z0-9_]*\s*(?:=|;)')
DESIGN=re.compile(r'(?i)\b(retry|idempot|transaction|rollback|lock|concurr|cache|recovery|reconcile|audit|mask|secret|security|vendor|dialect|timeout)\b')
SECRET_LITERAL=re.compile(r'(?i)\b(password|secret|token|api[-_]?key)\s*[:=]\s*["\']?(?!\$\{|\$\(|<|changeme|example|masked|\*{3})[^\s"\']{6,}')
SKIP_PARTS={'.git','.gradle','node_modules','dist','out','target','__pycache__'}
SOURCE_EXT={'.java','.kt'}
CONFIG_EXT={'.yml','.yaml','.properties','.env'}
EXCLUDE_METHODS={'toString','hashCode','equals','getClass','clone','finalize'}

@dataclass
class Finding:
    code:str; path:str; line:int; detail:str


def source_identity(root: Path) -> str:
    import os, subprocess
    env = os.environ.get("CPF_SOURCE_SHA", "").strip()
    if re.fullmatch(r"[0-9a-fA-F]{40}", env):
        return env.lower()
    if (root / ".git").exists():
        cp = subprocess.run(["git", "rev-parse", "HEAD"], cwd=root, text=True, capture_output=True)
        value = (cp.stdout or "").strip()
        if cp.returncode == 0 and re.fullmatch(r"[0-9a-fA-F]{40}", value):
            return value.lower()
    base = root / "cpf-docs/work/BASE_SHA.txt"
    if base.is_file():
        value = base.read_text(encoding="utf-8", errors="ignore").strip()
        if re.fullmatch(r"[0-9a-fA-F]{40}", value):
            return value.lower()
    return "UNKNOWN"

def rel(p:Path,r:Path)->str:return p.relative_to(r).as_posix()

def _manifest_paths(root:Path, manifest:Path|None)->set[str]:
    if manifest is None or not manifest.exists(): return set()
    out=set()
    with manifest.open(encoding='utf-8-sig',newline='') as f:
        for row in csv.DictReader(f):
            for key in ('path','relative_path','file','target_path'):
                v=(row.get(key) or '').replace('\\','/').strip('/')
                if v: out.add(v); break
    return out

def _iter(root:Path, manifest_paths:set[str])->Iterable[Path]:
    if manifest_paths:
        for rp in sorted(manifest_paths):
            p=root/rp
            if p.is_file() and p.suffix.lower() in SOURCE_EXT|CONFIG_EXT: yield p
        return
    # ZIP에는 Git diff가 없으므로 default full scan은 '개발자가 직접 소비하는 Public/Generated/EDU/Config Surface'만 검사한다.
    # ADM/Backoffice 내부 구현까지 public Java method라는 이유만으로 개발자 Public API로 오인하지 않는다.
    roots=[root/'cpf-member',root/'cpf-external',root/'cpf-education',root/'cpf-core',root/'cpf-admin',root/'cpf-backoffice/online',root/'cpf-starters']
    seen=set()
    for base in roots:
        if not base.exists(): continue
        for p in base.rglob('*'):
            if not p.is_file() or p.suffix.lower() not in SOURCE_EXT|CONFIG_EXT: continue
            if any(x in p.relative_to(root).parts for x in SKIP_PARTS): continue
            rp=rel(p,root)
            if '/src/test/' in '/'+rp or '/src/jtaTest/' in '/'+rp:
                continue
            public_surface = rp.startswith(('cpf-member/','cpf-external/','cpf-education/'))
            if rp.startswith('cpf-core/'):
                public_surface = any(token in '/'+rp.lower() for token in ('/api/','/spi/','/annotation/','/context/'))
            if rp.startswith(('cpf-starters/','cpf-admin/','cpf-backoffice/online/')):
                low='/'+rp.lower()
                public_surface = any(token in low for token in ('/api/','/spi/','/annotation/','/config/')) or p.name.endswith(('Properties.java','Properties.kt'))
            if p.suffix.lower() in CONFIG_EXT:
                public_surface = public_surface or rp.startswith(('cpf-member/','cpf-external/','cpf-education/'))
            if not public_surface:
                continue
            if rp not in seen: seen.add(rp); yield p

def _korean_comment_before(lines:list[str], idx:int, window:int=7)->bool:
    start=max(0,idx-window)
    block='\n'.join(lines[start:idx])
    # A Korean comment/Javadoc immediately around the declaration is sufficient.
    return bool(HANGUL.search(block) and re.search(r'(?s)(//|/\*|\*|#|--).*?[가-힣]',block))

def _scan_source(path:Path,root:Path)->tuple[list[Finding],list[str]]:
    rp=rel(path,root); text=path.read_text(encoding='utf-8-sig',errors='replace'); lines=text.splitlines(); f=[]; w=[]
    if '/src/test/' in '/'+rp: return f,w
    # Type-level role/responsibility comment.
    for i,line in enumerate(lines):
        if TYPE_DECL.search(line) and re.search(r'\b(public|protected)\b', line) and not re.match(r'^\s*//',line):
            if not _korean_comment_before(lines,i,9): f.append(Finding('TYPE_KOREAN_COMMENT_MISSING',rp,i+1,line.strip()))
    # Public/protected major methods and constructors.
    for i,line in enumerate(lines):
        if re.search(r"\b(class|interface|enum|record|@interface)\b", line):
            continue
        m=METHOD_DECL.match(line) or CTOR_DECL.match(line)
        if not m: continue
        name=m.group(2)
        if name in EXCLUDE_METHODS or name.startswith(('get','set','is')): continue
        if '@Override' in '\n'.join(lines[max(0,i-2):i+1]): continue
        if not _korean_comment_before(lines,i,7): f.append(Finding('PUBLIC_API_KOREAN_COMMENT_MISSING',rp,i+1,name))
    # ConfigurationProperties fields require meaning/default/impact explanation in Korean.
    if '@ConfigurationProperties' in text:
        for i,line in enumerate(lines):
            if FIELD_DECL.match(line) and 'static final' not in line:
                if not _korean_comment_before(lines,i,7): f.append(Finding('CONFIG_FIELD_KOREAN_COMMENT_MISSING',rp,i+1,line.strip()))
    # 동시성/재시도/트랜잭션/복구/DB Vendor 분기처럼 설계 이유가 필요한 제어문만 강제한다.
    complex_design = re.compile(r"(?i)(?:\bsynchronized\s*\(|@Transactional\b|\bcatch\s*\(|(?:\bif|\bswitch|\bwhen)\b[^\n]*(?:vendor|dialect)|(?:\bwhile|\bfor)\b[^\n]*retry|\b(?:rollback|reconcile)\s*\()")
    for i,line in enumerate(lines):
        if complex_design.search(line) and not re.match(r"^\s*(?://|\*|/\*)",line):
            if not _korean_comment_before(lines,i,10) and not HANGUL.search(line):
                f.append(Finding('DESIGN_INTENT_KOREAN_COMMENT_MISSING',rp,i+1,line.strip()[:180]))
    return f,w

def _scan_config(path:Path,root:Path)->tuple[list[Finding],list[str]]:
    rp=rel(path,root); text=path.read_text(encoding='utf-8-sig',errors='replace'); f=[];w=[]
    for i,line in enumerate(text.splitlines(),1):
        if SECRET_LITERAL.search(line) and '${' not in line:
            f.append(Finding('SECRET_LITERAL_FORBIDDEN',rp,i,line.strip()[:160]))
    meaningful=[x for x in text.splitlines() if x.strip() and not x.lstrip().startswith(('#','!'))]
    korean_comments=[x for x in text.splitlines() if x.lstrip().startswith(('#','!')) and HANGUL.search(x)]
    if meaningful and not korean_comments:
        w.append(f'CONFIG_KOREAN_COMMENT_RECOMMENDED={rp}')
    return f,w

def self_test()->dict:
    import tempfile
    with tempfile.TemporaryDirectory(prefix='cpf-kcomment-') as td:
        r=Path(td); good=r/'Good.java';bad=r/'Bad.java'
        good.write_text('/** 주문 조회 역할을 제공합니다. */\npublic class Good {\n /** 식별자로 주문을 조회합니다. */\n public String find(String id){ return id; }\n}\n',encoding='utf-8')
        bad.write_text('public class Bad { public String find(String id){ return id; } }\n',encoding='utf-8')
        gf,_=_scan_source(good,r);bf,_=_scan_source(bad,r)
        ok=(not gf and bool(bf))
        return {'status':'PASS' if ok else 'FAIL','goodFailures':len(gf),'badFailures':len(bf)}

def main()->int:
    ap=argparse.ArgumentParser();ap.add_argument('--root',default='.');ap.add_argument('--manifest');ap.add_argument('--json-out');ap.add_argument('--self-test',action='store_true');a=ap.parse_args()
    if a.self_test:
        x=self_test();print(json.dumps(x,ensure_ascii=False,indent=2));return 0 if x['status']=='PASS' else 1
    root=Path(a.root).resolve();mp=_manifest_paths(root,Path(a.manifest) if a.manifest else None); findings=[];warnings=[];scanned=0
    for p in _iter(root,mp):
        scanned+=1
        ff,ww=(_scan_source(p,root) if p.suffix.lower() in SOURCE_EXT else _scan_config(p,root));findings+=ff;warnings+=ww
    result={'requirement':'NXT3-KOREAN-COMMENT-001','executionSourceSha':source_identity(root),'filesScanned':scanned,'failures':len(findings),'warnings':len(warnings),'status':'PASS' if not findings else 'FAIL','findings':[asdict(x) for x in findings],'warningDetails':warnings}
    if a.json_out:
        o=Path(a.json_out);o.parent.mkdir(parents=True,exist_ok=True);o.write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    print(f"CPF_NXT3_KOREAN_COMMENT={result['status']} scanned={scanned} failures={len(findings)} warnings={len(warnings)}")
    for x in findings[:250]: print(f'{x.code} {x.path}:{x.line} :: {x.detail}')
    for x in warnings[:50]: print('WARN '+x)
    return 0 if not findings else 1
if __name__=='__main__': raise SystemExit(main())
