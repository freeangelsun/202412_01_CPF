# CPF CoreFlow Platform Framework

CPF(CoreFlow Platform Framework)는 Java 21, Spring Boot 3.4 기반의 MSA 업무 프레임워크 예제입니다. 저장소/제품 표준명은 `cpf-coreflow-platform-framework`이며, Java 패키지는 `cpf.*`를 사용합니다.

기존 프레임워크 API 호환성을 위해 `FpsTransaction`, `FpsWebClient`, `FpsWorkflow` 같은 `Fps*` 클래스명은 유지합니다. 운영 환경변수는 `CPF_*`를 우선 사용하고, 기존 `FPS_*` 값은 fallback으로 지원합니다.

## Modules

| Module | Role |
| --- | --- |
| `pfw` | 거래ID, 표준 헤더, 거래 로그 AOP, 표준 예외/응답, OpenAPI, WebClient, 워크플로우, 동적 로그레벨 |
| `cmn` | 공통 코드/메시지/응답코드/설정 캐시, 캐시 리프레시 이벤트, MQ, 전문, 파일/원격 연계, 보안/JWT/암호화 |
| `mbr` | 회원 업무 샘플 서비스 |
| `acc` | 계좌 업무 샘플 서비스 |
| `xyz` | 교육용 샘플 서비스. CRUD, 캐시, 예외, MQ, 전문, 파일, 트랜잭션, 동적 로그, 보안 샘플 |
| `adm` | 운영자 화면/API. 로그, 감사, 캐시, 응답코드, 동적 로그레벨, 운영자 권한/세션 관리 |

## Quick Start

```powershell
.\gradlew.bat :pfw:compileJava :cmn:compileJava :adm:compileJava :acc:compileJava :mbr:compileJava :xyz:compileJava --offline
.\gradlew.bat runLocalServices
```

개별 서비스 실행:

```powershell
.\gradlew.bat :mbr:bootRun
.\gradlew.bat :acc:bootRun
.\gradlew.bat :xyz:bootRun
.\gradlew.bat :adm:bootRun
```

## Local Ports

| Service | URL |
| --- | --- |
| ACC | `http://localhost:8080` |
| MBR | `http://localhost:8081` |
| ADM | `http://localhost:8090/adm` |
| XYZ | `http://localhost:8099` |

Swagger UI는 각 서비스의 `/swagger-ui.html`, Scalar는 `/scalar`에서 확인합니다.

## Initial DB SQL

초기 DB 스크립트는 `specs/sql` 아래에 있습니다.

```powershell
mysql -h localhost -P 3306 -u root -p < specs/sql/00_all_install_and_smoke.sql
```

스크립트는 PFW 거래 로그와 프레임워크 코드/메시지/응답코드/설정/캐시/보안/파일연계, ADM 운영자/역할/메뉴/세션/감사/동적로그, ACC 계좌 샘플, MBR 회원 샘플 seed data를 포함합니다.

## Standards

- 응답코드: `{S|E}{MODULE}{GROUP}{SEQ}` 예: `SACC000000`, `EACC010001`, `EPFW900001`
- 메시지코드: `M{MODULE}{GROUP}{SEQ}` 예: `MPFW900001`, `MMBR010102`
- 거래코드: `{MODULE}{TYPE}{DOMAIN}{SEQ}` 예: `MBR01BSE0001`, `ADM05OPR0021`
- `message_table`은 한 row에서 `external_message`, `internal_message`를 함께 관리합니다.
- 동적 메시지는 `{0}`, `{1}` indexed parameter 형식을 사용합니다.
- 소스와 문서는 UTF-8 기준입니다. 기본 검사는 `.\scripts\check-utf8.ps1`로 실행하고, 기존 깨진 문자열까지 찾을 때는 `-CheckMojibake`를 추가합니다.
- 개발자는 응답코드/메시지코드를 전달하고, PFW/CMN resolver가 캐시에서 메시지를 해석해 응답 body/header/log/exception metadata에 반영합니다.

## Documentation

상세 문서는 `specs/index.html`에서 시작합니다.

| Document | Purpose |
| --- | --- |
| `specs/프레임워크_구성_가이드.html` | 모듈 구조, 공통 기능, DB, OpenAPI, 실행 기준 |
| `specs/개발_가이드.html` | 개발 규칙, 코드/메시지 표준, 샘플 API, 검증 명령 |
| `specs/관리자_가이드.html` | ADM 로그인, 세션, 권한, 감사, 동적 로그, 운영 설정 |
| `specs/sql/README.md` | DB 설치 순서, 대상 DB/테이블, smoke check |

## Current Reinforcement Notes

- 실제 MariaDB 환경에서 `specs/sql/00_all_install_and_smoke.sql` 직접 실행과 재실행 idempotent 검증을 완료했습니다.
- DB 역할은 `pfwDB` 프레임워크 메타데이터, `admDB` 운영관리, `accDB` 계좌 샘플, `mbrDB` 회원 샘플로 분리합니다. `cmnDB`는 사용하지 않습니다.
- ADM 권한은 UI 메뉴/버튼 제어와 `/adm/api/**` 서버 write/delete 검사에 함께 적용합니다.
- ADM 변경성 작업은 감사 사유를 필수로 받고 `operator_audit_log`에 저장합니다.
- `dynamic_log_level_rule` 변경 이벤트는 CMN 메시징 destination `cpf.adm.dynamic-log-level`로 발행하며, DB polling과 함께 다중 WAS 동기화 경로로 사용합니다.
- 운영 보안 완성도를 위해 ADM JWT signing key, CMN JWT secret, DB 비밀번호를 Vault/KMS 또는 배포 환경변수로 강제하는 구성이 다음 보강 포인트입니다.
