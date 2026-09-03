#!/usr/bin/env python3
"""Render current CPF requirement ledger statistics using the canonical verifier schema."""
from __future__ import annotations

import sys as _cpf_sys

# CPF 표준 인코딩은 UTF-8 이다. 호출자의 콘솔 코드페이지(Windows cp949 등)에 좌우되면
# 한글 출력이 깨져 진단 메시지를 읽을 수 없다. 진입점이 스스로 출력 스트림을 고정한다.
for _cpf_stream in (_cpf_sys.stdout, _cpf_sys.stderr):
    try:
        _cpf_stream.reconfigure(encoding='utf-8')
    except (AttributeError, ValueError):
        pass
import argparse,csv,json
from collections import Counter
from pathlib import Path

def main():
    ap=argparse.ArgumentParser(); ap.add_argument('--root',default='.'); ap.add_argument('--ledger',default='cpf-docs/governance/development-harness/current/REQUIREMENT_STATUS.csv'); ap.add_argument('--json-out'); ap.add_argument('--markdown-out'); ns=ap.parse_args()
    root=Path(ns.root).resolve(); path=Path(ns.ledger); path=path if path.is_absolute() else root/path
    with path.open(encoding='utf-8-sig',newline='') as f: rows=list(csv.DictReader(f))
    required={'exact_id','개발GPT_개발상태','개발GPT_검증상태','개발GPT_전체상태'}
    if not rows or not required.issubset(rows[0]): raise SystemExit('REQUIREMENT_PROGRESS_FAIL: unsupported/empty ledger schema')
    result={'schema':'CPF_REQUIREMENT_LEDGER_V2','rows':len(rows),'development':dict(Counter(r['개발GPT_개발상태'] for r in rows)),'verification':dict(Counter(r['개발GPT_검증상태'] for r in rows)),'overall':dict(Counter(r['개발GPT_전체상태'] for r in rows))}
    if ns.json_out:
        p=Path(ns.json_out); p=p if p.is_absolute() else root/p; p.parent.mkdir(parents=True,exist_ok=True); p.write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    if ns.markdown_out:
        p=Path(ns.markdown_out); p=p if p.is_absolute() else root/p; p.parent.mkdir(parents=True,exist_ok=True); p.write_text('# CPF Requirement Progress\n\n- Rows: **%d**\n- Development: `%s`\n- Verification: `%s`\n- Overall: `%s`\n' % (len(rows),result['development'],result['verification'],result['overall']),encoding='utf-8')
    print(json.dumps(result,ensure_ascii=False,sort_keys=True))
if __name__=='__main__': main()
