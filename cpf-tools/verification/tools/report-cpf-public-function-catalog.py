#!/usr/bin/env python3
"""Build a source-backed Public Function TOP 100 catalog for developer discovery."""
from __future__ import annotations

import sys as _cpf_sys

# CPF 표준 인코딩은 UTF-8 이다. 호출자의 콘솔 코드페이지(Windows cp949 등)에 좌우되면
# 한글 출력이 깨져 진단 메시지를 읽을 수 없다. 진입점이 스스로 출력 스트림을 고정한다.
for _cpf_stream in (_cpf_sys.stdout, _cpf_sys.stderr):
    try:
        _cpf_stream.reconfigure(encoding='utf-8')
    except (AttributeError, ValueError):
        pass

import argparse
import json
import re
from pathlib import Path

DECL = re.compile(r"\bpublic\s+(?:final\s+|sealed\s+|abstract\s+)?(?:class|interface|record|@interface|enum)\s+(\w+)")
PACKAGE = re.compile(r"^\s*package\s+([\w.]+);", re.MULTILINE)
METHOD = re.compile(r"\b(?:public\s+)?(?:default\s+|static\s+)?[\w<>, ?\[\].]+\s+(\w+)\s*\(")


def load_contract(root: Path) -> dict:
    return json.loads((root / 'cpf-tools/generator/contracts/cpf-developer-adoption-contract.json').read_text(encoding='utf-8'))


def load_catalog(root: Path) -> dict:
    return json.loads((root / 'cpf-tools/generator/contracts/cpf-starter-catalog.json').read_text(encoding='utf-8'))


def owner_for(path: str, modules: list[dict]) -> dict | None:
    candidates = [m for m in modules if path.startswith(str(m.get('ownerPath','')).rstrip('/') + '/')]
    return max(candidates, key=lambda x: len(str(x.get('ownerPath',''))), default=None)


def discover(root: Path, modules: list[dict], curated_sources: set[str]) -> list[dict]:
    rows=[]
    public_roots=[(root / m["ownerPath"],m) for m in modules if m.get("visibility")=="public"]
    public_roots.append((root / "cpf-core", {"artifactId":"cpf-core","usageHintKo":"Topology-independent CPF Public Contract"}))
    seen_files=set()
    for owner,module in public_roots:
        if not owner.is_dir():
            continue
        for file in sorted(owner.rglob("*.java")):
            rel=file.relative_to(root).as_posix()
            if rel in seen_files or "/src/test/" in rel or rel in curated_sources:
                continue
            seen_files.add(rel)
            # TOP 100 only exposes stable Public contract packages, never implementation/runtime classes.
            if not any(token in rel for token in ("/api/","/annotation/","/spi/")):
                continue
            text=file.read_text(encoding="utf-8",errors="ignore")
            decl=DECL.search(text)
            if not decl:
                continue
            package=PACKAGE.search(text)
            name=decl.group(1)
            rows.append({
                "name":name,
                "source":rel,
                "package":package.group(1) if package else "",
                "starter":module.get("artifactId",""),
                "usageLevel":("advanced" if "/spi/" in rel or name.endswith(("Router","Executor")) else "capability"),
                "when":module.get("usageHintKo",""),
                "methods":[],
            })
    rows.sort(key=lambda r:(0 if "/api/" in r["source"] else 1, len(r["name"]), r["name"], r["source"]))
    return rows


def build_rows(root: Path) -> list[dict]:
    contract=load_contract(root); catalog=load_catalog(root); modules=catalog['modules']
    rows=[]
    for item in contract['top20GoldenPath']:
        module=owner_for(item['source'],modules)
        rows.append({
            'name':item['name'],'source':item['source'],'starter':(module or {}).get('artifactId','framework/runtime'),
            'usageLevel':'golden','when':item['when'],'methods':[],
        })
    curated_sources={r['source'] for r in rows}
    rows.extend(discover(root,modules,curated_sources))
    # Deduplicate by display name + source and cap at TOP 100.
    unique=[]; seen=set()
    for row in rows:
        key=(row['name'],row['source'])
        if key in seen: continue
        seen.add(key); unique.append(row)
        if len(unique)>=100: break
    return unique


def render(rows: list[dict]) -> str:
    lines=[
        '# CPF Public Function TOP 100', '',
        '> 현재 Source와 Canonical Starter Catalog에서 자동 검증되는 개발자 탐색용 목록입니다. Internal 구현은 포함하지 않습니다.', '',
        '## TOP 20 — Golden Path', '',
        '| No | Function / Annotation | Starter | 언제 사용 | Source |',
        '|---:|---|---|---|---|',
    ]
    for i,row in enumerate(rows[:20],1):
        lines.append(f"| {i} | `{row['name']}` | `{row['starter']}` | {row['when']} | `{row['source']}` |")
    lines += ['', '## TOP 100 — 기능 탐색', '', '| No | Level | Public Function / Type | Starter | 대표 용도/메소드 | Source |','|---:|---|---|---|---|---|']
    for i,row in enumerate(rows,1):
        detail=row['when']
        if row.get('methods'):
            detail=(detail+' · ' if detail else '')+', '.join(f"`{m}()`" for m in row['methods'])
        lines.append(f"| {i} | {row['usageLevel']} | `{row['name']}` | `{row['starter']}` | {detail} | `{row['source']}` |")
    lines += ['', '## 선택 원칙', '', '- **golden**: 일반 업무개발자가 먼저 사용하는 표준 경로입니다.', '- **capability**: 해당 기능을 선택했을 때 사용하는 Public API입니다.', '- **advanced**: Adapter/Framework 개발용이며 일반 Golden Path와 분리합니다.', '- **internal**: 이 문서와 Public Starter 선택 화면에 노출하지 않습니다.', '', 'Starter 선택은 `cpf-docs/development/CPF_STARTER_QUICK_SELECT.md`를 먼저 봅니다.', '']
    return '\n'.join(lines)


def main()->int:
    ap=argparse.ArgumentParser(); ap.add_argument('--root',default='.'); ap.add_argument('--output',default='cpf-docs/development/CPF_PUBLIC_FUNCTION_TOP_100.md'); ap.add_argument('--check',action='store_true'); args=ap.parse_args()
    root=Path(args.root).resolve(); rows=build_rows(root)
    if len(rows)<100:
        print(f'FAIL public function inventory has only {len(rows)} eligible rows'); return 1
    out=root/args.output; expected=render(rows)
    if args.check:
        if not out.is_file() or out.read_text(encoding='utf-8')!=expected:
            print('FAIL Public Function TOP 100 is stale; regenerate report'); return 1
    else:
        out.parent.mkdir(parents=True,exist_ok=True); out.write_text(expected,encoding='utf-8')
    print(f'PASS public function catalog rows={len(rows)} golden={sum(r["usageLevel"]=="golden" for r in rows)} output={out.relative_to(root)}')
    return 0

if __name__=='__main__': raise SystemExit(main())
