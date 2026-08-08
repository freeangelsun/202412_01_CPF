# CPF Current Development / QA Review Index

> 기준 currentization SHA: `b2da6bd720d1a8506db6bddf5d2e35feb9dca964` (`07_15`)  
> 실제 실행 시작 시 latest `origin/master` exact SHA를 다시 확인한다.  
> 이 파일은 탐색 Index이며 Requirement·Evidence를 중복 소유하지 않는다.

## 1. 최상위 정본

1. `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
   - 제품 최상위 목표와 Canonical Requirement
   - 이번 currentization 후 Canonical **186개**, Legacy Alias 8개 별도
2. `cpf-docs/governance/CPF_REQUIREMENT_CONTINUITY_LEDGER.md`
   - Requirement 추가/강화/Supersede 연속성
3. `cpf-docs/governance/CPF_STARTER_ARCHITECTURE_AND_LIFECYCLE_POLICY.md`
   - Core/Foundation/Capability/Starter 물리 Architecture
4. `cpf-docs/specification/CPF_DOCUMENTATION_STANDARD.md`
   - 사용자 문서 + 내부 Current-State 문서 생성/폐기 규칙

## 2. 현재 개발 작업

- **단일 실행 정본:** `cpf-docs/work/CPF_CURRENT_WORK_REQUEST.md`
- exact ID 상태: `cpf-docs/work/REQUIREMENT_STATUS.csv`
- 상세 Requirement Matrix: `cpf-docs/work/CPF_REQUIREMENT_MATRIX.csv`
- Scenario Matrix: `cpf-docs/work/CPF_SCENARIO_MATRIX.csv`

`CPF_CURRENT_WORK_REQUEST.md` 자체가 Developer GPT 실행 계약이다. 별도 `DEVGPT_SESSION/REV/FINAL` 지침 문서를 만들지 않는다.

## 3. 설계/개발/운영 Guide

- `cpf-docs/architecture/ARCHITECTURE_GUIDE.md`
- `cpf-docs/development/DEVELOPER_GUIDE.md`
- `cpf-docs/development/EDU_GUIDE.md`
- `cpf-docs/security/SECURITY_GUIDE.md`
- `cpf-docs/operations/RECOVERY_GUIDE.md`

이번 작업에서 Core Slimming, Utility/Foundation, transactionId ownership, Health, JPA,
Distributed Session, Object Storage, Event Schema, GraphQL, Realtime의 설계·사용·복구 계약을 상세 currentize한다.

## 4. Evidence / Handover

- Test/Runtime/Evidence: `cpf-docs/work/TEST_AND_EVIDENCE.md`
- 다음 수행자/Open Issue/QA·Codex: `cpf-docs/work/HANDOVER.md`
- 현재 변경 목록: `cpf-docs/work/CPF_CHANGE_MANIFEST.csv`
- 승인 대기 삭제 exact list: `cpf-docs/work/CPF_DELETE_MANIFEST.csv`
- QA 전수 표준: `cpf-docs/work/handover/CPF_QA_SESSION_HANDOVER_STANDARD.md`

별도 `OPEN_ISSUES.md`, `CODEX_REVIEW_REQUEST.md`, `RUNTIME_ONLY_VERIFICATION.csv`의 Current 역할은
`HANDOVER.md`와 `TEST_AND_EVIDENCE.md`로 병합한다.

## 5. 대용량 논리 Dataset

`cpf-docs/work/current/**`의 Requirement/Scenario Master와 `.parts`는 하나의 논리 Dataset이다.
이 파일들은 Narrative 결과서가 아니며 다음 키와 Count/Hash를 보존한다.

- `CPF_REQUIREMENT_MASTER.csv` + Parts
- `CPF_SCENARIO_MASTER.csv` + Parts
- `CPF_REQUIREMENT_CONTINUITY.csv`
- `CPF_REQUIREMENT_SOURCE_COVERAGE.csv`
- `CPF_COVERAGE_CLOSURE_MATRIX.csv`
- `CPF_PHASE_GATE_REGISTER.csv`
- `CPF_REQUIREMENT_CONTROL_REGISTER.xlsx`

세션별 Snapshot을 추가하지 않는다.

## 6. 삭제/가비지 정책

Current Owner에 내용이 흡수된 다음 범주는 삭제 대상이다.

- `cpf-docs/work/v9i/**`
- REV/SESSION/FINAL/Checkpoint 누적 문서
- 과거 세션별 Review/Handover/Open Issues/Test Evidence
- 과거 Package Manifest/SHA bundle
- obsolete QA campaign result
- Current Owner와 중복된 historical project-control 문서

삭제는 `CPF_DELETE_MANIFEST.csv`의 exact path만 수행한다.
보호경로와 제품 Source는 이 문서 currentization 단계에서 삭제하지 않는다.

## 7. Verification Tool 정책

Canonical 통합 Entry는 `cpf-tools/scripts/verify-full-product.ps1`이다.
`cpf-tools/verification/**`와 개별 `*.py/*.ps1/*.sh`는 다음 개발에서 Consumer를 전수 추적하여:

`KEEP_CANONICAL_GATE / MERGE_INTO_CANONICAL_GATE / RENAME_CURRENT / REMOVE_CANDIDATE`

로 판정한다. CI/Gradle/Runbook/다른 Script Consumer가 없는 historical helper는 exact Delete Manifest로 제거한다.

## 8. QA 흐름

`Developer 구현/자체검수 → 최신 successor SHA → QA A 100% 전수 → QA B 동일 Scope 100% 독립 전수 → A/B Cross Validation → Finding 재개발/재검수 → Runtime-only 실환경/Codex → QA 최종판정`

Developer 자체완료는 QA PASS가 아니다.
