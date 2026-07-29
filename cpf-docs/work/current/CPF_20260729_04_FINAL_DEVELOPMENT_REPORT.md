# CPF 20260729_04 최종 개발 산출물 보고서

## 1. 기준

- Repository: `https://github.com/freeangelsun/202412_01_CPF`
- Branch: `master`
- 개발 기준 SHA: `b8941577b99535ff3e64a4fad99b74bafa544227` (`20260728_07`)
- 최상위 정본: `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
- Canonical Requirement: 162개
- Enterprise QA Requirement: 816개
- 최종 QA Scenario: 387개
- Git Commit/Push/Branch/Tag/PR: 수행하지 않음

## 2. 완료한 Source 개발 묶음

### BZA Tree와 Action Permission

- 조직·메뉴 무제한 재귀 Tree, 검색, 고아·순환 탐지
- 권한 Simulation의 Raw JSON 기본화면 제거
- Frontend Button과 Backend Filter의 단일 Manifest 사용
- 일반 WRITE/UPDATE/DELETE와 PII_RAW/SIMULATE/DECIDE/DOWNLOAD 위험 Action 분리
- 인증·세션·로그인 이력·비밀번호 변경 응답 Typed DTO 전환

### Redis/Cache Runtime

- `cpf-core` Cache/Invalidation/Distributed Lock API·SPI
- Local/Redis Provider와 운영 Profile Local 차단
- Standalone/Sentinel/Cluster 설정, TLS·Secret Reference Guard
- Cache Aside, Negative Cache, Single-flight, TTL, Namespace Eviction
- Lock 획득과 Fencing Token의 Lua 원자 처리
- DB Durable Invalidation 원장, Redis Fast Channel, Checkpoint Reconcile
- ADM Provider 상태·지표·Evict·Reconcile 운영 API/UI

### Streaming File Job

- Provider-neutral Tabular API
- CSV/XLSX Streaming Adapter
- Formula Injection, Macro, Zip Bomb, 행·셀 상한, SHA-256
- Download/Upload Job, Template, Dry-run, 행별 결과, Retry/Cancel/Rollback
- Notification Rule 실제 Import Consumer
- Lease/Fencing, Idempotency, Retention Cleanup
- Oracle/PostgreSQL/MariaDB V69/R69

### Runtime Control과 Notification

- Runtime Status/Target Preview/Change Diff/Error/Target Result Typed 계약
- Runtime Payload canonical JSON·schema/hash 계약과 내부 Parser 분리
- Notification 발송상태·Retry·Cancel Typed 계약
- Notification Action별 Frontend/Backend/DB 권한

### Generator와 Local Runtime

- Golden Template 고정 `.reference` 제거
- Typed Query/Command Port 분리
- Typed Controller/Facade/Service/Remote/Local/Memory Adapter
- Memory CRUD, Version 충돌, CREATE/UPDATE/DELETE Request Hash 멱등성, 운영 Profile Fake 차단
- 업무 Row의 마지막 멱등 Key와 분리된 Generated Domain 멱등 원장 및 재호출·충돌 처리
- 3개 Vendor Typed MyBatis Mapper, Count Query, Install/Migration/Rollback/Verify 멱등 원장 parity
- Local Web/Batch Profile, Domain 조립, 운영/원격 Bind Guard
- Local Runtime 물리 Source를 `cpf-tools/runtime/*`로 이관하고 Gradle 논리 Project 이름은 유지; Root `deploy`는 제품 배포 자산 정본으로 유지

### DB·Gate·Hygiene

- 공식 Vendor Oracle/PostgreSQL/MariaDB만 유지
- V69/V70/V71/V72 Migration/Rollback/권한·승인 통제 parity
- Canonical BZA Seed와 Vendor SQL 동기화
- Public Raw Map, Core Internal Import, Direct Client, Secret Literal, Vendor Drift Gate
- 루트의 `cpf-gradle-plugins`, `cpf-platform-bom` 금지; 정식 Owner는 `cpf-tools/build/*`

## 3. 직접 실행한 검증

- `check_final_source_closure.py`: 31 PASS / 0 FAIL
- Generator 멱등 Template Gate: 3 Vendor PASS
- Generator 핵심 Java Template Stub Compile: 33 Source PASS (`--release 21`)
- Frontend TypeScript/Vue 구문 검사: 20 files / 0 error
- Java 전체 변경 Source Parse-only 구문 검사: 127 files / 0 error
- Java 변경 Source Package/경로/중복 Type/금지 Import 검사: PASS
- JSON/CSV 구조 검사: PASS
- V69/V70/V71/V72 Vendor parity와 SHA-256 검사: PASS
- Root Overlay Hygiene: PASS
- Java 21 부분 컴파일: Runtime Control/Cache/Tabular Public API, Local Cache/Cache Aside, ADM File Job Repository·Service·Controller·Consumer PASS
- Cache TTL/Lock wait/Fencing/Loader failure Smoke: PASS
- CSV BOM Round-trip/Formula Injection/SHA-256 Smoke: PASS

실행 환경은 Java 21, Node 22.16.0, npm 10.9.2였다. Java 25, Gradle 9.1, PowerShell, 3개 DB, Redis/Sentinel/Cluster, Browser와 다중 인스턴스 환경은 이 실행 환경에 없어 성공으로 기록하지 않았다.

## 4. 완료 판정의 범위

- Source/SQL/API/Test/Frontend/Script/Guide 개발 상태: **완료**
- 최신 master exact-SHA 통합 실행 검증: **미검증**
- QA 387개 Scenario의 실행 결과: **미검증**
- Codex 역할: 구현이 아니라 검수와 결함 반환만 수행

`미검증`은 구현을 Codex에 넘겼다는 뜻이 아니다. 현재 산출물을 사용자 Local 최신 master에 적용한 뒤 Codex가 exact SHA에서 실행하고, 실패가 나오면 Source 수정은 다시 ChatGPT 개발 세션이 담당한다.

Closure CSV의 `baseline_status`는 첨부 QA 원장의 작업 시작 전 상태를 보존한 이력 열이다. 현재 개발 판정은 `development_status=완료` 열을 사용하며, 실행 판정은 `verification_status` 또는 `execution_status`를 사용한다.

## 5. 정본 산출물

- `cpf-docs/quality/CPF_FINAL_TARGET_162_TRACEABILITY_20260729_04.csv`
- `cpf-docs/quality/qa-20260729/CPF_ENTERPRISE_REQA_816_DEVELOPMENT_CLOSURE_20260729_04.csv`
- `cpf-docs/quality/qa-20260729/CPF_QA_387_FINAL_VALIDATION_MATRIX_20260729_04.csv`
- `cpf-docs/quality/qa-20260729/CPF_SOURCE_VERIFIED_KNOWN_GAPS_CLOSURE_20260729_04.csv`
- `cpf-docs/guides/CPF_20260729_04_FINAL_APPLY_AND_VALIDATION_GUIDE.md`
- `cpf-docs/work/current/CPF_20260729_04_CODEX_FINAL_REVIEW_REQUEST.md`
- `cpf-docs/work/current/CPF_20260729_04_FINAL_HANDOVER.md`
