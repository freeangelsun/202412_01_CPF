# CPF 개발 정본 Current-only 현행화 리뷰

## 1. 목적

현재 Source를 정본에 맞춘 것이 아니라 CPF가 상용 Business Platform Framework로 도달해야 하는 Current Target을 유지한다. Source와 Target 차이는 `CANONICAL_SOURCE_GAP_BACKLOG.csv`로 분리하며, 문서 정리 때문에 Requirement 의미를 축소하지 않는다.

## 2. 이번 Governance 최적화의 문제

현행화 후에도 다음과 같은 **복수 Current Truth**가 남아 있었다.

- Final Target은 Current Evidence를 `cpf-docs/work/*`로 가리키고 Document Index/Path Map은 `cpf-docs/deliverables/*`를 가리킴
- `cpf-docs/work/CPF_CURRENT_WORK_REQUEST.md`와 `work/current/CPF_CURRENT_WORK_REQUEST.md`가 서로 다른 개발 상태를 설명
- Evidence/Open Issues/QA Rework/Change Manifest/Package Manifest가 `work/`와 `deliverables/`에 서로 다른 내용으로 공존
- `work/HANDOVER.md`와 `work/current/CPF_DEVELOPMENT_HANDOVER.md`가 과거 Source identity와 과거 PASS를 반복
- `STEERING_INTERPRETATION.md`가 Final Target에 이미 흡수된 정책을 다시 별도 정본처럼 설명

이 상태에서는 GPT/Codex/QA가 읽는 경로에 따라 개발 필요/완료 판정이 갈릴 수 있으므로 Current-only 원칙에 맞지 않는다.

## 3. 최적화 결정

### 최상위 정본

- Target/Architecture/Requirement: `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
- Navigation: `CPF_CANONICAL_PATH_AND_ROLE_MAP.md`, `CPF_DOCUMENT_CANONICAL_INDEX.md`
- Current development request: `cpf-docs/work/current/CPF_CURRENT_WORK_REQUEST.md`
- Requirement status: `cpf-docs/work/REQUIREMENT_STATUS.csv`

### Current Deliverables

`cpf-docs/deliverables/`를 다음 역할의 단일 위치로 고정한다.

- `TEST_AND_EVIDENCE.md`
- `OPEN_ISSUES.md`
- `QA_REWORK_REQUEST.md`
- `CHANGE_MANIFEST.csv`
- `PACKAGE_MANIFEST.json`
- `DELETE_MANIFEST.csv`

### Current Handover

`cpf-docs/work/current/CPF_DEVELOPMENT_HANDOVER.md` 하나만 현재 세션/PC 인수인계로 사용한다. Handover는 정책 정본이 아니라 Source identity, 실제 검증 결과, 미완료 조건, 다음 명령을 전달한다.

## 4. Steering 의미 보존 검산

삭제 대상 `STEERING_INTERPRETATION.md`의 주요 판단을 Final Target과 대조했다. System6/Receiver trust, BZA/Backoffice ownership, DB-less Web/BFF, Optionality, instanceId, Source structure quality, EDU 20+15, Public default-deny, 과거 Evidence 비승계는 이미 Final Target에 존재한다.

삭제 전 다음 두 표현은 Final Target에 추가해 의미 손실을 막았다.

1. `operationId`는 안정적인 Handler/Registry/OpenAPI 계약 ID이고 `executionId`는 실행 건 ID이며 서로 대체하지 않는다.
2. Gateway와 허용 Direct Public HTTP는 동일 보안/정책/감사/System6 계약을 지키고 Gateway→Direct 자동 fallback은 금지한다.

## 5. 이번 물리 삭제 대상

- `cpf-docs/work/CPF_CURRENT_WORK_REQUEST.md` — 현재 요청은 `work/current/`가 유일 Owner
- `cpf-docs/work/TEST_AND_EVIDENCE.md` — Current Evidence는 deliverables가 유일 Owner
- `cpf-docs/work/OPEN_ISSUES.md` — Current Open Issues는 deliverables가 유일 Owner
- `cpf-docs/work/QA_REWORK_REQUEST.md` — Current QA Rework는 deliverables가 유일 Owner
- `cpf-docs/work/CHANGE_MANIFEST.csv` — Current Change Manifest는 deliverables가 유일 Owner
- `cpf-docs/work/PACKAGE_MANIFEST.json` — Current Package Manifest는 deliverables가 유일 Owner
- `cpf-docs/work/HANDOVER.md` — 과거 baseline/PASS를 반복하는 중복 Handover
- `cpf-docs/work/current/STEERING_INTERPRETATION.md` — 의미 흡수 완료 후 경쟁 정본 제거

보호된 `cpf-docs/deliverables/**`의 Current 산출물은 삭제하지 않는다. 기존 `DELETE_MANIFEST.csv`의 다른 historical/source 후보는 이번 요청 범위에서 자동 일괄 삭제하지 않는다.

## 6. 최신 Source/검증 상태

- Current local source ZIP SHA-256: `f73988097aef77a1bcc795ba66394326dd5a9f875a2d1b530e2c99e315cf5ceb`
- Files: `8,288`
- 최신 사용자 로컬 Gradle integration: **FAIL / 9 failed tasks / BUILD FAILED in 7m 22s**

따라서 문서 정본 구조가 정리되더라도 제품 전체 완료나 Runtime PASS를 의미하지 않는다. 상세 실패는 `TEST_AND_EVIDENCE.md`와 `OPEN_ISSUES.md`를 따른다.

## 7. Closure 판정

이번 Overlay 범위의 목표는 **Current 역할 충돌 제거**다. 더 넓은 historical document/source cleanup은 `CANON-GAP-010`과 canonical Delete Manifest에서 별도 검증 후 진행한다. 즉 이번 정리로 문서 구조를 False Green으로 `전체 history 0`이라고 표현하지 않는다.
