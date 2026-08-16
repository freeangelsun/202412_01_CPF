# CPF 개발 GPT Test & Evidence

## 기준 Source

- 입력: `CPF_FULL_SOURCE_FOR_NEXT_QA(20260816-061343).zip`
- 입력 FullLocal: `CPF_LOCAL_VALIDATION_20260816_132902.zip`
- 결과 Content SHA-1: `6ce96c49fbfca3b26ab172187ac06fe279e09040`
- 결과 Content SHA-256: `91a58a0a50abbba56f75c5ba5f4aa5cf84965353a54cbb586bca38177ba09eea`
- 사용자 승인 없는 Git write/delete/history rewrite: 수행하지 않음
- 제품 Source 삭제 대상: 0

## 입력 FullLocal 분석

- 전체 135
- PASS 102
- FAIL 30
- SKIP_ENV 3
- FAIL은 Build graph, package contract, stale evidence, Windows Python, DB lifecycle stale test, Frontend/generated client, Performance orchestration 7개 Root Cause로 묶어 재개발했다.

## 현재 Assistant 재검수

| 범위 | 결과 | 판정 |
|---|---:|---|
| QA-B3 dedicated current-source | 22 PASS / 3 미완료 | QA-B3-008/010/011 유지 |
| Verification tests | 45/45 PASS | PASS |
| Testing Tools | 80/80 test files PASS / FAIL 0 | PASS |
| NXT3 | 22/22 individual gates PASS | PASS; Windows aggregate 재검수 |
| Runtime tool tests | 65 PASS / 2 SKIP / 7 subtests PASS | 실행 가능 범위 PASS |
| Generator verification | 27 PASS / 10 SKIP / 6 subtests PASS | 실행 가능 범위 PASS |
| DB verification | 75/75 PASS | PASS |
| Batch targeted | 6/6 Gate PASS | PASS |
| Starter Catalog | 64 modules, public 24/internal 40 | PASS |
| Public Function Catalog | 100 rows, Golden 20 | PASS |
| Batch Developer Catalog | 50/50 Public Batch API | PASS |
| ADM OpenAPI static | 321 operations / Orval 145 mutations / consumer 321 | PASS |
| BZA OpenAPI static | 96 operations / Orval 47 mutations / consumer gate | PASS |
| Upgrade snapshot | Public API 263 / Public Starter 24 | PASS |
| Performance contract | broker/batch/resource 3개 selected dry-run + specialized self-test | PASS; live HTTP workload 미검증 |
| Source identity | 9,206 files / 109,812,481 bytes / Git-independent digest | PASS |

## 이번 재개발 핵심

- 4개 Internal JDBC module의 same `group + leaf(jdbc)` 자기 component 충돌 제거 및 회귀 Gate 추가.
- Local Runtime의 domain `packageName` 과도한 필수 계약 보정.
- DB lifecycle V118 기준 stale test currentization.
- Windows timezone/encoding 및 Source identity에서 `bin/.vscode/build/cache` false drift 제거.
- Query DB3 scanner가 transient tree에 내려가기 전 prune하도록 수정.
- BZA Orval pre-runtime sync/normalize 순서 복구, request boundary와 generated model currentization.
- ADM/BZA generated client 검증에서 Git HEAD fallback 제거, Git-independent source-state 사용.
- FullLocal에서 실패 downstream을 SKIP_ENV로 오표기하지 않고 NOT_EXECUTED로 분리하며 1-WAS가 살아 있을 때 Logging/Performance를 실행하도록 순서 보정.
- FileLog/DB Log/ADM Timeline 같은 transactionId/traceId 실거래 correlation smoke 추가.
- `cpfVerifyTargeted` 및 Developer/Adoption 10개 REWORK, Public TOP100/Golden20, Starter Quick Select, Upgrade impact gate 구현.
- 배치 교육용 Source-backed `CPF_BATCH_DEVELOPER_TOP_50.md`와 자동 Gate 추가.

## 현재 환경에서 완료 판정하지 않은 검증

- Java25 Gradle clean build/test/publication/SBOM
- ADM/BZA npm ci 이후 lint/typecheck/unit/build 및 Browser E2E/A11y
- Local 1-WAS actual transaction + restart/shutdown
- FileLog ↔ DB Log ↔ ADM Timeline live correlation
- Oracle/PostgreSQL/MariaDB Live lifecycle
- Redis/Valkey reconnect/multi-instance
- Kafka restart/DLQ/outbox/inbox
- Batch 2-worker process kill/UNKNOWN/reconcile/fencing
- Gateway OFF/ON, separated topology, multi-instance
- Performance/Backpressure live workload
- Windows fresh extract Java25 lifecycle
- post-commit exact Git SHA와 QA 최종 통과

위 항목은 다음 Windows FullLocal에서 실행하며 실행 전에는 전체 완료로 판정하지 않는다.

## Fresh Apply / Package Integrity

- Root-relative Overlay Fresh Apply: PASS
- 결과 Source identity 재확인: SHA-1/SHA-256 일치
- 보호경로 4개 영역 Hash 불변: PASS
- Development Evidence Integrity: PASS
- 고의 corruption negative: PASS (manifest 대상 파일 변경 시 non-zero, `manifest size mismatch`)
- 원복 후 Integrity 재검증: PASS
- 이번 Overlay 삭제 대상: 0

상세 실행 요약은 `cpf-docs/work/evidence/current/developer-rework/QA12_REGRESSION_EXECUTION_SUMMARY.txt`, 패키지 무결성 근거는 `QA12_PACKAGE_INTEGRITY_EVIDENCE.txt`를 사용한다.
