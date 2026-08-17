#!/usr/bin/env python3
"""현재 개발/검수 문서의 canonical entrypoint와 중복 version 문서를 검증한다."""
from __future__ import annotations
import argparse,re,sys
from pathlib import Path

ap=argparse.ArgumentParser()
ap.add_argument('--root',default='.')
ap.add_argument('--baseline')
a=ap.parse_args()
root=Path(a.root).resolve(); work=root/'cpf-docs/work'
fail=[]
required=['REVIEW_INDEX.md','HANDOVER.md','OPEN_ISSUES.md','TEST_AND_EVIDENCE.md','CPF_CURRENT_WORK_REQUEST.md','REQUIREMENT_STATUS.csv','current/DELETE_MANIFEST.txt','current/CPF_DEVELOPER_GPT_NEXT_WORK_INSTRUCTION.md']
for name in required:
    if not (work/name).is_file(): fail.append('CURRENT_DOC_MISSING:'+name)
for p in work.rglob('*'):
    if not p.is_file(): continue
    rel=p.relative_to(root).as_posix()
    if rel.startswith('cpf-docs/work/evidence/') or '.parts/' in rel: continue
    n=p.name.upper()
    if re.search(r'(_R\d+|_REV(?:\d+|_|\.|$)|_SESSION(?:\d+|_|\.|$)|_FINAL_FINAL|_20\d{6,8}|CHECKPOINT)',n):
        fail.append('DUPLICATE_VERSIONED_DOC:'+rel)
# 실행 기준 SHA는 Current Work/Open/Evidence에 표시하되, GitHub CI에서는 현재 github.sha를 강제한다.
if a.baseline:
    for name in ['CPF_CURRENT_WORK_REQUEST.md','OPEN_ISSUES.md','TEST_AND_EVIDENCE.md','REVIEW_INDEX.md']:
        p=work/name
        if p.exists() and a.baseline not in p.read_text(encoding='utf-8-sig',errors='ignore'):
            fail.append('BASELINE_SHA_MISSING:'+name)
for name in required:
    p=work/name
    if not p.exists(): continue
    t=p.read_text(encoding='utf-8-sig',errors='ignore')
    for phrase in ['최종 완료입니다','전체 완료입니다']:
        if phrase in t: fail.append('FALSE_FINAL_WORDING:'+name+':'+phrase)
if fail:
    print('CPF_CURRENT_DOC_CONSOLIDATION=FAIL'); print('\n'.join(sorted(set(fail)))); sys.exit(1)
print(f'CPF_CURRENT_DOC_CONSOLIDATION=PASS currentDocs={len(required)} versionedDuplicates=0')
