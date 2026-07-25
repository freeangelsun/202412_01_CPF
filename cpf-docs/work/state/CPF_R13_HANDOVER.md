# CPF R13 Handover — 2026-07-26

## 1. 기준

- 시작 master: `9b12ba025a0c6f2df59589681a862959232be16f`
- 산출물: `CPF_20260726_R13_ROOT_OVERLAY.zip`
- Git commit/push/branch: 수행하지 않음
- 적용 후 SHA: `PENDING_USER_PUSH`

## 2. 핵심 구현

### QA 1~10
- canonical Generator version gate
- distinct Release Manifest/SBOM/Provenance + schema/hash validator
- ADM/BAT liveness/readiness + 503 + runtime readiness probe
- ADM log Backend raw 제거/masking
- MBR NotFound/DB error 분리
- DB sequence memberNo + AUTO/MANUAL issue history + Owner/ADM 조회
- member/status/role version CAS + role idempotency
- ADM member detail partial read state
- Code/Message/ResponseCode/Config cache commit-safe snapshot, distinct keys, DB-read-first, event REQUIRES_NEW, bounded retry, publisher/listener status
- CSV formula injection protection + audit policy version

### QA 밖 동일 원인 보정
- MBR Runtime SQL vs DB baseline role/login/history drift
- canonical/fresh install MBR operational schema drift
- BAT Health와 ADM Cache API의 core internal import/actor fallback 회귀 제거
- BAT가 사용할 공개 `CpfLogPaths` facade 추가

### 제한형 제품 기반
- vendor-neutral Telemetry API + optional OTel OTLP trace adapter
- Feature Flag API/SPI + safe-default/property provider
- Contract compatibility engine/schema/Gradle gate
- controlled Fault Injection boundary
- Generator Golden Path gate, Portal 미생성

## 3. Architecture 결정

- 외부 표준 SDK 타입을 CPF Public API로 노출하지 않고 CPF API/SPI 뒤 adapter로 둔다.
- Feature Flag는 kill switch/safe default/targeting 중심이며 대형 A/B 제품을 만들지 않는다.
- Fault Injection은 test/verification 우선, production 기본 비활성이다.
- Developer Portal 신규 Module을 만들지 않는다. canonical CLI Generator가 정본이다.
- 승인/사유/감사가 완성되지 않은 raw log endpoint는 만들지 않는다.
- Cache mutation은 업무 Transaction 내부에서 cache를 먼저 clear하지 않는다. DB snapshot은 transaction 안에서 준비하고 local cache 변경/refresh event는 commit 이후 수행한다.
- Cache event DB write는 별도 Bean의 `REQUIRES_NEW`로 실제 proxy 경계를 보장한다. DB 일시 장애는 bounded in-memory retry와 periodic reconciliation으로 복구하며 process crash+DB down 조합은 통합검증에서 잔존 위험을 재평가한다.

## 4. DB 변경

- `V52__qa_product_quality_hardening.sql`
- `R52__qa_product_quality_hardening.sql`
- `30_adm_schema.sql`, `40_business_modules_schema.sql`, `00_empty_install.sql`
- source/runtime checksum manifest

R52는 R13 신규 sequence/issue/idempotency table 및 CSV policy column을 제거한다. `version_no`, `role_type`, `grant_reason`은 기존 개발 DB drift와 backward compatibility를 고려해 destructive rollback에서 의도적으로 유지한다.

## 5. 검증 상태

### 직접 수행한 정적 검증
- V52 source/runtime byte parity + SHA-256/checksum manifest
- Release/Contract JSON schema parse
- QA 핵심 required/forbidden marker
- 신규 credential-like literal scan
- overlay Java parser-level syntax scan
- external module 변경 파일의 신규 `core.common.*` 의존 회귀 검사

### 미검증
- 전체 Repository Gradle compile/test/qualityGate
- PowerShell Gate 실제 실행 (`pwsh` 없음)
- MariaDB V52 실제 lifecycle
- ADM/BAT 503 Runtime/LB
- MBR multi-instance concurrency
- Browser/Spreadsheet
- Cache multi-instance/process-restart
- OTel Collector/exporter-down
- external Feature Flag Provider
- real Contract Registry/can-deploy
- 상세 Fault scenario

미검증 결과는 다음 작업자가 성공으로 승계하지 않는다.

## 6. 다음 작업자 주의

R11/R12 public boundary, BAT ownership, durable audit, verified actor, Calendar conflict, Generator parity, checksum/read-only verification을 회귀시키지 않는다. 특히 MBR는 Golden Generated Domain 역할도 있으므로 R13의 회원 운영 보강이 Generator 표준과 충돌하지 않는지 lifecycle 검증에서 확인한다.
