#!/usr/bin/env python3
from pathlib import Path
import csv
ROOT=Path(__file__).resolve().parents[4];H=ROOT/'cpf-docs/governance/development-harness';C=H/'current'
def rows(n):
    with (C/n).open(encoding='utf-8-sig',newline='') as f:return list(csv.DictReader(f))
work=rows('CURRENT_WORK_ITEM_REGISTRY.csv');status={r['work_item_id']:r for r in rows('CURRENT_DEVELOPMENT_STATUS.csv')}
roles={}
for r in rows('ROLE_EXECUTION_LEDGER.csv'):roles.setdefault(r['work_item_id'],[]).append(r)
tests={}
for r in rows('TEST_EXECUTION_LEDGER.csv'):tests.setdefault(r['work_item_id'],[]).append(r)
out=['# CPF Current Detailed Development / QA Review','',f'- Work Items: **{len(work)}**','- 출력 규칙: Current Work Item 전건 1:1, omitted=0','']
for i,w in enumerate(work,1):
    wid=w['work_item_id'];s=status.get(wid,{})
    out += [f'## {i}. {wid} — {w.get("work_package","")}', '',
      f'- **원 Requirement/Signal:** {w.get("source_requirement_ids","")} / {w.get("source_signal","")}',
      f'- **현재 Observation/Root Cause:** {w.get("current_observation","")}',
      f'- **개발 범위:** {w.get("development_scope","")}',
      f'- **영향 Source/Consumer:** {w.get("source_consumer_scope","")}',
      f'- **Static 최대강도 Acceptance:** {w.get("static_high_intensity_acceptance","")}',
      f'- **Runtime 최대강도 Acceptance:** {w.get("runtime_high_intensity_acceptance","")}',
      f'- **문서 현행화:** {w.get("documentation_currentization","")}',
      f'- **Garbage/Delete:** {w.get("garbage_cleanup","")}',
      f'- **상태:** development={s.get("development_status",w.get("developer_status",""))} / verification={s.get("verification_status",w.get("verification_status",""))} / runtime={s.get("runtime_status","")} / overall={s.get("overall_status",w.get("overall_status",""))}',
      f'- **개발 완료 사유:** {s.get("development_completion_reason","")}',
      f'- **검증 미완료 사유:** {s.get("verification_incomplete_reason","")}',
      f'- **Source Identity:** {s.get("source_identity",w.get("source_identity",""))}',
      '- **역할별 수행:**']
    for r in roles.get(wid,[]):
        out.append(f'  - {r["role"]}: performed={r["performed"]}, role_status={r["role_status"]}, execution={r["execution_status"]}, completion_reason={r["completion_reason"] or "-"}, incomplete_reason={r["incomplete_reason"] or "-"}, command={r["command"] or "-"}, environment={r["environment"] or "-"}, exit={r["exit_code"] or "-"}, evidence={r["evidence"] or "-"}')
    out.append('- **Test/Runtime 수행:**')
    for r in tests.get(wid,[]):
        out.append(f'  - {r["test_kind"]}: mandatory={r["mandatory"]}, performed={r["performed"]}, status={r["status"]}, acceptance={r["acceptance"]}, completion_reason={r["completion_reason"] or "-"}, incomplete_reason={r["incomplete_reason"] or "-"}, command={r["command"] or "-"}, environment={r["environment"] or "-"}, exit={r["exit_code"] or "-"}, observed={r["observed_result"] or "-"}, evidence={r["evidence"] or "-"}')
    out += ['',f'- **Closure Rule:** {w.get("closure_rule","")}','']
(C/'CURRENT_DETAILED_REVIEW.md').write_text('\n'.join(out)+'\n',encoding='utf-8')
print(f'DETAILED_REVIEW=PASS WORK_ITEMS={len(work)} OMITTED=0')
