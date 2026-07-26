# CPF Canonical Path and Role Map

## 목적

회사 Codex, 집 Codex, ChatGPT가 Root 정리 이후 정본을 찾지 못해 과거 경로를 복구하는 일을 막는다.

## Canonical 위치

| 역할 | 정본 위치 | 비고 |
|---|---|---|
| 제품 README | `README.md` | Root 유일 문서 |
| 최종 목표 WIP 정본 | `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md` | 최종 제품화 시 영구 Spec에 흡수 후 제거 대상 |
| Requirement 연속성 | `cpf-docs/governance/CPF_REQUIREMENT_CONTINUITY_LEDGER.md` | ID 삭제/통합 추적 |
| 현재 전체 작업 요청 | `cpf-docs/work/current/CPF_CURRENT_WORK_REQUEST.md` | Codex/ChatGPT 첫 실행 문서 |
| Continuity State | `cpf-docs/work/state/CPF_CODEX_CONTINUITY_STATE.md` | PC/세션 인수인계 |
| Decision Log | `cpf-docs/work/state/CPF_CODEX_DECISION_LOG.md` | 장기 Architecture 결정 |
| Gap/Stabilization | `cpf-docs/work/state/` | 작업 중 상태 정본 |
| Review/Change Handover | `cpf-docs/work/review/<date_seq>/` | 현재 작업 검수 기록 |
| Evidence Index | `cpf-docs/evidence/CPF_EVIDENCE_INDEX.md` | 실제 실행 Evidence 인덱스 |
| 개발/Generator 가이드 | `cpf-docs/development/` | 개발자용 |
| 운영/설치/복구 가이드 | `cpf-docs/operations/` | 운영자용 |
| Architecture/Specification | `cpf-docs/architecture/` | 영구 제품 구조/계약 |
| 제품 Tool/Shell | `cpf-tools/scripts/` | Root `scripts/`를 사용하지 않음 |
| Gradle Convention Plugin | `cpf-tools/build/gradle-plugin/` | 제품 Runtime과 분리된 추적 대상 격리 Build |
| Platform BOM | `cpf-tools/build/platform-bom/` | 제품 Runtime과 분리된 추적 대상 격리 Build |
| DB Source SSOT | `cpf-tools/db/vendor/<vendor>/source/` | Vendor Pack 경계 안의 사람이 수정하는 split SQL/metadata |
| DB Vendor Pack | `cpf-tools/db/vendor/<vendor>/` | Source·Lifecycle·Runtime·Template을 함께 소유하는 배포/Runtime 선택 Pack |
| Generated Domain DB Template | `cpf-tools/db/vendor/<vendor>/domain-template/` | 임의 Domain 생성 정본 |

## 이전 경로 → 현재 경로

| 이전 | 현재 | 처리 |
|---|---|---|
| `cpf-docs/work/current/CPF_CURRENT_WORK_REQUEST.md` | `cpf-docs/work/current/CPF_CURRENT_WORK_REQUEST.md` | 이동 |
| `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md` | `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md` | 이동 |
| `cpf-docs/work/state/CPF_GAP_MATRIX.md` | `cpf-docs/work/state/CPF_GAP_MATRIX.md` | 이동 |
| `cpf-docs/work/state/CPF_STABILIZATION_REPORT.md` | `cpf-docs/work/state/CPF_STABILIZATION_REPORT.md` | 이동 |
| `cpf-docs/governance/CPF_REVIEW_PROGRESS_COMPLETION_GUIDE.md` | `cpf-docs/work/state/CPF_REVIEW_PROGRESS_COMPLETION_GUIDE.md` | 이동/최종 흡수 |
| `cpf-docs/evidence/CPF_EVIDENCE_INDEX.md` | `cpf-docs/evidence/CPF_EVIDENCE_INDEX.md` | 이동 |
| Root `scripts/*` | `cpf-tools/scripts/*` | Merge 후 Root scripts 삭제 |
| Root `cpf-gradle-plugin/*` | `cpf-tools/build/gradle-plugin/*` | Build Tooling Owner로 이동 |
| Root `cpf-platform-bom/*` | `cpf-tools/build/platform-bom/*` | Build Tooling Owner로 이동 |
| `cpf-tools/db/source/mariadb/*` | `cpf-tools/db/vendor/mariadb/source/*` | 중앙 Vendor Pack 경계의 DB Source SSOT로 이동 |

## DB 수정 절차

다음 순서를 거꾸로 하지 않는다.

```text
Requirement / Data Model
→ Canonical Schema / Metadata
→ Generator / Domain Template
→ cpf-tools/db/vendor/<vendor>/source
→ build-all-install-sql
→ cpf-tools/db/vendor/<vendor>
→ migration/rollback
→ Mapper/Repository
→ Service/API/UI
→ Test/Runtime/Evidence
```

Generated Domain DB는 별도로 `domain-template`이 정본이다.

## Codex/ChatGPT 시작 순서

```text
git status
→ HEAD/origin/master
→ Final Target
→ Requirement Continuity Ledger
→ Current Work Request
→ Decision Log
→ Continuity State
→ Path/Role Map
→ 최신 Review/Handover
→ Source/Diff/Evidence
```

옛 Root 경로가 없다는 이유로 파일을 새로 만들거나 복구하지 않는다.
