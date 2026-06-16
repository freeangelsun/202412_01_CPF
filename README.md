# CPF CoreFlow Platform Framework

CPF(CoreFlow Platform Framework)는 Java 21, Spring Boot 3.4 기반의 MSA형 업무 프레임워크입니다. 저장소/제품 표준명은 `cpf-coreflow-platform-framework`입니다.

Java 패키지는 `cpf.*`를 표준으로 사용합니다. 다만 기존 프레임워크 API 호환성을 위해 `FpsTransaction`, `FpsWebClient`, `FpsWorkflow` 같은 `Fps*` 클래스명은 유지했습니다. 운영 환경변수는 `CPF_*`를 우선 사용하고 기존 `FPS_*`는 fallback으로 지원합니다.

## Modules

| Module | Role |
| --- | --- |
| `pfw` | 거래ID, 표준 헤더, 거래 로그 AOP, 표준 예외/응답, OpenAPI, WebClient, 워크플로우, 동적 로그레벨 |
| `cmn` | 공통 코드/메시지/응답코드/설정 캐시, 캐시 리프레시, MQ, 전문, 파일/원격연계, 보안/JWT/암호화 |
| `mbr` | 회원 업무 샘플 서비스 |
| `acc` | 계좌 업무 샘플 서비스 |
| `xyz` | 교육용 샘플 서비스. CRUD, 캐시, 예외, MQ, 전문, 파일, 트랜잭션, 동적로그, 보안 샘플 |
| `adm` | 운영자 화면/API. 로그, 캐시, 응답코드, 동적 로그레벨, 운영자/권한/세션 관리 |

## Standards

- 응답코드: `{S|E}{MODULE}{GROUP}{SEQ}`. 예: `SACC000000`, `EACC010001`, `EPFW900001`
- 메시지코드: `M{MODULE}{GROUP}{SEQ}`. 예: `MPFW900001`, `MMBR010102`
- `message_table` 한 레코드에서 `external_message`, `internal_message`를 함께 관리합니다.
- 동적 메시지는 `{0}`, `{1}` indexed parameter 형식을 공통으로 사용합니다.
- 개발자는 응답코드 또는 메시지코드만 던지고, PFW/CMN resolver가 캐시에서 코드/메시지를 찾아 응답 body/header/log/exception metadata에 자동 세팅합니다.
- `TRAN_LOG`는 `RESPONSE_CODE`, `MESSAGE_CODE`, `ERROR_CODE`, `ERROR_MESSAGE`, `EXTERNAL_MESSAGE`, `INTERNAL_MESSAGE`를 운영 추적 기준으로 저장합니다. `ERROR_CODE`, `ERROR_MESSAGE`는 실패 시에만 세팅합니다.

## Common APIs

| Area | Main API |
| --- | --- |
| Service call | `FpsWebClient`, `cpf.services.*.base-url` |
| Code/cache | `CodeCacheService`, `MessageCacheService`, `ResponseCodeCacheService`, `ConfigCacheService` |
| Security | `CmnCryptoService`, `CmnJwtService`, `CmnOAuthBearerTokenService` |
| Messaging | `CmnMessagePublisher`, `CmnMessageConsumer` |
| File/remote | `CmnFileExchangeService` |
| Telegram | `CmnTelegramService` |

`CmnCryptoService`는 Base64, Base64Url, SHA-256 hex/Base64Url, HMAC-SHA256 hex/Base64Url, AES-GCM, PBKDF2, secure random token/hex를 제공합니다.

## OpenAPI

| Service | Swagger UI | Scalar |
| --- | --- | --- |
| ACC | `http://localhost:8080/swagger-ui.html` | `http://localhost:8080/scalar` |
| MBR | `http://localhost:8081/swagger-ui.html` | `http://localhost:8081/scalar` |
| ADM | `http://localhost:8090/swagger-ui.html` | `http://localhost:8090/scalar` |
| XYZ | `http://localhost:8099/swagger-ui.html` | `http://localhost:8099/scalar` |

Swagger/OpenAPI 문서는 보통 DB 없이도 로딩됩니다. 실제 API 호출은 해당 API가 사용하는 DB/캐시/외부연계 상태에 따라 성공하거나 표준 오류 응답을 반환합니다.

## Initial DB SQL

초기 DB 스크립트는 `specs/sql` 아래에서 관리합니다.

```text
00_all_install.sql
00_all_install_and_smoke.sql
01_create_databases.sql
10_pfw_schema.sql
20_cmn_schema.sql
30_adm_schema.sql
40_business_sample_schema.sql
50_framework_seed_data.sql
60_adm_seed_data.sql
70_test_data.sql
99_smoke_check.sql
```

스크립트는 PFW 거래 로그, CMN 코드/메시지/응답코드/설정/캐시, CMN 보안/파일연계, ADM 운영자/역할/메뉴/세션/감사/동적로그, ACC/MBR/CMN 샘플 테이블과 데이터를 포함합니다.
로컬 설치는 `mysql -h localhost -P 3306 -u root -p < specs/sql/00_all_install_and_smoke.sql` 한 번으로 실행할 수 있습니다.

## Build

```powershell
.\gradlew.bat :pfw:compileJava :cmn:compileJava :adm:compileJava :acc:compileJava :mbr:compileJava :xyz:compileJava --offline
.\gradlew.bat :mbr:bootRun
.\gradlew.bat :acc:bootRun
.\gradlew.bat :xyz:bootRun
.\gradlew.bat :adm:bootRun
```

## ADM State

- ADM 세션은 `operator_session`에 token hash 기반으로 저장/조회/폐기되며, DB가 없으면 메모리 fallback으로 동작합니다.
- ADM 운영자/권한은 `operator_user`, `operator_user_role`, `operator_role`, `operator_menu`, `operator_role_menu` 기반 DB-first repository 흐름으로 조회/인증/등록합니다. DB가 없으면 로컬 교육용 seed로 fallback합니다.
- 동적 로그레벨은 런타임 적용을 위해 WAS 메모리에 등록하고, 운영 추적을 위해 `dynamic_log_level_rule`에 함께 저장합니다. 다중 WAS 실시간 전파는 Redis/event broadcast가 다음 확장 포인트입니다.
- 각 WAS는 `cpf.adm.dynamic-log.sync-millis` 주기로 `dynamic_log_level_rule` 활성 룰을 런타임 메모리에 재동기화할 수 있습니다.
- ADM UI는 로그 검색, 정렬, 페이지 이동, 응답코드/운영자/동적로그 입력 검증 메시지를 포함합니다.

## Documentation Rule

소스, 테이블, seed data, 응답코드/메시지, ADM 운영 기능이 바뀌면 관련 문서를 같은 작업에서 함께 갱신합니다. 주요 문서는 `README.md`, `specs/index.html`, `specs/개발_가이드.html`, `specs/관리자_가이드.html`, `specs/프레임워크_구성_가이드.html`, `specs/sql/README.md`입니다.

## Current Reinforcement Notes

- 실제 MariaDB에서 `specs/sql` 전체 실행 후 `99_smoke_check.sql` 검증이 필요합니다.
- 다중 WAS 운영 완성도를 위해 `dynamic_log_level_rule` 변경 이벤트를 Redis/Kafka 등으로 전파하고 각 WAS가 룰을 재적재하는 구조가 다음 보강 포인트입니다.
- 운영 보안 완성도를 위해 ADM JWT signing key/Vault/KMS 연동, 운영자 감사 로그 상세 사유, 권한별 UI 제어를 더 보강할 수 있습니다.
