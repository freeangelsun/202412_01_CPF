# CPF CoreFlow Platform Framework

CPF는 Java 21, Spring Boot 3.4 기반의 업무 개발 표준 프레임워크입니다. 목표는 신규 업무 모듈을 만들 때 거래 추적, 표준 응답, 권한, 감사, 배치, 대외연계, 공통 업무 기능을 처음부터 다시 설계하지 않도록 기준 코드를 제공하는 것입니다.

README는 짧은 진입점입니다. 세부 구현 기준, 운영 절차, SQL 설치, 기능별 검증 증거는 `specs` HTML 가이드에서 확인합니다.

## 핵심 원칙

- `pfw`는 프레임워크 코어 영역입니다. 거래 ID, 표준 헤더, 표준 응답/예외, 거래 로그, OpenAPI 공통 설정, WebClient, Spring Batch 공통 실행 API, 프레임워크 메시지/응답코드/설정 같은 CPF 자체 기능을 제공합니다.
- `cmn`은 프로젝트 공통 영역입니다. CPF를 적용한 프로젝트에서 필요한 업무 공통 기능을 구현하는 곳입니다. 예: 업무 채번, 업무 알림 로그, 업무 로그, 파일/전문/MQ 보조 기능.
- 업무 모듈은 `pfwDB` 테이블을 직접 관리하지 않습니다. 프레임워크 기능이 필요하면 PFW Java API, Filter, Interceptor, AOP, Facade를 통해 사용합니다.
- 운영 기능은 화면 표시만으로 완료가 아닙니다. 서버 권한검사, 감사 사유, before/after diff, 마스킹, 다운로드 감사, 테스트 증거가 함께 있어야 완료로 봅니다.
- 기능을 추가하면 소스, SQL, Swagger, EDU 샘플, README, 가이드 문서를 함께 현행화합니다.

## 모듈 지도

| 모듈 | 책임 | 주요 구현 |
| --- | --- | --- |
| `pfw` | CPF 프레임워크 코어 | `TransactionIdGenerator`, `TransactionContextFilter`, `CpfGlobalExceptionHandler`, `LoggingAspect`, `CpfBatchLauncher`, `CpfBatchOperationRepository`, OpenAPI 공통 설정 |
| `cmn` | 프로젝트 공통 기능 | `CmnSequenceService`, `CmnNotificationLogService`, `CmnBusinessLogService`, 파일 교환, 고정길이 전문, 메시지 브리지, 보안 유틸 |
| `adm` | 프레임워크 운영 관리자 | 운영자/권한, 회원 관리, 로그 관제, 배치 관제, 캐시, 메시지, 코드, 설정, 보안 운영, 다운로드 감사 |
| `bizadm` | 업무 관리자 기본 구현체 | 업무 관리자 인증, 고객/상품/주문/설정 조회, 마스킹 해제 감사, 다운로드 정책 |
| `mbr` | 회원 기본 구현체 | 회원 CRUD, 로그인, refresh token hash, 로그인 이력, 회원 상태/권한 연계 |
| `acc` | 계정 업무 예제 | 회원 조회 연계와 CPF 거래 로그 적용 예제 |
| `exs` | 대외연계 기본 구현체 | 기관/채널/endpoint, token hash, 통제 정책, 수신/송신 로그, 재처리 요청 |
| `xyz` | EDU 교육 API | CRUD, 표준 응답/오류, 거래 헤더, CMN 공통 기능, 배치, 대외호출, 전문, 파일, 보안 샘플 |

## DB 소유권

| DB | 소유 영역 | 대표 테이블 |
| --- | --- | --- |
| `pfwDB` | 프레임워크 코어와 운영 메타 | `pfw_transaction_log`, `pfw_message`, `pfw_response_code`, `pfw_config`, `pfw_batch_job`, `pfw_batch_schedule`, `BATCH_*` |
| `cmnDB` | 프로젝트 업무 공통 | `cmn_sequence`, `cmn_sequence_issue_log`, `cmn_notification_log`, `cmn_business_log` |
| `admDB` | 운영자/권한/감사 | `adm_operator`, `adm_role`, `adm_menu`, `adm_button`, `adm_audit_log`, `adm_download_audit_log` |
| `bizadmDB` | 업무 관리자 기본 구현 | `bizadm_admin_user`, `bizadm_role`, `bizadm_customer`, `bizadm_product`, `bizadm_order` |
| `mbrDB` | 회원 기본 구현 | `mbr_member`, `mbr_member_role`, `mbr_login_history`, `mbr_refresh_token` |
| `exsDB` | 대외연계 기본 구현 | `exs_institution`, `exs_channel`, `exs_endpoint`, `exs_token_store`, `exs_transaction_log`, `exs_retry_log` |

Spring Batch의 `BATCH_*` 테이블은 JobRepository 원천 실행 메타이고, CPF의 `pfw_batch_*` 테이블은 ADM 관제, 스케줄, 수동 실행, 재수행, 중지, 운영 이력을 위한 운영 메타입니다.

## 거래 ID와 표준 헤더

`transactionGlobalId`는 `yyyyMMddHHmmssSSS + moduleId(3) + wasId(7) + sequence(7)` 구조의 34자리 ID입니다.

예: `20260615120000000MBRlocal010000001`

주요 헤더는 `X-Transaction-Id`, `X-Trace-Id`, `X-Span-Id`, `X-Channel-Code`, `X-Request-Type`, `X-Client-Version`, `X-Client-Ip`, `X-Idempotency-Key`, `Authorization`입니다. 모든 API에서 전부 필수는 아니지만, 운영 추적과 장애 분석을 위해 가능한 모든 호출에서 전달하는 것을 기준으로 합니다.

## 검증 명령

```powershell
.\gradlew.bat compileJava --offline
.\gradlew.bat test --offline
.\gradlew.bat qualityGate --offline
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-adm-ui.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-openapi.ps1
```

MariaDB 설치 검증은 `specs/sql/00_all_install_and_smoke.sql`로 수행합니다. 실행하지 않은 검증은 성공으로 쓰지 않고 미검증으로 남깁니다.

## 문서 진입점

| 문서 | 목적 |
| --- | --- |
| `specs/index.html` | 문서 읽는 순서와 모듈 책임 요약 |
| `specs/프레임워크_구성_가이드.html` | PFW/CMN/ADM/업무 모듈 경계, profile, batch, 품질 gate |
| `specs/개발_가이드.html` | 신규 업무 개발 절차, Controller/Service/Repository/SQL/Test/EDU 연결 |
| `specs/관리자_가이드.html` | ADM 운영 기능, 권한, 로그, 배치, 회원, 캐시, 보안 운영 절차 |
| `specs/SQL_가이드.html` | DB 소유권, 설치 SQL, Flyway, 권한 분리, smoke 기준 |
| `specs/기능_구현_매트릭스.html` | 기능별 구현 상태, 파일 경로, 테스트 증거, 미검증 항목 |

## 현재 주의사항

- Redis/Kafka/MQ는 로컬 fallback과 mock 검증이 우선이며, 실제 broker 장애 시나리오는 별도 환경에서 검증해야 합니다.
- OpenAPI JSON smoke는 앱이 기동 중이어야 검증할 수 있습니다.
- ADM UI smoke는 정적 marker 검증입니다. 브라우저 실제 클릭 검증은 별도 실행 결과가 필요합니다.
- 기능별 완료 여부는 `CPF_STABILIZATION_REPORT.html`의 실제 검증 결과를 기준으로 판단합니다.
