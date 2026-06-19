# CPF CoreFlow Platform Framework

CPF는 Java 21과 Spring Boot 3.4 기반의 범용 업무 프레임워크입니다. 금융권 수준의 거래 추적, 감사, 권한, 마스킹, 배치 관제 기준을 포함하지만 금융권 전용으로 묶이지 않고, 회원/계정/업무관리/대외연계/배치가 필요한 일반 업무 시스템에도 적용할 수 있도록 구성합니다.

## 핵심 방향

- `pfw`는 프레임워크 코어입니다. 거래 ID, 표준 헤더, 표준 응답/오류, 거래 로그, OpenAPI 공통 설정, WebClient, workflow, 보안 메타, 프레임워크 메시지/응답코드/설정, 배치 공통 API를 제공합니다.
- `cmn`은 프로젝트 공통 영역입니다. CPF를 적용하는 프로젝트가 업무 공통 기능을 확장하는 곳이며, 업무 채번, 업무 알림, 업무 로그처럼 프로젝트마다 달라지는 기능은 이 영역에서 구현합니다.
- 업무 모듈은 `pfwDB` 테이블을 직접 관리하지 않습니다. 프레임워크 표준 기능이 필요하면 PFW가 제공하는 Java API, Facade, Service, Filter, Interceptor, AOP를 통해 사용합니다.
- ADM 운영 기능은 UI 숨김만으로 끝내지 않고 서버 API 권한, 감사 사유, before/after diff, 마스킹, 다운로드 감사 기준과 함께 동작해야 합니다.
- 운영 기본 구현체에는 `sample` 패키지와 하드코딩 데이터 저장소를 두지 않습니다. 교육 코드는 `xyz/edu`에만 두고, 클래스명도 Education 도메인 용어를 사용합니다.

## 모듈 구조

| 모듈 | 책임 | 주요 기준 |
| --- | --- | --- |
| `pfw` | CPF 프레임워크 코어 | `TransactionIdGenerator`, `LoggingAspect`, 표준 오류, OpenAPI, `CpfBatchLauncher`, `CpfBatchOperationRepository` |
| `cmn` | 프로젝트 공통 확장 | 업무 채번, 업무 알림 로그, 업무 로그, 파일/전문/MQ 보조, 보안 유틸, 선택형 `cmnDB` |
| `adm` | 프레임워크 운영 관리자 | 운영자/권한, 회원 관제, 로그, 배치, 캐시, 메시지, 코드, 설정, 보안 운영, 다운로드 감사 |
| `bizadm` | 프로젝트 업무 관리자 기본 구현 | 업무 관리자 로그인, 역할/권한, 고객/상품/주문/설정/다운로드/마스킹 운영 |
| `mbr` | 회원 기본 구현 | 회원 CRUD, 로그인, refresh token hash, 로그인 이력, 회원 권한 연계 |
| `acc` | 계정 업무 예제 | 회원 조회 연계와 거래 로그 표준 적용 예제 |
| `exs` | 대외연계 기본 구현 | 기관/채널/endpoint, token hash, 통제 정책, 수신/송신 로그, 재처리 |
| `xyz` | 개발자 교육 API | CRUD, 예외, 헤더, 보안, CMN 업무 공통, 배치, 대외 호출 교육 |

## DB 소유권

| DB | 소유 주제 | 대표 테이블 |
| --- | --- | --- |
| `pfwDB` | 프레임워크 코어와 운영 메타 | `pfw_transaction_log`, `pfw_message`, `pfw_response_code`, `pfw_config`, `pfw_batch_job`, `pfw_batch_schedule`, `BATCH_*` |
| `cmnDB` | 프로젝트 업무 공통 | `cmn_sequence`, `cmn_sequence_issue_log`, `cmn_notification_log`, `cmn_business_log` |
| `admDB` | ADM 운영자와 권한 | `adm_operator`, `adm_role`, `adm_menu`, `adm_button`, `adm_audit_log` |
| `bizadmDB` | 업무 관리자 운영 | `bizadm_admin_user`, `bizadm_role`, `bizadm_customer`, `bizadm_download_audit` |
| `mbrDB` | 회원 기본 구현 | `mbr_member`, `mbr_member_role`, `mbr_login_history`, `mbr_refresh_token` |
| `exsDB` | 대외연계 | `exs_institution`, `exs_channel`, `exs_endpoint`, `exs_transaction_log`, `exs_retry_request` |

