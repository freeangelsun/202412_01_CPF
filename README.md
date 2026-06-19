# CPF CoreFlow Platform Framework

CPF는 Java 21, Spring Boot 3.4 기반의 업무 서비스 표준 프레임워크입니다. 저장소 이름은 `cpf-coreflow-platform-framework`, Java 패키지는 `cpf.*`를 사용합니다.

## 모듈

| 모듈 | 역할 |
| --- | --- |
| `pfw` | 거래 ID, 표준 거래 헤더, 거래 로그 AOP, 공통 응답/예외, OpenAPI, WebClient, workflow, 보안 메타 |
| `cmn` | 공통 코드/메시지 캐시, MQ 브리지, 파일/원격 연계, 보안 유틸, 선택형 `cmnDB` 업무 공통 기능 |
| `adm` | 관리자 API/화면, 회원/운영자 권한, 통합 로그, 알림, 다운로드 감사, 배치 관제, 캐시/메시지/코드/설정, 보안 운영 |
| `acc` | 계정 업무 샘플 |
| `mbr` | 회원 업무 샘플, 회원 로그인, refresh token, 로그인 이력 기본 구현 |
| `xyz` | 개발자 교육 샘플 API |
| `bizadm` | CPF 적용 프로젝트의 업무/프로젝트 관리자 샘플, BIZADM 전용 로그인/권한 token 기본 구현 |
| `exs` | 대외 연계 주제영역 샘플, 대외 token/통제 정책/재처리 운영 기본 구현 |

## 빠른 검증

```powershell
.\gradlew.bat :pfw:compileJava :cmn:compileJava :adm:compileJava :acc:compileJava :mbr:compileJava :xyz:compileJava :bizadm:compileJava :exs:compileJava --offline
.\gradlew.bat test --offline
.\gradlew.bat qualityGate --offline
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-adm-ui.ps1
```

MariaDB 초기화는 `specs/sql/00_all_install_and_smoke.sql`을 사용합니다. 운영 secret은 콘솔과 리포트에 노출하지 않고 환경변수, Vault, KMS, Secret Manager로 주입합니다.

## 주요 문서

| 문서 | 내용 |
| --- | --- |
| [문서 인덱스](specs/index.html) | 전체 가이드 진입점 |
| [프레임워크 구성 가이드](specs/프레임워크_구성_가이드.html) | 모듈, DB, profile, migration, observability, quality gate |
| [개발 가이드](specs/개발_가이드.html) | 신규 모듈, API, 응답/오류, CMN API, EDU 샘플 매트릭스 |
| [관리자 가이드](specs/관리자_가이드.html) | ADM 회원관리, 권한, 통합 로그, 알림, 다운로드 감사, 배치, 캐시, 보안 운영 |
| [SQL 가이드](specs/SQL_가이드.html) | 설치 SQL, Flyway, 권한 계정, smoke 검증 |
| [기능 구현 매트릭스](specs/기능_구현_매트릭스.html) | 기능별 소스, API, ADM 화면, DB, Swagger, EDU, 테스트 연결 상태 |

## 핵심 표준

- 테이블명은 `{주제영역}_{업무명}` lower snake case를 사용합니다. 예: `pfw_message`, `cmn_sequence`, `adm_menu`, `mbr_member`, `bizadm_customer`, `exs_transaction_log`.
- 모든 테이블은 `created_by`, `created_at`, `updated_by`, `updated_at` 공통 감사 컬럼을 가집니다.
- 공식 schema SQL의 모든 테이블과 컬럼에는 한글 `COMMENT`를 작성합니다.
- ADM 운영 기능은 UI 숨김에 그치지 않고 서버 API 권한 검사를 함께 적용합니다.
- 다운로드는 사유, 권한, 마스킹, 감사 로그를 표준 공통 API로 처리합니다.
- 배치 자동 실행은 `cpf.batch.scheduler.enabled=true`일 때 활성화하며, 수동 smoke는 ADM `scheduler/run-once` API로 수행합니다.
- 품질 gate는 `.\gradlew.bat qualityGate --offline`을 기준으로 하며 UTF-8, mojibake, SQL 표준, 거래 ID 표준, 보안 seed 검사를 함께 수행합니다.
