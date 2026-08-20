#!/usr/bin/env python3
"""Current-only canonical document entrypoints and duplicate-role absence gate."""
from __future__ import annotations
import argparse,re,sys
from pathlib import Path

ap=argparse.ArgumentParser(); ap.add_argument('--root',default='.'); ap.add_argument('--baseline'); a=ap.parse_args()
root=Path(a.root).resolve(); fail=[]
required=[
 'cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md',
 'cpf-docs/governance/CPF_DOCUMENT_CANONICAL_INDEX.md',
 'cpf-docs/governance/CPF_CANONICAL_PATH_AND_ROLE_MAP.md',
 'cpf-docs/work/current/CPF_CURRENT_WORK_REQUEST.md',
 'cpf-docs/work/REQUIREMENT_STATUS.csv',
 'cpf-docs/deliverables/TEST_AND_EVIDENCE.md',
 'cpf-docs/deliverables/OPEN_ISSUES.md',
 'cpf-docs/deliverables/DELETE_MANIFEST.csv',
 'cpf-docs/work/current/CPF_DEVELOPMENT_HANDOVER.md',
]
for rel in required:
    if not (root/rel).is_file(): fail.append('CURRENT_DOC_MISSING:'+rel)
forbidden=[
 'cpf-docs/work/CPF_CURRENT_WORK_REQUEST.md','cpf-docs/work/TEST_AND_EVIDENCE.md','cpf-docs/work/OPEN_ISSUES.md',
 'cpf-docs/work/QA_REWORK_REQUEST.md','cpf-docs/work/CHANGE_MANIFEST.csv','cpf-docs/work/PACKAGE_MANIFEST.json',
 'cpf-docs/work/SHA256SUMS.txt','cpf-docs/work/HANDOVER.md','cpf-docs/work/current/STEERING_INTERPRETATION.md',
]
for rel in forbidden:
    if (root/rel).exists(): fail.append('DUPLICATE_CURRENT_ROLE:'+rel)
work=root/'cpf-docs/work'
for p in work.rglob('*'):
    if not p.is_file(): continue
    rel=p.relative_to(root).as_posix()
    if rel.startswith('cpf-docs/work/evidence/') or '.parts/' in rel: continue
    n=p.name.upper()
    if re.search(r'(_R\d+|_REV(?:\d+|_|\.|$)|_SESSION(?:\d+|_|\.|$)|_FINAL_FINAL|_20\d{6,8}|CHECKPOINT)',n): fail.append('DUPLICATE_VERSIONED_DOC:'+rel)
if a.baseline and not re.fullmatch(r'[0-9a-fA-F]{40}',a.baseline): fail.append('INVALID_BASELINE_SHA_ARGUMENT')
for rel in required:
    p=root/rel
    if not p.exists(): continue
    t=p.read_text(encoding='utf-8-sig',errors='ignore')
    for phrase in ['최종 완료입니다','전체 완료입니다']:
        if phrase in t: fail.append('FALSE_FINAL_WORDING:'+rel+':'+phrase)
if fail:
    print('CPF_CURRENT_DOC_CONSOLIDATION=FAIL'); print('\n'.join(sorted(set(fail)))); sys.exit(1)
print(f'CPF_CURRENT_DOC_CONSOLIDATION=PASS currentDocs={len(required)} duplicateCurrentRoles=0')
