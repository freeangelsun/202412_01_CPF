# CPF CoreFlow Platform Framework

CPF는 Java 21, Spring Boot 3.4 기반의 업무 서비스 표준 프레임워크입니다. 저장소 이름은 `cpf-coreflow-platform-framework`, Java 패키지는 `cpf.*`를 사용합니다.

## 모듈

| 모듈 | 역할 |
| --- | --- |
| `pfw` | 거래 ID, 표준 거래 헤더, 거래 로그 AOP, 공통 응답/예외, OpenAPI, WebClient, workflow, 보안 메타 |
| `cmn` | 공통 코드/메시지 캐시, MQ 브리지, 파일/원격 연계, 보안 유틸, 선택형 `cmnDB` 업무 공통 기능 |
| `adm` | 관리자 API/화면, 회원/운영자 권한, 통합 로그, 배치 관제, 캐시/메시지/코드/설정, 보안 운영 |
| `acc` | 계정 업무 샘플 |
| `mbr` | 회원 업무 샘플 |
| `xyz` | 개발자 교육 샘플 API |

## 빠른 검증

```powershell
.\gradlew.bat :pfw:compileJava :cmn:compileJava :adm:compileJava :acc:compileJava :mbr:compileJava :xyz:compileJava --offline
.\gradlew.bat :cmn:test --offline
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-sql-standard.ps1
```

MariaDB 초기화는 `specs/sql/00_all_install_and_smoke.sql`을 사용합니다. 운영 secret은 콘솔 기록에 남기지 않고 환경변수, Vault, KMS, Secret Manager로 주입합니다.

## 주요 문서

| 문서 | 내용 |
| --- | --- |
| [문서 인덱스](specs/index.html) | 전체 가이드 진입점 |
| [프레임워크 구성 가이드](specs/프레임워크_구성_가이드.html) | 모듈, DB, profile, migration, observability |
| [개발 가이드](specs/개발_가이드.html) | 신규 모듈, API, 응답/오류, CMN API, EDU 샘플 매트릭스 |
| [관리자 가이드](specs/관리자_가이드.html) | ADM 회원관리, 권한, 통합 로그, 배치, 캐시, 보안 운영 |
| [SQL 가이드](specs/sql/README.md) | 설치 SQL, Flyway 기준, 권한 계정, smoke 검증 |
| [거래 헤더 표준](specs/transaction-header-standard.md) | 필수/선택 거래 헤더, 전파, 로그 적재 기준 |
| [운영/보안 가이드](specs/operation-security-guide.md) | secret, 권한, 로그, 장애 대응 |
| [Definition of Done](specs/definition-of-done.md) | 신규 기능 완료 기준 |
| [CI 품질 Gate](specs/ci-quality-gate.md) | compile, test, SQL naming, UTF-8, mojibake 기준 |

## 핵심 표준

- 테이블명은 `{주제영역}_{업무명}` lower snake case를 사용합니다. 예: `pfw_message`, `cmn_sequence`, `adm_menu`, `mbr_member`.
- 모든 테이블은 `created_by`, `created_at`, `updated_by`, `updated_at` 공통 감사 컬럼을 가집니다.
- 모든 공식 SQL 테이블과 컬럼에는 한글 `COMMENT`를 작성합니다.
- 신규 외부 공개 API는 `/api/v1` 정책을 따르고 기존 샘플 경로는 호환성을 유지합니다.
- prod profile은 DB/JWT/crypto secret 기본값을 두지 않고 배포 환경에서 주입합니다.
- 표준 거래 헤더는 Swagger, `TransactionContext`, 외부 호출 전파, `pfw_transaction_log` 적재 기준과 일치해야 합니다.
- 품질 gate는 `.\gradlew.bat qualityGate --offline`을 기준으로 합니다.
