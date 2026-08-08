#!/usr/bin/env python3
from __future__ import annotations
import argparse, re, sys
from pathlib import Path

CANONICAL_EXPECTED = 169
ALIAS_EXPECTED = 8
ROW_RE = re.compile(r"^\|\s*`([A-Z0-9][A-Z0-9-]+)`\s*\|")
HEADER_RE = re.compile(r"Canonical Requirement Count:\s*\*\*(\d+)개\*\*")
SECTION21_RE = re.compile(r"현재 Canonical Requirement Count는\s*\*\*(\d+)개\*\*")
ALIAS_HEADER_RE = re.compile(r"Legacy Alias:\s*\*\*(\d+)개\*\*")

def section(text: str, start: str, end: str | None) -> str:
    s=text.index(start)
    e=text.index(end, s) if end else len(text)
    return text[s:e]

def ids_from_table(block: str) -> list[str]:
    return [m.group(1) for line in block.splitlines() if (m:=ROW_RE.match(line))]

def verify(path: Path) -> list[str]:
    text=path.read_text(encoding='utf-8')
    errors=[]
    canonical=ids_from_table(section(text, '## 22. 상세 Requirement Catalog', '## 23. Legacy Alias Mapping'))
    aliases=ids_from_table(section(text, '## 23. Legacy Alias Mapping', '## 24. 영구 완료 금지 조건'))
    for name, rx, expected in [('header',HEADER_RE,CANONICAL_EXPECTED),('section21',SECTION21_RE,CANONICAL_EXPECTED),('alias-header',ALIAS_HEADER_RE,ALIAS_EXPECTED)]:
        m=rx.search(text)
        if not m: errors.append(f'{name}: count declaration missing')
        elif int(m.group(1))!=expected: errors.append(f'{name}: declared={m.group(1)} expected={expected}')
    if len(canonical)!=CANONICAL_EXPECTED: errors.append(f'canonical rows={len(canonical)} expected={CANONICAL_EXPECTED}')
    if len(set(canonical))!=len(canonical): errors.append('canonical duplicate IDs: '+','.join(sorted({x for x in canonical if canonical.count(x)>1})))
    if len(aliases)!=ALIAS_EXPECTED: errors.append(f'legacy aliases={len(aliases)} expected={ALIAS_EXPECTED}')
    if len(set(aliases))!=len(aliases): errors.append('legacy alias duplicate IDs')
    overlap=set(canonical)&set(aliases)
    if overlap: errors.append('legacy aliases inflate canonical denominator: '+','.join(sorted(overlap)))
    return errors

def main() -> int:
    ap=argparse.ArgumentParser()
    ap.add_argument('path', nargs='?', default='cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md')
    args=ap.parse_args()
    errors=verify(Path(args.path))
    if errors:
        for e in errors: print('FAIL',e,file=sys.stderr)
        return 1
    print(f'PASS canonical={CANONICAL_EXPECTED} aliases={ALIAS_EXPECTED} denominator={CANONICAL_EXPECTED}')
    return 0
if __name__=='__main__': raise SystemExit(main())
