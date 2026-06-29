# CPF CoreFlow Platform Framework

CPF는 Java 21, Spring Boot 3.4 기반의 업무 개발 표준 프레임워크입니다. 신규 업무가 거래 추적, 표준 응답, 예외 처리, 권한, 감사, 배치, 외부 연계, 공통 업무 기능을 매번 새로 만들지 않도록 공통 코드와 운영 기준을 제공합니다.

이 README는 짧은 진입점입니다. 상세 기준은 `specs` 아래 HTML 가이드가 정본입니다.

## 핵심 원칙

- `pfw`는 프레임워크 코어입니다. 거래 ID, 거래 헤더, 표준 응답, 예외 처리, 거래 로그, OpenAPI 공통 설정, WebClient, Spring Batch 공통 실행 API, 프레임워크 메시지/응답코드/설정 같은 CPF 자체 기능을 제공합니다.
- `cmn`은 프로젝트 공통 영역입니다. CPF를 적용하는 프로젝트에서 업무 공통 채번, 업무 알림, 업무 로그, 파일/전문/MQ 보조 기능을 프로젝트 판단에 맞게 확장합니다.
- `bat`는 배치 실행 worker입니다. ADM이 Job을 직접 실행하지 않고, BAT가 PFW Batch 공통 API와 Spring Batch JobRepository를 사용해 실제 Job/Step을 실행합니다.
- `adm`은 운영 콘솔입니다. 권한, 회원, 로그, 배치, 캐시, 메시지, 코드, 설정, 보안 운영을 조회하고 조치합니다.
- 기능을 추가하거나 변경하면 테스트, SQL, Swagger, EDU 샘플, README, HTML 가이드를 함께 최신화합니다.

## 모듈

| 모듈 | 책임 | 주요 구현 |
| --- | --- | --- |
| `pfw` | CPF 프레임워크 코어 | `TransactionIdGenerator`, `TransactionContextFilter`, `CpfGlobalExceptionHandler`, `LoggingAspect`, `CpfBatchLauncher`, OpenAPI 공통 설정 |
| `cmn` | 프로젝트 공통 기능 | `CmnSequenceService`, `CmnNotificationLogService`, `CmnBusinessLogService`, 파일 교환, 고정길이 전문, 메시지 브리지 |
| `bat` | 업무 배치 실행 worker | `BatApplication`, `BatSmokeJobConfig`, `BatHealthController`, `scripts/smoke-bat-runtime.ps1` |
| `adm` | 프레임워크 운영 관리자 | 운영자 권한, 회원 관리, 로그 관제, 배치 관제, 캐시, 메시지, 코드, 설정, 보안 운영 |
| `bizadm` | 업무 관리자 예제 | 업무 관리자 인증, 고객/상품/주문/설정 조회, 마스킹 해제 감사, 다운로드 정책 |
| `mbr` | 회원 업무 예제 | 회원 CRUD, 로그인, refresh token hash, 로그인 이력, 회원 상태/권한 연결 |
| `acc` | 계정 업무 예제 | 계정 조회 연계와 CPF 거래 로그 적용 예제 |
| `exs` | 외부 연계 예제 | 기관/채널/endpoint, token hash, 통제 정책, 송수신 로그, 재처리 요청 |
| `xyz` | EDU 교육 API | CRUD, 표준 응답/오류, 거래 헤더, CMN 공통 기능, 배치, 외부 호출, 전문, 파일, 보안 샘플 |

## DB 소유권

| DB | 소유 영역 | 대표 테이블 |
| --- | --- | --- |
| `pfwDB` | 프레임워크 코어와 운영 메타 | `pfw_transaction_log`, `pfw_transaction_meta`, `pfw_log_policy`, `pfw_message`, `pfw_response_code`, `pfw_config`, `BATCH_*`, `pfw_batch_*` |
| `cmnDB` | 프로젝트 업무 공통 | `cmn_sequence`, `cmn_sequence_issue_log`, `cmn_notification_log`, `cmn_business_log` |
| `admDB` | 운영자 권한과 감사 | `adm_operator`, `adm_role`, `adm_menu`, `adm_button`, `adm_audit_log`, `adm_download_audit_log` |
| `bizadmDB` | 업무 관리자 예제 | `bizadm_admin_user`, `bizadm_role`, `bizadm_customer`, `bizadm_product`, `bizadm_order` |
| `mbrDB` | 회원 업무 예제 | `mbr_member`, `mbr_member_role`, `mbr_member_login_history`, `mbr_refresh_token` |
| `exsDB` | 외부 연계 예제 | `exs_institution`, `exs_channel`, `exs_endpoint`, `exs_token_store`, `exs_transaction_log`, `exs_message_log`, `exs_retry_log` |

Spring Batch의 `BATCH_*` 테이블은 JobRepository 원천 실행 메타입니다. CPF의 `pfw_batch_*` 테이블은 ADM 관제, 수동 실행, 재수행, 중지, worker heartbeat, 진행률, ghost 조치, lock, 운영 이력을 위한 운영 메타입니다.

## 온라인/배치 균형 상태