Spring Batch 표준 `BATCH_*` 테이블은 JobRepository의 원천 실행 메타입니다. CPF `pfw_batch_*` 테이블은 ADM 관제, 스케줄, 영업일, 선행/트리거 관계, 수행 대상, 운영 로그를 위한 CPF 운영 메타입니다. 두 영역은 `spring_batch_execution_id`와 `pfw_execution_id`로 연결합니다.

## 거래 ID와 헤더

`transactionGlobalId`는 `yyyyMMddHHmmssSSS + moduleId(3) + wasId(7) + sequence(7)` 형식의 34자리 표준 ID입니다. 예: `20260615120000000MBRlocal010000001`.

기본 헤더는 `X-Transaction-Id`, `X-Trace-Id`, `X-Span-Id`, `X-Channel-Code`, `X-Request-Type`, `X-Client-Version`, `X-Client-Ip`, `X-Idempotency-Key`, `Authorization`을 기준으로 합니다. 필수 여부는 API 성격에 따라 달라지지만, 거래 추적과 운영 분석을 위해 가능한 모든 호출에서 전달하는 것을 표준으로 둡니다.

## 인증/권한/감사

- ADM, BIZADM, MBR token realm은 분리합니다. 서로 다른 도메인의 access token을 혼용하지 않습니다.
- 운영 write/delete/execute/retry/stop API는 서버에서 역할, 메뉴, 버튼, API 권한을 확인해야 합니다.
- 비밀번호 초기화, 잠금 해제, 마스킹 해제, 다운로드, 배치 실행/중지/재수행, 설정 변경은 감사 사유를 요구하고 before/after diff를 남겨야 합니다.
- secret, DB password, JWT secret, 원문 token은 문서, SQL seed, 로그, 리포트에 노출하지 않습니다.

## 로컬 실행

```powershell
.\gradlew.bat :pfw:compileJava :cmn:compileJava :adm:compileJava :acc:compileJava :mbr:compileJava :xyz:compileJava :bizadm:compileJava :exs:compileJava --offline
.\gradlew.bat runLocalServices --offline
```

로컬 서비스 포트는 MBR 8081, ACC 8080, XYZ 8099, ADM 8090, BIZADM 8091, EXS 8092를 기준으로 합니다. PFW와 CMN은 단독 서비스가 아니라 각 서비스 내부에서 라이브러리로 동작합니다.

## 검증

```powershell
.\gradlew.bat test --offline
.\gradlew.bat qualityGate --offline
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-adm-ui.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-sample-standard.ps1
```

MariaDB 설치 검증은 `specs/sql/00_all_install_and_smoke.sql`로 수행합니다. 앱 기동 후 OpenAPI 검증은 `scripts/smoke-openapi.ps1`로 수행합니다. 실제 실행하지 않은 검증은 성공으로 보고하지 않고 미검증으로 남깁니다.

## 상세 문서

| 문서 | 역할 |
| --- | --- |
| [문서 인덱스](specs/index.html) | 개발자, 운영자, 검수자가 어떤 순서로 문서를 볼지 안내합니다. |
| [프레임워크 구성 가이드](specs/프레임워크_구성_가이드.html) | 모듈 책임, DB 소유권, PFW/CMN 경계, 배치 구조, profile, 품질 gate를 설명합니다. |
| [개발 가이드](specs/개발_가이드.html) | 신규 업무 개발 절차, PFW 공통 API 사용법, EDU 매핑표, 테스트 기준을 제공합니다. |
| [관리자 가이드](specs/관리자_가이드.html) | ADM 운영 기능, 권한, 로그, 배치, 보안, BIZADM/MBR/EXS 관제 절차를 설명합니다. |
| [SQL 가이드](specs/SQL_가이드.html) | 설치 SQL, Flyway, DB 권한, 테이블/컬럼/COMMENT 표준, smoke 검증 기준을 정리합니다. |
| [기능 구현 매트릭스](specs/기능_구현_매트릭스.html) | 기능별 소스, API, DB, Swagger, EDU, 테스트, 검증 결과를 대조합니다. |

## 현재 주의사항

- Redis/Kafka/MQ 실 broker 검증은 로컬 fallback 기준 구현과 문서화를 먼저 두고, broker 기동 검증은 별도 환경에서 수행합니다.
- OpenAPI JSON smoke는 대상 애플리케이션이 기동 중이어야 합니다.
- 빌드 산출물의 과거 클래스는 `clean` 전까지 남을 수 있으므로 검수 기준은 `src/main/java`와 `qualityGate` 결과를 우선합니다.
