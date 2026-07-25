# CPF R12 Implementation Report — 2026-07-26

## 기준
Start master `90130d3f34a8483718b4222b57b3618e8fffc919`. Commit/Push는 사용자 승인 전 수행하지 않았다.

## QA 11개 결과
|QA|원인/최신 Git 판정|R12 구현|상태|
|---|---|---|---|
|1 Audit swallow|`AdmAuditLogService` insert/query 장애 은폐|Owner 작업 전 durable reservation, REQUESTED/SUCCEEDED/FAILED/UNKNOWN, PENDING/RETRY/FAILED/DELIVERED, relay/retry, ADM 조회/수동 재처리|구현 완료 / Runtime 미검증|
|2 Calendar 권한/삭제/Audit|UI가 writable+권한을 함께 보장하지 못하고 감사 사유 분리 부족|WRITE/DELETE fail-closed, delete UX, 업무사유/auditReason 분리, durable audit 연결|구현 완료 / Browser 미검증|
|3 DB-less Calendar|cmnDataSource/JDBC Store 기본 강제와 Library mode 충돌|`cpf.common.runtime-mode`; product cmnDB+actor-aware writable 필수, library/edu/test/local read-only fallback|구현 완료 / Boot 미검증|
|4 Calendar Race/actor|SELECT→INSERT race, actor 미기록|direct INSERT+duplicate CREATE_CONFLICT, version/delete CAS, created_by/updated_by 실제 actor|구현 완료 / DB concurrency 미검증|
|5 BAT 조회 오류 은폐|`queryOrEmpty`, 상세 Map.empty|DB/SQL 실패 `BatOperationQueryException`, transactionId/operation log|구현 완료 / DB-down 미검증|
|6 Ghost Lock/Transaction|`OR ? IS NULL`, terminal overwrite, partial commit|BAT tx, terminal CAS, Runtime 동일 lockKey hash, owner exact match, null owner fail-closed|구현 완료 / race 미검증|
|7 Batch requestUser|Body/fallback actor 신뢰|Batch DTO actor 삭제, Controller verified actor, BAT port actor required, ADM global query/body mismatch guard|구현 완료 / Security E2E 미검증|
|8 Generated boundary|R11에서 이미 `core.common` 제거|보호 Gate 유지, stale duplicate generator 제거, canonical launcher|R11 구현 보호 / lifecycle 미검증|
|9 Memory fake runtime|fake CRUD가 제품 선택 가능|memory adapter local/test/edu + non-product only|구현 완료 / generated boot 미검증|
|10 Verification/checksum|verify가 sync/checksum 정본을 변경, parity 호출 계약 오류|Full Verify read-only, checksum check/update 분리, worktree immutability, 잘못된 generic parity 제거|구현 완료 / pwsh 미검증|
|11 cpf-common 정책|Generator 기본 dependency 누락/DB-less 정책 불명|Generated Domain `cpf-common` dependency, product CMN DB policy, Guide/README 정합화|구현 완료 / full build 미검증|

## 요청서 외 추가 결함 및 보완
- 삭제된 ADM frontend `features/access|observability|platform|reference` methods import 잔존: methods를 `app/methods`로 이관하고 mixin import 수정.
- `permission()` fallback이 read/write/delete true인 fail-open: 모두 false로 변경.
- `cpf-tools/scripts/create-domain.ps1`에 pre-R11 Generator Template 중복: canonical launcher로 축소.
- `verify-full-product.ps1`의 필수 파라미터 없는 generic `check-generated-domain-parity.ps1` 호출 제거.
- MariaDB source checksum 정본이 V41에서 멈추고 lifecycle이 V39에서 멈춘 drift: V40~V51 동기화.
- PRE-GA canonical repair: source V6/V29와 lifecycle 불일치 정리. obsolete lifecycle V6는 Push 전에 cleanup script로 정확히 삭제.
- ADM mutation requestUser legacy surface는 RequestBodyAdvice/query guard로 인증 actor와 불일치 시 전역 거부.

## DB/SQL
MariaDB canonical source, install, seed, migration V51, rollback R51, lifecycle migration/checksum을 반영했다. MySQL/PostgreSQL/Oracle/SQLServer는 현재 manifest상 `미구현`이므로 MariaDB SQL 복사/치환으로 거짓 완료하지 않았다.

## 삭제
정확히 1개: `cpf-tools/db/vendor/mariadb/migration/flyway/V6__bizadm_exs_transaction_identity.sql`.
ZIP overlay는 삭제를 표현하지 못하므로 `cleanup-r12-obsolete.ps1`로 사용자 Push 전에 제거한다.

## 완료 판정
R12 Source 구현 backlog: 알려진 직접 잔존 없음. Release/전체 CPF 완료 판정: **미검증**. Full Product Runtime Evidence 후에만 완료 가능.