온라인 API는 `@CpfTransaction`, `TransactionContextFilter`, 표준 거래 헤더, Swagger 공통 헤더, `pfw_transaction_log`/`pfw_transaction_log_detail` 기반 거래 로그 저장까지 구현되어 있습니다. 이번 기준부터 `pfw_transaction_meta`와 RequestMapping scan 기반 자동 upsert, ADM 거래 메타 조회/재스캔 API, `pfw_log_policy`/`pfw_log_policy_override`/`pfw_log_policy_audit` 기본 테이블, `LogPolicyResolver`/`LogPolicyCache` 기반 런타임 정책 적용, ADM 로그 정책 cache refresh/clear API, ADM 통합 추적 API(`/adm/api/observability`)와 정책 감사 조회 API(`/adm/api/log-policy-audits`)를 포함합니다. Redis/Kafka/MQ 기반 다중 인스턴스 실시간 전파는 다음 보강 대상입니다.

배치는 Spring Batch `BATCH_*`와 CPF `pfw_batch_*`를 함께 사용합니다. V10 기준 heartbeat/progress 컬럼은 로컬 MariaDB에 실제 적용했고, BAT runtime smoke에서 fallback 없이 `processed_count`, `progress_rate`, `last_heartbeat_at` 갱신을 확인했습니다.

## 기본 실행과 검증

```powershell
.\gradlew.bat compileJava --offline
.\gradlew.bat test --offline
.\gradlew.bat qualityGate --offline
.\gradlew.bat :bat:test --offline
.\gradlew.bat :bat:bootJar --offline
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/apply-pfw-runtime-migrations.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/apply-v13-adm-permission-seed.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-bat-runtime.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-adm-runtime.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-transaction-meta-runtime.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-log-policy-runtime.ps1
```

MariaDB 설치 검증은 `specs/sql/00_all_install_and_smoke.sql`로 수행합니다. 실행하지 않은 DB 검증은 성공으로 보고하지 않고 미검증으로 남깁니다.
이미 설치된 로컬 DB에 런타임 보강 migration만 보정해야 할 때는 `scripts/apply-pfw-runtime-migrations.ps1`로 PFW V11/V12를 적용하고, `scripts/apply-v13-adm-permission-seed.ps1`로 ADM 거래 메타와 로그 정책 운영 권한을 적용합니다. 두 스크립트는 재실행 idempotent 결과를 `build/runtime-smoke` 아래 JSON으로 남깁니다.

BAT runtime smoke는 정상 Job, heartbeat 진행률 Job, 실패 Job을 순서대로 실행해 Spring Batch JobRepository와 CPF 배치 메타 연결을 확인합니다. 기본 모드는 V10 확장 메트릭을 엄격히 요구하며, 과거 write_count fallback 확인은 `-AllowLegacyFallback`를 명시한 경우에만 허용합니다.
ADM runtime smoke는 거래 메타 scan, 목록, 상세 조회까지 확인하고, 로그 정책 runtime smoke는 `dbLogEnabled=N` override 적용 시 거래 로그 DB 저장이 멈추고 해제 후 다시 저장되는지 확인합니다. 또한 request body, response body, error stack 저장 제외 정책, active override, future override, DB policy, application 기본값, CPF 기본값 fallback, `transactionGlobalId`/`traceId`/`businessTransactionId` 기준 통합 추적 API, 일반 감사 API, 정책 감사 API를 runtime smoke와 단위 테스트로 나눠 검증합니다.

```powershell
mysql --ssl=0 --default-character-set=utf8mb4 -u root -p < specs/sql/00_all_install_and_smoke.sql
```

## 문서 진입점

| 문서 | 목적 |
| --- | --- |
| `specs/index.html` | 문서 읽는 순서와 모듈 책임 요약 |
| `specs/프레임워크_구성_가이드.html` | PFW/CMN/BAT/ADM/업무 모듈 경계, profile, 품질 gate |
| `specs/배치_개발_가이드.html` | BAT 개발, Spring Batch JobRepository, CPF 배치 메타 연동 기준 |
| `specs/운영_매뉴얼.html` | ADM/BAT 구동, 중지, 장애 조치, smoke 검증 절차 |
| `specs/개발_가이드.html` | 신규 업무 개발 절차, Controller/Service/Repository/SQL/Test/EDU 연결 |
| `specs/관리자_가이드.html` | ADM 운영 기능, 권한, 로그, 배치, 회원, 캐시, 보안 운영 절차 |
| `specs/SQL_가이드.html` | DB 소유권, 설치 SQL, Flyway, 권한 분리, smoke 기준 |
| `specs/기능_구현_매트릭스.html` | 기능별 구현 상태, 파일 경로, 테스트 증거, 잔여 리스크 |

## 현재 주의사항

- Redis/Kafka/MQ는 로컬 fallback과 mock 검증이 우선이며, 실제 broker 장애 시나리오는 별도 환경에서 검증합니다.
- OpenAPI JSON smoke는 앱이 실제 기동 중이어야 검증할 수 있습니다.
- ADM UI 정적 smoke는 메뉴와 API marker 확인입니다. 실제 브라우저 클릭 검증은 자동화 환경과 시나리오가 있을 때 별도로 수행합니다.
- 기능별 완료 여부는 최종 안정화 리포트의 실제 검증 결과를 기준으로 판단합니다.
- 다음 보강 우선순위는 ADM 거래/오류/감사 통합 관제 UX 고도화, 로그 정책 다중 인스턴스 broker 전파, 브라우저 클릭 검증, broker 실연동 검증, center-cut 기본 구현체 순서입니다.

<!-- evidence: batch-development-guide operation-runbook -->
