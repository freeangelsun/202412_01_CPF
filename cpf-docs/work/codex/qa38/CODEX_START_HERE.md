# CPF QA38 Codex Start Here

## Mission

QA37에서 남은 실제 미검증과 Source Gap을 이어서 검수하고, 결함을 발견하면 보완 개발과 재검증까지 수행한다.

Review baseline: `38089a96e3f4c7c2ba05cda549785b47f67cd462`
Execution baseline: actual clean `HEAD == origin/master`

## First read

1. `cpf-docs/work/current/CPF_CURRENT_WORK_REQUEST.md`
2. `cpf-docs/work/current/CPF_20260802_05_POST_QA37_INTEGRATED_DEVELOPMENT_REQUEST.md`
3. `cpf-docs/work/state/CPF_CODEX_CONTINUITY_STATE.md`
4. `cpf-docs/work/codex/qa38/CPF_CODEX_QA38_VERIFICATION_REMEDIATION_REQUEST.md`
5. `cpf-docs/work/codex/qa38/STAGE_PLAN.csv`
6. `cpf-docs/work/codex/qa38/VERIFICATION_HISTORY.csv`

Do not reread all historical QA documents before a failure requires them.

## Critical resume rule

A historical PASS may skip analysis only when current HEAD, command hash, environment/profile and relevant artifact hash match.
It cannot be used as current completion evidence after Source or Git SHA changed.

## Highest priority

1. Confirm all pushes and Working Tree.
2. Finish Core/Starter/Generator source development.
3. Finish official Fresh DB lifecycle tooling.
4. Start each Vendor from zero CPF objects.
5. Continue first incomplete expensive stage.
6. Repair, do not only report.
7. Keep execution, defect and verification history updated.

## Protected

No commit/push/reset/restore/stash/clean.
No user DB reset.
No Docker prune/down -v/image/volume/secret deletion.
No Vendor SQL first.
No old Primary restoration.

## 타 GPT 전담 보호 경로

다음 경로는 Read Only다.

```text
cpf-docs/deliverables/**
cpf-docs/guides/**
cpf-docs/environment/docker/**
cpf-tools/environment/docker-development-test/**
```

이 작업과 다음 Codex 작업은 해당 경로를 참조할 수 있지만 수정·추가·삭제·이동·이름 변경·자동 포맷·일괄 치환·Stage하지 않는다.
변경 필요성이 발견되면 실제 파일을 건드리지 않고 담당 GPT용 영향도와 작업요건만 기록한다.
Overlay·Delete Manifest·Cleanup 대상에도 포함하지 않는다.
