# CPF Current Work Request — Canonical Target Alignment

> 상위 정본: `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
> 원칙: Source를 Target에 맞춘다. Source와 다르다는 이유로 Target을 되돌리지 않는다.

## 현재 개발 우선순위

1. **P0 Ownership** — `cpf-common` 고객 업무 공통 Owner를 제품 구조/API/Starter/DB/Consumer까지 구현하고 기술 `cpf-starter-common`과 분리한다.
2. **P0 Generated Domain** — `cpf-<domain>/cpf-domain.yaml` source-controlled logical definition으로 create/setup/sync/diff를 currentize하고 environment DB binding을 분리한다.
3. **P0 EDU 35** — Online 정확히 20개(Required/Requires-New 분리 포함) + Batch 15개를 목적별 Canonical 그룹으로 구현한다.
4. **P1 System6/Operation** — Remote all-six serialization, receiver validation, Caller System Policy, separate optional Channel Policy, multi-instance discovery lifecycle을 전수 재검증한다.
5. **P1 Runtime Instance** — same-host/same-system multi-process explicit instanceId 및 duplicate READY rejection을 검증한다.
6. **P1 Public Distribution** — Public Workspace + Public Binary Repository를 isolated cache/no mavenLocal/private repo 조건에서 닫는다.
7. **P1 Local Bootstrap** — shared engine, progress/timeout, selected DB lifecycle, stop≠reset, domain rediscovery를 닫는다.
8. **P1 Documentation Closure** — Delete Manifest 적용 후 stale canonical/history link와 verifier reference 0을 확인한다.
9. **P0 Requirement Ledger** — QA가 신규 11 Canonical ID/영향 ID를 상태 원장에 재개방·추가하도록 요청하고 Developer는 자기 역할 컬럼만 갱신한다.
10. **P1 Derived Dataset** — 205 Canonical 기준 Requirement/Scenario/Execution dataset을 지정 Pipeline으로 재생성·검증한다.
11. **P1 Official Guides** — Generator/Developer/EDU/Architecture Guide를 구현 완료 Target과 동일하게 currentize한다.

상세 Gap과 Acceptance는 `cpf-docs/deliverables/CANONICAL_SOURCE_GAP_BACKLOG.csv` 및 Final Target의 연결 Requirement를 사용한다.

## 완료 방식

각 항목은 Source/API/SQL/Config/Frontend/Generator/Test/Evidence를 필요한 범위에서 함께 구현한다. 미실행 Runtime은 `미검증`으로 남기고 QA만 최종 상태를 변경한다.
