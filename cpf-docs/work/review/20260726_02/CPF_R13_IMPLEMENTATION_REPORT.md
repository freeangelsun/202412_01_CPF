# CPF R13 Implementation Report — QA Product Quality Hardening

## 1. Basis

- Base: `master@9b12ba025a0c6f2df59589681a862959232be16f`
- Date: 2026-07-26
- Target: `CPF_FINAL_TARGET_REQUIREMENTS.md`
- Input: QA 현재 결함 10건 + 제품 기능 Gap 5건
- Git mutation: 없음

## 2. QA 재검증 및 구현 상태

| QA | 주제 | R13 Source 판정 | 구현 결과 |
|---|---|---|---|
| 1 | Generator version path | 완료 | Gradle canonical generator 사용, old path는 thin launcher만 허용 |
| 2 | Manifest/SBOM/Provenance | 부분 구현 | 서로 다른 schema/payload/artifact hash/dependency/provenance validator. License 확정과 승인 서명/attestation 미완료 |
| 3 | ADM/BAT Health | 완료 | liveness/readiness, required dependency DOWN=503, runtime harness readiness probe |
| 4 | ADM raw log 노출 | 완료 | explicit column, raw details 제거, Backend recursive masking |
| 5 | MBR DB error→NotFound | 완료 | EmptyResult만 NotFound, DB/SQL/무결성 오류 분리 |
| 6 | 분산 회원번호 | 완료 | DB AUTO_INCREMENT sequence, unique, issue history, Owner query와 ADM 운영조회 API |
| 7 | 회원/권한 optimistic lock | 완료 | member/status/role version CAS, expectedVersion, role idempotency |
| 8 | 회원 상세 부분장애 | 부분 구현 | API section AVAILABLE/FAILED 구현. 기존 Frontend에 독립 member 화면이 확인되지 않아 Browser 표현은 미검증 |
| 9 | Response Code Cache | 완료 | ResponseCode 직접 수정 + 동일원인 Code/Message/Config 정리. explicit snapshot, distinct key, commit-after replace, refresh event REQUIRES_NEW, bounded retry/status/consumer retry |
| 10 | CSV Formula Injection | 완료 | typed sanitizer, whitespace/control 우회 차단, header 보호, policy audit |
| 11 | OpenTelemetry | 부분 구현 | CPF API + optional OTLP trace adapter + Online transaction consumer. remote/message/batch/metric/log/Admin exporter 상태 미완료 |
| 12 | Feature Flag | 부분 구현 | safe-default, kill switch, targeting, percentage, Provider SPI. 대형 A/B/ADM control plane 제외 |
| 13 | Contract Compatibility | 부분 구현 | REST/Shared/Event/Fixed/File/Batch schema/breaking engine/self-test/Gradle gate. Registry/env matrix 미완료 |
| 14 | Fault Injection | 부분 구현 | test/verification profile, allowlist, bounded delay/failure transaction boundary. 상세 fault harness 미완료 |
| 15 | Golden Path/Portal | 완료(합의 범위) | canonical Generator DryRun/conflict/ownership/result gate. 별도 Portal은 의도적으로 제외 |

`완료`는 Source 구조가 목표 계약을 구현했다는 판정이다. Build/DB/Runtime/Browser/Multi-instance Evidence가 필요한 항목은 실행 결과가 별도로 `미검증`이다.

## 3. 주요 변경

### Root/Release
- `build.gradle`: canonical version gate, distinct release metadata, validator, Contract/R13 gate, publication gate
- `cpf-tools/release/schema/*`: 3개 독립 schema

### `cpf-admin`
- Health readiness 503
- Log backend sanitizer/raw 제거
- CSV sanitizer/audit policy
- Member expectedVersion/idempotency/partial-read + member-number issue history API
- Cache operation: verified actor, public core API, 503, cache/event producer/consumer 상태

### `cpf-member`
- precise DB error classification
- distributed member number allocation + issue history query
- optimistic CAS + role idempotency
- runtime/schema column drift repair

### `cpf-common`
- Code/Message/ResponseCode/Config cache의 explicit snapshot/commit-safe refresh
- Response Code `ALL`/`CODE:` key 분리 및 startup preload 실적재
- `CacheRefreshEventStore`: 실제 `REQUIRES_NEW`
- `CacheRefreshEventPublisher`: bounded retry/운영상태
- `CacheRefreshEventListener`: 실패 event ID 미진행/다음 poll 재처리 + 운영상태

### `cpf-core`
- public `CpfErrorCode.CONFLICT`
- public `CpfLogPaths` facade로 BAT의 core internal dependency 제거
- observability / feature flag / fault injection API/SPI/runtime

### DB/Tools
- V52/R52 + canonical/install/checksum
- Contract compatibility gate
- Generator Golden Path gate
- R13 static/verification runner

## 4. 추가 발견 Gap 처리

QA 숫자를 늘리지 않고 동일 원인/Owner 범위로 함께 처리했다.

- MBR role Runtime SQL이 baseline과 다른 `role_type`, `grant_reason` 사용
- login history Runtime column drift
- role history 필수 `service_code`/`operator_id` 누락
- canonical/fresh install MBR 운영 schema 누락
- BAT Health의 `core.common.*` 직접 의존
- ADM Cache API의 `core.common.*`, requestUser fallback, raw DB cause 노출
- ResponseCode와 같은 self-invocation/clear-before-read 패턴이 Message/Config에도 존재

## 5. Security/Failure 정책

- 민감정보는 Frontend가 아니라 Backend 응답 경계에서 제거/마스킹한다.
- CSV formula 방어는 실제 String/Header에 적용하고 numeric/boolean 의미는 보존한다.
- liveness와 readiness를 분리한다.
- DB 장애를 NotFound/0건으로 위장하지 않는다.
- cache DB read 실패 시 기존 cache를 먼저 삭제하지 않는다.
- 업무 rollback 시 cache/event가 변경되지 않도록 commit 이후 동기화한다.
- cache event 저장 장애는 업무를 오염시키지 않되 retry/drop/consumer 상태가 운영에서 보인다.
- Feature Flag provider 실패는 caller safe default로 닫는다.
- Telemetry export/instrumentation 실패는 업무를 불필요하게 실패시키지 않는다.
- Fault Injection은 production 기본 비활성이다.

## 6. 남은 검증/완료 금지

전체 Gradle, MariaDB lifecycle, multi-instance, Browser/Spreadsheet, OTel Collector, external Flag Provider, real Contract Registry, detailed Fault scenarios는 이 작업환경에서 실행하지 못했다. `CPF_NEXT_WORK_REQUEST.md`에서 최신 적용 SHA 기준으로 검증하고 실패 시 Source/SQL/Test/Guide/Evidence를 다시 수정한다.
